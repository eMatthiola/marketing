package marketing.activity.service;

import marketing.activity.model.vo.ProductVO;

/**
 * @ClassName ProductService
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/25 14:11
 */
public interface ProductService {
    //扣减库存
    ProductVO reduceStock(Long productId);

    //同步库存到redis
    void syncStockToRedis(Long productId);

    //执行redis + Lua脚本扣减库存
    boolean reduceStockWithLua(Long productId);

    //todo MQ异步扣减库存
}
