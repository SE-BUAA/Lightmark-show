package top.ortus.timemark.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import top.ortus.timemark.backend.config.TimemarkDatabaseProperties;
import top.ortus.timemark.backend.exception.ApiException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用的 CRUD 服务类，提供对数据库表的增删改查操作
 * 支持动态表名操作，使用命名参数进行 SQL 查询
 * 自动处理主键、字段过滤和软删除
 */
@Service
public class GenericCrudService {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DataSource dataSource;
    private final TimemarkDatabaseProperties databaseProperties;
    private final ObjectMapper objectMapper;
    // 表元数据缓存，避免重复读取数据库结构
    private final Map<String, TableMeta> metaCache = new ConcurrentHashMap<>();

    /**
     * 构造函数，初始化 JDBC 模板和相关组件
     * @param jdbcTemplate JDBC 模板，用于执行 SQL 查询
     * @param dataSource 数据源，用于获取数据库连接读取元数据
     * @param databaseProperties 数据库配置，包含允许访问的表列表
     * @param objectMapper JSON 对象映射器，用于类型转换
     */
    public GenericCrudService(JdbcTemplate jdbcTemplate,
                              DataSource dataSource,
                              TimemarkDatabaseProperties databaseProperties,
                              ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.dataSource = dataSource;
        this.databaseProperties = databaseProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询指定表的所有数据，支持可选的过滤条件
     * @param table 表名
     * @param filters 过滤条件，键为列名，值为过滤值
     * @return 符合条件的数据列表，每条数据为 Map
     */
    public List<Map<String, Object>> list(String table, Map<String, String> filters) {
        TableMeta meta = loadMeta(table);
        StringBuilder sql = new StringBuilder("select * from `" + table + "`");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (filters != null && !filters.isEmpty()) {
            List<String> clauses = new ArrayList<>();
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String key = entry.getKey().toLowerCase(Locale.ROOT);
                if (meta.columns.contains(key)) {
                    clauses.add("`" + key + "` = :" + key);
                    params.addValue(key, entry.getValue());
                }
            }
            if (!clauses.isEmpty()) {
                sql.append(" where ").append(String.join(" and ", clauses));
            }
        }
        return namedParameterJdbcTemplate.queryForList(sql.toString(), params);
    }

    /**
     * 查询指定表的数据，并将结果转换为指定的类型
     * @param table 表名
     * @param filters 过滤条件
     * @param type 目标类型，用于将查询结果转换为该类型的列表
     * @return 符合条件的数据列表，类型为指定的类型
     */
    public <T> List<T> listTyped(String table, Map<String, String> filters, Class<T> type) {
        List<Map<String, Object>> items = list(table, filters);
        return objectMapper.convertValue(items, objectMapper.getTypeFactory().constructCollectionType(List.class, type));
    }

    /**
     * 根据主键 ID 查询单条数据
     * @param table 表名
     * @param id 主键值
     * @return 查询到的数据 Map，如果不存在则抛出异常
     * @throws ApiException 如果表有复合主键或记录不存在
     */
    public Map<String, Object> getById(String table, String id) {
        TableMeta meta = loadMeta(table);
        if (meta.pkColumns.size() != 1) {
            throw new ApiException(400, "table has composite primary key");
        }
        String pk = meta.pkColumns.get(0);
        String sql = "select * from `" + table + "` where `" + pk + "` = ?";
        return jdbcTemplate.queryForMap(sql, id);
    }

    /**
     * 根据主键 ID 查询单条数据，并将结果转换为指定的类型
     * @param table 表名
     * @param id 主键值
     * @param type 目标类型
     * @return 查询到的数据，类型为指定的类型
     */
    public <T> T getByIdTyped(String table, String id, Class<T> type) {
        Map<String, Object> data = getById(table, id);
        return objectMapper.convertValue(data, type);
    }

