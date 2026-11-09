package com.swd392.onlineshop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String baseUrl = "http://localhost:8080";
    private int pageSize = 10;
    private String uploadDir = "uploads";

    private Home home = new Home();
    private Sider sider = new Sider();
    private Token token = new Token();
    private Payment payment = new Payment();
    private Shop shop = new Shop();

    @Getter
    @Setter
    public static class Home {
        private int featuredSize = 8;
    }

    @Getter
    @Setter
    public static class Sider {
        private int latestSize = 5;
    }

    @Getter
    @Setter
    public static class Token {
        private int verifyEmailExpiryMinutes = 1440;
        private int resetPasswordExpiryMinutes = 30;
    }

    @Getter
    @Setter
    public static class Payment {
        private String bankName = "Vietcombank";
        private String accountNumber = "0123456789";
        private String accountName = "CONG TY TNHH ONLINE SHOP";
        private String transferPrefix = "ONLINESHOP";
    }

    @Getter
    @Setter
    public static class Shop {
        private String hotline = "1900 1234";
        private String email = "support@onlineshop.com";
        private String address = "Ha Noi, Viet Nam";
    }
}

