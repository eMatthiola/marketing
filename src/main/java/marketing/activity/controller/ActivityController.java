package marketing.activity.controller;

import jakarta.validation.Valid;
import marketing.activity.model.dto.CreateActivityDTO;
import marketing.activity.model.vo.ActivityVO;
import marketing.activity.service.ActivityService;
import marketing.common.Result;
import marketing.common.idempotent.Idempotent;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName activity
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/21 11:17
 */
@RestController
@RequestMapping("/activity")
public class ActivityController {
//    @Autowired
//    private ActivityService activityService; //field injection not recommended
    private final ActivityService activityService; //constructor injection recommended

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * @param activityDTO
     * @return activityVO
     */
    @PostMapping("/create")
    public Result<ActivityVO> createActivity(@Valid @RequestBody CreateActivityDTO activityDTO) {
        ActivityVO activityVO = activityService.createActivity(activityDTO);
        return Result.success(activityVO);
    }

    /**
     * 用户报名活动,防止重复报名
     * @param userId
     * @param activityId
     * @return
     */
     @PostMapping ("/activitySignUp")
     @Idempotent(key ="'idempotent:signup:' + #userId + ':' + #activityId", expireSeconds = 5) //防止重复报名
     public Result<String> activitySignUp(@RequestParam Long userId, @RequestParam Long activityId) {

         activityService.activitySignUp(userId, activityId);
         return Result.success("报名成功");//由service层抛出异常时会被全局异常处理器捕获
     }
}
