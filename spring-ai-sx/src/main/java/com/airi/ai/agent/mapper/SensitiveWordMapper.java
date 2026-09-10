package com.airi.ai.agent.mapper;

import com.airi.ai.agent.pojo.SensitiveWord;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 敏感词 Mapper
 *
 * <p>使用纯 MyBatis 注解方式，所有 SQL 显式声明。</p>
 * <p>禁止使用 MyBatis-Plus 的 BaseMapper 继承方式。</p>
 *
 * <p>SQL 统一使用 Java 15+ 文本块（text block）编写，杜绝字符串 "+" 拼接；</p>
 * <p>分页与条件统计通过 &lt;script&gt; 动态 SQL 按需拼装过滤条件。</p>
 */
@Repository
public interface SensitiveWordMapper {

    /**
     * 新增敏感词
     *
     * <p>配合 @Options(useGeneratedKeys) 开启主键回填：插入成功后自增主键自动写入实体 id 字段。</p>
     *
     * @param sensitiveWord 敏感词实体（word/type 必填）
     * @return 受影响行数（1 表示插入成功）
     */
    @Insert("""
            INSERT INTO t_sensitive_word(word, type) VALUES(#{word}, #{type})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SensitiveWord sensitiveWord);

    /**
     * 根据 ID 删除敏感词
     *
     * @param id 敏感词主键
     * @return 受影响行数（1 表示删除成功）
     */
    @Delete("""
            DELETE FROM t_sensitive_word WHERE id = #{id}
            """)
    int deleteById(@Param("id") Long id);

    /**
     * 根据 ID 查询敏感词
     *
     * @param id 敏感词主键
     * @return 敏感词实体，记录不存在时返回 null
     */
    @Select("""
            SELECT id, word, type FROM t_sensitive_word WHERE id = #{id}
            """)
    SensitiveWord selectById(@Param("id") Long id);

    /**
     * 根据类型查询所有敏感词
     *
     * <p>供 SensitiveWordConfig 启动构建词库时加载黑/白名单使用。</p>
     *
     * @param type 类型：deny=黑名单 / allow=白名单
     * @return 该类型下的全部敏感词列表
     */
    @Select("""
            SELECT id, word, type FROM t_sensitive_word WHERE type = #{type}
            """)
    List<SensitiveWord> selectByType(@Param("type") String type);

    /**
     * 分页查询——支持模糊匹配
     *
     * @param offset 起始偏移量（由服务层按 (page-1) * size 计算）
     * @param limit  每页条数
     * @param word   模糊搜索词（可为空）
     * @param type   类型过滤（可为空）
     * @return 当前页的敏感词列表
     */
    @Select("""
            <script>
            SELECT id, word, type FROM t_sensitive_word
            <where>
              <if test='word != null and word != ""'>AND word LIKE CONCAT('%', #{word}, '%')</if>
              <if test='type != null and type != ""'>AND type = #{type}</if>
            </where>
            ORDER BY id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<SensitiveWord> selectByPage(@Param("offset") int offset,
                                     @Param("limit") int limit,
                                     @Param("word") String word,
                                     @Param("type") String type);

    /**
     * 统计符合条件的总数
     *
     * <p>过滤条件与 selectByPage 保持一致，供前端分页组件计算总页数。</p>
     *
     * @param word 模糊搜索词（可为空）
     * @param type 类型过滤（可为空）
     * @return 符合条件的记录总数
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM t_sensitive_word
            <where>
              <if test='word != null and word != ""'>AND word LIKE CONCAT('%', #{word}, '%')</if>
              <if test='type != null and type != ""'>AND type = #{type}</if>
            </where>
            </script>
            """)
    int countByCondition(@Param("word") String word, @Param("type") String type);
}
