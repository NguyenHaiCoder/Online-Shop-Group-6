# Kiểm tra bản hợp nhất Admin / Marketing / Sale

Đầu main được tích hợp: `82fb336` (Marketing và Sale). Nhánh làm việc: `daivn/admin`.

## Xử lý tích hợp

- Hợp nhất 12 file conflict bằng cách giữ các phương thức repository cần cho cả public/Admin và Marketing/Sale.
- DTO sản phẩm/bài viết giữ builder và trường dữ liệu của Marketing; thêm ánh xạ public. Template public sử dụng cấu trúc gallery Map chung.
- Tách `CatalogFilter` public (nhiều danh mục/khoảng giá) khỏi `ProductFilter` Marketing (một danh mục/trạng thái), tránh lỗi binding.
- Giữ CSS sửa nút/bảng của Admin và CSS biểu mẫu Sale từ main.
- Giá bán dùng `originalPrice` trên entity và DTO để public/Marketing không hiển thị hai giá khác nhau từ cột salePrice cũ.
- Không thay đổi migration hoặc tên bảng.

## Kết quả

- `.\mvnw.cmd -B "-Djava.version=17" clean test`: **18 test, 0 lỗi, 0 thất bại**. Máy chạy kiểm tra có Java 17; pom vẫn yêu cầu Java 21.
- Test tích hợp mới: render 20 màn Marketing, kiểm tra lọc/giá/ẩn sản phẩm, Sale chỉ truy cập đơn được giao, Manager phân công lại và chuyển quyền xem, chặn chuyển trạng thái sai, ghi lịch sử, hoàn kho khi hủy đúng một lần.
- Chrome với SQL Server Express local: **82 lượt kiểm tra** ở 1440px/390px (30 public/Admin + 52 Marketing/Sale/Manager), HTTP 200, không tràn ngang toàn trang, không có lỗi JavaScript.
- Đã đăng nhập bằng các tài khoản mẫu Admin, Marketing, Sale và Sale Manager. Kiểm tra trình duyệt chỉ đọc dữ liệu; các thao tác ghi được kiểm tra trên H2 và rollback.
- Không gọi upload Cloudinary thật vì chưa có khóa dịch vụ. Giỏ hàng/checkout/hồ sơ vẫn phụ thuộc các phân hệ chưa triển khai; đây không phải chứng nhận hoàn tất mọi chức năng của hệ thống.