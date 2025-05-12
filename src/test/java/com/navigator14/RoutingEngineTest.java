package com.navigator14;

import com.leastfixedpoint.json.JSONReader;
import com.leastfixedpoint.json.JSONWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoutingEngineTest {
    private PipedInputStream inputStream;

    private PipedOutputStream pipedOutputStream;
    private ByteArrayOutputStream outputStream;
    private RoutingEngine routingEngine;

    @BeforeEach
    public void setUp() throws IOException {
        pipedOutputStream = new PipedOutputStream(); // Initialize here
        inputStream = new PipedInputStream(pipedOutputStream); // Connect the streams
        outputStream = new ByteArrayOutputStream();
        routingEngine = new RoutingEngine(
                new JSONReader(new InputStreamReader(inputStream)),
                new JSONWriter<>(new OutputStreamWriter(outputStream))
        );
    }

    @Test
    public void testValidRouteRequest() throws IOException, InterruptedException {
        String inputJson = """
                {"routeFrom":{"lat":41.904,"lon":12.5004},"to":{"lat":41.8791,"lon":12.5221},"startingAt":"09:30:00"}
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
        assertTrue(output.contains("\"ok\""));
    }

    @Test
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
    public void testNoPathFound() throws IOException, InterruptedException {
        String inputJson = """
                {"routeFrom":{"lat":0,"lon":0},"to":{"lat":0,"lon":0},"startingAt":"09:30:00"}
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

    @Test
    public void loadData() throws IOException, InterruptedException {
        String path = System.getenv("ZIP_PATH");
        if (path == null || path.isEmpty()) {
            throw new IllegalStateException("ZIP_PATH environment variable is not set or empty.");
        }
        String inputJson = """
                {"load":"%s"}
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

        String output = outputStream.toString();
        System.out.println("Output: " + output);
        assertTrue(output.contains("\"ok\""));
    }

    @Test
    public void testInteractiveInput() throws IOException {
        // Start the RoutingEngine in a separate thread
        Thread engineThread = new Thread(() -> {
            try {
                routingEngine.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        engineThread.start();

        // Write input to the PipedOutputStream
        String inputJson = """
                {"routeFrom":{"lat":41.904,"lon":12.5004},"to":{"lat":41.8791,"lon":12.5221},"startingAt":"09:30:00"}
                """;
        pipedOutputStream.write(inputJson.getBytes());
        pipedOutputStream.flush();

        // Wait for the engine to process the input
        try {
            engineThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify the output
        String output = outputStream.toString();
        System.out.println("Output: " + output);
        assertTrue(output.contains("\"ok\""));
    }
}