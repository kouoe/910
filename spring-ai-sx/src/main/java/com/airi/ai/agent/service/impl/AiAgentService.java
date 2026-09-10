package com.airi.ai.agent.service.impl;

import com.airi.ai.agent.prompt.PromptScene;
import com.airi.ai.agent.prompt.PromptTemplateManager;
import com.airi.ai.agent.pojo.Faq;
import com.airi.ai.agent.search.VectorSearchService;
import com.airi.ai.agent.service.IAiAgentService;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

/**
 * AI 智能客服核心服务
 *
 * <p>三级问答优先级：</p>
 * <ol>
 *   <li><b>敏感词检测</b>——拦截不当内容，直接返回提示</li>
 *   <li><b>FAQ 语义检索</b>——通过 {@link VectorSearchService} 匹配 FAQ 知识库</li>
 *   <li><b>LLM 生成回答</b>——大模型 + Function Calling + 会话记忆</li>
 * </ol>
 *
 * <p>依赖组件：ChatClient（对话）、VectorSearchService（Milvus 检索）、
 * PromptTemplateManager（提示词渲染）、SensitiveWordBs（敏感词引擎）。</p>
 */
@Service
public class AiAgentService implements IAiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiAgentService.class);

    private final ChatClient chatClient;
    private final VectorSearchService vectorSearchService;
    private final PromptTemplateManager promptTemplateManager;
    private final SensitiveWordBs sensitiveWordBs;

    /**
     * 构造注入核心组件
     *
     * @param chatClient           ChatClient（LLM 对话，已配置工具与记忆顾问）
     * @param vectorSearchService  向量检索服务（FAQ 语义检索）
     * @param promptTemplateManager 提示词模板管理器（结构化渲染）
     * @param sensitiveWordBs      敏感词检测引擎
     */
    public AiAgentService(ChatClient chatClient,
                          VectorSearchService vectorSearchService,
                          PromptTemplateManager promptTemplateManager,
                          SensitiveWordBs sensitiveWordBs) {
        this.chatClient = chatClient;
        this.vectorSearchService = vectorSearchService;
        this.promptTemplateManager = promptTemplateManager;
        this.sensitiveWordBs = sensitiveWordBs;
    }

    /**
     * AI 聊天主入口（三级问答流水线）
     *
     * @param question 用户问题
     * @return AI 回答文本
     */
    @Override
    public String chat(String question) {
        // 第一级：敏感词检测
        boolean hasSensitive = sensitiveWordBs.contains(question);
        if (hasSensitive) {
            log.warn("检测到敏感词，问题被拦截：{}", question);
            return "您的问题包含不当内容，AI 客服暂时无法回答。请换一种方式提问。";
        }

        // 第二级：FAQ 语义检索（Milvus 精确命中 topK=1、阈值 0.7）
        Faq matchedFaq = vectorSearchService.searchFAQ(question);
        if (matchedFaq != null) {
            log.info("FAQ 匹配成功：id={}, question={}", matchedFaq.getId(), matchedFaq.getQuestion());
            return matchedFaq.getAnswer();
        }

        // 第三级：LLM 生成回答（宽松检索注入知识库 + Function Calling + 会话记忆）
        return generateAIResponse(question);
    }

    /**
     * 通过 LLM 生成回答
     *
     * <p>将 Milvus 检索到的 FAQ 知识库内容注入 System Prompt（RAG），
     * 并要求 LLM 只能基于注入内容回答，从源头抑制编造（幻觉）。</p>
     *
     * @param question 用户问题
     * @return LLM 生成的回答
     */
    private String generateAIResponse(String question) {
        String knowledge = vectorSearchService.buildKnowledgeContext(question);

        String resolvedPrompt = promptTemplateManager.render(PromptScene.CUSTOMER_SERVICE, Map.of(
                "current_date", LocalDate.now().toString(),
                "knowledge", knowledge
        ));

        return chatClient.prompt()
                .system(resolvedPrompt)
                .user(question)
                .call()
                .content();
    }
}
