package marketing.common.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName Idempotent
 * @Description Idempotent注解类
 * @Author Matthiola
 * @Date 2025/5/29 15:27
 */

@Target(ElementType.METHOD) // 注解可以应用于方法
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时可用
public @interface Idempotent {
    String key(); // 键名，用于标识幂等操作的唯一性 Spring Expression Language SpEL

    int expireSeconds() default 5; // 过期时间，单位为秒，默认5秒

}
