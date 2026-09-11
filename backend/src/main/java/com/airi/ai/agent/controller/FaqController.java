package com.airi.ai.agent.controller;

import com.airi.ai.agent.pojo.Faq;
import com.airi.ai.agent.result.R;
import com.airi.ai.agent.service.IFaqService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FAQ 知识库管理控制器
 *
 * <p>提供 FAQ 的增删改查接口及 MySQL ↔ Milvus 全量同步。</p>
 */
@RestController
public class FaqController {

    private final IFaqService faqService;

    /**
     * 构造注入 FAQ 服务
     *
     * @param faqService FAQ 服务接口
     */
    public FaqController(IFaqService faqService) {
        this.faqService = faqService;
    }

    /**
     * 新增 FAQ（自动同步到 Milvus）
     *
     * @param faq FAQ 请求体（categoryId/question/answer/status）
     * @return 统一响应对象 R（code=0 表示成功）
     */
    @PostMapping("/add")
    public R add(@RequestBody Faq faq) {
        faqService.add(faq);
        return R.ok();
    }

    /**
     * 删除 FAQ（自动从 Milvus 移除）
     *
     * @param faqId FAQ ID
     * @return 统一响应对象 R（code=0 表示成功）
     */
    @GetMapping("/deleteFaq")
    public R deleteFaq(@RequestParam String faqId) {
        faqService.deleteFaq(faqId);
        return R.ok();
    }

    /**
     * 更新 FAQ（自动同步 Milvus：删旧向量 + 插新向量）
     *
     * @param faq FAQ 请求体（必须携带 id）
     * @return 统一响应对象 R（code=0 表示成功）
     */
    @PostMapping("/update")
    public R updateFaq(@RequestBody Faq faq) {
        faqService.updateFaq(faq);
        return R.ok();
    }

    /**
     * 分页查询 FAQ（支持按分类和关键字过滤）
     *
     * @param page       页码（从 1 开始）
     * @param size       每页条数
     * @param categoryId 分类 ID（可选）
     * @param keyword    关键字（可选）
     * @return dataMap 中携带 data（当前页列表）与 total（总记录数）
     */
    @GetMapping("/getFaqByPage/{page}/{size}")
    public R getFaqByPage(@PathVariable int page,
                          @PathVariable int size,
                          @RequestParam(required = false) Integer categoryId,
                          @RequestParam(required = false) String keyword) {
        List<Faq> faqs = faqService.getFaqByPage(page, size, categoryId, keyword);
        int total = faqService.countFaq(categoryId, keyword);
        return R.ok().data("data", faqs).data("total", total);
    }

    /**
     * 全量同步 MySQL → Milvus（先清空 Milvus 再分页重建）
     *
     * @return 统一响应对象 R（code=0 表示同步触发成功）
     */
    @GetMapping("/syncToMilvus")
    public R syncToMilvus() {
        faqService.syncMySQLToVector();
        return R.ok();
    }
}
