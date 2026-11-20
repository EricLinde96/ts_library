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
public class LoginLogoutIntegrationTest {
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

    //  helpers

    private static HttpResponse<String> get(int port, String path, String cookie)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder =
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (cookie != null) {
            builder.header("Cookie", cookie);
        }
        return client.send(builder.build(), BodyHandlers.ofString());
    }

    private static HttpResponse<String> login(int port, String username, String password)
            throws IOException, InterruptedException {
        String json = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/login"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        return client.send(req, BodyHandlers.ofString());
    }

    private static String sessionCookie(HttpResponse<String> response) {
        return response.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith("session="))
                .findFirst()
                .orElseThrow()
                .split(";")[0];
    }

    // tests
    @Test
    public void alwaysOkToLogout(int serverPort) throws IOException, InterruptedException {
        HttpResponse<String> resp = get(serverPort, "/logout", null);

        assertThat(resp.body()).isEmpty();
        assertThat(resp.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    public void canLoginAndLogout(int serverPort) throws IOException, InterruptedException {
        HttpResponse<String> loginResp = login(serverPort, "test", "yrgoP4ssword");

        assertThat(loginResp.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(loginResp.body()).contains("ADMIN");

        HttpResponse<String> logoutResp = get(serverPort, "/logout", sessionCookie(loginResp));

        assertThat(logoutResp.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    public void unknownUserCannotLogin(int serverPort) throws IOException, InterruptedException {
        HttpResponse<String> resp = login(serverPort, "nobody", "yrgoP4ssword");

        assertThat(resp.statusCode()).isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
        assertThat(resp.body()).isEqualTo("[]");
    }

    @Test
    public void wrongPasswordCannotLogin(int serverPort) throws IOException, InterruptedException {
        HttpResponse<String> resp = login(serverPort, "test", "wrongpassword");

        assertThat(resp.statusCode()).isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
        assertThat(resp.body()).isEqualTo("[]");
    }
}