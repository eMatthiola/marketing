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

    /**
     * 扣减库存接口
     *
     * @param productId 商品ID
     * @return 扣减后的商品信息
     */
    @PostMapping("/reduce")
    public Result<ProductVO> reduceProduct(@RequestParam Long productId) {

        ProductVO productVO = productService.reduceStock(productId);

        if (productVO == null) {
            return Result.failure("库存扣减失败或商品不存在");
        }
        return Result.success(productVO);
    }


    /**
     * 同步库存到Redis
     *
     * @param productId 商品ID
     */
    @PostMapping("/syncStockToRedis")
    public String syncStockToRedis(@RequestParam Long productId){
        productService.syncStockToRedis(productId);
        return "库存同步到Redis成功";
    }

    /**
     * 执行Redis + Lua脚本扣减库存
     *
     * @param productId 商品ID
     * @return 是否扣减成功
     */
    @PostMapping("/reduceStockWithLua")
    public String reduceStockWithLua(@RequestParam Long productId) {
        boolean result = productService.reduceStockWithLua(productId);
        if (result) {
            return "扣减成功";
        } else {
            return "库存扣减失败";
        }
    }
}
