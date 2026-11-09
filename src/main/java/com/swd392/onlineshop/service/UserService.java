package com.swd392.onlineshop.service;
import com.swd392.onlineshop.dto.request.*;
import com.swd392.onlineshop.dto.response.UserRow;
import org.springframework.data.domain.*;
public interface UserService {
    Page<UserRow> search(UserFilter filter, Pageable pageable);
    com.swd392.onlineshop.dto.response.UserView view(Long id);
    UserRow current(String email);
    UserForm form(Long id);
    Long save(Long id, UserForm form, String actor);
    void toggle(Long id);
}