package com.demo.rabbitmq.outbox.repository.impl;

import static java.lang.StringTemplate.STR;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.demo.rabbitmq.outbox.repository.OutboxRepository;
import com.demo.rabbitmq.outbox.repository.entity.Outbox;
import com.demo.rabbitmq.outbox.enums.QueryKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OutboxRepositoryImpl implements OutboxRepository {

    /**
     * Jdbc template is used for dml operations
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Each microservice should have their own outbox tables
     */
    @Value("${rabbit-config.outbox.table-name}")
    private String outboxTableName;
    /**
     * Map for insert, delete, select, update queries
     */
    private Map<QueryKey, String> queryMap = new EnumMap<>(QueryKey.class);

    public OutboxRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     *  Gets by primary key
     * @param uniqueId
     * @return
     */
    @Override
    public Optional<Outbox> getById(String uniqueId) {
        List<Outbox> outboxList = jdbcTemplate.query(queryMap.get(QueryKey.GET_BY_ID), new OutboxMapper(), uniqueId);
        return Optional.ofNullable(outboxList).orElse(Collections.emptyList()).stream().findFirst();
    }

    /**
     * Insert generic outbox table
     * @param outbox
     * @return
     */
    @Override
    public int insert(Outbox outbox) {
        return jdbcTemplate.update(queryMap.get(QueryKey.INSERT), outbox.getUniqueId(), outbox.getUpdatedDate(),
                outbox.getStatus(), outbox.getProcessCode(), outbox.getEventCode(), outbox.getHeader(), outbox.getPayload());
    }

    /**
     * Update generic outbox table
     * @param outbox
     * @return
     */
    @Override
    public int update(Outbox outbox) {
        return jdbcTemplate.update(queryMap.get(QueryKey.INSERT), outbox.getUniqueId(),
                outbox.getStatus(), outbox.getProcessCode(), outbox.getEventCode(), outbox.getHeader(), outbox.getPayload());
    }

    @Override
    public int delete(String uniqueId) {
        return 0;
    }

    /**
     * Update status outbox table by unique id
     * @param uniqueId
     * @param status
     * @return
     */
    @Override
    public int updateStatus(String uniqueId, String status) {
        return jdbcTemplate.update(queryMap.get(QueryKey.UPDATE_STATUS), status, uniqueId);
    }

    /**
     * Build sql scripts during spring boot application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void buildQueries() {
        buildSqls();
    }

    /**
     * Building sql scripts with generic outbox table
     */
    private void buildSqls() {
        // FOR UPDATE WAIT 2 eklenemediği için locklanmaz.
        String sql = STR."""
                SELECT UNIQUE_ID, VERSION, CREATED_DATE, UPDATED_DATE, STATUS, PROCESS_CODE, EVENT_CODE, HEADER, PAYLOAD
                FROM \{outboxTableName}
                WHERE UNIQUE_ID = ?
                """;
        queryMap.put(QueryKey.GET_BY_ID, sql);

        sql = STR."""
                INSERT INTO \{outboxTableName} (UNIQUE_ID, VERSION, CREATED_DATE, UPDATED_DATE, STATUS, PROCESS_CODE, EVENT_CODE, HEADER, PAYLOAD)
                VALUES (?, 0, NOW(), ?, ?, ?, ?, ?, ?)
                """;
        queryMap.put(QueryKey.INSERT, sql);

        sql = STR."""
                UPDATE \{outboxTableName}
                SET VERSION = VERSION + 1, UPDATED_DATE = NOW(), STATUS = ?, PROCESS_CODE = ?,
                EVENT_CODE = ?, HEADER = ?, PAYLOAD = ?
                WHERE UNIQUE_ID = ?
                """;
        queryMap.put(QueryKey.UPDATE, sql);

        sql = STR."""
                UPDATE \{outboxTableName}
                SET VERSION = VERSION + 1, UPDATED_DATE = NOW(), STATUS = ?
                WHERE UNIQUE_ID = ?
                """;
        queryMap.put(QueryKey.UPDATE_STATUS, sql);
    }

    /**
     * Mapper class for table to entity class
     */
    private static class OutboxMapper implements RowMapper<Outbox> {

        @Override
        public Outbox mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            String uniqueId = resultSet.getString("unique_id");
            Long version = resultSet.getLong("version");
            LocalDateTime createdDate = resultSet.getTimestamp("created_date").toLocalDateTime();
            Timestamp updatedTime = resultSet.getTimestamp("updated_date");
            LocalDateTime updatedDate = updatedTime != null ? updatedTime.toLocalDateTime() : null;
            String status = resultSet.getString("status");
            String processCode = resultSet.getString("process_code");
            String eventCode = resultSet.getString("event_code");
            String header = resultSet.getString("header");
            String payload = resultSet.getString("payload");
            return new Outbox(uniqueId, version, createdDate, updatedDate, status, processCode, eventCode, header, payload);
        }
    }
}
