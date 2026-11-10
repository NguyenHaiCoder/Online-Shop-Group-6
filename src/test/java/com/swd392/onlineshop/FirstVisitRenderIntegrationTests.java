package com.swd392.onlineshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Loi da gap: lan dau vao trang dai (trang chu, san pham) khi chua co session, CSRF token chi duoc tao luc
 * Thymeleaf render toi form dang nhap o cuoi trang. Luc do response da gui di mot phan nen Tomcat bao
 * "Cannot create a session after the response has been committed" -> trang bi cat ngang, mat ui.js va chatbox.
 * Sua: SecurityConfig tao CSRF token (va session) ngay dau moi request, truoc khi render.
 */
@SpringBootTest
@ActiveProfiles("test")
class FirstVisitRenderIntegrationTests {

    @Autowired
    WebApplicationContext context;

    @Test
    void csrfTokenAndSessionAreCreatedBeforeAnyPageOutput() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        // Request khong render form nao: neu token van duoc tao thi chac chan no co truoc khi trang duoc ghi ra.
        MockHttpServletRequest request = mvc.perform(get("/js/ui.js"))
                .andExpect(status().isOk())
                .andReturn().getRequest();

        assertThat(request.getSession(false)).as("session duoc tao ngay dau request").isNotNull();
        assertThat(request.getSession(false).getAttributeNames().hasMoreElements())
                .as("CSRF token da duoc luu vao session").isTrue();
    }
}
