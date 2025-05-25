package marketing.activity.service.impl;

import marketing.activity.mapper.ProductMapper;
import marketing.activity.model.entity.Product;
import marketing.activity.model.vo.ProductVO;
import marketing.activity.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName ProductServicelmpl
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/5/25 14:12
 */
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductMapper productMapper;
    @Override
    public ProductVO reduceStock(Long productId) {
        // 扣减库存
        int result = productMapper.reduceStock(productId);

        // 如果扣减成功，查询商品信息
        if (result > 0) {
            // 查询商品信息
            Product product = productMapper.getProductById(productId);
            if (product != null) {
                // 将商品信息转换为VO对象
                ProductVO productVO = new ProductVO();
                BeanUtils.copyProperties(product, productVO);
                // 返回商品VO对象
                return productVO;
            }
        }

        // 如果扣减失败或商品不存在，返回null
        return null;

    }
}
