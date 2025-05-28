package marketing.activity.service.impl;

import lombok.extern.slf4j.Slf4j;
import marketing.activity.mapper.ProductMapper;
import marketing.activity.model.entity.Product;
import marketing.activity.model.vo.ProductVO;
import marketing.activity.mq.producer.StockProducer;
import marketing.activity.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName ProductServicelmpl
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/25 14:12
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
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
     * 同步库存到Redis
     * @param productId 商品ID
     */
    @Override
    public void syncStockToRedis(Long productId) {
        //查询商品信息
        Product product = productMapper.getProductById(productId);
        //如果商品存在，将库存信息存入Redis
        if(product != null && product.getStock() != null) {
            String key = "product:stock:" + productId;
            String value = String.valueOf(product.getStock());
            stringRedisTemplate.opsForValue().set(key, value);
            log.info("同步库存到Redis成功，key={}, value={}", key, value);
        } else {
            log.warn("商品不存在或库存信息为空，productId={}", productId);
        }
    }

    /**
     * 执行Redis + Lua脚本扣减库存
     * @param productId
     * @return
     */
    @Override
    public boolean reduceStockWithLua(Long productId) {
        //Lua脚本的key
        String key = "product:stock:" + productId;

        //Lua脚本内容
        String script =
                "local stock = tonumber(redis.call('get', KEYS[1]));" +
                "if stock and stock > 0 then " +
                "    redis.call('decr', KEYS[1])" +
                "    return 1;" +
                "else " +
                "    return 0;" +
                "end";

        //执行Lua脚本
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);
        Long result = stringRedisTemplate.execute(redisScript, java.util.Collections.singletonList(key));


        //发送kafka消息, MQ异步扣减库存
        //todo quantity 未来可以加参数
        stockProducer.sendStockMessage(productId, 1);

        //判断执行结果
        log.info("执行Lua脚本扣减库存，productId={}, result={}（1=成功，0=失败）", productId, result);
        return result != null && result == 1L;
    }
}
