package com.airi.ai.agent.controller;

import com.airi.ai.agent.pojo.SensitiveWord;
import com.airi.ai.agent.result.R;
import com.airi.ai.agent.service.ISensitiveWordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 敏感词管理控制器
 *
 * <p>提供敏感词的增删查接口，操作同时更新 MySQL 和内存词库。</p>
 * <p>所有接口统一返回 R 结构，由全局异常处理器兜底异常场景。</p>
 */
@RestController
@RequestMapping("/sensitiveWord")
public class SensitiveWordController {

    private final ISensitiveWordService sensitiveWordService;

    /**
     * 构造器注入服务，避免字段注入
     *
     * @param sensitiveWordService 敏感词服务
     */
    public SensitiveWordController(ISensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    /**
     * 添加敏感词
     *
     * @param sensitiveWord 敏感词实体
     * @return 统一响应（成功 code=0）
     */
    @PostMapping("/add")
    public R addSensitiveWord(@RequestBody SensitiveWord sensitiveWord) {
        sensitiveWordService.addSensitiveWord(sensitiveWord);
        return R.ok();
    }

    /**
     * 根据 ID 删除敏感词
     *
     * @param id 敏感词主键（Query 参数，如 ?id=1）
     * @return 统一响应（成功 code=0；记录不存在时由全局异常返回 code=101）
     */
    @DeleteMapping("/delete")
    public R deleteSensitiveWord(@RequestParam Long id) {
        sensitiveWordService.deleteSensitiveWord(id);
        return R.ok();
    }

    /**
     * 分页查询敏感词（支持模糊搜索）
     *
     * @param page 页码，默认 1
     * @param size 每页条数，默认 10
     * @param word 模糊搜索词（可选）
     * @param type 类型过滤 deny/allow（可选）
     * @return 统一响应，列表放 dataMap.data、总数放 dataMap.total
     */
    @GetMapping("/search")
    public R search(@RequestParam(defaultValue = "1") int page,
                    @RequestParam(defaultValue = "10") int size,
                    @RequestParam(required = false) String word,
                    @RequestParam(required = false) String type) {
        List<SensitiveWord> list = sensitiveWordService.findSensitiveWords(page, size, word, type);
        int total = sensitiveWordService.countSensitiveWords(word, type);
        return R.ok().data("data", list).data("total", total);
    }
}
