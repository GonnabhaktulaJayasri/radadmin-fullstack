package com.radadmin.radadminbackend.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "application", "RadAdmin",
            "timestamp", Instant.now().toString()
        );
    }

    @GetMapping("/db-check")
    public Map<String, Object> databaseCheck() {
        Map<String, Object> result = jdbcTemplate.queryForMap(
            "SELECT current_database() AS database, " +
            "current_user AS username, " +
            "1 AS query_result"
        );

        return Map.of(
            "status", "UP",
            "database", result.get("database"),
            "username", result.get("username"),
            "queryResult", result.get("query_result")
        );
    }
}