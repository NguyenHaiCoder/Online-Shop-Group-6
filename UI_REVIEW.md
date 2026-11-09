# Rà giao diện NOVA SHOP

Đã đối chiếu SF01, AU01, ADU01–ADU05, ADS01–ADS05 trong `NOVA_SHOP_UI` và chụp bằng Chrome headless ở 1440px/390px.

## Những lỗi đã sửa

- Trang chủ trước đây luôn nhận danh sách rỗng: đã nối repository → service → service impl → controller để hiển thị sản phẩm nổi bật, bài viết, danh mục và slider thực tế.
- Nối trang danh sách/chi tiết sản phẩm, blog để các link trên trang chủ hoạt động. Chỉ hiển thị bản ghi visible; giá bán lấy originalPrice theo AGENTS.md.
- Form đăng nhập/đăng ký không còn dùng lưới hai cột của form Admin. Nút submit và các link chiếm đủ chiều rộng.
- Sửa CSS row-actions làm chữ trên nút Chỉnh sửa/Ẩn bị bó vào ô 32px. Ô thao tác trong bảng giữ display: table-cell để không làm lệch hàng.
- Định nghĩa detail-grid của Admin được giới hạn trong staff-content, tránh ảnh hưởng trang sản phẩm.
- Sửa bộ lọc giá gây tràn ngang trên mobile; form/filter/menu/footer thích ứng màn nhỏ.
- Sửa phân trang, breadcrumb, logo N, tên người đăng nhập và khoảng cách các thẻ.
- Hộp thoại có điều khiển focus/Tab/Escape. Slider có nút trước/sau và chấm điều hướng hoạt động.
- Ảnh sản phẩm/bài viết trong seed trỏ đến file không có trong repository. Resource handler dùng trạng thái “Chưa có ảnh”; không thay đổi URL lưu trong DB hoặc giả ảnh của sản phẩm khác. Slider chưa có ảnh dùng ảnh góc làm việc.

## Kiểm tra

- Script kiểm tra Chrome: `.ui-review/verify.cjs`; kết quả `.ui-review/results.json`.
- 15 trang × 2 kích thước = 30 lượt kiểm tra HTTP 200, không tràn ngang toàn trang, không có ảnh hỏng hiển thị hoặc lỗi JavaScript.
- Kiểm tra mở/đóng modal đăng nhập và xác nhận ẩn, cùng đăng nhập Admin thật trên SQL Server.
- Ảnh trước/sau nằm trong `.ui-review/screenshots/` (giữ local, được Git ignore).
- Test tự động kiểm tra dữ liệu public, chặn chi tiết bản ghi ẩn và phục vụ ảnh dự phòng; kết hợp test Admin/auth đã có.

Phạm vi kiểm tra giao diện lần này: trang chủ, login/register, public product/blog và các trang quản lý người dùng/cấu hình Admin. Giỏ hàng/checkout và hồ sơ/đổi mật khẩu vẫn cần backend tương ứng. Marketing/Sale sau đó đã được tích hợp và kiểm tra bổ sung: xem MERGE_VALIDATION.md.

## Ảnh banner/đăng nhập

Tool: built-in `image_gen` (skill imagegen), không dùng CLI/API key. File đã lưu trong project: `src/main/resources/static/images/nova-workspace.png`. Ảnh tham chiếu: `NOVA_SHOP_UI/01_Desktop/SF/SF01.png`.

Prompt:

> Use case: product-mockup. Asset type: wide photographic website hero, also usable on a login panel. Create a photorealistic premium desktop workspace matching the supplied NOVA shop UI reference's photographic style. White and light gray compact mechanical keyboard with a few blue accent keys, white wireless mouse, navy blue over-ear headphones, a subtle desk mat, a silver monitor and small leafy plant on a pale cool gray desk. Soft daylight, understated blue and white palette, realistic materials. Wide landscape composition: product arrangement predominantly on right two thirds, left third clean softly lit pale blue/white space for HTML headline overlay. No text, no logo, no UI controls, no website chrome, no labels, no watermark. The attached image is style/composition reference only; deliver only a clean workspace photograph, not a screenshot or UI mockup.