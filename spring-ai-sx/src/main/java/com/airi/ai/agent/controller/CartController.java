package com.airi.ai.agent.controller;

import com.airi.ai.agent.mapper.CartMapper;
import com.airi.ai.agent.pojo.CartItem;
import com.airi.ai.agent.result.ErrorCode;
import com.airi.ai.agent.result.R;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 购物车控制器（商城购物车功能）
 *
 * <p>提供加入、查看、改数量、删除等接口，按 userId 区分用户购物车。</p>
 */
@RestController
public class CartController {

    private final CartMapper cartMapper;

    /**
     * 构造注入购物车 Mapper
     *
     * @param cartMapper 购物车 Mapper
     */
    public CartController(CartMapper cartMapper) {
        this.cartMapper = cartMapper;
    }

    /**
     * 加入购物车（重复商品自动累加数量）
     *
     * @param userId    用户 ID
     * @param productNo 商品编号
     * @param quantity  数量
     * @return 统一响应 R
     */
    @PostMapping("/cart/add")
    public R add(@RequestParam Long userId,
                 @RequestParam String productNo,
                 @RequestParam(defaultValue = "1") int quantity) {
        if (userId == null || !StringUtils.hasText(productNo) || quantity <= 0) {
            return R.error(ErrorCode.PARAMETER_ERROR);
        }
        cartMapper.add(userId, productNo, quantity);
        return R.ok();
    }

    /**
     * 查询购物车列表
     *
     * @param userId 用户 ID
     * @return dataMap 携带 data（购物车条目列表，含商品名称/价格等）
     */
    @GetMapping("/cart/list")
    public R list(@RequestParam Long userId) {
        List<CartItem> items = cartMapper.selectByUserId(userId);
        return R.ok().data("data", items);
    }

    /**
     * 修改购物车条目数量
     *
     * @param userId    用户 ID
     * @param productNo 商品编号
     * @param quantity  新数量
     * @return 统一响应 R
     */
    @PostMapping("/cart/update")
    public R update(@RequestParam Long userId,
                    @RequestParam String productNo,
                    @RequestParam int quantity) {
        if (userId == null || !StringUtils.hasText(productNo) || quantity <= 0) {
            return R.error(ErrorCode.PARAMETER_ERROR);
        }
        cartMapper.updateQuantity(userId, productNo, quantity);
        return R.ok();
    }

    /**
     * 删除购物车单条商品
     *
     * @param userId    用户 ID
     * @param productNo 商品编号
     * @return 统一响应 R
     */
    @PostMapping("/cart/remove")
    public R remove(@RequestParam Long userId, @RequestParam String productNo) {
        cartMapper.delete(userId, productNo);
        return R.ok();
    }
}
