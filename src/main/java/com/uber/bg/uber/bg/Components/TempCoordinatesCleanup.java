package com.uber.bg.uber.bg.Components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TempCoordinatesCleanup {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TempCoordinatesCleanup(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void purgeOrphanedPoints() {
        String sql = "DELETE FROM temp_ride_coordinates WHERE created_at < NOW();";
        jdbcTemplate.update(sql);
    }

}
