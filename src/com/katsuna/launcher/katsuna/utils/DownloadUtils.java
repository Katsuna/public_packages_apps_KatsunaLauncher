package com.katsuna.launcher.katsuna.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import timber.log.Timber;

public class DownloadUtils {

    public static DownloadResponse getUrl(String address) {
        Timber.d("get url: %s", address);
        DownloadResponse output = new DownloadResponse();

        URL url;
        try {
            url = new URL(address);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            output.responceCode = conn.getResponseCode();
            if (output.responceCode == 200) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    result.append(line).append("\n");
                }
                output.response = result.toString();
            }

            r.close();
            conn.disconnect();
        } catch (IOException e) {
            output.exception = e;
        }

        return output;
    }

}
