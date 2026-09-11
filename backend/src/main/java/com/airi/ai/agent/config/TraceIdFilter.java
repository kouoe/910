package com.airi.ai.agent.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 链路追踪过滤器
 *
 * <p>为每个 HTTP 请求生成/透传 TraceId：</p>
 * <ul>
 *   <li>请求头携带 X-Trace-Id 则透传（便于外部链路关联）</li>
 *   <li>否则生成新的 TraceId，并通过响应头 X-Trace-Id 返回给调用方</li>
 *   <li>放入 SLF4J MDC，配合 logging.pattern 中的 %X{traceId} 输出到日志，便于排查问题</li>
 * </ul>
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    /** 链路追踪 ID 的请求/响应头名称 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    /** MDC 中的键名，需与 logging.pattern 中的 %X{traceId} 对应 */
    private static final String MDC_KEY = "traceId";
    /** 自动生成的 TraceId 前缀，便于在日志中与外部透传 ID 区分 */
    private static final String TRACE_ID_PREFIX = "TRACE-";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = TRACE_ID_PREFIX
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        }

        MDC.put(MDC_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
