package marketing.activity.service.impl;

import com.google.common.hash.BloomFilter;
import lombok.extern.slf4j.Slf4j;
import marketing.activity.mapper.ProductMapper;
import marketing.activity.model.entity.Product;
import marketing.activity.model.vo.ProductVO;
import marketing.activity.mq.producer.StockProducer;
import marketing.activity.service.IProductService;
import marketing.activity.service.ITtlStrategy;
import marketing.activity.tools.bloomFilter.ProductBloomFilter;
import marketing.common.BizException;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ProductServicelmpl
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/25 14:12
 */
@Slf4j
@Service
public class ProductServiceImpl implements IProductService {
    @Autowired
    ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private StockProducer stockProducer;

    @Autowired
    private ProductBloomFilter productBloomFilter;

    @Autowired
    private ITtlStrategy ttlStrategy;

    @Autowired
    private RedissonClient redissonClient;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductVO reduceStock(Long productId) {

        // 扣减库存
        log.info("开始扣减库存，productId={}", productId);
        int result = productMapper.reduceStock(productId);
        log.info("扣减库存操作执行结果：{}", result);

        // 如果扣减成功，查询商品信息
        if (result > 0) {
            // 查询商品信息
            Product product = productMapper.getProductById(productId);
            log.info("查询商品信息结果：{}", product);
            if (product != null) {
                // 将商品信息转换为VO对象
                ProductVO productVO = new ProductVO();
                BeanUtils.copyProperties(product, productVO);
                // 返回商品VO对象
                return productVO;
            }
        }

        // 如果扣减失败或商品不存在，返回null
        log.warn("库存扣减失败或商品不存在，productId={}", productId);
        return null;

    }


    /**
     * 预热
     * 同步库存,商品信息到Redis
     * @param productId 商品ID
     */
    @Override
    public void syncStockToRedis(Long productId) {
        //查询商品信息
        Product product = productMapper.getProductById(productId);
        //如果商品存在
        if(product != null && product.getStock() != null) {
            //获取动态 TTL（秒）
            long ttlSeconds = ttlStrategy.getTtlSeconds(productId);


            //将库存信息同步到Redis
            String stockKey = "product:stock:" + productId;
            String value = String.valueOf(product.getStock());
            stringRedisTemplate.opsForValue().set(stockKey, value, ttlSeconds, TimeUnit.SECONDS);
            log.info("同步库存到Redis成功，key={}, value={}", stockKey, value, ttlSeconds);

            // 同步商品基本信息（Hash）
            String productKey = "product:info:" + productId;
            Map<String, String> productMap = new HashMap<>();
            productMap.put("name", product.getProductName());
            //后续可以添加更多商品属性
            stringRedisTemplate.opsForHash().putAll(productKey, productMap);//
            stringRedisTemplate.expire(productKey, ttlSeconds, TimeUnit.SECONDS);// 设置过期时间
            log.info("同步商品信息到Redis成功，key={}, value={}", productKey, productMap, ttlSeconds);

        } else {
            log.warn("商品不存在或库存信息为空，productId={}", productId);
        }
    }

    /**
     * 执行Redis + Lua脚本扣减库存, 避免超卖
     * @param productId
     * @param quantity 扣减数量
     * @return true = 扣减成功；false = 库存不足或商品不存在
     */
    @Override
    public boolean reduceStockWithLua(Long productId, int quantity) {
        //Lua脚本的key
        String stockKey = "product:stock:" + productId;
        String productKey = "product:info:" + productId;

        // 1：查询数据库，确认商品存在,先查 Redis，再查数据库
        if (!stringRedisTemplate.hasKey(productKey)) {
            if (productMapper.getProductById(productId) == null) {//查数据库
                log.warn("商品在数据库中也不存在，可能为非法请求，productId={}", productId);
            } else {
                log.warn("商品存在但未预热到 Redis，productId={}", productId);
            }
            return false; // 商品不存在
        }


        //2. Lua 脚本（返回：1 成功，0 库存不足，-1 无库存 key）
        String script =
                "local stock = redis.call('get', KEYS[1]);" +
                        "if not stock then return -1; end;" +                      // Redis 中无库存 key
                        "stock = tonumber(stock);" +
                        "if stock < tonumber(ARGV[1]) then return 0; end;" +       // 库存不足
                        "redis.call('decrby', KEYS[1], tonumber(ARGV[1]));" +      // 扣减库存
                        "return 1;";

        // ✨加这一句：主动 GET，触发 Redis 命中记录
        stringRedisTemplate.opsForValue().get(stockKey);

        //3 执行Lua脚本
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);
        Long result = stringRedisTemplate.execute(
                redisScript,
                java.util.Collections.singletonList(stockKey),
                String.valueOf(quantity) // 库存扣减数量
        );



        //4判断执行结果
        if (result == null) {
            log.error("Lua 脚本执行失败，返回 null，productId={}", productId);
            return false;
        }

