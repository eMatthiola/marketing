package marketing.activity.model.entity;

import java.time.LocalDateTime;

/**
 * @ClassName ActivitySignUp
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/28 13:27
 */
public class ActivitySignUp {
    private Long id; // 报名ID

    private Long userId; // 用户ID

    private Long activityId; // 活动ID

    private LocalDateTime signUpTime; // 报名时间

    //数据库定义联合唯一索引来保证唯一性
    //UNIQUE KEY uk_user_activity (user_id, activity_id)
}
