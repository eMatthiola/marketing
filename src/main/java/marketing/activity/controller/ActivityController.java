package marketing.activity.controller;

import jakarta.validation.Valid;
import marketing.activity.model.dto.ActivityDTO;
import marketing.activity.model.vo.ActivityVO;
import marketing.activity.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ActivityVO createActivity(@Valid @RequestBody ActivityDTO activityDTO) {
        ActivityVO activityVO = activityService.createActivity(activityDTO);
        return activityVO;
    }
}
