package marketing.activity.service;

/**
 * @ClassName IStrategy
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/6/5 12:31
 */
public interface ITtlStrategy {

    /**
     * 获取商品缓存的过期时间（秒）
     * @param productId 商品ID
     * @return TTL（单位：秒）
     */
    long getTtlSeconds(Long productId);
}
