package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.config.AppProperties;
import com.swd392.onlineshop.dto.request.ChatForm;
import com.swd392.onlineshop.dto.response.ChatReply;
import com.swd392.onlineshop.entity.Product;
import com.swd392.onlineshop.service.ChatbotService;
import com.swd392.onlineshop.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Goi Google Gemini qua REST (generateContent). Lay API key mien phi o https://aistudio.google.com/apikey
 * roi dat GEMINI_API_KEY trong .env. Model mac dinh la alias gemini-flash-latest (nam trong free tier).
 */
@Slf4j
@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
    private static final int MAX_MESSAGE = 500;
    private static final int MAX_TURNS = 10;
    private static final int MAX_TURN_TEXT = 1500;
    private static final int PRODUCTS_IN_CONTEXT = 30;

    private final ProductService productService;
    private final AppProperties appProperties;
    private final String apiKey;
    private final String model;
    private final String fallbackModel;
    private final RestClient http;

    public ChatbotServiceImpl(ProductService productService,
                              AppProperties appProperties,
                              @Value("${gemini.api-key:}") String apiKey,
                              @Value("${gemini.model:gemini-flash-latest}") String model,
                              @Value("${gemini.fallback-model:gemini-flash-lite-latest}") String fallbackModel) {
        this.productService = productService;
        this.appProperties = appProperties;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null || model.isBlank() ? "gemini-flash-latest" : model.trim();
        this.fallbackModel = fallbackModel == null ? "" : fallbackModel.trim();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.http = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public ChatReply ask(ChatForm form) {
        String message = form == null || form.getMessage() == null ? "" : form.getMessage().trim();
        if (message.isEmpty()) {
            return new ChatReply("Bạn hãy nhập câu hỏi nhé.", false);
        }
        if (message.length() > MAX_MESSAGE) {
            message = message.substring(0, MAX_MESSAGE);
        }
        if (apiKey.isEmpty()) {
            return new ChatReply("Trợ lý ảo chưa được cấu hình (thiếu GEMINI_API_KEY). Bạn vui lòng liên hệ hotline "
                    + appProperties.getShop().getHotline() + " để được hỗ trợ.", false);
        }

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", systemPrompt()))),
                "contents", contents(form.getHistory(), message),
                "generationConfig", Map.of("temperature", 0.6, "maxOutputTokens", 1024)
        );

        // Model chinh qua tai (503) hoac het luot (429) thi thu model du phong.
        List<String> models = fallbackModel.isEmpty() || fallbackModel.equals(model)
                ? List.of(model) : List.of(model, fallbackModel);
        int lastStatus = 0;
        for (String candidate : models) {
            try {
                Map<?, ?> response = http.post()
                        .uri(ENDPOINT, candidate)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(Map.class);
                String text = extractText(response);
                if (text.isBlank()) {
                    return new ChatReply("Xin lỗi, mình chưa trả lời được câu này. Bạn thử hỏi cách khác nhé.", false);
                }
                return new ChatReply(text.trim(), true);
            } catch (RestClientResponseException ex) {
                HttpStatusCode status = ex.getStatusCode();
                lastStatus = status.value();
                log.warn("Gemini ({}) tra loi {}: {}", candidate, lastStatus, ex.getResponseBodyAsString());
                if (lastStatus != 429 && !status.is5xxServerError()) {
                    break;
                }
            } catch (RestClientException ex) {
                log.warn("Khong goi duoc Gemini ({}): {}", candidate, ex.getMessage());
                return new ChatReply("Không kết nối được tới trợ lý ảo. Bạn thử lại sau nhé.", false);
            }
        }
        if (lastStatus == 429 || lastStatus == 503) {
            return new ChatReply("Trợ lý đang quá tải, bạn đợi ít phút rồi hỏi lại nhé.", false);
        }
        return new ChatReply("Trợ lý tạm thời không phản hồi. Bạn thử lại sau nhé.", false);
    }

    private List<Map<String, Object>> contents(List<ChatForm.Turn> history, String message) {
        List<Map<String, Object>> contents = new ArrayList<>();
        if (history != null) {
            int from = Math.max(0, history.size() - MAX_TURNS);
            for (ChatForm.Turn turn : history.subList(from, history.size())) {
                if (turn == null || turn.getText() == null || turn.getText().isBlank()) {
                    continue;
                }
                String role = "model".equals(turn.getRole()) ? "model" : "user";
                String text = turn.getText().length() > MAX_TURN_TEXT
                        ? turn.getText().substring(0, MAX_TURN_TEXT) : turn.getText();
                contents.add(Map.of("role", role, "parts", List.of(Map.of("text", text))));
            }
        }
        // Gemini yeu cau luot dau tien la cua user.
        while (!contents.isEmpty() && "model".equals(contents.get(0).get("role"))) {
            contents.remove(0);
        }
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", message))));
        return contents;
    }

    private String systemPrompt() {
        AppProperties.Shop shop = appProperties.getShop();
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý ảo của NOVA SHOP, một cửa hàng trực tuyến. ")
          .append("Luôn trả lời bằng tiếng Việt, thân thiện, ngắn gọn (tối đa khoảng 5 câu), dùng văn bản thường, không dùng markdown. ")
          .append("Chỉ hỗ trợ các câu hỏi liên quan tới cửa hàng: sản phẩm, giá, đặt hàng, giao hàng, thanh toán, tài khoản. ")
          .append("Nếu câu hỏi không liên quan, lịch sự từ chối và gợi ý điều bạn có thể giúp. ")
          .append("Không bịa thông tin: nếu không có trong dữ liệu dưới đây, hãy đề nghị khách liên hệ hotline.\n\n")
          .append("Thông tin cửa hàng:\n")
          .append("- Hotline: ").append(shop.getHotline()).append('\n')
          .append("- Email: ").append(shop.getEmail()).append('\n')
          .append("- Địa chỉ: ").append(shop.getAddress()).append('\n')
          .append("- Thanh toán: chỉ hỗ trợ thanh toán khi nhận hàng (COD).\n")
          .append("- Đặt hàng: thêm sản phẩm vào Giỏ hàng, nhập thông tin người nhận rồi gửi đơn. ")
          .append("Khách đã đăng nhập xem và theo dõi đơn ở mục \"Đơn hàng của tôi\".\n")
          .append("- Trạng thái đơn: Đã gửi đơn, Đã xác nhận, Đang giao, Hoàn tất, Đã hủy.\n")
          .append("- Góp ý, phản hồi: dùng trang Phản hồi trên website.\n\n")
          .append("Một số sản phẩm đang bán (tên — giá — mô tả ngắn):\n");

        List<Product> products;
        try {
            products = productService.getLatestProducts(PRODUCTS_IN_CONTEXT);
        } catch (RuntimeException ex) {
            log.warn("Khong lay duoc san pham cho chatbot: {}", ex.getMessage());
            products = List.of();
        }
        NumberFormat vnd = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
        for (Product p : products) {
            BigDecimal price = p.getSalePrice() != null ? p.getSalePrice() : p.getOriginalPrice();
            sb.append("- ").append(p.getName());
            if (price != null) {
                sb.append(" — ").append(vnd.format(price)).append(" ₫");
            }
            if (p.getStockQuantity() <= 0) {
                sb.append(" (hết hàng)");
            }
            if (p.getBriefInfo() != null && !p.getBriefInfo().isBlank()) {
                String brief = p.getBriefInfo().strip();
                sb.append(" — ").append(brief.length() > 160 ? brief.substring(0, 160) + "…" : brief);
            }
            sb.append('\n');
        }
        if (products.isEmpty()) {
            sb.append("(chưa có dữ liệu sản phẩm)\n");
        }
        return sb.toString();
    }

    private static String extractText(Map<?, ?> response) {
        if (response == null || !(response.get("candidates") instanceof List<?> candidates) || candidates.isEmpty()) {
            return "";
        }
        if (!(candidates.get(0) instanceof Map<?, ?> candidate)
                || !(candidate.get("content") instanceof Map<?, ?> content)
                || !(content.get("parts") instanceof List<?> parts)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object part : parts) {
            // Bo qua phan "thought" neu model tra ve.
            if (part instanceof Map<?, ?> map && map.get("text") instanceof String text
                    && !Boolean.TRUE.equals(map.get("thought"))) {
                sb.append(text);
            }
        }
        return sb.toString();
    }
}
