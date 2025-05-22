package marketing.activity.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName UpdateActivityDTO
 * @Description 更新时不允许直接修改状态、创建人等字段，这些字段通过专门接口或由系统自动维护
 * @Author Matthiola
 * @Date 2025/5/22 11:22
 */
@Data
public class UpdateActivityDTO {
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
