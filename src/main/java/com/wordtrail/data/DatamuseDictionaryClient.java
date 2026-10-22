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

/** Definitions supplied by Datamuse (WordNet/Wiktionary); exact matches only. */
public final class DatamuseDictionaryClient implements DictionaryGateway {
    private final HttpClient client;
    private final URI endpoint;

    public DatamuseDictionaryClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build(),
                URI.create("https://api.datamuse.com/words"));
    }

    DatamuseDictionaryClient(HttpClient client, URI endpoint) { this.client = client; this.endpoint = endpoint; }

    @Override public Entry lookup(String term) {
        URI uri = URI.create(endpoint + "?sp=" + URLEncoder.encode(term, StandardCharsets.UTF_8) + "&md=d&max=1");
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(8))
                .header("Accept", "application/json").GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) { throw new AppException("Datamuse returned HTTP " + response.statusCode() + "."); }
            JSONArray entries = new JSONArray(response.body());
            for (int i = 0; i < entries.length(); i++) {
                JSONObject entry = entries.getJSONObject(i);
                if (!entry.optString("word").equalsIgnoreCase(term)) { continue; }
                JSONArray definitions = entry.optJSONArray("defs");
                if (definitions == null) { continue; }
                for (int j = 0; j < definitions.length(); j++) {
                    String text = definitions.getString(j);
                    int tab = text.indexOf('\t');
                    String definition = (tab < 0 ? text : text.substring(tab + 1)).strip();
                    if (!definition.isEmpty()) { return new Entry(entry.getString("word"), definition, "", uri.toString()); }
                }
            }
            throw new AppException("No exact dictionary definition was found for that word.");
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AppException("Dictionary lookup was interrupted.", ex);
        }
        catch (IOException ex) { throw new AppException("Cannot reach the backup dictionary. Check your connection.", ex); }
        catch (JSONException ex) { throw new AppException("The backup dictionary returned an unexpected response.", ex); }
    }
}
