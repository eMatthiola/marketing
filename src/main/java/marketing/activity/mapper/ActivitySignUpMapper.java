package marketing.activity.mapper;

import marketing.activity.model.entity.ActivitySignUp;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName ActivitySignUpMapper
 * @Description 活动报名Mapper接口
 * @Author Matthiola
 * @Date 2025/5/29 12:45
 */
public interface ActivitySignUpMapper {
    /**
     * 报名活动
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 报名结果，成功返回1，失败返回0
     */
    int insert(@Param("userId") Long userId, @Param(("activityId")) Long activityId);


}
