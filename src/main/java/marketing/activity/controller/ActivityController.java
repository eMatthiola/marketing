package marketing.activity.controller;

import jakarta.validation.Valid;
import marketing.activity.model.dto.CreateActivityDTO;
import marketing.activity.model.vo.ActivityVO;
import marketing.activity.service.ActivityService;
import marketing.common.Result;
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
}
