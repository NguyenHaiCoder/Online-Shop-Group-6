# Admin và xác thực

Nhánh làm việc: `daivn/admin`.

## Chạy ứng dụng

- Dùng JDK 21 theo `pom.xml` và SQL Server theo `application.properties`.
- Chạy từ thư mục gốc: `.\mvnw.cmd spring-boot:run`.
- Flyway tạo/cập nhật schema và dữ liệu mẫu qua các migration hiện có.
- Đăng nhập: `/auth/login`; đăng ký: `/auth/register`.
- Admin: `/admin/users`, `/admin/settings`.

Luồng backend: controller gọi service interface; service impl xử lý nghiệp vụ và gọi repository. Form/filter ở `dto/request`, dữ liệu hiển thị ở `dto/response`. Không bind entity vào form.

## Cloudinary

File `.env` ở thư mục gốc đã được tạo; nếu clone mới, copy `.env.example` thành `.env`.
Điền `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` từ Cloudinary Console > Settings > API Keys, sau đó khởi động lại ứng dụng.
Spring đọc `.env` qua `spring.config.import`. Biến môi trường hệ điều hành có độ ưu tiên cao hơn file.
Không commit `.env`; Git chỉ theo dõi `.env.example` trống.

Admin có thể upload avatar khi tạo/sửa người dùng. Avatar tối đa 2 MB, JPG/PNG/WEBP; upload qua `FileStorageService.store(file, "avatars")`, DB lưu `secure_url`. Không ghi ảnh local. Chưa kiểm tra kết nối Cloudinary thực tế vì chưa có thông tin tài khoản.

## Quyền và phiên đăng nhập

`User.role` liên kết đến `Setting` có `type=USER_ROLE`. Spring Security lấy authority từ `ROLE_` + `Setting.value`:

| Giá trị | Khu vực |
| --- | --- |
| ADMIN | `/admin/**` |
| MARKETING | `/marketing/**` |
| SALE, SALE_MANAGER | `/sales/**` |
| CUSTOMER | `/my-orders/**` |

Marketing và Sale/Sale Manager đã được tích hợp từ main. OrderService kiểm tra quyền trên từng đơn: Sale chỉ xử lý đơn được giao; Manager có thể phân công lại. Kết quả kiểm tra tích hợp nằm trong MERGE_VALIDATION.md.

Đăng ký công khai luôn nhận CUSTOMER, trạng thái ACTIVE, emailVerified=false; không gửi email xác thực. Khách đăng ký có thể đăng nhập ngay. Nếu đã có hồ sơ Customer cùng email, tài khoản được liên kết và giữ thông tin hồ sơ cũ.

Admin nhập và xác nhận mật khẩu khi tạo tài khoản; DB chỉ lưu BCrypt. Khi sửa không đổi email hoặc mật khẩu. Ẩn/Hiện (`visible`) không thay đổi khả năng đăng nhập; trạng thái đăng nhập (`status`) là trường riêng. Không tự đổi vai trò hoặc trạng thái đăng nhập của chính mình. Không đổi giá trị/loại của vai trò đã tạo và không ẩn vai trò đang có người sử dụng.

Đổi quyền hoặc khóa tài khoản được áp dụng cho phiên đang mở ở request tiếp theo. Logout dùng POST và CSRF, hủy session. Mọi POST thay đổi dữ liệu đều được bảo vệ bằng CSRF.

## UI và kiểm tra

Admin bám ADU01–ADU05, ADS01–ADS05 trong `NOVA_SHOP_UI`: danh sách/lọc/phân trang, thêm, chi tiết, sửa, hộp xác nhận ẩn/hiện. Login dùng template AU01 hiện có. Theo `NOVA_SHOP_UI/DOC_TRUOC.md`, phạm vi Admin gồm người dùng và cấu hình, không có Dashboard hoặc Chatbot AI.

Chạy kiểm tra: `.\mvnw.cmd test` trên JDK 21. Test profile dùng H2 riêng, không kết nối hoặc thay đổi SQL Server thật. Máy thực hiện hiện chỉ có JDK 17, đã kiểm tra tương thích bằng `.\mvnw.cmd "-Djava.version=17" test`; không đổi yêu cầu JDK 21 của project. Đã chạy thành công cả 4 migration trên SQL Server Express local và kiểm tra đăng nhập tài khoản mẫu.
## Cấu hình local hiện tại

Database: OnlineShopDB trên localhost:1433; tài khoản riêng của project nằm trong .env qua DB_USERNAME/DB_PASSWORD. Không cần đổi mật khẩu sa.

Máy hiện dùng Java 17, chạy từ thư mục gốc bằng lệnh:

    .\mvnw.cmd "-Djava.version=17" spring-boot:run

Tài khoản mẫu Admin: admin@onlineshop.com / 123456.
