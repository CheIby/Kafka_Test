package org.example;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.example.Serdes.JsonDeserializer;
import org.example.Serdes.JsonSerializer;

public class Main {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        final Serializer<JsonNode> jsonSerializer = new JsonSerializer();
        final Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
        final Serde<JsonNode> jsonSerde = Serdes.serdeFrom(jsonSerializer,jsonDeserializer);
        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream("source-topic", Consumed.with(Serdes.String(), jsonSerde))
                .filter((key, value) -> {
                    System.out.println(value.get("speed"));
                    System.out.println(value.get("speed").isInt());
                    return Integer.parseInt(value.get("speed").toString()) > 50;
                })

                .to("new-topic", Produced.with(Serdes.String(), jsonSerde));

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