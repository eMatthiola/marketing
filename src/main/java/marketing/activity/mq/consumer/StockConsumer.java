package marketing.activity.mq.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import marketing.activity.mapper.ProductMapper;
import marketing.activity.mapper.StockLogMapper;
import marketing.activity.model.dto.StockMessageDTO;
import marketing.activity.model.entity.StockLog;
import org.springframework.beans.factory.annotation.Autowired;
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

    @KafkaListener(topics = "stock-topic", groupId = "stock-group")
    public void consumeStockMessage(String json) {

        try {
            // 解析JSON字符串为StockMessageDTO对象
            StockMessageDTO stockMessageDTO = objectMapper.readValue(json, StockMessageDTO.class);

            //1. 检查消息的幂等性，避免重复处理
            if (stockLogMapper.existsByMessageId(stockMessageDTO.getMessageId())) {
                log.info("Duplicate message received, ignoring: {}", stockMessageDTO.getMessageId());
                return; // 如果消息已存在，直接返回
            }
            // 2. 执行扣减库存操作
            int update = productMapper.reduceStock(stockMessageDTO.getProductId());//todo 只扣减一个,未来可以加参数
            if (update == 0) {
                log.error("Failed to reduce stock for product ID,inventory may not enough: {}", stockMessageDTO.getProductId());
                return; // 如果扣减失败，记录日志并返回
            }else{
                log.info("Product reduced successfully for product ID: {}, quantity: {}", stockMessageDTO.getProductId(), stockMessageDTO.getQuantity());
            }


            // 3. 记录日志或更新数据库状态
            StockLog stockLog = new StockLog();
            stockLog.setMessageId(stockMessageDTO.getMessageId());
            stockLog.setProductId(stockMessageDTO.getProductId());
            stockLog.setQuantity(stockMessageDTO.getQuantity());
            stockLogMapper.insert(stockLog); // 插入库存日志记录

            log.info("Stock reduced successfully for product ID: {}, quantity: {}", stockMessageDTO.getProductId(), stockMessageDTO.getQuantity());


        } catch (Exception e) {
            log.error("Error processing stock message: {}", json, e);
            // todo 处理异常情况，例如记录错误日志或重试,写入 Redis 补偿队列
        }

    }
}
