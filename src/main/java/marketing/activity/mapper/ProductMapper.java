package marketing.activity.mapper;

import marketing.activity.model.entity.Product;

import java.util.List;

/**
 * @ClassName ProductMapper
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/25 14:45
 */
public interface ProductMapper {

    /**
     * 扣减库存
     *
     * @param productId 商品ID
     * @return 扣减后的商品信息
     */
    int reduceStock(Long productId);

    /**
     * 根据商品ID查询商品信息
     *
     * @param productId 商品ID
     * @return 商品信息,实体类
     */
    Product getProductById(Long productId);

    List<Long> getAllProductIds();

}
