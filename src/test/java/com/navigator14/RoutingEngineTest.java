package com.navigator14;

import com.leastfixedpoint.json.JSONReader;
import com.leastfixedpoint.json.JSONWriter;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class RoutingEngineTest {
    private PipedOutputStream pipedOutputStream;
    private ByteArrayOutputStream outputStream;
    private RoutingEngine routingEngine;

    @BeforeEach
    public void setUp() throws IOException {
        pipedOutputStream = new PipedOutputStream(); // Initialize here
        PipedInputStream inputStream = new PipedInputStream(pipedOutputStream); // Connect the streams
        outputStream = new ByteArrayOutputStream();
        routingEngine = new RoutingEngine(
                new JSONReader(new InputStreamReader(inputStream)),
                new JSONWriter<>(new OutputStreamWriter(outputStream))
        );
    }

    @Test
    @Order(1)
    public void testInvalidJsonInput() throws IOException, InterruptedException {
        String inputJson = "INVALID_JSON";
        pipedOutputStream.write(inputJson.getBytes());
        pipedOutputStream.flush();

        Thread engineThread = new Thread(() -> {
            try {
                routingEngine.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        engineThread.start();
        engineThread.join();

        String output = outputStream.toString();
        System.out.println("Output: " + output);
        assertTrue(output.contains("\"error\":\"Bad JSON input\""));
    }

    @Test
    @Order(2)
    public void loadData() throws IOException, InterruptedException {
        String path = System.getenv("ZIP_PATH");
        if (path == null || path.isEmpty()) {
            throw new IllegalStateException("ZIP_PATH environment variable is not set or empty.");
        }
        String inputJson = """
                {"load":"%s"}
                {"exit":true}
                """.formatted(path);
        pipedOutputStream.write(inputJson.getBytes());
        pipedOutputStream.flush();

        Thread engineThread = new Thread(() -> {
            try {
                routingEngine.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        engineThread.start();
        engineThread.join();

        outputStream.flush();
        String output = outputStream.toString(StandardCharsets.UTF_8); // Capture the output from responseWriter
        System.out.println("Output: " + output);
        assertTrue(output.contains("\"ok\":\"loaded\""));
    }

    @Test
    @Order(3)
    public void testValidRouteRequest() throws IOException, InterruptedException {
        String inputJson = """
                {"routeFrom":{"lat":41.904,"lon":12.5004},"to":{"lat":41.8791,"lon":12.5221},"startingAt":"09:30:00"}
                {"exit":true}
                """;
        pipedOutputStream.write(inputJson.getBytes());
        pipedOutputStream.flush();

        Thread engineThread = new Thread(() -> {
            try {
                routingEngine.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        engineThread.start();
        engineThread.join();

        outputStream.flush(); // Ensure all data is written
        String output = outputStream.toString(StandardCharsets.UTF_8); // Capture the output from responseWriter
        System.out.println("Output: " + output);
        assertTrue(output.contains("\"ok\"")); // Validate the expected output
    }


    @Test
    @Order(4)
    public void testNoPathFound() throws IOException, InterruptedException {
        String inputJson = """
                {"routeFrom":{"lat":0,"lon":0},"to":{"lat":0,"lon":0},"startingAt":"09:30:00"}
                {"exit":true}
                """;
        pipedOutputStream.write(inputJson.getBytes());
        pipedOutputStream.flush();

        Thread engineThread = new Thread(() -> {
            try {
                routingEngine.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        engineThread.start();
        engineThread.join();

        String output = outputStream.toString();
        System.out.println("Output: " + output);
        assertTrue(output.contains("\"error\":\"No path found\""));
    }

}