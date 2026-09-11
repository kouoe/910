package com.airi.ai.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI 实训项目 —— 艾瑞电商 AI 客服系统
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>FAQ 知识库管理（MySQL + Milvus 双存储）</li>
 *   <li>AI 智能问答（DashScope 通义千问）</li>
 *   <li>Function Calling 订单查询</li>
 *   <li>敏感词内容安全过滤</li>
 *   <li>多轮对话记忆</li>
 * </ul>
 *
 * @author spring-ai-sx
 */
@SpringBootApplication
// 扫描 Mapper 接口包，将 FaqMapper/SensitiveWordMapper/OrderMapper 注册为 Spring Bean
@MapperScan(basePackages = "com.airi.ai.agent.mapper")
public class SpringAiSxApplication {

    /**
     * 应用启动入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringAiSxApplication.class, args);
    }
}
