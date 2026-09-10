package com.airi.ai.agent.controller;

import com.airi.ai.agent.mapper.ProductMapper;
import com.airi.ai.agent.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品控制器（商城首页商品展示）
 *
 * <p>复用 ProductMapper，向商城前端提供在售商品列表与热销商品数据。</p>
 */
@RestController
public class ProductController {

    private final ProductMapper productMapper;

    /**
     * 构造注入商品 Mapper
     *
     * @param productMapper 商品 Mapper
     */
    public ProductController(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /**
     * 查询全部在售商品（商城首页商品列表）
     *
     * @return dataMap 携带 data（商品列表，按销量降序）
     */
    @GetMapping("/product/list")
    public R list() {
        return R.ok().data("data", productMapper.selectAll());
    }

    /**
     * 查询热销商品
     *
     * @param limit 返回条数（默认 6）
     * @return dataMap 携带 data（热销商品列表）
     */
    @GetMapping("/product/hot")
    public R hot(@RequestParam(defaultValue = "6") int limit) {
        return R.ok().data("data", productMapper.selectHotProducts(limit));
    }
}
