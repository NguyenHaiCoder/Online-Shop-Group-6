# Kiểm tra bản merge chỉ gồm Admin và xác thực

Main được tích hợp: `82fb336`. Nhánh làm việc: `daivn/admin`.

## Phạm vi bản đưa lên main

- Admin: người dùng, cấu hình, lọc/phân trang, thêm/xem/sửa, xác nhận Ẩn/Hiện và upload avatar.
- Đăng nhập/đăng xuất/đăng ký, BCrypt và phân quyền từ Setting USER_ROLE.
- Cấu hình Cloudinary/database qua .env (không đưa bí mật lên Git).
- CSS Admin được giới hạn bằng `.admin-body`; CSS trang đăng nhập/đăng ký bằng `.auth-body` và `.auth-card`.
- Giữ nguyên controller, service nghiệp vụ, DTO và entity sản phẩm của Marketing/Sale/public trên main. Đã bỏ các bổ sung trang chủ/catalog/blog của nhánh Admin khỏi bản cuối.
- Chỉ bổ sung phương thức repository dùng chung để Admin và đăng ký vẫn hoạt động cùng code main. Không thay đổi migration hoặc tên bảng.

## Kiểm tra

- `.\mvnw.cmd -B "-Djava.version=17" clean test`: **13 test, 0 lỗi/thất bại**, kiểm tra Admin/auth và hồi quy khi ghép với Marketing/Sale. Máy kiểm tra có JDK 17; pom vẫn yêu cầu JDK 21.
- Test Admin/auth: render list/form/detail, đăng ký luôn nhận CUSTOMER, email trùng/dữ liệu sai, BCrypt, CSRF, logout hủy phiên, Ẩn không khóa login, quyền/trạng thái đổi được áp dụng cho phiên đang mở, chặn tự đổi quyền và sửa/ẩn vai trò đang dùng.
- Test hồi quy chỉ đọc màn Marketing và kiểm tra quyền Sale/Manager, phân công lại, lịch sử trạng thái và hoàn kho khi hủy. Đây là test code sẵn có trên main, không phải triển khai thêm luồng Marketing/Sale cho nhánh Admin.
- Chrome trên SQL Server local: **20 lượt kiểm tra đạt**, login/register và 8 trang Admin ở 1440px/390px; kiểm tra HTTP, overflow, JavaScript, modal mở/đóng. Không thay đổi dữ liệu thật trong kiểm tra trình duyệt.
- Không kiểm tra upload Cloudinary thực tế khi chưa có khóa dịch vụ.