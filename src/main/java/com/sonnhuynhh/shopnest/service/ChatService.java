package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.model.Product;
import com.sonnhuynhh.shopnest.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.NumberFormat;
import java.util.*;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final RestTemplate restTemplate = new RestTemplate();
    
    @Autowired
    private ProductRepository productRepository;

    // System prompt for ShopNest assistant
    private static final String SYSTEM_PROMPT_TEMPLATE = """
        Bạn là trợ lý ảo của ShopNest - một cửa hàng thương mại điện tử.
        Nhiệm vụ của bạn là:
        - Hỗ trợ khách hàng tìm kiếm sản phẩm
        - Trả lời câu hỏi về đơn hàng, giao hàng, thanh toán
        - Tư vấn sản phẩm phù hợp với nhu cầu khách hàng
        - Giải đáp thắc mắc về chính sách đổi trả, bảo hành
        
        DANH SÁCH SẢN PHẨM HIỆN CÓ:
        %s
        
        CHÍNH SÁCH CỬA HÀNG:
        - Miễn phí vận chuyển đơn từ 500,000đ
        - Đổi trả trong 7 ngày nếu lỗi từ nhà sản xuất
        - Bảo hành 12 tháng cho sản phẩm điện tử
        - Thanh toán: COD, chuyển khoản, Momo, VNPay
        - Hotline: 1900 1234
        - Email: support@shopnest.vn
        
        Hãy trả lời ngắn gọn, thân thiện và hữu ích bằng tiếng Việt.
        Khi giới thiệu sản phẩm, hãy nêu rõ tên và giá chính xác từ danh sách trên.
        Nếu khách hỏi về sản phẩm không có trong danh sách, hãy thông báo hiện chưa có và đề xuất sản phẩm tương tự nếu có.
        """;

    public String chat(String userMessage) {
        try {
            // Check API key
            if (apiKey == null || apiKey.isEmpty()) {
                logger.error("Gemini API key is not configured");
                return "Chatbot chưa được cấu hình. Vui lòng liên hệ admin.";
            }
            
            String url = GEMINI_API_URL + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Get product list for context
            String productList = getProductListForContext();
            String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, productList);

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            
            // System instruction
            Map<String, Object> systemInstruction = new HashMap<>();
            Map<String, String> systemPart = new HashMap<>();
            systemPart.put("text", systemPrompt);
            systemInstruction.put("parts", List.of(systemPart));
            requestBody.put("system_instruction", systemInstruction);
            
            // User message
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            content.put("role", "user");
            
            Map<String, String> part = new HashMap<>();
            part.put("text", userMessage);
            content.put("parts", List.of(part));
            contents.add(content);
            
            requestBody.put("contents", contents);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            logger.info("Sending request to Gemini API...");
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity, 
                    (Class<Map<String, Object>>)(Class<?>)Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            }

            return "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này.";

        } catch (HttpClientErrorException e) {
            logger.error("Gemini API Error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return "API key không hợp lệ. Vui lòng liên hệ admin.";
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return "Hệ thống đang bận. Vui lòng thử lại sau ít phút.";
            }
            return "Xin lỗi, đã xảy ra lỗi khi kết nối với AI.";
        } catch (Exception e) {
            logger.error("Chat error: ", e);
            return "Xin lỗi, đã xảy ra lỗi. Vui lòng thử lại sau.";
        }
    }
    
    private String getProductListForContext() {
        try {
            List<Product> products = productRepository.findAll();
            if (products.isEmpty()) {
                return "Hiện chưa có sản phẩm nào.";
            }
            
            NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
            
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Product p : products) {
                if (count >= 50) break; // Limit to avoid too long context
                sb.append(String.format("- %s: %s đ (Còn %d sản phẩm)\n", 
                    p.getName(), 
                    formatter.format(p.getPrice()),
                    p.getStock()));
                count++;
            }
            return sb.toString();
        } catch (Exception e) {
            return "Không thể tải danh sách sản phẩm.";
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                if (content != null) {
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Không thể đọc phản hồi từ AI.";
    }
}
