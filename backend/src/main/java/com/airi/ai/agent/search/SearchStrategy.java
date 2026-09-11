package com.airi.ai.agent.search;

/**
 * 向量检索策略枚举
 *
 * <p>定义项目双通道检索参数（topK 与相似度阈值），供 {@link VectorSearchService} 使用：</p>
 * <ul>
 *   <li><b>EXACT_MATCH（精确命中）</b>：topK=1、阈值 0.7 —— 只接受高度相似的唯一结果，
 *       用于"直接返回 FAQ 标准答案"的一级命中；</li>
 *   <li><b>CONTEXT_INJECTION（上下文注入）</b>：topK=3、阈值 0.5 —— 宽松召回多条相关 FAQ，
 *       用于 RAG 场景将知识注入 System Prompt 供 LLM 作答。</li>
 * </ul>
 *
 * <p>两档阈值形成"先严格后宽松"的漏斗：严格通道保证命中即高质量答案，
 * 宽松通道兜底给 LLM 足够上下文，同时配合 System Prompt 的防幻觉约束抑制编造。</p>
 */
public enum SearchStrategy {

    /** 精确命中：返回最匹配的 1 条，低于 0.7 视为不匹配 */
    EXACT_MATCH(1, 0.7D),

    /** 上下文注入：返回最多 3 条，低于 0.5 视为不相关 */
    CONTEXT_INJECTION(3, 0.5D),
    ;

    /** 返回结果条数上限 */
    private final int topK;

    /** 相似度阈值（低于该值的结果被过滤） */
    private final double similarityThreshold;

    SearchStrategy(int topK, double similarityThreshold) {
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
    }

    public int getTopK() {
        return topK;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }
}
