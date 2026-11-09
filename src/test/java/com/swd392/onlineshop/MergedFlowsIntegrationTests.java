package com.swd392.onlineshop;

import com.swd392.onlineshop.dto.request.ProductFilter;
import com.swd392.onlineshop.entity.*;
import com.swd392.onlineshop.enums.*;
import com.swd392.onlineshop.repository.*;
import com.swd392.onlineshop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @ActiveProfiles("test") @Transactional
class MergedFlowsIntegrationTests {
    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    @Autowired SettingRepository settings;
    @Autowired ProductRepository products;
    @Autowired PostRepository posts;
    @Autowired SliderRepository sliders;
    @Autowired FeedbackRepository feedbacks;
    @Autowired CustomerRepository customers;
    @Autowired OrderRepository orders;
    @Autowired OrderHistoryRepository history;
    @Autowired ProductService productService;
    @Autowired PasswordEncoder passwords;
    private MockMvc mvc;
    private User marketing,sale,otherSale,manager;
    private Product product;
    private Post post;
    private Slider slider;
    private Customer customer;
    private Feedback feedback;
    private Order assigned,other;
    @BeforeEach void setup() {
        mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        marketing=account("MARKETING","marketing@merge.test","NDMKT");
        sale=account("SALE","sale@merge.test","NDSALE");
        otherSale=account("SALE","other@merge.test","NDOTHER");
        manager=account("SALE_MANAGER","manager@merge.test","NDMANAGER");
        Setting category=setting(SettingType.PRODUCT_CATEGORY,"KEYBOARD");
        product=new Product();product.setCode("MERGE-KEY");product.setName("Merge Keyboard");product.setCategory(category);
        product.setOriginalPrice(new BigDecimal("690000"));product.setSalePrice(BigDecimal.ONE);product.setStockQuantity(10);
        product.setBriefInfo("Thông tin ngắn");product.setDescription("Mô tả");product.setThumbnail("/images/products/demo.jpg");products.saveAndFlush(product);
        post=new Post();post.setCode("MERGE-POST");post.setTitle("Merge Article");post.setContent("Nội dung");post.setAuthor(marketing);posts.saveAndFlush(post);
        slider=new Slider();slider.setTitle("Merge Banner");slider.setImage("/images/sliders/demo.jpg");slider.setButtonLabel("Xem");slider.setBacklink("/products");slider.setDisplayOrder(1);sliders.saveAndFlush(slider);
        customer=new Customer();customer.setCode("KHMERGE");customer.setFullName("Merge Customer");customer.setEmail("customer@merge.test");customer.setMobile("0912345678");customer.setAddress("Hà Nội");customers.saveAndFlush(customer);
        feedback=new Feedback();feedback.setCode("PHMERGE");feedback.setCustomer(customer);feedback.setProduct(product);feedback.setFullName(customer.getFullName());feedback.setEmail(customer.getEmail());feedback.setSubject("Merge feedback");feedback.setContent("Nội dung");feedbacks.saveAndFlush(feedback);
        assigned=order("OD-MINE",sale);other=order("OD-OTHER",otherSale);
    }
    private Setting setting(SettingType type,String value) {
        return settings.findByTypeAndValue(type,value).orElseGet(()->{
            Setting s=new Setting();s.setType(type);s.setCode(value);s.setValue(value);s.setName(value);s.setGroupName(type.name());s.setDisplayOrder(1);return settings.saveAndFlush(s);
        });
    }
    private User account(String role,String email,String code) {
        User u=new User();u.setCode(code);u.setEmail(email);u.setFullName(code);u.setPassword(passwords.encode("Password123"));u.setRole(setting(SettingType.USER_ROLE,role));u.setStatus(UserStatus.ACTIVE);return users.saveAndFlush(u);
    }
    private Order order(String code,User owner) {
        Order o=new Order();o.setCode(code);o.setCustomer(customer);o.setAssignedSale(owner);o.setReceiverName(customer.getFullName());o.setReceiverEmail(customer.getEmail());o.setReceiverMobile(customer.getMobile());o.setReceiverAddress(customer.getAddress());o.setTotalAmount(product.getOriginalPrice());
        OrderItem item=new OrderItem();item.setOrder(o);item.setProduct(product);item.setProductCode(product.getCode());item.setProductName(product.getName());item.setUnitPrice(product.getOriginalPrice());item.setQuantity(1);o.getItems().add(item);
        return orders.saveAndFlush(o);
    }
    private MockHttpSession login(User u,String target) throws Exception {
        return (MockHttpSession)mvc.perform(post("/auth/login").with(csrf()).param("email",u.getEmail()).param("password","Password123"))
            .andExpect(redirectedUrl(target)).andReturn().getRequest().getSession(false);
    }
    @Test void rendersAllMarketingListsFormsAndDetailsWithMergedDtos() throws Exception {
        MockHttpSession session=login(marketing,"/marketing/products");
        String[] modules={"products","posts","sliders","customers","feedbacks"};
        Long[] ids={product.getId(),post.getId(),slider.getId(),customer.getId(),feedback.getId()};
        for(int i=0;i<modules.length;i++) {
            String base="/marketing/"+modules[i];
            for(String path:new String[]{base,base+"/new",base+"/"+ids[i],base+"/"+ids[i]+"/edit"})
                mvc.perform(get(path).session(session)).andExpect(status().isOk());
        }
        mvc.perform(get("/admin/users").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/sales/orders").session(session)).andExpect(status().isForbidden());
    }
    @Test void marketingAndPublicAgreeOnPriceAndVisibility() throws Exception {
        ProductFilter filter=new ProductFilter();filter.setCategoryId(product.getCategory().getId());filter.setKeyword(product.getCode());
        assertThat(productService.search(filter,1).getContent()).hasSize(1);
        assertThat(productService.getView(product.getId()).getCurrentPrice()).isEqualByComparingTo("690000");
        assertThat(productService.getForm(product.getId()).getOriginalPrice()).isEqualByComparingTo("690000");
        MockHttpSession session=login(marketing,"/marketing/products");
        mvc.perform(post("/marketing/products/"+product.getId()+"/toggle").session(session).with(csrf())).andExpect(status().is3xxRedirection());
        assertThat(product.isVisible()).isFalse();
        mvc.perform(get("/products/"+product.getId())).andExpect(status().isNotFound());
    }
    @Test void salesCanHandleOnlyTheirAssignedOrders() throws Exception {
        MockHttpSession session=login(sale,"/sales/orders");
        mvc.perform(get("/sales/orders").session(session)).andExpect(status().isOk())
            .andExpect(content().string(containsString("OD-MINE"))).andExpect(content().string(not(containsString("OD-OTHER"))));
        mvc.perform(get("/sales/orders/"+assigned.getId()).session(session)).andExpect(status().isOk());
        mvc.perform(get("/sales/orders/"+other.getId()).session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/sales/orders/"+other.getId()).session(session).with(csrf()).param("status","CONFIRMED")).andExpect(status().isForbidden());
        mvc.perform(post("/sales/orders/"+assigned.getId()+"/assign").session(session).with(csrf()).param("saleId",otherSale.getId().toString())).andExpect(status().isForbidden());
        assertThat(other.getStatus()).isEqualTo(OrderStatus.SUBMITTED);
    }
    @Test void managerCanAssignAndNewAssigneeGetsAccess() throws Exception {
        MockHttpSession managerSession=login(manager,"/sales/orders");
        mvc.perform(get("/sales/orders").session(managerSession)).andExpect(status().isOk())
            .andExpect(content().string(containsString("OD-MINE"))).andExpect(content().string(containsString("OD-OTHER")));
        mvc.perform(post("/sales/orders/"+assigned.getId()+"/assign").session(managerSession).with(csrf()).param("saleId",otherSale.getId().toString())).andExpect(status().is3xxRedirection());
        assertThat(assigned.getAssignedSale().getId()).isEqualTo(otherSale.getId());assertThat(history.count()).isEqualTo(1);
        MockHttpSession oldSession=login(sale,"/sales/orders");
        mvc.perform(get("/sales/orders/"+assigned.getId()).session(oldSession)).andExpect(status().isForbidden());
        MockHttpSession newSession=login(otherSale,"/sales/orders");
        mvc.perform(get("/sales/orders/"+assigned.getId()).session(newSession)).andExpect(status().isOk());
    }
    @Test void orderTransitionsWriteHistoryAndCancellationRestocksOnce() throws Exception {
        MockHttpSession session=login(sale,"/sales/orders");
        mvc.perform(post("/sales/orders/"+assigned.getId()).session(session).with(csrf()).param("status","SUCCESS"))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("errorMessage"));
        assertThat(assigned.getStatus()).isEqualTo(OrderStatus.SUBMITTED);assertThat(history.count()).isZero();
        mvc.perform(post("/sales/orders/"+assigned.getId()).session(session).with(csrf()).param("status","CONFIRMED").param("saleNote","Đã gọi khách")).andExpect(status().is3xxRedirection());
        assertThat(assigned.getStatus()).isEqualTo(OrderStatus.CONFIRMED);assertThat(history.count()).isEqualTo(1);
        mvc.perform(post("/sales/orders/"+assigned.getId()).session(session).with(csrf()).param("status","CANCELLED")).andExpect(status().is3xxRedirection());
        assertThat(product.getStockQuantity()).isEqualTo(11);assertThat(history.count()).isEqualTo(2);
        mvc.perform(post("/sales/orders/"+assigned.getId()).session(session).with(csrf()).param("status","CANCELLED")).andExpect(status().is3xxRedirection());
        assertThat(product.getStockQuantity()).isEqualTo(11);assertThat(history.count()).isEqualTo(2);
    }
}