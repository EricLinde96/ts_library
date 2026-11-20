package se.yrgo.libraryapp.integrations;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.jooby.JoobyTest;
import se.yrgo.libraryapp.App;

@Tag("integration")
@Testcontainers
@JoobyTest(value = App.class, factoryMethod = "createApp")
public class RegisterIntegrationTest {
    @Container
    private static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0.29");

    private static final HttpClient client = HttpClient.newHttpClient();

    public static App createApp() {
        System.setProperty("db.url", mySQLContainer.getJdbcUrl());
        System.setProperty("db.user", mySQLContainer.getUsername());
        System.setProperty("db.password", mySQLContainer.getPassword());
        return new App();
    }

    @AfterAll
    static void reset() {
        System.clearProperty("db.url");
        System.clearProperty("db.user");
        System.clearProperty("db.password");
    }

    //helpers

    private static HttpResponse<String> postJson(int port, String path, String json)
            throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        return client.send(req, BodyHandlers.ofString());
    }

    private static String register(int port, String name, String realName, String password)
            throws IOException, InterruptedException {
        String json = "{\"name\":\"" + name + "\",\"realName\":\"" + realName
                + "\",\"password\":\"" + password + "\"}";
        return postJson(port, "/register", json).body();
    }

    private static String isAvailable(int port, String name)
            throws IOException, InterruptedException {
        HttpRequest req = HttpRequest
                .newBuilder(URI.create("http://localhost:" + port + "/register/available?name=" + name))
                .build();
        return client.send(req, BodyHandlers.ofString()).body();
    }

    //  tests

    @Test
    public void newUserCanRegisterAndLogin(int serverPort)
            throws IOException, InterruptedException {
        assertThat(register(serverPort, "reguser1", "Reg User", "secret123")).isEqualTo("true");

        HttpResponse<String> login = postJson(serverPort, "/login",
                "{\"username\":\"reguser1\",\"password\":\"secret123\"}");

        assertThat(login.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(login.body()).contains("USER");
    }

    @Test
    public void nameIsAvailableUntilRegistered(int serverPort)
            throws IOException, InterruptedException {
        assertThat(isAvailable(serverPort, "reguser2")).isEqualTo("true");

        register(serverPort, "reguser2", "Reg User", "secret123");

        assertThat(isAvailable(serverPort, "reguser2")).isEqualTo("false");
    }

    @Test
    public void sameNameCannotBeRegisteredTwice(int serverPort)
            throws IOException, InterruptedException {
        assertThat(register(serverPort, "reguser3", "Reg User", "secret123")).isEqualTo("true");
        assertThat(register(serverPort, "reguser3", "Reg User", "secret123")).isEqualTo("false");
    }

    @Test
    public void tooShortUsernameIsRejected(int serverPort)
            throws IOException, InterruptedException {
        assertThat(register(serverPort, "ab", "Reg User", "secret123")).isEqualTo("false");
    }

    @Test
    public void badWordInRealNameIsRejected(int serverPort)
            throws IOException, InterruptedException {
        assertThat(register(serverPort, "reguser5", "cat", "secret123")).isEqualTo("false");
        assertThat(isAvailable(serverPort, "reguser5")).isEqualTo("true");
    }
}