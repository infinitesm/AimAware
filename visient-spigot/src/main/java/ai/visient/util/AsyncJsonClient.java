package ai.visient.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

public class AsyncJsonClient {

    private CloseableHttpAsyncClient client;
    private boolean running = false;

    /**
     * Initializes and starts the async HTTP client.
     */
    public void open() {
        if (!running) {
            client = HttpAsyncClients.createDefault();
            client.start();
            running = true;
        }
    }

    /**
     * Sends a JSON payload asynchronously via HTTP POST.
     *
     * @param url         The target URL
     * @param jsonPayload JSON string payload
     * @param callback    Callback to handle response
     */
    public void postJson(String url, String jsonPayload, FutureCallback<HttpResponse> callback) {
        if (!running) {
            throw new IllegalStateException("AsyncJsonClient is not started. Call open() first.");
        }

        try {
            HttpPost request = new HttpPost(url);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(jsonPayload, "UTF-8"));

            client.execute(request, callback);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send JSON request", e);
        }
    }

    /**
     * Closes the async HTTP client cleanly.
     */
    public void close() {
        if (running) {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            running = false;
        }
    }
}
