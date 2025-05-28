package marketing.activity.mapper;

import marketing.activity.model.entity.StockLog;

/**
 * @ClassName StockLogMapper
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/27 15:18
 */
public interface StockLogMapper {
    void insert(StockLog stockLog);

    boolean existsByMessageId(String messageId);
}
