package com.swd392.onlineshop;

import com.swd392.onlineshop.entity.Post;
import com.swd392.onlineshop.entity.Product;
import com.swd392.onlineshop.entity.Setting;
import com.swd392.onlineshop.entity.Slider;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.enums.Gender;
import com.swd392.onlineshop.enums.SettingType;
import com.swd392.onlineshop.enums.UserStatus;
import com.swd392.onlineshop.repository.PostRepository;
import com.swd392.onlineshop.repository.ProductRepository;
import com.swd392.onlineshop.repository.SettingRepository;
import com.swd392.onlineshop.repository.SliderRepository;
import com.swd392.onlineshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PublicCustomerIntegrationTests {

    @Autowired
    WebApplicationContext context;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SettingRepository settingRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    SliderRepository sliderRepository;

    private MockMvc mvc;
    private Product product;
    private Post post;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        Setting roleSetting = settingRepository.findByTypeAndValue(SettingType.USER_ROLE, "SALE").orElseGet(() -> {
            Setting s = new Setting();
            s.setType(SettingType.USER_ROLE);
            s.setCode("ROLE_SALE");
            s.setName("Sale");
            s.setValue("SALE");
            s.setActive(true);
            return settingRepository.saveAndFlush(s);
        });

        User sale = new User();
        sale.setCode("SALE001");
        sale.setFullName("Sale User");
        sale.setEmail("sale.test@novashop.vn");
        sale.setPassword("secret");
        sale.setRole(roleSetting);
        sale.setStatus(UserStatus.ACTIVE);
        sale.setGender(Gender.MALE);
        userRepository.saveAndFlush(sale);

        Setting category = settingRepository.findByTypeAndValue(SettingType.PRODUCT_CATEGORY, "CAT1").orElseGet(() -> {
            Setting s = new Setting();
            s.setType(SettingType.PRODUCT_CATEGORY);
            s.setCode("CAT1");
            s.setName("Test Category");
            s.setValue("CAT1");
            s.setActive(true);
            return settingRepository.saveAndFlush(s);
        });

        product = new Product();
        product.setCode("PRD001");
        product.setName("Bàn phím cơ Test");
        product.setCategory(category);
        product.setOriginalPrice(new BigDecimal("500000"));
        product.setStockQuantity(20);
        product.setVisible(true);
        product.setBriefInfo("Bàn phím cơ chất lượng cao");
        product.setDescription("Mô tả chi tiết");
        productRepository.saveAndFlush(product);

        post = new Post();
        post.setCode("BV0001");
        post.setTitle("Hướng dẫn chọn bàn phím cơ");
        post.setContent("Nội dung bài viết mẫu");
        post.setBriefInfo("Tóm tắt");
        post.setVisible(true);
        post.setAuthor(sale);
        postRepository.saveAndFlush(post);

        Slider slider = new Slider();
        slider.setTitle("Banner khuyến mãi");
        slider.setImage("/images/sliders/demo.jpg");
        slider.setButtonLabel("Xem ngay");
        slider.setBacklink("/products");
        slider.setVisible(true);
        slider.setDisplayOrder(1);
        sliderRepository.saveAndFlush(slider);
    }

    @Test
    void testPublicPagesAccessible() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/products")).andExpect(status().isOk());
        mvc.perform(get("/products/" + product.getId())).andExpect(status().isOk());
        mvc.perform(get("/blogs")).andExpect(status().isOk());
        mvc.perform(get("/blogs/" + post.getId())).andExpect(status().isOk());
        mvc.perform(get("/feedback")).andExpect(status().isOk());
        mvc.perform(get("/cart")).andExpect(status().isOk());
    }

    @Test
    void testCartFlow() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // 1. Add product to cart
        mvc.perform(post("/cart/add")
                        .session(session)
                        .with(csrf())
                        .param("productId", String.valueOf(product.getId()))
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection());

        // 2. View cart
        mvc.perform(get("/cart").session(session))
                .andExpect(status().isOk());

        // 3. Checkout submit
        mvc.perform(post("/cart/submit")
                        .session(session)
                        .with(csrf())
                        .param("fullName", "Khách Hàng Mới")
                        .param("gender", "MALE")
                        .param("email", "customer.checkout@test.com")
                        .param("mobile", "0912345678")
                        .param("address", "123 Đường Cầu Giấy, Hà Nội")
                        .param("note", "Giao trong giờ hành chính"))
                .andExpect(status().is3xxRedirection());
    }
}
