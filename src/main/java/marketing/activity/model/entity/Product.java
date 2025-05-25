package marketing.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @ClassName Product
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/25 14:13
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private Long id;

    private Long productId;

    private String productName;

    private Integer stock;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
