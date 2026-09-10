package com.airi.ai.agent.functioncall;

import com.airi.ai.agent.mapper.ProductMapper;
import com.airi.ai.agent.pojo.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 商品查询 Function Calling 服务
 *
 * <p>使用 Spring AI 1.0 新版 @Tool 注解声明 AI 可调用的工具函数，
 * 覆盖商品搜索、价格查询、分类查询、价格区间查询、热销排行等业务能力。</p>
 * <p>商品数据存储在 MySQL t_product 表中，通过 {@link ProductMapper} 查询。</p>
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductMapper productMapper;

    /**
     * 构造注入商品 Mapper
     *
     * @param productMapper 商品 Mapper（查询 MySQL t_product 表）
     */
    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /**
     * 按关键字搜索商品
     *
     * @param keyword 商品名称关键字（如：耳机、电脑）
     * @return 匹配的商品列表（按销量降序）
     */
    @Tool(description = "按商品名称关键字搜索商品，输入关键字如\"耳机\"返回匹配的商品列表")
    public List<Product> searchProducts(@ToolParam(description = "商品名称关键字，如：耳机、电脑、鼠标") String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        List<Product> products = productMapper.selectByKeyword(keyword.trim());
        log.info("按关键字搜索商品：keyword={}, 命中 {} 条", keyword, products.size());
        return products;
    }

    /**
     * 查询单个商品价格
     *
     * @param name 商品名称（如：智能手机）
     * @return 商品对象（含价格），未找到返回 null
     */
    @Tool(description = "根据商品名称查询商品价格和详情，输入商品名称如\"智能手机\"返回该商品的价格、库存、销量等信息")
    public Product getProductPrice(@ToolParam(description = "商品名称，如：智能手机、蓝牙耳机") String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        return productMapper.selectByName(name.trim());
    }

    /**
     * 按分类查询商品
     *
     * @param category 商品分类（如：电脑办公）
     * @return 该分类下的商品列表（按销量降序）
     */
    @Tool(description = "按商品分类查询商品列表，分类包括：手机数码、电脑办公、智能穿戴、智能家居、网络设备")
    public List<Product> getProductsByCategory(@ToolParam(description = "商品分类，如：电脑办公、手机数码") String category) {
        if (!StringUtils.hasText(category)) {
            return Collections.emptyList();
        }
        List<Product> products = productMapper.selectByCategory(category.trim());
        log.info("按分类查询商品：category={}, 命中 {} 条", category, products.size());
        return products;
    }

    /**
     * 按价格区间查询商品
     *
     * @param minPrice 最低价（可为空）
     * @param maxPrice 最高价（可为空）
     * @return 价格区间内的商品列表（按价格升序）
     */
    @Tool(description = "按价格区间查询商品，输入最低价和最高价，返回价格区间内的商品列表")
    public List<Product> getProductsByPriceRange(@ToolParam(description = "最低价（元），可不填") Double minPrice,
                                                 @ToolParam(description = "最高价（元），可不填") Double maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return Collections.emptyList();
        }
        List<Product> products = productMapper.selectByPriceRange(minPrice, maxPrice);
        log.info("按价格区间查询商品：min={}, max={}, 命中 {} 条", minPrice, maxPrice, products.size());
        return products;
    }

    /**
     * 查询热销商品排行
     *
     * @param limit 返回条数（默认 5）
     * @return 热销商品列表（按累计销量降序）
     */
    @Tool(description = "查询热销商品排行榜，输入返回条数如5，返回销量最高的前N个商品")
    public List<Product> getHotProducts(@ToolParam(description = "返回商品条数，如5，默认5") Integer limit) {
        int size = limit == null || limit <= 0 ? 5 : limit;
        List<Product> products = productMapper.selectHotProducts(size);
        log.info("查询热销商品：limit={}, 命中 {} 条", size, products.size());
        return products;
    }
}
