package com.airi.ai.agent.config;

import com.airi.ai.agent.mapper.SensitiveWordMapper;
import com.airi.ai.agent.pojo.SensitiveWord;
import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词过滤配置类
 *
 * <p>核心设计：</p>
 * <ul>
 *   <li><b>黑名单（deny）</b>：被拦截的敏感词，包含后拒绝回答</li>
 *   <li><b>白名单（allow）</b>：虽包含敏感字符但允许通过的安全词</li>
 *   <li><b>数据库驱动</b>：黑/白名单从 MySQL 动态加载，支持运行时增删</li>
 *   <li><b>.ignoreCase(true)</b>：忽略英文大小写</li>
 *   <li><b>.ignoreWidth(true)</b>：忽略全角/半角差异</li>
 * </ul>
 */
@SpringBootConfiguration
public class SensitiveWordConfig {

    private final SensitiveWordMapper sensitiveWordMapper;

    /**
     * 构造器注入 Mapper，避免字段注入
     *
     * @param sensitiveWordMapper 敏感词 Mapper
     */
    public SensitiveWordConfig(SensitiveWordMapper sensitiveWordMapper) {
        this.sensitiveWordMapper = sensitiveWordMapper;
    }

    /**
     * 构建 SensitiveWordBs Bean
     *
     * @return 配置完成的敏感词过滤器核心对象（全局单例，供 AiAgentService 检测调用）
     */
    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
                // 黑名单 = 默认库 + MySQL deny 词（匿名 IWordDeny 每次调用实时查库）
                .wordDeny(WordDenys.chains(WordDenys.defaults(), new IWordDeny() {
                    @Override
                    public List<String> deny() {
                        return listSensitiveWordByType("deny");
                    }
                }))
                // 白名单 = 默认库 + MySQL allow 词（匿名 IWordAllow 每次调用实时查库）
                .wordAllow(WordAllows.chains(WordAllows.defaults(), new IWordAllow() {
                    @Override
                    public List<String> allow() {
                        return listSensitiveWordByType("allow");
                    }
                }))
                .ignoreCase(true)          // 忽略大小写
                .ignoreWidth(true)         // 忽略半角和全角
                .enableEmailCheck(true)    // 开启邮箱地址检查
                .enableUrlCheck(true)      // 开启 URL 检查
                .enableNumCheck(true)      // 开启数字检查（拦截手机号/银行卡等隐私数字）
                .numCheckLen(6)            // 连续 ≥6 位数字才判定敏感
                .init();
    }

    /**
     * 从 MySQL 查询指定类型的敏感词
     *
     * @param type "deny" 黑名单 / "allow" 白名单
     * @return 敏感词列表
     */
    private List<String> listSensitiveWordByType(String type) {
        List<SensitiveWord> sensitiveWords = sensitiveWordMapper.selectByType(type);
        List<String> wordList = new ArrayList<>();
        for (SensitiveWord word : sensitiveWords) {
            wordList.add(word.getWord());
        }
        return wordList;
    }
}
