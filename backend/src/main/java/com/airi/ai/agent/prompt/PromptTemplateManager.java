package com.airi.ai.agent.prompt;

import com.airi.ai.agent.exception.BusinessException;
import com.airi.ai.agent.result.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 结构化提示词模板管理器
 *
 * <p>核心能力：</p>
 * <ul>
 *   <li><b>动态参数替换</b>：将模板中的 <code>{param}</code> 占位符替换为业务传入的参数值；</li>
 *   <li><b>参数完整性校验</b>：渲染前扫描模板占位符，缺失参数直接抛 {@link BusinessException}，
 *       避免占位符被原样送入大模型；</li>
 *   <li><b>多场景适配</b>：按 {@link PromptScene} 选择模板，支持 application.yml
 *       中 <code>spring.ai.prompt-template.&lt;sceneKey&gt;</code> 覆盖内置默认模板；</li>
 *   <li><b>依赖收口</b>：全项目提示词渲染统一走本类，替换散落的
 *       <code>new PromptTemplate(...)</code> 直用代码。</li>
 * </ul>
 */
@Component
public class PromptTemplateManager {

    /** 占位符正则：{参数名}，参数名由字母/数字/下划线组成 */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\w+)}");

    /** 客服问答场景模板（来源：application.yml spring.ai.dashscope.prompt），为空则回退内置默认 */
    @Value("${spring.ai.dashscope.prompt:}")
    private String customerServiceTemplate;

    /**
     * 渲染指定场景的提示词
     *
     * <p>模板优先级：配置模板 &gt; 场景内置默认模板。均不可用则抛业务异常。</p>
     *
     * @param scene  提示词场景
     * @param params 动态参数（键为占位符名，不含花括号）
     * @return 渲染后的完整提示词
     */
    public String render(PromptScene scene, Map<String, Object> params) {
        String template = resolveTemplate(scene);
        if (!StringUtils.hasText(template)) {
            throw new BusinessException(ErrorCode.PROMPT_TEMPLATE_NOT_FOUND);
        }
        return render(template, params);
    }

    /**
     * 渲染指定模板字符串
     *
     * <p>先校验所有占位符均有对应参数，再逐一替换。
     * 校验不通过抛 {@link ErrorCode#PROMPT_TEMPLATE_MISSING_PARAM}，防止占位符残留。</p>
     *
     * @param template 提示词模板（含 {param} 占位符）
     * @param params   动态参数
     * @return 渲染后的提示词
     */
    public String render(String template, Map<String, Object> params) {
        if (!StringUtils.hasText(template)) {
            throw new BusinessException(ErrorCode.PROMPT_TEMPLATE_NOT_FOUND);
        }
        Map<String, Object> safeParams = params == null ? new HashMap<>() : params;

        // 1. 扫描模板占位符，校验参数完整性
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder missing = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = safeParams.get(key);
            if (value == null || !StringUtils.hasText(String.valueOf(value))) {
                if (missing.length() > 0) {
                    missing.append(", ");
                }
                missing.append(key);
            }
        }
        if (missing.length() > 0) {
            throw new BusinessException(
                    ErrorCode.PROMPT_TEMPLATE_MISSING_PARAM.getCode(),
                    "提示词模板缺少必需参数：" + missing);
        }

        // 2. 依次替换占位符（null 值跳过，不写入模板）
        String result = template;
        for (Map.Entry<String, Object> entry : safeParams.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }

    /**
     * 解析场景模板
     *
     * <p>优先级：配置模板 &gt; 场景内置默认模板。</p>
     *
     * @param scene 提示词场景
     * @return 解析得到的模板字符串；场景为空返回 null
     */
    private String resolveTemplate(PromptScene scene) {
        if (scene == null) {
            return null;
        }
        if (scene == PromptScene.CUSTOMER_SERVICE && StringUtils.hasText(customerServiceTemplate)) {
            return customerServiceTemplate;
        }
        return scene.getDefaultTemplate();
    }
}
