package marketing.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName Result
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/22 11:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    private Integer code;

    private String message;

    private T data;


    public static <T> Result<T> success(T data) {
        return new Result<>(Constants.ResultCode.SUCCESS.getCode(),
                            Constants.ResultCode.SUCCESS.getMessage(),
                            data);
    }
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }
    public static <T> Result<T> failure(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(Constants.ResultCode.FAILURE.getCode(), message, null);
    }


}
