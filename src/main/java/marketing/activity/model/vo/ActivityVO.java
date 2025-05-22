package marketing.activity.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName activityVO
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/21 11:22
 */
@Data
public class ActivityVO {
    private Long id;

    private String name;

    private String description;
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;
}
