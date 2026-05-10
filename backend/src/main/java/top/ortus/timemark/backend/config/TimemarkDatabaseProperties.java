package top.ortus.timemark.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * TimeMark 数据库配置属性类
 * 从配置文件中读取数据库相关配置
 */
@Component
@ConfigurationProperties(prefix = "timemark.database")
public class TimemarkDatabaseProperties {

    private List<String> tables = new ArrayList<>();

    /**
     * 获取允许访问的表列表
     * @return 表名列表
     */
    public List<String> getTables() {
        return tables;
    }

    /**
     * 设置允许访问的表列表
     * @param tables 表名列表
     */
    public void setTables(List<String> tables) {
        this.tables = tables;
    }
}