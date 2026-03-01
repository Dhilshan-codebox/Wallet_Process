package com.paypal.wallet.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrencyConversionService {

    @Value("${app.currency.api-url:https://api.exchangerate-api.com/v4/latest/}")
    private String apiBaseUrl;

    @Value("${app.currency.cache-minutes:60}")
    private int cacheDurationMinutes;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    // In-memory cache: base currency -> (rates map, cached at)
    private final ConcurrentHashMap<String, Map<String, Double>> ratesCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> rateCacheTime = new ConcurrentHashMap<>();

    /**
     * Converts an amount from one currency to another.
     */
    public double convert(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }
        Map<String, Double> rates = getRates(fromCurrency);
        if (rates == null || !rates.containsKey(toCurrency.toUpperCase())) {
            // Fallback: return as-is
            System.out.println("Currency conversion failed for " + fromCurrency + " -> " + toCurrency);
            return amount;
        }
        return amount * rates.get(toCurrency.toUpperCase());
    }

    /**
     * Returns all exchange rates for the given base currency.
     * Caches the result for the configured duration.
     */
    public Map<String, Double> getRates(String baseCurrency) {
        String base = baseCurrency.toUpperCase();

        // Check cache
        if (ratesCache.containsKey(base)) {
            LocalDateTime cachedAt = rateCacheTime.get(base);
            if (cachedAt != null && cachedAt.plusMinutes(cacheDurationMinutes).isAfter(LocalDateTime.now())) {
                return ratesCache.get(base);
            }
        }

        // Fetch fresh rates
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiBaseUrl + base))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Double> rates = parseRates(response.body());
            if (rates != null && !rates.isEmpty()) {
                ratesCache.put(base, rates);
                rateCacheTime.put(base, LocalDateTime.now());
                return rates;
            }
        } catch (Exception e) {
            System.out.println("Currency API error: " + e.getMessage());
        }

        // Return static fallback rates (approximate USD base)
        return getStaticFallbackRates(base);
    }

    /**
     * Simplified JSON parser for the exchangerate-api.com response format.
     * Response: {"rates":{"EUR":0.85,"GBP":0.73,...}}
     */
    private Map<String, Double> parseRates(String json) {
        Map<String, Double> rates = new HashMap<>();
        try {
            int ratesStart = json.indexOf("\"rates\"");
            if (ratesStart == -1)
                return rates;
            int objStart = json.indexOf('{', ratesStart + 7);
            int objEnd = json.indexOf('}', objStart);
            String ratesJson = json.substring(objStart + 1, objEnd);

            String[] pairs = ratesJson.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().replaceAll("\"", "");
                    double value = Double.parseDouble(kv[1].trim());
                    rates.put(key, value);
                }
            }
        } catch (Exception e) {
            System.out.println("Rate parsing error: " + e.getMessage());
        }
        return rates;
    }

    /**
     * Static fallback rates relative to USD (used if API is unreachable).
     */
    private Map<String, Double> getStaticFallbackRates(String base) {
        Map<String, Double> usdRates = new HashMap<>();
        usdRates.put("USD", 1.0);
        usdRates.put("EUR", 0.92);
        usdRates.put("GBP", 0.79);
        usdRates.put("JPY", 149.5);
        usdRates.put("INR", 83.0);
        usdRates.put("CAD", 1.36);
        usdRates.put("AUD", 1.53);
        usdRates.put("CHF", 0.89);
        usdRates.put("CNY", 7.24);
        usdRates.put("SGD", 1.34);

        if (base.equals("USD"))
            return usdRates;

        // Cross-rate from USD
        Map<String, Double> crossRates = new HashMap<>();
        double baseInUsd = usdRates.getOrDefault(base, 1.0);
        for (Map.Entry<String, Double> entry : usdRates.entrySet()) {
            crossRates.put(entry.getKey(), entry.getValue() / baseInUsd);
        }
        return crossRates;
    }

    public java.util.Set<String> getSupportedCurrencies() {
        return java.util.Set.of("USD", "EUR", "GBP", "JPY", "INR", "CAD", "AUD", "CHF", "CNY", "SGD");
    }
}
