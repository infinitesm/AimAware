package ai.visient.data.relay.impl;

import ai.visient.debug.DebugUtil;
import ai.visient.network.client.AsyncJsonClient;
import ai.visient.profile.model.Profile;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * CollectionRelay sends raw signals and extracted features
 * to the backend server for storage.
 */
public class CollectionRelay {

    private final Profile profile;
    private final AsyncJsonClient asyncJsonClient;

    private Map<String, Double> latestFeatures;
    private Map<String, List<Double>> latestSignals;

    public CollectionRelay(Profile profile) {
        this.profile = profile;
        this.asyncJsonClient = profile.getPlugin().getAsyncJsonClient();
    }

    public void handleFeatures(Map<String, Double> features) {
        this.latestFeatures = features;
        postIfReady();
    }

    public void handleSignals(Map<String, List<Double>> signals) {
        this.latestSignals = signals;
        postIfReady();
    }

    /**
     * Combines signals + features and posts to the server
     * once both are available.
     */
    private void postIfReady() {
        if (latestFeatures == null || latestSignals == null) {
            return;
        }

        JSONObject json = new JSONObject();
        json.put("uuid", profile.getUuid());
        json.put("signals", new JSONObject(latestSignals));
        json.put("features", new JSONObject(latestFeatures));

        asyncJsonClient.postCollection(json.toString(), new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    DebugUtil.broadcast(
                            "Collection data saved for player %s → Server responded: %s%n",
                            profile.getUuid(),
                            responseBody
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Exception ex) {
                System.err.printf("Collection request failed for player %s%n", profile.getUuid());
                ex.printStackTrace();
            }

            @Override
            public void cancelled() {
                System.out.printf("Collection request cancelled for player %s%n", profile.getUuid());
            }
        });

        // Clear references to avoid duplicate sends
        latestSignals = null;
        latestFeatures = null;
    }
}
