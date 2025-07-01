package ai.visient.network.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

/**
 * AsyncJsonClient handles posting JSON payloads
 * to the inference and collection servers asynchronously.
 */
public class AsyncJsonClient {

    private final String inferenceUrl;
    private final String collectionUrl;

    private CloseableHttpAsyncClient client;
    private boolean running = false;

    /**
     * Constructs a new AsyncJsonClient.
     *
     * @param inferenceUrl URL of the inference server endpoint
     * @param collectionUrl URL of the collection server endpoint
     */
    public AsyncJsonClient(String inferenceUrl, String collectionUrl) {
        this.inferenceUrl = inferenceUrl;
        this.collectionUrl = collectionUrl;
    }

    /**
     * Starts the async client.
     */
    public void open() {
        if (!running) {
            client = HttpAsyncClients.createDefault();
            client.start();
            running = true;
        }
    }

    /**
     * Posts a JSON payload to the inference server asynchronously.
     *
     * @param jsonPayload JSON payload as string
     * @param callback    Callback for the async HTTP response
     */
    public void postInference(String jsonPayload, FutureCallback<HttpResponse> callback) {
        postJson(inferenceUrl, jsonPayload, callback);
    }

    /**
     * Posts a JSON payload to the collection server asynchronously.
     *
     * @param jsonPayload JSON payload as string
     * @param callback    Callback for the async HTTP response
     */
    public void postCollection(String jsonPayload, FutureCallback<HttpResponse> callback) {
        postJson(collectionUrl, jsonPayload, callback);
    }

    /**
     * Internal method to send JSON payloads via HTTP POST.
     */
    private void postJson(String url, String jsonPayload, FutureCallback<HttpResponse> callback) {
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
     * Close the async HTTP client.
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
