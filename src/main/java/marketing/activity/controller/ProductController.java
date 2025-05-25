package marketing.activity.controller;

import marketing.activity.model.vo.ProductVO;
import marketing.activity.service.ProductService;
import marketing.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName ProductController
 * @Description K减库存控制器
 * @Author Matthiola
 * @Date 2025/5/25 14:10
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @PostMapping("/reduce")
    public Result<ProductVO> reduceProduct(@RequestParam Long productId) {

        ProductVO productVO = productService.reduceStock(productId);

        if (productVO == null) {
            return Result.failure("库存扣减失败或商品不存在");
        }
        return Result.success(productVO);

    }
}
