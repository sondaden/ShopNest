package com.sonnhuynhh.shopnest.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SupabaseService {
    private final String baseUrl;
    private final String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    public SupabaseService() {
        Dotenv dotenv = Dotenv.configure().directory("./").load();
        this.baseUrl = dotenv.get("SHOPNEST_SUPABASE_URL");
        this.apiKey = dotenv.get("SHOPNEST_ANON_KEY");
    }

    // Phương thức chèn dữ liệu vào bảng products hiện tại
    public ResponseEntity<String> insertProduct(Map<String, Object> productData) {
        String url = baseUrl + "/rest/v1/products";

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        headers.set("Prefer", "return=representation"); // Trả về dữ liệu vừa chèn

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(productData, headers);

        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    // Thêm vào cuối class SupabaseService
    public ResponseEntity<String> testConnection() {
        String url = baseUrl + "/rest/v1/";  // Endpoint gốc để kiểm tra kết nối

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }
}