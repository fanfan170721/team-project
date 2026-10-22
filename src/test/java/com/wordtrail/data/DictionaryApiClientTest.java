package com.wordtrail.data;

import com.sun.net.httpserver.HttpServer;
import com.wordtrail.usecase.AppException;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DictionaryApiClientTest {
    private HttpServer server;
    private DictionaryApiClient client;
    private int status = 200;
    private String body = "[{\"word\":\"hello\",\"meanings\":[{\"definitions\":[{\"definition\":\"A greeting.\",\"example\":\"Hello, friend!\"}]}]}]";
    private String requestedPath;

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requestedPath = exchange.getRequestURI().getRawPath();
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (java.io.OutputStream output = exchange.getResponseBody()) { output.write(bytes); }
        });
        server.start();
        client = new DictionaryApiClient(HttpClient.newHttpClient(),
                URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/entries/"));
    }

    @AfterEach void stop() { server.stop(0); }

    @Test void parsesARealHttpResponseAndEncodesTheWord() {
        DictionaryGateway.Entry entry = client.lookup("ice cream");
        assertEquals("A greeting.", entry.definition);
        assertEquals("Hello, friend!", entry.example);
        assertEquals("/entries/ice%20cream", requestedPath);
        assertTrue(entry.source.endsWith("ice%20cream"));
    }

    @Test void mapsNotFoundRateLimitAndServerFailures() {
        for (int code : new int[]{404, 429, 503}) {
            status = code;
            assertThrows(AppException.class, () -> client.lookup("hello"));
        }
    }

    @Test void rejectsMalformedAndEmptyDefinitions() {
        for (String response : new String[]{"not json", "[]", "[{}]", "[{\"meanings\":[{\"definitions\":[]}]}]"}) {
            body = response;
            assertThrows(AppException.class, () -> client.lookup("hello"));
        }
    }
}
