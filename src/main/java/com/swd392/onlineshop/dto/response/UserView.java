package com.swd392.onlineshop.dto.response;
import com.swd392.onlineshop.entity.User;
import java.time.LocalDateTime;
public record UserView(UserRow info, String address, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static UserView from(User u) { return new UserView(UserRow.from(u), u.getAddress(), u.getCreatedAt(), u.getUpdatedAt()); }
}