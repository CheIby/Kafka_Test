package org.example;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

public class Main {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream("streams-filter-input", Consumed.with(Serdes.String(), Serdes.String()))
                .filter((key, value) -> Integer.parseInt(value) > 100)
                .to("streams-filter-output", Produced.with(Serdes.String(), Serdes.String()));

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            streams.close();
        }));

        try {
            // Keep the main thread alive by blocking indefinitely
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            e.printStackTrace();
        }
    }
}