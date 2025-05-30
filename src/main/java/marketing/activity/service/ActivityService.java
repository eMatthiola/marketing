package marketing.activity.service;

import marketing.activity.model.dto.CreateActivityDTO;
import marketing.activity.model.vo.ActivityVO;
import marketing.common.BizException;

/**
 * @ClassName activity
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/21 11:42
 */
public interface ActivityService {
    /**
     * @param activityDTO
     * @return activityVO
     */
    ActivityVO createActivity(CreateActivityDTO activityDTO);

    /**
     * 用户报名活动,防止重复报名
     * @param userId
     * @param activityId
     * @throws BizException 如果用户已报名或报名失败
     */
    void activitySignUp(Long userId, Long activityId);
}
