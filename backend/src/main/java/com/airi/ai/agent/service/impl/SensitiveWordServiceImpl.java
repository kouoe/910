package com.airi.ai.agent.service.impl;

import com.airi.ai.agent.exception.BusinessException;
import com.airi.ai.agent.mapper.SensitiveWordMapper;
import com.airi.ai.agent.pojo.SensitiveWord;
import com.airi.ai.agent.result.ErrorCode;
import com.airi.ai.agent.service.ISensitiveWordService;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 敏感词服务实现类
 *
 * <p>核心原则：MySQL 与内存词库保持实时同步。</p>
 * <p>每次增删操作同时更新数据库 + SensitiveWordBs 内存库。</p>
 */
@Service
public class SensitiveWordServiceImpl implements ISensitiveWordService {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordServiceImpl.class);

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordBs sensitiveWordBs;

    /**
     * 构造器注入 Mapper 与敏感词过滤器
     *
     * @param sensitiveWordMapper 敏感词 Mapper（负责 MySQL 持久化）
     * @param sensitiveWordBs     敏感词过滤器核心（负责内存词库实时生效）
     */
    public SensitiveWordServiceImpl(SensitiveWordMapper sensitiveWordMapper,
                                    SensitiveWordBs sensitiveWordBs) {
        this.sensitiveWordMapper = sensitiveWordMapper;
        this.sensitiveWordBs = sensitiveWordBs;
    }

    /**
     * 添加敏感词
     *
     * <p>流程：MySQL INSERT → 刷新 SensitiveWordBs 内存库，保证新增词即刻生效。</p>
     *
     * @param sensitiveWord 敏感词实体（word/type 必填）
     */
    @Override
    public void addSensitiveWord(SensitiveWord sensitiveWord) {
        // 1. 写入 MySQL（先持久化，保证重启后词库不丢失）
        sensitiveWordMapper.insert(sensitiveWord);

        // 2. 同步内存词库：按类型分别加入黑名单（deny）或白名单（allow）
        if (StringUtils.hasText(sensitiveWord.getType()) && "deny".equals(sensitiveWord.getType())) {
            sensitiveWordBs.addWord(sensitiveWord.getWord());
            log.info("添加黑名单词：{}", sensitiveWord.getWord());
        } else if (StringUtils.hasText(sensitiveWord.getType()) && "allow".equals(sensitiveWord.getType())) {
            sensitiveWordBs.addWordAllow(sensitiveWord.getWord());
            log.info("添加白名单词：{}", sensitiveWord.getWord());
        }
    }

    /**
     * 删除敏感词
     *
     * <p>流程：先查后删 → MySQL DELETE → 从 SensitiveWordBs 移除。</p>
     *
     * @param id 敏感词主键
     */
    @Override
    public void deleteSensitiveWord(Long id) {
        // 1. 根据 id 查询，确认存在（不存在则抛业务异常，避免"删空气"）
        SensitiveWord sensitiveWord = sensitiveWordMapper.selectById(id);
        if (sensitiveWord == null) {
            throw new BusinessException(ErrorCode.SENSITIVE_WORD_ID_ERROR);
        }

        // 2. 从 MySQL 删除
        sensitiveWordMapper.deleteById(id);

        // 3. 从内存词库移除（与新增逻辑对称，按类型选择移除入口）
        if (StringUtils.hasText(sensitiveWord.getType()) && "deny".equals(sensitiveWord.getType())) {
            sensitiveWordBs.removeWord(sensitiveWord.getWord());
            log.info("移除黑名单词：{}", sensitiveWord.getWord());
        } else if (StringUtils.hasText(sensitiveWord.getType()) && "allow".equals(sensitiveWord.getType())) {
            sensitiveWordBs.removeWordAllow(sensitiveWord.getWord());
            log.info("移除白名单词：{}", sensitiveWord.getWord());
        }
    }

    /**
     * 分页查询敏感词
     *
     * @param page 页码（从 1 开始）
     * @param size 每页条数
     * @param word 模糊搜索词（可为空）
     * @param type 类型过滤（可为空）
     * @return 当前页敏感词列表
     */
    @Override
    public List<SensitiveWord> findSensitiveWords(int page, int size,
                                                   String word, String type) {
        int offset = (page - 1) * size;
        return sensitiveWordMapper.selectByPage(offset, size, word, type);
    }

    /**
     * 统计敏感词总数（与分页查询共用同一套过滤条件）
     *
     * @param word 模糊搜索词（可为空）
     * @param type 类型过滤（可为空）
     * @return 符合条件的记录总数
     */
    @Override
    public int countSensitiveWords(String word, String type) {
        return sensitiveWordMapper.countByCondition(word, type);
    }
}
