package marketing.activity.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @ClassName activityDTO
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/21 11:20
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDTO {
    @NotBlank(message = "活动名称不能为空")
    private String name;

    @NotBlank(message = "活动描述不能为空")
    private String description;

    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime endTime;

//    private Integer status;
//
//    private String createTime;
//
//    private String updateTime;
//
//    private Long createUser;
//
//    private Long updateUser;
}
