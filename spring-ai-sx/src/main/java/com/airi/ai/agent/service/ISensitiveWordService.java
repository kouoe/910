package com.airi.ai.agent.service;

import com.airi.ai.agent.pojo.SensitiveWord;

import java.util.List;

/**
 * 敏感词服务接口
 *
 * <p>定义敏感词增删查能力，实现类需保证 MySQL 与 SensitiveWordBs 内存词库实时同步。</p>
 */
public interface ISensitiveWordService {

    /**
     * 添加敏感词（同时刷新内存词库）
     *
     * @param sensitiveWord 敏感词实体（word/type 必填）
     */
    void addSensitiveWord(SensitiveWord sensitiveWord);

    /**
     * 删除敏感词（同时从内存词库移除）
     *
     * @param id 敏感词主键
     */
    void deleteSensitiveWord(Long id);

    /**
     * 分页查询敏感词
     *
     * @param page 页码（从 1 开始）
     * @param size 每页条数
     * @param word 模糊搜索词（可为空）
     * @param type 类型过滤（可为空）
     * @return 当前页敏感词列表
     */
    List<SensitiveWord> findSensitiveWords(int page, int size, String word, String type);

    /**
     * 统计总数
     *
     * @param word 模糊搜索词（可为空）
     * @param type 类型过滤（可为空）
     * @return 符合条件的记录总数
     */
    int countSensitiveWords(String word, String type);
}
