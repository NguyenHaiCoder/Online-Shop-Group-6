# Online Shop — quy ước cho agent

Làm việc theo màn hình trong `NOVA_SHOP_UI` và template Thymeleaf. Không thêm Dashboard. Không thêm Email Service (không `UserToken`, không gửi mail xác thực hay quên mật khẩu).

## Entity

Mỗi bảng có `id`, `created_at`, `updated_at` từ `BaseEntity`. Không sửa tên bảng cũ. Field dưới đây là phần màn hình yêu cầu thêm so với schema V1.

| Entity | Màn | Field đúng theo UI |
|---|---|---|
| `Product` | PR, SF03 | `code`, `name`, `briefInfo`, `description`, `originalPrice` là giá bán, `stockQuantity`, `visible`, `thumbnail`, `images`, `specifications` (từng dòng tên/giá trị) |
| `Customer` | MC | `code` (KH0001), `fullName`, `email`, `mobile`, `address`, `note`, `visible` là Hiện/Ẩn |
| `User` | AU02, ADU | `code` (ND0001), `fullName`, `email`, `mobile`, `password`, `avatar`, `role`, `emailVerified`, `visible` là Hiện/Ẩn. Ẩn không khóa đăng nhập |
| `Post` | MP | `code`, `title`, `thumbnail`, `briefInfo`, `content`, `author`, `visible` |
| `Slider` | MS | `title`, `image`, `notes`, `buttonLabel`, `backlink`, `displayOrder`, `visible` |
| `Setting` | ADS | `name`, `code`, `groupName`, `displayOrder`, `value`, `description`, `active` |
| `Feedback` | SF11, MF | `code` (PH0001), `fullName`, `email`, `subject`, `product`, `content`, `source`, `visible`, `updatedBy` |
| `Order` | SF06, CU, SL | `code`, người nhận, `note`, `shippingFee`, `totalAmount`, `paymentMethod` = COD, `status`, `assignedSale`, `saleNote`, `OrderHistory` |

`OrderStatus`: `SUBMITTED`, `CONFIRMED`, `SHIPPING`, `SUCCESS`, `CANCELLED`. Nhãn màn hình của `SUCCESS` là "Hoàn tất".

Ảnh upload đi qua `FileStorageService.store(file, folder)`. `folder` chỉ được `avatars`, `posts`, `sliders`, `products`, `feedbacks`. Hàm trả `secure_url` của Cloudinary, DB lưu URL đó. `url(publicId)` dựng lại link nếu chỉ có public id. Không ghi file vào thư mục `uploads/`.

## DTO

Form không bind entity. Request và response nằm ở hai package khác nhau.

`dto/request` — dữ liệu đi vào:

- `XxxForm`: body tạo hoặc sửa của một màn. Ví dụ `ProductForm`, `ReceiverForm`, `UserForm`.
- `XxxFilter`: query của một list. Ví dụ `ProductFilter`, `OrderFilter`.

`dto/response` — dữ liệu đi ra màn hình:

- `XxxRow`: một dòng của list. Ví dụ `ProductRow`, `OrderRow`.
- `XxxView`: một màn chi tiết. Ví dụ `ProductView`, `OrderView`.
- `CartLine`: một dòng giỏ hàng.

```java
// request
public class ProductForm { private String code; private String name; private Long categoryId; }
public class ProductFilter { private String keyword; private Long categoryId; private Boolean visible; }

// response
public class ProductRow { private Long id; private String code; private String name; private BigDecimal currentPrice; }
public class ProductView { private String code; private String specifications; private List<String> imageUrls; }
```

Tên field DTO trùng tên input trên template (`code`, `fullName`, `shippingFee`). Không đặt `ProductRequest` hay `ProductResponse`.
