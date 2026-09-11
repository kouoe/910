package com.airi.ai.agent.controller;

import com.airi.ai.agent.result.ErrorCode;
import com.airi.ai.agent.result.R;
import com.airi.ai.agent.service.IAiAgentService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 聊天控制器
 *
 * <p>前端通过 POST /ai/agent/chat 与此接口通信。</p>
 * <p>请求格式：<code>{"message": "用户问题"}</code></p>
 * <p>响应格式：<code>{"code":0, "msg":"成功", "dataMap":{"response":"AI回答"}}</code></p>
 */
@RestController
@RequestMapping("/ai/agent")
public class AiAgentController {

    private final IAiAgentService aiAgentService;

    /**
     * 构造器注入服务，避免字段注入
     *
     * @param aiAgentService AI 对话服务（三级问答核心逻辑）
     */
    public AiAgentController(IAiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    /**
     * AI 聊天接口
     *
     * <p>三级处理流程由 {@link com.airi.ai.agent.service.impl.AiAgentService} 内部实现：</p>
     * <ol>
     *   <li>敏感词检测</li>
     *   <li>Milvus 语义检索</li>
     *   <li>LLM 生成回答</li>
     * </ol>
     *
     * @param question 请求体，包含 message 字段
     * @return 统一响应，AI 回答放在 dataMap.response 中
     */
    @PostMapping("/chat")
    public R chat(@RequestBody(required = false) Map<String, String> question) {
        String msg = question == null ? null : question.get("message");
        if (!StringUtils.hasText(msg)) {
            return R.error(ErrorCode.PARAMETER_ERROR);
        }
        String response = aiAgentService.chat(msg);
        return R.ok().data("response", response);
    }
}
