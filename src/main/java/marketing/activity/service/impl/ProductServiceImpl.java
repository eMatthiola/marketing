package marketing.activity.service.impl;

import lombok.extern.slf4j.Slf4j;
import marketing.activity.mapper.ProductMapper;
import marketing.activity.model.entity.Product;
import marketing.activity.model.vo.ProductVO;
import marketing.activity.mq.producer.StockProducer;
import marketing.activity.service.IProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

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
            //将库存信息同步到Redis
            String stockKey = "product:stock:" + productId;
            String value = String.valueOf(product.getStock());
            stringRedisTemplate.opsForValue().set(stockKey, value);
            log.info("同步库存到Redis成功，key={}, value={}", stockKey, value);

            // 同步商品基本信息（Hash）
            String productKey = "product:info:" + productId;
            Map<String, String> productMap = new HashMap<>();
            productMap.put("name", product.getProductName());
            //后续可以添加更多商品属性
            stringRedisTemplate.opsForHash().putAll(productKey, productMap);
            log.info("同步商品信息到Redis成功，key={}, value={}", productKey, productMap);

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
}
