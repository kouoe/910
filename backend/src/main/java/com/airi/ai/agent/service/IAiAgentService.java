package com.airi.ai.agent.service;

/**
 * AI 智能客服服务接口
 *
 * <p>定义 AI 对话的核心入口，实现类负责三级问答流水线：</p>
 * <ol>
 *   <li>敏感词检测</li>
 *   <li>FAQ 语义检索（Milvus）</li>
 *   <li>LLM 生成回答（Function Calling + 会话记忆 + RAG）</li>
 * </ol>
 */
public interface IAiAgentService {

    /**
     * AI 聊天主入口
     *
     * @param question 用户问题
     * @return AI 回答文本
     */
    String chat(String question);
}