        switch (result.intValue()) {
            case 1:
                log.info("库存扣减成功，productId={}", productId);
                //3.扣减成功时才发送kafka消息, MQ异步扣减库存
                stockProducer.sendStockMessage(productId, quantity);
                return true; // 扣减成功
            case 0:
                log.warn("库存不足，无法扣减，productId={}", productId);
                return false; // 库存不足
            case -1:
                log.error("Redis 中无库存 key，productId={}", productId);
                return false; // 无库存 key
            default:
                log.error("未知错误，Lua 脚本执行结果：{}", result);
                return false; // 未知错误
        }
    }


    /**
     * 获取商品信息
     * @param productId 商品ID
     * @return 商品信息VO
     */
    @Override
    public ProductVO getProductInfo(Long productId) {
        // 1. 记录访问行为，用于动态 TTL 策略判断商品热度
        try {
            stringRedisTemplate.opsForValue().increment("product:access:" + productId);
            // 设置过期时间为1小时，形成滑动窗口统计（可选）
            stringRedisTemplate.expire("product:access:" + productId, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("商品访问计数失败，productId={}", productId, e);
        }


        //2. 先用布隆过滤器拦截非法ID
        if (!productBloomFilter.mightContain(productId)) {
            log.warn("布隆过滤器拦截非法 productId={}", productId);
            return null; // 返回null表示商品不存在
        }

        String productKey = "product:info:" + productId;

        // 3.尝试从 Redis 获取缓存
        Map<Object, Object> productMap = stringRedisTemplate.opsForHash().entries(productKey);

        //3.1.命中redis空值
        if (productMap != null && productMap.containsKey("empty")) {
            log.warn("缓存命中空值，productId={}", productId);
            return null; // 返回null表示商品不存在
        }
        //3.2.命中redis缓存
        if (productMap != null && !productMap.isEmpty()) {
            log.info("缓存命中，productId={}", productId);
            ProductVO vo = new ProductVO();
            vo.setProductName((String) productMap.get("name"));
            return vo; // 返回缓存中的商品信息
        }

        //4.如果未命中Redis缓存, 尝试 Redisson 分布式加锁
        String lockKey = "lock:product:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(0, 5, TimeUnit.SECONDS)) {
                // 成功获取锁，继续处理
                log.info("获取到分布式锁，productId={}", productId);
                try {
                    // 双重检查缓存，防止重复加载
                    productMap = stringRedisTemplate.opsForHash().entries(productKey);
                    if (productMap != null && !productMap.isEmpty()) {
                        log.info("缓存命中，productId={}", productId);
                        ProductVO vo = new ProductVO();
                        vo.setProductName((String) productMap.get("name"));
                        return vo; // 返回缓存中的商品信息
                    }

                    // 5.查询数据库，预热缓存
                    Product product = productMapper.getProductById(productId);
                    if (product != null) {
                        syncStockToRedis(productId); // 预热
                        ProductVO vo = new ProductVO();
                        BeanUtils.copyProperties(product, vo);
                        return vo;
                    }

                    // 6.如果数据库中也没有商品信息，缓存空值到redis防止缓存穿透
                    Map<String, String> emptyMap = new HashMap<>();
                    emptyMap.put("empty", "1");
                    stringRedisTemplate.opsForHash().putAll(productKey, emptyMap);
                    stringRedisTemplate.expire(productKey, 5, java.util.concurrent.TimeUnit.MINUTES); // 设置过期时间
                    log.warn("商品不存在，已缓存空值，productId={}", productId);
                    return null; // 返回null表示商品不存在
                } finally {
                    if (lock != null && lock.isHeldByCurrentThread()) {
                        lock.unlock(); // 释放锁
                        log.info("释放分布式锁，productId={}", productId);
                    } else {
                        log.warn("尝试释放未持有的锁，productId={}", productId);
                    }
                }
            } else {
                log.warn("获取分布式锁失败，可能其他线程正在处理，productId={}", productId);
                // 如果获取锁失败，可以选择重试然后兜底逻辑
                Thread.sleep(100); // 等待一段时间后重试
                Map<Object, Object> cache = stringRedisTemplate.opsForHash().entries(productKey);
                if (cache != null && !cache.isEmpty()) {
                    log.info("重试获取缓存命中，productId={}", productId);
                    ProductVO vo = new ProductVO();
                    vo.setProductName((String) cache.get("name"));
                    return vo; // 返回缓存中的商品信息
                }
                // 兜底逻辑：仍然未命中缓存
                log.error("等待后仍未命中缓存，降级返回，productId={}", productId);
                throw new BizException("系统繁忙，请稍后再试"); // 或返回 null/默认值
            }
        } catch (Exception e) {
            log.error("获取商品信息异常，productId={}", productId, e);
        }

        return null;
    }
}