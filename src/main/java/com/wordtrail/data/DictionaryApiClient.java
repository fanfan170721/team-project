package com.wordtrail.data;

import com.wordtrail.usecase.AppException;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Adapter for https://dictionaryapi.dev/; no API key or user credentials are sent. */
public final class DictionaryApiClient implements DictionaryGateway {
    private final HttpClient client;
    private final URI baseUri;

    public DictionaryApiClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build(),
                URI.create("https://api.dictionaryapi.dev/api/v2/entries/en/"));
    }

    DictionaryApiClient(HttpClient client, URI baseUri) {
        this.client = client;
        this.baseUri = baseUri;
    }

    @Override
    public Entry lookup(String term) {
        String encoded = URLEncoder.encode(term, StandardCharsets.UTF_8).replace("+", "%20");
        URI uri = baseUri.resolve(encoded);
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(8))
                .header("Accept", "application/json").header("User-Agent", "WordTrail/1.0 (student project)")
                .GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 404) { throw new AppException("No dictionary entry was found for that word."); }
            if (response.statusCode() == 429) { throw new AppException("The dictionary is busy. Please try again later."); }
            if (response.statusCode() != 200) { throw new AppException("Dictionary service returned HTTP " + response.statusCode() + "."); }
            JSONArray entries = new JSONArray(response.body());
            for (int entryIndex = 0; entryIndex < entries.length(); entryIndex++) {
                JSONObject entry = entries.getJSONObject(entryIndex);
                JSONArray meanings = entry.getJSONArray("meanings");
                for (int meaningIndex = 0; meaningIndex < meanings.length(); meaningIndex++) {
                    JSONArray definitions = meanings.getJSONObject(meaningIndex).getJSONArray("definitions");
                    for (int i = 0; i < definitions.length(); i++) {
                        JSONObject definition = definitions.getJSONObject(i);
                        String text = definition.optString("definition", "").strip();
                        if (!text.isEmpty()) {
                            return new Entry(entry.optString("word", term), text,
                                    definition.optString("example", ""), uri.toString());
                        }
                    }
                }
            }
            throw new AppException("The dictionary returned no usable definitions.");
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AppException("Dictionary lookup was interrupted. Please try again.", ex);
        }
        catch (IOException ex) {
            throw new AppException("Cannot reach the dictionary. Check your connection; local study features still work.", ex);
        }
        catch (JSONException ex) { throw new AppException("The dictionary returned an unexpected response.", ex); }
    }
}
