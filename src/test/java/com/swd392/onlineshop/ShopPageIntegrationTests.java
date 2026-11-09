package com.swd392.onlineshop;
import com.swd392.onlineshop.entity.*;
import com.swd392.onlineshop.repository.*;
import com.swd392.onlineshop.service.ShopService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @ActiveProfiles("test") @Transactional
class ShopPageIntegrationTests {
    @Autowired WebApplicationContext context;
    @Autowired ProductRepository products;
    @Autowired PostRepository posts;
    @Autowired ShopService shop;
    private MockMvc mvc;
    private Product visible, hidden;
    @BeforeEach void setup() {
        mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        visible=product("UI-KEYBOARD",true); hidden=product("UI-HIDDEN",false);
    }
    private Product product(String code,boolean visible) {
        Product p=new Product();p.setCode(code);p.setName(code);p.setVisible(visible);p.setFeatured(true);
        p.setOriginalPrice(new BigDecimal("690000"));p.setSalePrice(BigDecimal.ONE);p.setStockQuantity(5);
        return products.saveAndFlush(p);
    }
    @Test void homeLoadsRealVisibleRecordsAndUsesSellingPrice() throws Exception {
        assertThat(shop.home().featuredProducts()).extracting("code").contains("UI-KEYBOARD").doesNotContain("UI-HIDDEN");
        assertThat(shop.home().featuredProducts().get(0).getCurrentPrice()).isEqualByComparingTo("690000");
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("UI-KEYBOARD")));
    }
    @Test void productNavigationFiltersAndRejectsHiddenDetails() throws Exception {
        mvc.perform(get("/products").param("keyword","UI-KEYBOARD").param("minPrice","600000"))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("UI-KEYBOARD")))
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("UI-HIDDEN"))));
        mvc.perform(get("/products/"+visible.getId())).andExpect(status().isOk());
        mvc.perform(get("/products/"+hidden.getId())).andExpect(status().isNotFound());
    }
    @Test void missingDemoImagesHaveExplicitFallbacks() throws Exception {
        mvc.perform(get("/images/products/not-uploaded.jpg")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("image/svg+xml"));
        mvc.perform(get("/images/sliders/not-uploaded.jpg")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("image/png"));
    }
    @Test void blogNavigationShowsOnlyVisiblePosts() throws Exception {
        Post p=new Post();p.setCode("UI-BLOG");p.setTitle("UI public blog");p.setContent("Nội dung bài viết");p.setFeatured(true);p.setVisible(true);posts.saveAndFlush(p);
        mvc.perform(get("/blogs")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("UI public blog")));
        mvc.perform(get("/blogs/"+p.getId())).andExpect(status().isOk());
        p.setVisible(false);posts.flush();
        mvc.perform(get("/blogs/"+p.getId())).andExpect(status().isNotFound());
    }
}