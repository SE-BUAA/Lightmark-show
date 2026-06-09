package top.ortus.lightmark.backend.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Component
public class WebSearchTool {
    private static final int MAX_RESULTS = 5;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";

    private final HttpClient httpClient;
    private final String searchBaseUrl;

    public WebSearchTool(@Value("${lightmark.search.base-url:https://cn.bing.com}") String searchBaseUrl) {
        this.searchBaseUrl = searchBaseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Tool(name = "search_web", description = "Search the web for information.")
    public Map<String, Object> searchWeb(
            @ToolParam(description = "Search keywords") String query) {
        System.out.println("The web search tool is called with query: " + query);
        Map<String, Object> result = new HashMap<>();
        if (query == null || query.isBlank()) {
            result.put("success", false);
            result.put("message", "missing_query");
            return result;
        }

        try {
            String requestUrl = buildSearchUrl(query);
            HttpRequest request = HttpRequest.newBuilder(URI.create(requestUrl))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                result.put("success", false);
                result.put("message", "search_failed");
                result.put("status", response.statusCode());
                return result;
            }

            List<Map<String, String>> items = parseResults(response.body(), requestUrl);
            result.put("success", true);
            result.put("message", items.isEmpty() ? "no_results" : "ok");
            result.put("query", query);
            result.put("engine", searchBaseUrl);
            result.put("results", items);
            return result;
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", "exception: " + ex.getMessage());
            return result;
        }
    }

    private String buildSearchUrl(String query) {
        String trimmed = query.trim();
        String encoded = URLEncoder.encode(trimmed, StandardCharsets.UTF_8);
        return searchBaseUrl.replaceAll("/+$", "") + "/search?q=" + encoded;
    }

    private List<Map<String, String>> parseResults(String html, String baseUrl) {
        List<Map<String, String>> items = new ArrayList<>();
        Document document = Jsoup.parse(html, baseUrl);
        Elements results = document.select("li.b_algo");
        for (Element result : results) {
            Element link = result.selectFirst("h2 a");
            if (link == null) {
                continue;
            }
            String title = link.text();
            String url = link.absUrl("href");
            Element snippetNode = result.selectFirst(".b_caption p");
            String snippet = snippetNode == null ? "" : snippetNode.text();

            Map<String, String> item = new LinkedHashMap<>();
            item.put("title", title);
            item.put("url", url);
            item.put("snippet", snippet);
            items.add(item);

            if (items.size() >= MAX_RESULTS) {
                break;
            }
        }
        return items;
    }
}
