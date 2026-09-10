package com.airi.ai.agent.config;

import com.airi.ai.agent.functioncall.OrderService;
import com.airi.ai.agent.functioncall.ProductService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 应用配置类
 *
 * <p>配置 ChatClient Bean：</p>
 * <ul>
 *   <li>temperature=0.3：客服场景偏保守回答</li>
 *   <li>注册 OrderService 为 Function Calling 工具</li>
 *   <li>添加 SimpleLoggerAdvisor 记录调用日志</li>
 *   <li>添加 PromptChatMemoryAdvisor 实现多轮对话记忆</li>
 * </ul>
 */
@SpringBootConfiguration
@ComponentScan(basePackages = "com.airi.ai.agent")
public class AppConfig {

    private final ChatModel chatModel;
    private final ChatMemory chatMemory;
    private final OrderService orderService;
    private final ProductService productService;

    /**
     * 构造注入核心组件
     *
     * @param chatModel      大模型（DashScope 通义千问，由 spring-ai-alibaba starter 自动装配）
     * @param chatMemory     会话记忆存储（多轮对话历史）
     * @param orderService   订单查询工具（Function Calling 的 @Tool 方法所在类）
     * @param productService 商品查询工具（Function Calling 的 @Tool 方法所在类）
     */
    public AppConfig(ChatModel chatModel,
                     ChatMemory chatMemory,
                     OrderService orderService,
                     ProductService productService) {
        this.chatModel = chatModel;
        this.chatMemory = chatMemory;
        this.orderService = orderService;
        this.productService = productService;
    }

    /**
     * 构建 ChatClient Bean
     *
     * @return ChatClient 实例
     */
    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(chatModel)
                .defaultOptions(
                        // 必须使用支持工具调用的 ToolCallingChatOptions
                        ToolCallingChatOptions.builder()
                                .temperature(0.3D)
                                .build()
                )
                // 注册 Function Calling 工具（OrderService/ProductService 中的 @Tool 方法自动暴露给模型）
                .defaultTools(orderService, productService)
                // 分析过程日志
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                // 多轮对话记忆
                .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
