package marketing.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @ClassName Constants
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/22 12:06
 */
public class Constants {


    public enum ResultCode {
        SUCCESS(200, "成功"),
        FAILURE(500, "失败");

        private final int code;
        private final String message;

        ResultCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    public enum ActivityStatus {
        OFFLINE(0, "下线"),
        ONLINE(1, "上线");

        private final int code;
        private final String description;

        ActivityStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
