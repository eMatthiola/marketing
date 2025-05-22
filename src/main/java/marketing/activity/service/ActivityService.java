package marketing.activity.service;

import marketing.activity.model.dto.ActivityDTO;
import marketing.activity.model.vo.ActivityVO;

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
    ActivityVO createActivity(ActivityDTO activityDTO);
}
