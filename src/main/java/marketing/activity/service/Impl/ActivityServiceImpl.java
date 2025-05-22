package marketing.activity.service.Impl;

import marketing.activity.mapper.ActivityMapper;
import marketing.activity.model.dto.CreateActivityDTO;
import marketing.activity.model.entity.Activity;
import marketing.activity.model.vo.ActivityVO;
import marketing.activity.service.ActivityService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @ClassName activityImpl
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/21 11:44
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;

    public ActivityServiceImpl(ActivityMapper activityMapper) {
        this.activityMapper = activityMapper;
    }

    @Override
    public ActivityVO createActivity(CreateActivityDTO activityDTO) {
        //1.DTO -> Entity
        Activity activity = new Activity();
        BeanUtils.copyProperties(activityDTO, activity);

        //2.补充剩余系统字段
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        activity.setStatus(0); //TODO:状态枚举
        activity.setCreateUser(1L); //TODO:获取当前登录用户
        activity.setUpdateUser(1L); //TODO:获取当前登录用户
        //3.调用mapper
         activityMapper.createActivity(activity);

        //3.VO -> DTO
        ActivityVO activityVO = new ActivityVO();
        BeanUtils.copyProperties(activity, activityVO);
        return activityVO;
    }
}
