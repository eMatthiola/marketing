package marketing.activity.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName StockMessage
 * @Description message对象，用于异步扣减库存
 * @Author Matthiola
 * @Date 2025/5/27 14:19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMessageDTO {

    private String messageId; // 消息ID，用于幂等性控制

    private Long productId; // 商品ID

    private Integer quantity; // 扣减的库存数量

    private Long timestamp; // 消息时间戳，用于记录消息的创建时间




}
