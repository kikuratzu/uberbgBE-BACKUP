package com.uber.bg.uber.bg.Services;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class HistoricalRouteDatabaseConsumerService {

    private final JdbcTemplate jdbcTemplate;


    @Autowired
    public HistoricalRouteDatabaseConsumerService(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @KafkaListener(topics = "ride-coordinates-db-store", groupId = "uber-db-writer-group")
    public void consumeAndSaveToPostgres(List<ConsumerRecord<String,String>> records) {
        String sql = "INSERT INTO temp_ride_coordinates (ride_id, longitude, latitude) VALUES(?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, records, records.size(), ((ps, argument) -> {
            String rideIdStr = argument.key();
            String[] tokens = argument.value().split("\\|");

            ps.setObject(1, UUID.fromString(rideIdStr));
            ps.setObject(2, Double.parseDouble(tokens[0]));
            ps.setObject(3, Double.parseDouble(tokens[1]));
        }));
    }
}
