package marketing.activity.tools.bloomFilter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import marketing.activity.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName ProductBloomFilter
 * @Description TODO
 * @Author Matthiola
 * @Date 2025/6/5 11:48
 */
@Component
@Slf4j
public class ProductBloomFilter {

    private BloomFilter<Long> bloomFilter;

    @Autowired
    private ProductMapper productMapper;


    @PostConstruct
    public void init() {
        // 初始化布隆过滤器
        bloomFilter = BloomFilter.create(Funnels.longFunnel(),
                1000000, // 预计插入的元素数量
                0.001);// 误判率，0.01% 即 0.0001


        // 加载所有商品ID到布隆过滤器
        //todo 运行阶段 .put() 要加锁
        //todo 分页优化
        List<Long> ids = productMapper.getAllProductIds();
        if (ids != null) {
            ids.forEach(bloomFilter::put);
        }
        log.info("布隆过滤器初始化完成，共加载 {} 条商品ID", ids.size());

    }

    // 检查商品ID是否存在于布隆过滤器中
    public Boolean mightContain(Long productId) {
        if (bloomFilter == null) {
            throw new IllegalStateException("Bloom filter is not initialized");
        }
        return bloomFilter.mightContain(productId);
    }

    //添加新增商品到布隆过滤器
    public void addProduct(Long productId) {
        if (bloomFilter != null) {
            bloomFilter.put(productId);
        }
    }

}
