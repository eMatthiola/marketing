package marketing.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @ClassName GlobalExceptionHandler
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/22 12:23
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 其他异常处理方法...
    // 空指针等运行时异常
    // 参数校验异常（比如 @Valid 校验失败）
    // 业务异常：自定义的异常类
    //sql异常

    //兜底：常统一异常处理
    @ExceptionHandler(Exception.class)
    public Result<?> handleGenericException(Exception e) {
         // 记录日志
        // log.error("系统异常", e);
        // 返回统一的错误响应
        return Result.failure(500, "系统异常，请稍后再试");
     }


}
