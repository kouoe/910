package com.airi.ai.agent.mapper;

import com.airi.ai.agent.pojo.Faq;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FAQ Mapper 接口（纯 MyBatis 注解方式）
 *
 * <p>所有 SQL 通过注解显式声明，不继承 BaseMapper。</p>
 * <p>禁止使用 MyBatis-Plus 的 BaseMapper 继承方式。</p>
 * <p>动态 SQL（分页/条件过滤）使用 &lt;script&gt; 标签实现。</p>
 */
@Repository
public interface FaqMapper {

    /**
     * 新增 FAQ 记录（写入 MySQL t_faq 表）
     *
     * @param faq FAQ 实体（id 一般由服务层先生成 UUID，useCount 默认 0）
     * @return 受影响行数（1 表示插入成功）
     */
    @Insert("""
            INSERT INTO t_faq(id, category_id, question, answer, status, use_count)
            VALUES(#{id}, #{categoryId}, #{question}, #{answer}, #{status}, #{useCount})
            """)
    int insert(Faq faq);

    /**
     * 根据 ID 删除 FAQ 记录
     *
     * @param id FAQ ID
     * @return 受影响行数（0 表示记录不存在）
     */
    @Delete("""
            DELETE FROM t_faq WHERE id = #{id}
            """)
    int deleteById(@Param("id") String id);

    /**
     * 更新 FAQ 记录（不更新 use_count，命中次数由 incrementUseCount 单独原子累加）
     *
     * @param faq FAQ 实体（必须携带 id）
     * @return 受影响行数（1 表示更新成功）
     */
    @Update("""
            UPDATE t_faq SET category_id = #{categoryId}, question = #{question},
                   answer = #{answer}, status = #{status}
            WHERE id = #{id}
            """)
    int updateById(Faq faq);

    /**
     * 根据 ID 查询 FAQ（使用 @Results 显式声明列与属性的映射关系）
     *
     * @param id FAQ ID
     * @return FAQ 对象，未找到返回 null
     */
    @Select("""
            SELECT id, category_id, question, answer, status, use_count
            FROM t_faq
            WHERE id = #{id}
            """)
    @Results(id = "faqResultMap", value = {
        @Result(column = "id", property = "id", id = true),
        @Result(column = "category_id", property = "categoryId"),
        @Result(column = "question", property = "question"),
        @Result(column = "answer", property = "answer"),
        @Result(column = "status", property = "status"),
        @Result(column = "use_count", property = "useCount")
    })
    Faq selectById(@Param("id") String id);

    /**
     * 查询所有 FAQ（用于数据导出/全量同步等场景）
     *
     * @return 全部 FAQ 列表
     */
    @Select("""
            SELECT id, category_id, question, answer, status, use_count
            FROM t_faq
            """)
    @ResultMap("faqResultMap")
    List<Faq> selectAll();

    /**
     * 分页查询 FAQ（支持按分类和关键字过滤，动态 SQL）
     *
     * @param offset     SQL 偏移量（从 0 开始）
     * @param limit      每页条数
     * @param categoryId 分类 ID（可选，为空不过滤）
     * @param keyword    关键字（可选，为空不过滤，匹配 question 模糊查询）
     * @return 当前页 FAQ 列表（按 use_count 降序，即热度优先）
     */
    @Select("""
            <script>
            SELECT id, category_id, question, answer, status, use_count FROM t_faq
            <where>
              <if test='categoryId != null'>AND category_id = #{categoryId}</if>
              <if test='keyword != null and keyword != ""'>AND question LIKE CONCAT('%', #{keyword}, '%')</if>
            </where>
            ORDER BY use_count DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    @ResultMap("faqResultMap")
    List<Faq> selectByPage(@Param("offset") int offset,
                           @Param("limit") int limit,
                           @Param("categoryId") Integer categoryId,
                           @Param("keyword") String keyword);

    /**
     * 统计 FAQ 总数（支持按分类和关键字过滤，与 selectByPage 条件保持一致）
     *
     * @param categoryId 分类 ID（可选）
     * @param keyword    关键字（可选）
     * @return 符合条件的记录总数
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM t_faq
            <where>
              <if test='categoryId != null'>AND category_id = #{categoryId}</if>
              <if test='keyword != null and keyword != ""'>AND question LIKE CONCAT('%', #{keyword}, '%')</if>
            </where>
            </script>
            """)
    int count(@Param("categoryId") Integer categoryId,
              @Param("keyword") String keyword);

    /**
     * 原子递增 FAQ 命中次数（线程安全）
     *
     * <p>使用 COALESCE 防止 use_count 为 NULL 时的 NPE。</p>
     *
     * @param id FAQ ID
     * @return 受影响行数（1 表示累加成功）
     */
    @Update("""
            UPDATE t_faq SET use_count = COALESCE(use_count, 0) + 1
            WHERE id = #{id}
            """)
    int incrementUseCount(@Param("id") String id);
}
