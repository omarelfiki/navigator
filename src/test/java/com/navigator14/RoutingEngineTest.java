package com.navigator14;

import com.leastfixedpoint.json.JSONReader;
import com.leastfixedpoint.json.JSONWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoutingEngineTest {
    private ByteArrayInputStream inputStream;
    private ByteArrayOutputStream outputStream;
    private RoutingEngine routingEngine;

    @BeforeEach
    public void setUp() {
        inputStream = new ByteArrayInputStream(new byte[0]); // Initialize with an empty stream
        outputStream = new ByteArrayOutputStream();
        routingEngine = new RoutingEngine(
                new JSONReader(new InputStreamReader(inputStream)),
                new JSONWriter<>(new OutputStreamWriter(outputStream))
        );
    }

    @Test
    public void testValidRouteRequest() throws IOException {
        String inputJson = """
                {"routeFrom":{"lat":41.904,"lon":12.5004},"to":{"lat":41.8791,"lon":12.5221},"startingAt":"09:30:00"}
                """;
        inputStream = new ByteArrayInputStream(inputJson.getBytes());
        routingEngine.run();

        String output = outputStream.toString();
        System.out.println("Output: " + output);
        assertTrue(output.contains("\"ok\""));
    }

    @Test
    public void testInvalidJsonInput() throws IOException {
        String inputJson = "INVALID_JSON";
        inputStream = new ByteArrayInputStream(inputJson.getBytes());
        routingEngine.run();

        String output = outputStream.toString();
        System.out.println("Output: " + output);
        assertTrue(output.contains("\"error\":\"Bad JSON input\""));
    }

    @Test
    public void testNoPathFound() throws IOException {
        String inputJson = """
                {"routeFrom":{"lat":0,"lon":0},"to":{"lat":0,"lon":0},"startingAt":"09:30:00"}
                """;
        inputStream = new ByteArrayInputStream(inputJson.getBytes());
        routingEngine.run();

        String output = outputStream.toString();
        System.out.println("Output: " + output);
        assertTrue(output.contains("\"error\":\"No path found\""));
    }
}