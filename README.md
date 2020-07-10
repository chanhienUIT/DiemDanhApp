# DiemDanhApp
Ứng dụng điểm danh Android.
## Cài đặt
## Nếu ứng dụng không thể đăng nhập thì hãy cài đặt ```app-debug.apk``` trực tiếp.
### Yêu cầu phần mềm
* Android Studio phiên bản 4.0 trở đi.
* Thiết bị Android có phiên bản SDK 26 hoặc mới hơn.
* Vào cài đặt điện thoại, chọn ```Ứng dụng```, chọn ```App Điểm Danh```, chọn ```Quyền``` và cho phép quyền truy cập vị trí.
### Build
* Mở Android Studio và chọn ```Open an existing Android Studio project```
* Trỏ đến thư mục ```DiemDanhApp-master``` và chọn ```OK```
* Nếu được hỏi ```Gradle Sync``` thì chọn ```OK```
### Server
* Tại file ```strings.xml```, trường ```server_client_id``` chứa giá trị OAuth 2.0 Client ID.
* Thay thế ```http://diemdanh.ddns.net``` trong tất cả file trong source code bằng tên miên riêng hoặc địa chỉ IP của máy chủ.
