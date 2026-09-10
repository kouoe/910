package com.airi.ai.agent.result;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一 API 响应包装类
 *
 * <p>所有 Controller 接口统一使用 R 返回，前端只需处理一种数据结构。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 *   // 成功（无数据）
 *   return R.ok();
 *
 *   // 成功（带数据）
 *   return R.ok().data("user", user).data("total", 100);
 *
 *   // 业务失败
 *   return R.error(ErrorCode.PARAMETER_ERROR);
 *
 *   // 自定义错误
 *   return R.error(500, "服务器内部错误");
 * </pre>
 */
@Data
public class R {

    /** 状态码（0=成功, 非0=失败） */
    private Integer code;
    /** 提示消息 */
    private String msg;
    /** 响应数据 */
    private Map<String, Object> dataMap = new HashMap<>();

    /** 私有构造——不允许外部 new，统一通过静态工厂方法创建 */
    private R() {}

    // ========== 静态工厂方法 ==========

    /**
     * 成功（无数据）
     *
     * @return code=0、msg="成功" 的空 R 实例
     */
    public static R ok() {
        R r = new R();
        r.setCode(ErrorCode.SUCCESS.getCode());
        r.setMsg(ErrorCode.SUCCESS.getMessage());
        return r;
    }

    /**
     * 失败（通用）
     *
     * @return code=-1、msg="失败" 的 R 实例
     */
    public static R error() {
        R r = new R();
        r.setCode(ErrorCode.FAIL.getCode());
        r.setMsg(ErrorCode.FAIL.getMessage());
        return r;
    }

    /**
     * 失败（指定错误码）
     *
     * @param errorCode 错误码枚举（code 与 msg 同时取自枚举）
     * @return 携带指定错误码的 R 实例
     */
    public static R error(ErrorCode errorCode) {
        R r = new R();
        r.setCode(errorCode.getCode());
        r.setMsg(errorCode.getMessage());
        return r;
    }

    /**
     * 失败（自定义 code 和 msg）
     *
     * @param code 自定义错误码
     * @param msg  自定义错误消息
     * @return 携带自定义错误信息的 R 实例
     */
    public static R error(Integer code, String msg) {
        R r = new R();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    /**
     * 链式添加数据
     *
     * @param key 数据字段名
     * @param val 数据值
     * @return 当前 R 实例（支持链式调用）
     */
    public R data(String key, Object val) {
        this.dataMap.put(key, val);
        return this;
    }

    /**
     * 批量添加数据
     *
     * @param maps 待合并的数据 Map
     * @return 当前 R 实例（支持链式调用）
     */
    public R data(Map<String, Object> maps) {
        this.dataMap.putAll(maps);
        return this;
    }
}
