package top.ortus.lightmark.backend.tools;

import java.util.Map;

/**
 * 简单的工具接口，允许通过名字注册并执行带参数的工具
 */
public interface Tool {
    /**
     * 工具名称（用于模型函数调用识别）
     */
    String name();

    /**
     * 执行工具
     * @param args 参数键值对
     * @return 执行结果
     */
    ToolResult execute(Map<String, Object> args);
}

