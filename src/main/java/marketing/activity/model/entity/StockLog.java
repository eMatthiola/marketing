package marketing.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @ClassName stockLog
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/27 14:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockLog {

    private Long id;

    private Long productId;

    private Integer quantity;

    private String messageId;

    private LocalDateTime createTime;

}
