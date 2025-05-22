package marketing.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.Target;
import java.time.LocalDateTime;

/**
 * @ClassName activity
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/21 11:07
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {


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
