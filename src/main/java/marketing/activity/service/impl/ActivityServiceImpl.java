package marketing.activity.service.impl;

import lombok.extern.slf4j.Slf4j;
import marketing.activity.mapper.ActivityMapper;
import marketing.activity.mapper.ActivitySignUpMapper;
import marketing.activity.model.dto.CreateActivityDTO;
import marketing.activity.model.entity.Activity;
import marketing.activity.model.vo.ActivityVO;
import marketing.activity.service.ActivityService;
import marketing.common.BizException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @ClassName activityImpl
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/21 11:44
 */
@Service
@Slf4j
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;


    public ActivityServiceImpl(ActivityMapper activityMapper) {
        this.activityMapper = activityMapper;
    }


    @Autowired
    private ActivitySignUpMapper activitySignUpMapper;

    @Override
    public ActivityVO createActivity(CreateActivityDTO activityDTO) {
        //1.DTO -> Entity
        //todo mapstruts
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

    @Override
    public void activitySignUp(Long userId, Long activityId) {
        try{
            int insert = activitySignUpMapper.insert(userId, activityId);
            if (insert != 1) {
                log.info("用户{}报名活动{}失败，可能已报名或活动不存在", userId, activityId);
                throw new BizException("报名失败，可能已报名或活动不存在");
            }
        } catch(DuplicateKeyException e) {
            log.info("用户{}报名活动{}失败，已报名", userId, activityId);
            throw new BizException("报名失败，已报名(数据库重复)");

        } catch(Exception e) {
            log.error("用户{}报名活动{}失败，异常信息：{}", userId, activityId, e);
            throw new BizException("报名失败，运行时异常,请稍后再试");
        }


    }



}
