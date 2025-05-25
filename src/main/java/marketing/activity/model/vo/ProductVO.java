package marketing.activity.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @ClassName ProductVO
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/25 14:17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO {
    private Long id;

    private Long productId;

    private String productName;

    private Integer stock;

    //返回更改时间
    private LocalDateTime updateTime;

}
