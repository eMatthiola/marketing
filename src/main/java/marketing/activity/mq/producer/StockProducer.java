package marketing.activity.mq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import marketing.activity.model.dto.StockMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @ClassName StockProducer
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/27 14:21
 */

@Service
@Slf4j
public class StockProducer {

    /**
     * MQ主题：
     *
     *
     * cd H:\Kafka\kafka_2.13-2.8.0
     * 启动zk：
     * .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
     * 启动kafka：
     * .\bin\windows\kafka-server-start.bat .\config\server.properties
     *
     * 创建topic：
     * .\bin\windows\kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic lottery_invoice
     *docker exec -it kafka kafka-topics --create --topic stock-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
     *
     * 查看分区
     * cd H:\Kafka\kafka_2.13-2.8.0\bin\windows
     * kafka-topics.bat --list --bootstrap-server localhost:9092
     * docker exec -it kafka kafka-topics --describe --topic stock-topic --bootstrap-server localhost:9092
     *
     * */


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发送库存消息到消息队列
     * @param productId
     * @param quantity
     */
    public void sendStockMessage(Long productId, Integer quantity) {
        // 创建消息对象
        StockMessageDTO stockMessage = new StockMessageDTO(
                UUID.randomUUID().toString(),
                productId,
                quantity,
                System.currentTimeMillis()
        );

        try {

            log.info("Sending stock message: {}", stockMessage);// 打印日志，模拟发送消息
            String json = objectMapper.writeValueAsString(stockMessage); // 将消息对象转换为JSON字符串
            // 发送消息到消息队列
            kafkaTemplate.send("stock-topic", json); //
        } catch (JsonProcessingException e) {
            throw new RuntimeException("发送kafka消息失败", e);

            // todo 处理异常情况，补偿机制例如重试或记录错误

        }







    }
}
