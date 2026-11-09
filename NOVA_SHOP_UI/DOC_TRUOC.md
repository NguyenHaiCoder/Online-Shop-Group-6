# NOVA SHOP — Bộ giao diện đã dựng

Gói gồm 84 ảnh PNG bản cuối: 60 màn desktop chính, 11 màn mobile, 13 trạng thái bổ sung. Các bản cũ đã được thay bằng bản rà soát; không đưa ảnh cũ trùng mã vào gói.

## Cách xem

Giải nén toàn bộ ZIP, mở index.html trong trình duyệt để tìm và xem ảnh. Bấm ảnh để mở PNG đầy đủ. Danh_muc_man_hinh.csv chứa tên màn, vai trò, use case, điểm vào, CTA/đích đến và điều kiện nghiệp vụ. Có thể mở CSV bằng Excel hoặc Google Sheets.

## Phạm vi bàn giao

Đây là ảnh mockup, không phải mã nguồn hoặc prototype có nút thao tác nghiệp vụ. Các ảnh mobile mô tả bố cục thích ứng; độ phân giải ảnh xuất có thể khác kích thước thiết kế 390px. Danh mục chứa đúng những ảnh đã tạo tại thời điểm đóng gói; không tuyên bố đã hoàn tất toàn bộ biến thể lỗi trong master prompt.

13 biến thể đã có: đơn Confirmed, Shipping, Completed, Cancelled; chặn sửa; chặn hủy; loading sản phẩm; danh sách sản phẩm trống; tìm kiếm không có kết quả; giỏ trống; chưa có đơn Customer; Sale chưa có đơn; giỏ có vấn đề số lượng/sản phẩm ngừng bán.

## Quy tắc bàn giao

Guest được checkout không cần đăng nhập. Customer chỉ sửa/hủy đơn Submitted. Sale chỉ xem đơn được giao. Manager xem và phân công; phân công độc lập trạng thái đơn. Admin chỉ quản lý người dùng và cấu hình. Marketing quản lý năm phân hệ nội dung/hồ sơ. Không có Chatbot AI. Ẩn/Hiện không phải xóa.

Đơn minh họa OD000125 có bàn phím 690.000đ và chuột 290.000đ, tiền hàng980.000đ, phí30.000đ, tổng1.010.000đ. Những ảnh trạng thái là các thời điểm hoặc nhánh khác nhau của cùng ví dụ, không phải đồng thời. Giỏ lỗi là tình huống khác với số lượng3 bàn phím.

Vòng đời Submitted→Confirmed→Shipping→Completed, nhánh Submitted→Cancelled; COD; trường dữ liệu; phí giao hàng và ngành hàng đều là giả định thiết kế cần đối chiếu SRS, không phải yêu cầu đã xác nhận.

## Lưu ý khi triển khai

Ảnh sinh có thể có sai khác nhỏ về ảnh sản phẩm, màu badge, căn chỉnh và nhãn chú thích. Ưu tiên master prompt và cột điều kiện/CTA trong danh mục khi có mâu thuẫn. Đặc biệt: CU02-SHIPPING không dẫn đến CU04; SF05-EMPTY dẫn đến SF02, không phải SF06. Các nhãn chân ảnh sai đã được đính chính trong danh mục. Không triển khai quyền hoặc hành vi chỉ vì chúng vô tình xuất hiện trong ảnh. Dùng chung component, asset sản phẩm và bộ màu trạng thái khi lập trình.
