package marketing.activity.service.impl;

import marketing.activity.mapper.ActivityMapper;
import marketing.activity.model.dto.CreateActivityDTO;
import marketing.activity.model.entity.Activity;
import marketing.activity.model.vo.ActivityVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @ClassName ActivityServiceImplTest
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/23 20:04
 */

@ExtendWith(MockitoExtension.class)
class ActivityServiceImplTest {
    @Mock
    private ActivityMapper activityMapper;
    @InjectMocks
    private ActivityServiceImpl activityServiceImpl;
    @Test
     void testCreateActivity() {
        // Arrange
        CreateActivityDTO createActivityDTO = new CreateActivityDTO();
        createActivityDTO.setName("Test Activity");
        createActivityDTO.setDescription("This is a test activity.");
        createActivityDTO.setStartTime(LocalDateTime.of(2025, 5, 21, 10, 0));
        createActivityDTO.setEndTime(LocalDateTime.of(2025, 5, 22, 18, 0));


        // Act
        ActivityVO result = activityServiceImpl.createActivity(createActivityDTO);

        // Assert
        assertNotNull(result);
        assertEquals(createActivityDTO.getName(), result.getName());
        assertEquals(createActivityDTO.getDescription(), result.getDescription());

        //verify that the mapper was called with the correct parameters
        verify(activityMapper, times(1)).createActivity(any(Activity.class));

        // Additional assertions can be added to verify other fields
        //验证 Mapper 的参数，不是为了“重复验证 Service”，而是为了验证你填进去的值是否真的被 Mapper 收到
        ArgumentCaptor<marketing.activity.model.entity.Activity> captor = ArgumentCaptor.forClass(marketing.activity.model.entity.Activity.class);
        verify(activityMapper).createActivity(captor.capture());
        assertEquals("Test Activity", captor.getValue().getName());
    }


}
