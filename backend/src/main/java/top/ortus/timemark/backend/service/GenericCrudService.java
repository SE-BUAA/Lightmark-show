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

@Service
public class GenericCrudService {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DataSource dataSource;
    private final TimemarkDatabaseProperties databaseProperties;
    private final ObjectMapper objectMapper;
    private final Map<String, TableMeta> metaCache = new ConcurrentHashMap<>();

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

    public <T> List<T> listTyped(String table, Map<String, String> filters, Class<T> type) {
        List<Map<String, Object>> items = list(table, filters);
        return objectMapper.convertValue(items, objectMapper.getTypeFactory().constructCollectionType(List.class, type));
    }

    public Map<String, Object> getById(String table, String id) {
        TableMeta meta = loadMeta(table);
        if (meta.pkColumns.size() != 1) {
            throw new ApiException(400, "table has composite primary key");
        }
        String pk = meta.pkColumns.get(0);
        String sql = "select * from `" + table + "` where `" + pk + "` = ?";
        return jdbcTemplate.queryForMap(sql, id);
    }

    public <T> T getByIdTyped(String table, String id, Class<T> type) {
        Map<String, Object> data = getById(table, id);
        return objectMapper.convertValue(data, type);
    }

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

    public <T> T createTyped(String table, T payload, Class<T> type) {
        Map<String, Object> map = objectMapper.convertValue(payload, new TypeReference<>() {});
        Map<String, Object> result = create(table, map);
        return objectMapper.convertValue(result, type);
    }

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

    public <T> T updateTyped(String table, T payload, Class<T> type) {
        Map<String, Object> map = objectMapper.convertValue(payload, new TypeReference<>() {});
        Map<String, Object> result = update(table, map);
        return objectMapper.convertValue(result, type);
    }

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

    public <T> boolean deleteTyped(String table, T payload) {
        Map<String, Object> map = objectMapper.convertValue(payload, new TypeReference<>() {});
        return delete(table, map);
    }

    private TableMeta loadMeta(String table) {
        if (!databaseProperties.getTables().contains(table)) {
            throw new ApiException(404, "unknown table");
        }
        return metaCache.computeIfAbsent(table, this::readMeta);
    }

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

