package com.example.downloadhelper.util.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UrlUtil {
    public static Map<String, String> splitUrl(String url) {
        Map<String, String> urlParts = new HashMap<>();
        try {
            URL parsedUrl = new URL(url);
            String baseUrl = parsedUrl.getProtocol() + "://" + parsedUrl.getHost();
            if (parsedUrl.getPort() != -1) {
                baseUrl += ":" + parsedUrl.getPort();
            }
            String path = parsedUrl.getPath();

            urlParts.put("baseUrl", baseUrl);
            urlParts.put("path", path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return urlParts;
    }
}
