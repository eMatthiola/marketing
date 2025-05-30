package marketing.common;

/**
 * @ClassName BizException
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/29 13:33
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
