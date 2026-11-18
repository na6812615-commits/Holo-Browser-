# Holo Browser

Trình duyệt web Android hiện đại được xây dựng bằng Kotlin và Jetpack Compose (Material Design 3), hỗ trợ đa tab, chế độ ẩn danh, dấu trang, lịch sử duyệt web, tìm kiếm thông minh và trình quản lý tải xuống.

## Cách đưa lên GitHub để tự động xuất ra file APK

Dự án đã được cấu hình sẵn **GitHub Actions** tại `.github/workflows/build-apk.yml` để tự động biên dịch mã nguồn thành file `.apk` cài đặt trực tiếp trên Android.

### Bước 1: Tải mã nguồn lên GitHub
1. Tải file **ZIP** của dự án này về máy và **giải nén**.
2. Tạo một repository mới trên [GitHub](https://github.com/new).
3. Tải toàn bộ các tệp và thư mục sau khi giải nén lên repository (lưu ý **giữ nguyên thư mục `.github/workflows/`**; nếu tải bằng giao diện web GitHub, hãy đảm bảo chọn hiển thị tệp ẩn hoặc dùng `git push`).

### Bước 2: Chạy quy trình tạo APK
1. Mở repository trên GitHub và chọn tab **Actions**.
2. Quy trình **Build Holo Browser APK** sẽ tự động chạy ngay khi bạn đẩy code lên nhánh `main` hoặc `master`.
3. Bạn cũng có thể chọn **Build Holo Browser APK** → bấm **Run workflow** để chạy thủ công bất cứ lúc nào.

### Bước 3: Tải file APK về máy
1. Sau khi quy trình chạy xong (hiện dấu tích xanh ✅), bấm vào lần chạy đó.
2. Kéo xuống phần **Artifacts** ở cuối trang và tải **Holo-Browser-APK**.
3. Giải nén tệp vừa tải để lấy **`Holo-Browser.apk`** và cài đặt lên điện thoại Android.

> **Mẹo:** Nếu bạn tạo một tag phiên bản dạng `v1.0.0` (ví dụ `git tag v1.0.0 && git push origin v1.0.0`), GitHub sẽ tự động tạo một bản phát hành trong mục **Releases** kèm sẵn tệp `Holo-Browser.apk`.
