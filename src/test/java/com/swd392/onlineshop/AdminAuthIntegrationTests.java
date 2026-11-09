package com.swd392.onlineshop;

import com.swd392.onlineshop.entity.*;
import com.swd392.onlineshop.enums.*;
import com.swd392.onlineshop.repository.*;
import com.swd392.onlineshop.service.*;
import com.swd392.onlineshop.dto.request.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @ActiveProfiles("test") @Transactional
class AdminAuthIntegrationTests {
    @Autowired WebApplicationContext context;
    @Autowired SettingRepository settings;
    @Autowired UserRepository users;
    @Autowired CustomerRepository customers;
    @Autowired PasswordEncoder passwords;
    @Autowired UserService userService;
    @Autowired SettingService settingService;
    private MockMvc mvc;
    private Setting adminRole, customerRole;
    private User admin, customer;
    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        adminRole = role("ADMIN"); customerRole = role("CUSTOMER");
        admin = account("admin@test.com", "ND0001", adminRole);
        customer = account("customer@test.com", "ND0002", customerRole);
    }
    private Setting role(String value) {
        Setting s = new Setting(); s.setType(SettingType.USER_ROLE); s.setName(value); s.setCode(value);
        s.setValue(value); s.setGroupName("Vai trò"); s.setDisplayOrder(1); return settings.saveAndFlush(s);
    }
    private User account(String email, String code, Setting role) {
        User u = new User(); u.setCode(code); u.setFullName("Test User"); u.setEmail(email);
        u.setPassword(passwords.encode("Password123")); u.setRole(role); u.setStatus(UserStatus.ACTIVE);
        return users.saveAndFlush(u);
    }
    private MockHttpSession login(String email, String destination) throws Exception {
        return (MockHttpSession) mvc.perform(post("/auth/login").with(csrf()).param("email",email).param("password","Password123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(destination)).andReturn().getRequest().getSession(false);
    }
    @Test void anonymousAndWrongRoleCannotReachAdmin() throws Exception {
        mvc.perform(get("/admin/users")).andExpect(status().is3xxRedirection());
        MockHttpSession session = login(customer.getEmail(), "/");
        mvc.perform(get("/admin/users").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/settings").session(session).with(csrf())).andExpect(status().isForbidden());
    }
    @Test void rendersNovaAdminScreensAndAuthPages() throws Exception {
        mvc.perform(get("/auth/login")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Đăng nhập")));
        mvc.perform(get("/auth/register")).andExpect(status().isOk());
        MockHttpSession session = login(admin.getEmail(), "/admin/users");
        for (String path : new String[]{"/admin/users", "/admin/users/new", "/admin/users/" + admin.getId(),
                "/admin/users/" + admin.getId() + "/edit", "/admin/settings", "/admin/settings/new",
                "/admin/settings/" + adminRole.getId(), "/admin/settings/" + adminRole.getId() + "/edit"}) {
            mvc.perform(get(path).session(session)).andExpect(status().isOk());
        }
    }
    @Test void registersOnlyCustomerAndAllowsImmediateLogin() throws Exception {
        mvc.perform(post("/auth/register").with(csrf()).param("fullName","New Customer").param("email","NEW@test.com")
                .param("gender","MALE").param("mobile","0912345678").param("address","Hà Nội")
                .param("password","Password123").param("confirmPassword","Password123").param("roleId",adminRole.getId().toString()))
                .andExpect(redirectedUrl("/auth/login"));
        User u = users.findByEmailIgnoreCase("new@test.com").orElseThrow();
        assertThat(u.getRole().getValue()).isEqualTo("CUSTOMER"); assertThat(u.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(u.isEmailVerified()).isFalse(); assertThat(passwords.matches("Password123",u.getPassword())).isTrue();
        assertThat(customers.findByEmailIgnoreCase(u.getEmail()).orElseThrow().getUser().getId()).isEqualTo(u.getId());
        login("new@test.com", "/");
    }
    @Test void rejectsInvalidRegistrationAndDuplicateEmail() throws Exception {
        mvc.perform(post("/auth/register").with(csrf()).param("fullName","Bad").param("email","invalid")
                .param("password","x").param("confirmPassword","y"))
                .andExpect(status().isOk()).andExpect(model().attributeExists("registerErrors"));
        mvc.perform(post("/auth/register").with(csrf()).param("fullName","New Customer").param("email",customer.getEmail())
                .param("gender","MALE").param("mobile","0912345678").param("address","Hà Nội")
                .param("password","Password123").param("confirmPassword","Password123"))
                .andExpect(status().isOk()).andExpect(model().attributeExists("registerErrors"));
        assertThat(users.count()).isEqualTo(2);
    }
    @Test void hidingDoesNotDisableLoginAndLogoutInvalidatesSession() throws Exception {
        userService.toggle(customer.getId());
        MockHttpSession session = login(customer.getEmail(), "/");
        mvc.perform(post("/auth/logout").session(session).with(csrf())).andExpect(redirectedUrl("/auth/login?logout"));
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(post("/auth/login").param("email",admin.getEmail()).param("password","Password123")).andExpect(status().isForbidden());
    }
    @Test void changedRoleAndBlockedStatusApplyToExistingSessions() throws Exception {
        User another = account("other@test.com", "ND0003", adminRole);
        MockHttpSession session = login(another.getEmail(), "/admin/users");
        another.setRole(customerRole); users.flush();
        mvc.perform(get("/admin/users").session(session)).andExpect(status().isForbidden());
        another.setStatus(UserStatus.INACTIVE); users.flush();
        mvc.perform(get("/admin/users").session(session)).andExpect(status().is3xxRedirection());
    }
    @Test void protectsOwnRoleAndAssignedRoleSettings() {
        UserForm f = userService.form(admin.getId()); f.setRoleId(customerRole.getId());
        assertThatThrownBy(() -> userService.save(admin.getId(),f,admin.getEmail())).isInstanceOf(FormException.class);
        SettingForm sf = settingService.form(adminRole.getId()); sf.setValue("CUSTOMER");
        assertThatThrownBy(() -> settingService.save(adminRole.getId(), sf)).isInstanceOf(FormException.class);
        assertThatThrownBy(() -> settingService.toggle(adminRole.getId())).isInstanceOf(FormException.class);
    }
    @Test void adminCanCreateEditFilterAndToggleRecords() throws Exception {
        MockHttpSession session = login(admin.getEmail(), "/admin/users");
        mvc.perform(post("/admin/users").session(session).with(csrf()).param("fullName","New Staff")
                .param("email","staff@test.com").param("roleId",customerRole.getId().toString()).param("status","ACTIVE")
                .param("visible","true").param("password","Password123").param("confirmPassword","Password123"))
                .andExpect(status().is3xxRedirection());
        User u = users.findByEmailIgnoreCase("staff@test.com").orElseThrow();
        mvc.perform(post("/admin/users/" + u.getId() + "/toggle").session(session).with(csrf())).andExpect(redirectedUrl("/admin/users"));
        assertThat(u.isVisible()).isFalse(); assertThat(u.getStatus()).isEqualTo(UserStatus.ACTIVE);
        mvc.perform(get("/admin/users").session(session).param("keyword","New Staff").param("visible","false"))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("staff@test.com")));
        mvc.perform(post("/admin/settings").session(session).with(csrf()).param("type","PRODUCT_CATEGORY")
                .param("code","KEYBOARD").param("name","Bàn phím").param("groupName","Sản phẩm").param("value","KEYBOARD")
                .param("displayOrder","1").param("active","true")).andExpect(status().is3xxRedirection());
        Setting s = settings.findByTypeAndValue(SettingType.PRODUCT_CATEGORY,"KEYBOARD").orElseThrow();
        SettingForm sf = settingService.form(s.getId()); sf.setName("Bàn phím mới"); settingService.save(s.getId(), sf);
        mvc.perform(post("/admin/settings/" + s.getId() + "/toggle").session(session).with(csrf())).andExpect(redirectedUrl("/admin/settings"));
        assertThat(s.isActive()).isFalse();
    }
}