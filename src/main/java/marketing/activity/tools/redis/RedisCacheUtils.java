package marketing.activity.tools.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 *@ClassName RedisCacheUtils
 *@Description  延迟双删工具类（用于解决缓存与数据库不一致问题）
 *@Author Matthiola
 *@Date 2025/6/5 15:16
 */
@Slf4j
@Component
public class RedisCacheUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 单线程延迟执行器（TODO 可扩展成线程池）
    private final ScheduledExecutorService schedule = Executors.newScheduledThreadPool(
            1,
            new CustomizableThreadFactory("cache-delay-delete-")
    );

    /**
     * 延迟删除缓存
     * @param key 缓存键
     * @param delayMillis 延迟时间（毫秒）
     */
    public void delayDelete(String key, long delayMillis) {

        //第一次删除
        try {
            redisTemplate.delete(key);
            log.info("立即删除缓存成功，key={}", key);
        } catch (Exception e) {
            log.error("立即删除缓存失败，key={}, error={}", key, e.getMessage());
        }

        // 延迟再次删除，防止并发期间旧数据写回缓存
        schedule.schedule(() -> {
            try {
                // 删除缓存
                redisTemplate.delete(key);
                log.info("延迟删除缓存成功，key={}", key);
            } catch (Exception e) {
                log.error("延迟删除缓存失败，key={}, error={}", key, e.getMessage());
            }
        }, delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS);




}   }
