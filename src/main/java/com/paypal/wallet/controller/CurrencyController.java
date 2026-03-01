package com.paypal.wallet.controller;

import com.paypal.wallet.service.CurrencyConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    @Autowired
    private CurrencyConversionService currencyConversionService;

    /**
     * GET /api/currency/rates?base=USD
     * Returns all exchange rates for the given base currency.
     */
    @GetMapping("/rates")
    public ResponseEntity<?> getRates(@RequestParam(defaultValue = "USD") String base) {
        try {
            Map<String, Double> rates = currencyConversionService.getRates(base.toUpperCase());
            Map<String, Object> response = new HashMap<>();
            response.put("base", base.toUpperCase());
            response.put("rates", rates);
            response.put("supported", currencyConversionService.getSupportedCurrencies());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Currency rates unavailable: " + e.getMessage());
        }
    }

    /**
     * GET /api/currency/convert?amount=100&from=USD&to=EUR
     * Converts an amount between two currencies.
     */
    @GetMapping("/convert")
    public ResponseEntity<?> convert(
            @RequestParam double amount,
            @RequestParam String from,
            @RequestParam String to) {
        try {
            double converted = currencyConversionService.convert(amount, from.toUpperCase(), to.toUpperCase());
            Map<String, Object> result = new HashMap<>();
            result.put("from", from.toUpperCase());
            result.put("to", to.toUpperCase());
            result.put("originalAmount", amount);
            result.put("convertedAmount", Math.round(converted * 100.0) / 100.0);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Conversion failed: " + e.getMessage());
        }
    }

    /**
     * GET /api/currency/supported
     * Returns list of supported currencies.
     */
    @GetMapping("/supported")
    public ResponseEntity<?> getSupportedCurrencies() {
        return ResponseEntity.ok(currencyConversionService.getSupportedCurrencies());
    }
}
