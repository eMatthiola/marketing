package marketing.common.idempotent;

import lombok.extern.slf4j.Slf4j;
import marketing.common.BizException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName IdempotentAspect
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/29 15:31
 */
@Aspect
@Slf4j
@Component
public class IdempotentAspect {

    private final StringRedisTemplate redisTemplate;
    public IdempotentAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     * 环绕通知，拦截所有标记了@Idempotent注解的方法
     * @param joinPoint 连接点，表示被拦截的方法
     * @param idempotent 注解参数，包含幂等键和过期时间
     * @return 方法执行结果
     */
    @Around("@annotation(idempotent)")
    public Object doIdempotentCheck(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        // 获取幂等键
        log.info("进入幂等切面检查，SpEL原始key = {}", idempotent.key());
        String key = parseKey(idempotent.key(), joinPoint);
        String value = "1";// 固定值，不需要唯一标识，因为自然过期

        // 检查Redis中是否存在该幂等键,
        // 如果键不存在，执行方法并设置键的过期时间
        log.info("解析后的幂等key = {}", key);
        Boolean success = redisTemplate.opsForValue().
                setIfAbsent(key, value, idempotent.expireSeconds(), TimeUnit.SECONDS);



        // 如果键存在且未过期，则抛出异常或返回错误结果
        if(Boolean.FALSE.equals(success)) {
            log.warn("幂等性检查失败，键值已存在，key: {}", key);
            // 可以抛出自定义异常或返回错误结果
            throw new BizException("操作已被执行，请勿重复提交(Redis幂等性检查失败)");
        }
        // 返回执行结果
        return joinPoint.proceed();
    }

    private String parseKey(String keySpEl, ProceedingJoinPoint joinPoint) {
        // 解析SpEL表达式，获取实际的key值
        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(keySpEl);

        // 这里可以根据需要传入joinPoint的参数进行SpEL解析
        MethodSignature methodsignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodsignature.getMethod();

        // 获取方法参数名
        String[] parameterNames = methodsignature.getParameterNames();//userId,activityId
        // 获取方法参数
        Object[] args = joinPoint.getArgs();//1, 2


        // 将方法参数放入SpEL上下文中
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        // 返回解析后的key值
        return expression.getValue(context, String.class);
    }
}
