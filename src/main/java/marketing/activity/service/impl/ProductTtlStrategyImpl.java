package marketing.activity.service.impl;

import lombok.extern.slf4j.Slf4j;
import marketing.activity.service.ITtlStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @ClassName ProductTtlStrategyImpl
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/6/5 12:32
 */

@Service
@Slf4j
public class ProductTtlStrategyImpl implements ITtlStrategy {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final Random random = new Random();

    /**
     * 获取商品缓存的过期时间（秒）
     *
     * @param productId 商品ID
     * @return TTL（单位：秒）
     */
    @Override
    public long getTtlSeconds(Long productId) {
        String key = "product:access" + productId;
        Long accessCount = 0L; // 假设从某处获取访问次数

        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                accessCount = Long.parseLong(value);
            }
        } catch (Exception e) {
            log.warn("获取访问量失败，默认按冷门商品处理 productId={}", productId);
        }

        long baseTtl;
        if (accessCount < 100) {
            // 冷门商品，TTL设置为1小时
            baseTtl = 3600;
        } else if (accessCount < 1000) {
            // 中等热度商品，TTL设置为30分钟
            baseTtl = 1800;
        } else {
            // 热门商品，TTL设置为10分钟
            baseTtl = 600;
        }

        long randomized = baseTtl + random.nextInt(300);// 随机化TTL，增加随机性，防止雪崩
        log.info("商品ID={}，访问量={}，基础TTL={}秒，随机化后TTL={}秒", productId, accessCount, baseTtl, randomized);
        return randomized;
    }
}