    /**
     * 向指定表插入一条新数据
     * @param table 表名
     * @param payload 要插入的数据，键为列名，值为列值
     * @return 插入的数据（已过滤只包含有效列）
     * @throws ApiException 如果 payload 为空或表不在允许列表中
     */
    public Map<String, Object> create(String table, Map<String, Object> payload) {
        TableMeta meta = loadMeta(table);
        Map<String, Object> filtered = filterColumns(meta, payload);
        if (filtered.isEmpty()) {
            throw new ApiException(400, "payload is empty");
        }
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        int index = 0;
        for (Map.Entry<String, Object> entry : filtered.entrySet()) {
            if (index > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append("`").append(entry.getKey()).append("`");
            values.append(":" + entry.getKey());
            params.addValue(entry.getKey(), entry.getValue());
            index++;
        }
        String sql = "insert into `" + table + "` (" + columns + ") values (" + values + ")";
        namedParameterJdbcTemplate.update(sql, params);
        return filtered;
    }

    /**
     * 向指定表插入一条新数据，并将输入对象转换为指定类型返回
     * @param table 表名
     * @param payload 要插入的数据对象
     * @param type 目标类型
     * @return 插入的数据，类型为指定的类型
     */
    public <T> T createTyped(String table, T payload, Class<T> type) {
        Map<String, Object> map = objectMapper.convertValue(payload, new TypeReference<>() {});
        Map<String, Object> result = create(table, map);
        return objectMapper.convertValue(result, type);
    }

    /**
     * 更新指定表的数据，使用主键作为更新条件
     * @param table 表名
     * @param payload 要更新的数据，必须包含主键字段
     * @return 更新后的数据（已过滤只包含有效列）
     * @throws ApiException 如果缺少主键、没有可更新字段或表不在允许列表中
     */
    public Map<String, Object> update(String table, Map<String, Object> payload) {
        TableMeta meta = loadMeta(table);
        Map<String, Object> filtered = filterColumns(meta, payload);
        if (filtered.isEmpty()) {
            throw new ApiException(400, "payload is empty");
        }
        Map<String, Object> pkValues = new LinkedHashMap<>();
        for (String pk : meta.pkColumns) {
            Object value = filtered.get(pk);
            if (value == null) {
                throw new ApiException(400, "missing primary key: " + pk);
            }
            pkValues.put(pk, value);
        }
        Map<String, Object> updates = new LinkedHashMap<>(filtered);
        for (String pk : meta.pkColumns) {
            updates.remove(pk);
        }
        if (updates.isEmpty()) {
            throw new ApiException(400, "no updatable fields");
        }
        StringBuilder sql = new StringBuilder("update `" + table + "` set ");
        MapSqlParameterSource params = new MapSqlParameterSource();
        int index = 0;
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            if (index > 0) {
                sql.append(", ");
            }
            sql.append("`").append(entry.getKey()).append("` = :" + entry.getKey());
            params.addValue(entry.getKey(), entry.getValue());
            index++;
        }
        List<String> where = new ArrayList<>();
        for (Map.Entry<String, Object> entry : pkValues.entrySet()) {
            where.add("`" + entry.getKey() + "` = :" + entry.getKey());
            params.addValue(entry.getKey(), entry.getValue());
        }
        sql.append(" where ").append(String.join(" and ", where));
        namedParameterJdbcTemplate.update(sql.toString(), params);
        return filtered;
    }

    /**
     * 更新指定表的数据，并将输入输出对象转换为指定类型
     * @param table 表名
     * @param payload 要更新的数据对象
     * @param type 目标类型
     * @return 更新后的数据，类型为指定的类型
     */
    public <T> T updateTyped(String table, T payload, Class<T> type) {
        Map<String, Object> map = objectMapper.convertValue(payload, new TypeReference<>() {});
        Map<String, Object> result = update(table, map);
        return objectMapper.convertValue(result, type);
    }

