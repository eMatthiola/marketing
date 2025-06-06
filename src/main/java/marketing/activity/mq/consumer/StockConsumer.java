package marketing.activity.mq.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import marketing.activity.mapper.ProductMapper;
import marketing.activity.mapper.StockLogMapper;
import marketing.activity.model.dto.StockMessageDTO;
import marketing.activity.model.entity.StockLog;
import marketing.activity.tools.redis.RedisCacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName StockConsumer
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/27 14:56
 */
@Component
@Slf4j
public class StockConsumer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StockLogMapper stockLogMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisCacheUtils redisCacheUtils;

    @KafkaListener(topics = "stock-topic", groupId = "stock-group")
    public void consumeStockMessage(String json) {

        try {
            // 1. 解析JSON字符串为StockMessageDTO对象
            StockMessageDTO stockMessageDTO = objectMapper.readValue(json, StockMessageDTO.class);

            //2. 检查消息的幂等性，避免重复处理
            if (stockLogMapper.existsByMessageId(stockMessageDTO.getMessageId())) {
                log.info("Duplicate message received, ignoring: {}", stockMessageDTO.getMessageId());
                return; // 如果消息已存在，直接返回
            }
            // 3. 执行扣减库存操作
            int update = productMapper.reduceStock(stockMessageDTO.getProductId());//todo 只扣减一个,未来可以加参数
            if (update == 0) {
                log.error("Failed to reduce stock for product ID,inventory may not enough: {}", stockMessageDTO.getProductId());
                return; // 如果扣减失败，记录日志并返回
            }else{
                log.info("Product reduced successfully for product ID: {}, quantity: {}", stockMessageDTO.getProductId(), stockMessageDTO.getQuantity());
            }


            // 4. 记录日志或更新数据库状态
            StockLog stockLog = new StockLog();
            stockLog.setMessageId(stockMessageDTO.getMessageId());
            stockLog.setProductId(stockMessageDTO.getProductId());
            stockLog.setQuantity(stockMessageDTO.getQuantity());
            stockLogMapper.insert(stockLog); // 插入库存日志记录
            log.info("Stock reduced successfully for product ID: {}, quantity: {}", stockMessageDTO.getProductId(), stockMessageDTO.getQuantity());

            //6.延迟双删：异步再次删除，防止并发期间脏数据写回缓存
            //5.删除 Redis 缓存（商品信息而不是product stock）
            String productKey = "product:info:" + stockMessageDTO.getProductId();
            redisCacheUtils.delayDelete(productKey, 1000);// 延迟 1 秒

        } catch (Exception e) {
            log.error("Error processing stock message: {}", json, e);
            // todo 处理异常情况，例如记录错误日志或重试,写入 Redis 补偿队列
        }

    }
}
