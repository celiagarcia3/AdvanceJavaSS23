package com.example.project.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PDBWebClient {

    public static ObservableList<String> main(String[] args) throws IOException {

        if (args.length == 0) {
            ObservableList<String> entries = FXCollections.observableArrayList();
            var url = new URL("https://search.rcsb.org/rcsbsearch/v2/query?json="
                    + URLEncoder.encode("{\"query\":{\"type\":\"terminal\",\"label\":\"text\",\"service\":\"text\",\"parameters\"" +
                    ":{\"attribute\":\"rcsb_entry_info.polymer_entity_count_protein\",\"operator\":\"greater_or_equal\"," +
                    "\"negation\":false,\"value\":1}},\"return_type\":\"entry\",\"request_options\":{\"return_all_hits\"" +
                    ":true,\"sort\":[{\"sort_by\":\"rcsb_entry_container_identifiers.entry_id\",\"direction\":\"asc\"}]}}", StandardCharsets.UTF_8));



            try (var r = Json.createReader(getFromURL(url))) {

                JsonObject responseObject = r.readObject();
                JsonArray resultsArray = responseObject.getJsonArray("result_set");

                if (resultsArray != null) {
                    for (var item : resultsArray) {
                        entries.add(item.asJsonObject().getString("identifier"));
                    }
                }
            }
            return entries;
        } else if (args.length == 1) {

            ObservableList<String> entry = FXCollections.observableArrayList();

            var code = args[0].toLowerCase();
            var url = new URL("https://files.rcsb.org/download/" + code.replace("\"", "") + ".pdb");
            entry.add(new String(getFromURL(url).readAllBytes()));
            return entry;

        } else {
            throw new IOException("Usage: _PDBWebClient_");
        }
    }

    public static InputStream getFromURL(URL url) throws IOException {
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        return connection.getInputStream();
    }
}