    /**
     * 删除指定表的数据，使用主键作为删除条件
     * 如果表有 deleted 字段，则执行软删除（设置 deleted=1），否则执行物理删除
     * @param table 表名
     * @param keys 包含主键值的 Map
     * @return 是否成功删除（影响行数大于0返回true）
     * @throws ApiException 如果缺少主键或表不在允许列表中
     */
    public boolean delete(String table, Map<String, Object> keys) {
        TableMeta meta = loadMeta(table);
        if (keys == null || keys.isEmpty()) {
            throw new ApiException(400, "primary key is required");
        }
        Map<String, Object> pkValues = new LinkedHashMap<>();
        for (String pk : meta.pkColumns) {
            Object value = keys.get(pk);
            if (value == null) {
                throw new ApiException(400, "missing primary key: " + pk);
            }
            pkValues.put(pk, value);
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> where = new ArrayList<>();
        for (Map.Entry<String, Object> entry : pkValues.entrySet()) {
            where.add("`" + entry.getKey() + "` = :" + entry.getKey());
            params.addValue(entry.getKey(), entry.getValue());
        }
        String sql;
        if (meta.hasDeleted) {
            sql = "update `" + table + "` set `deleted` = 1 where " + String.join(" and ", where);
        } else {
            sql = "delete from `" + table + "` where " + String.join(" and ", where);
        }
        return namedParameterJdbcTemplate.update(sql, params) > 0;
    }

    /**
     * 删除指定表的数据，并将输入对象转换为指定类型
     * @param table 表名
     * @param payload 包含主键值的数据对象
     * @return 是否成功删除
     */
    public <T> boolean deleteTyped(String table, T payload) {
        Map<String, Object> map = objectMapper.convertValue(payload, new TypeReference<>() {});
        return delete(table, map);
    }

    /**
     * 加载指定表的元数据，首先检查缓存，然后从数据库读取
     * @param table 表名
     * @return 表的元数据对象
     * @throws ApiException 如果表不在允许列表中
     */
    private TableMeta loadMeta(String table) {
        if (!databaseProperties.getTables().contains(table)) {
            throw new ApiException(404, "unknown table");
        }
        return metaCache.computeIfAbsent(table, this::readMeta);
    }

    /**
     * 从数据库读取指定表的元数据，包括列信息和主键信息
     * @param table 表名
     * @return 表的元数据对象
     * @throws ApiException 如果读取元数据失败
     */
    private TableMeta readMeta(String table) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            List<String> columns = new ArrayList<>();
            List<String> pkColumns = new ArrayList<>();
            try (ResultSet rs = metaData.getColumns(null, null, table, null)) {
                while (rs.next()) {
                    String name = rs.getString("COLUMN_NAME");
                    if (name != null) {
                        columns.add(name.toLowerCase(Locale.ROOT));
                    }
                }
            }
            try (ResultSet rs = metaData.getPrimaryKeys(null, null, table)) {
                while (rs.next()) {
                    String name = rs.getString("COLUMN_NAME");
                    if (name != null) {
                        pkColumns.add(name.toLowerCase(Locale.ROOT));
                    }
                }
            }
            boolean hasDeleted = columns.stream().map(name -> name.toLowerCase(Locale.ROOT))
                    .anyMatch(name -> name.equals("deleted"));
            return new TableMeta(columns, pkColumns, hasDeleted);
        } catch (Exception ex) {
            throw new ApiException(500, "failed to read table metadata");
        }
    }

    /**
     * 过滤 payload 中的字段，只保留表中存在的列
     * @param meta 表的元数据
     * @param payload 原始数据
     * @return 过滤后的数据，只包含表中存在的列
     */
    private Map<String, Object> filterColumns(TableMeta meta, Map<String, Object> payload) {
        Map<String, Object> filtered = new HashMap<>();
        if (payload == null) {
            return filtered;
        }
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey().toLowerCase(Locale.ROOT);
            if (meta.columns.contains(key)) {
                filtered.put(key, entry.getValue());
            }
        }
        return filtered;
    }

    /**
     * 表元数据内部类，用于缓存表的结构信息
     */
    private static class TableMeta {
        private final List<String> columns;
        private final List<String> pkColumns;
        private final boolean hasDeleted;

        private TableMeta(List<String> columns, List<String> pkColumns, boolean hasDeleted) {
            this.columns = columns;
            this.pkColumns = pkColumns;
            this.hasDeleted = hasDeleted;
        }
    }
}