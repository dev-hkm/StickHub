package com.hkm.stickhub.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Settings-facing copy. Keeping translations typed prevents language branches from leaking
 * into callbacks and keeps English as the safe fallback for newly added labels.
 */
data class StickHubStrings(
    val locale: AppLanguage,
    val settings: String,
    val settingsSubtitle: String,
    val backToLibrary: String,
    val general: String,
    val quickStickers: String,
    val library: String,
    val backupPrivacy: String,
    val appearance: String,
    val colorTheme: String,
    val themeMode: String,
    val language: String,
    val english: String,
    val vietnamese: String,
    val libraryLayout: String,
    val quickStickersSection: String,
    val availability: String,
    val appearanceSection: String,
    val presets: String,
    val bubble: String,
    val popupComposition: String,
    val stickerClarity: String,
    val openingBehavior: String,
    val popupContent: String,
    val whatsappStickers: String,
    val cloudBackup: String,
    val libraryAndData: String,
    val privacy: String,
    val manageCategories: String,
    val categoriesAvailable: (Int) -> String,
    val export: String,
    val import: String,
    val localFirstEncrypted: String,
    val light: String,
    val dark: String,
    val system: String,
    val grant: String,
    val done: String,
    val cancel: String
)

val LocalStickHubStrings = staticCompositionLocalOf { stringsFor(AppLanguage.ENGLISH) }

fun StickHubStrings.text(source: String): String {
    if (locale == AppLanguage.ENGLISH) return source
    return mapOf(
        "Show search" to "Hiện thanh tìm kiếm",
        "Show search field at the top of library" to "Hiện ô tìm kiếm ở đầu thư viện",
        "Show category filters" to "Hiện bộ lọc danh mục",
        "Show category tag rail below search" to "Hiện hàng danh mục bên dưới thanh tìm kiếm",
        "Enable Quick Stickers" to "Bật Sticker nhanh",
        "Floating bubble for instant sticker access while chatting" to "Nút nổi để mở sticker nhanh khi trò chuyện",
        "Display over other apps" to "Hiển thị trên ứng dụng khác",
        "Permission granted" to "Đã cấp quyền",
        "Permission required for floating bubble" to "Cần quyền để hiển thị nút nổi",
        "Show title in popup" to "Hiện tiêu đề trong popup",
        "Show search in popup" to "Hiện tìm kiếm trong popup",
        "Show categories in popup" to "Hiện danh mục trong popup",
        "Display app name at the top of quick stickers" to "Hiện tên ứng dụng ở đầu Sticker nhanh",
        "Display search input in quick stickers" to "Hiện ô tìm kiếm trong Sticker nhanh",
        "Display category tabs in quick stickers" to "Hiện tab danh mục trong Sticker nhanh",
        "Show fewer themes" to "Thu gọn chủ đề",
        "Show all themes" to "Hiện tất cả chủ đề",
        "Bubble size" to "Kích thước nút nổi",
        "Bubble opacity" to "Độ trong suốt nút nổi",
        "Whole popup opacity (master)" to "Độ trong suốt toàn popup",
        "Master multiplier for all popup elements" to "Hệ số chung cho mọi thành phần popup",
        "Popup background opacity" to "Độ trong suốt nền popup",
        "Surface background and border" to "Nền và viền bề mặt popup",
        "Sticker opacity" to "Độ trong suốt sticker",
        "Sticker images in the grid" to "Ảnh sticker trong lưới",
        "Header / search / tags opacity" to "Độ trong suốt tiêu đề / tìm kiếm / tag",
        "Title, search box, and category chips" to "Tiêu đề, ô tìm kiếm và chip danh mục",
        "Hidden (enable title, search, or categories above)" to "Đang ẩn (hãy bật tiêu đề, tìm kiếm hoặc danh mục ở trên)",
        "Close button opacity" to "Độ trong suốt nút đóng",
        "Top-right X button" to "Nút X ở góc trên bên phải",
        "Resize button opacity" to "Độ trong suốt nút thay đổi kích thước",
        "Bottom-right resize handle" to "Nút đổi kích thước ở góc dưới bên phải",
        "Sticker shadow strength" to "Độ đổ bóng sticker",
        "Silhouette drop shadow helps stickers stand out over chat backgrounds. Applies when you release the slider." to "Bóng viền giúp sticker nổi bật trên nền chat. Áp dụng khi thả thanh trượt.",
        "Reveal overlay controls" to "Hiện các điều khiển lớp phủ",
        "Temporarily sets bubble and all popup layers to 100% visibility for 5 seconds" to "Tạm đặt nút nổi và mọi lớp popup ở mức hiển thị 100% trong 5 giây",
        "Reset Quick Stickers appearance" to "Đặt lại giao diện Sticker nhanh",
        "Reset all opacities, shadow, and bubble size to defaults without changing position or data" to "Đặt lại độ trong suốt, bóng và kích thước nút nổi mà không đổi vị trí hay dữ liệu",
        "All stickers" to "Tất cả sticker",
        "Favorites" to "Yêu thích",
        "Frequently used" to "Dùng thường xuyên",
        "Last used filter" to "Bộ lọc gần nhất",
        "Custom category" to "Danh mục tùy chỉnh",
        "Close popup" to "Đóng popup",
        "Close popup (Default)" to "Đóng popup (Mặc định)",
        "Keep popup open" to "Giữ popup mở",
        "Open popup with" to "Mở popup với",
        "After copying a sticker" to "Sau khi sao chép sticker",
        "Add to WhatsApp" to "Thêm vào WhatsApp",
        "Preparing…" to "Đang chuẩn bị…",
        "Encrypted cloud backup" to "Sao lưu đám mây mã hóa",
        "Optional backup for your stickers and categories" to "Sao lưu tùy chọn cho sticker và danh mục",
        "Ready to sync your local library" to "Thư viện cục bộ sẵn sàng đồng bộ",
        "Set up cloud backup" to "Thiết lập sao lưu đám mây",
        "Restore with a recovery code" to "Khôi phục bằng mã phục hồi",
        "Restore cloud backup" to "Khôi phục sao lưu đám mây",
        "Recovery code" to "Mã phục hồi",
        "Copy recovery code" to "Sao chép mã phục hồi",
        "Back up" to "Sao lưu",
        "Restore" to "Khôi phục",
        "Categories" to "Danh mục",
        "Storage" to "Dung lượng",
        "Reset" to "Đặt lại",
        "Reset Quick Stickers appearance?" to "Đặt lại giao diện Sticker nhanh?",
        "Done" to "Xong",
        "Cancel" to "Hủy",
        "Manage" to "Quản lý",
        "Change layout" to "Đổi bố cục",
        "Selected" to "Đã chọn",
        "Active preset" to "Preset đang dùng",
        "Library layout" to "Bố cục thư viện",
        "Grid" to "Lưới",
        "List" to "Danh sách",
        "Add at least 3 stickers to a category to offer it as a pack." to "Thêm ít nhất 3 sticker vào danh mục để cung cấp dưới dạng gói."
        ,"Search" to "Tìm kiếm"
        ,"Clear" to "Xóa"
        ,"Settings" to "Cài đặt"
        ,"Create sticker" to "Tạo sticker"
        ,"Pick photo from device" to "Chọn ảnh từ thiết bị"
        ,"Use full photo as sticker" to "Dùng toàn bộ ảnh làm sticker"
        ,"Cancel" to "Hủy"
        ,"Close" to "Đóng"
        ,"Back" to "Quay lại"
        ,"Copy" to "Sao chép"
        ,"Save" to "Lưu"
        ,"Title" to "Tiêu đề"
        ,"Category" to "Danh mục"
        ,"Tags (comma separated)" to "Tag (ngăn cách bằng dấu phẩy)"
        ,"Auto detect" to "Tự nhận diện"
        ,"Choose subject" to "Chọn vật thể"
        ,"Subject" to "Vật thể"
        ,"Source Photo" to "Ảnh gốc"
        ,"Importing…" to "Đang nhập…"
        ,"Import" to "Nhập"
        ,"Import (" to "Nhập ("
        ,"Clipboard image" to "Ảnh trong clipboard"
        ,"Favorite" to "Yêu thích"
        ,"Selected" to "Đã chọn"
        ,"Copied" to "Đã sao chép"
        ,"Flip" to "Lật ảnh"
        ,"Rotate 90°" to "Xoay 90°"
        ,"Caption text" to "Nội dung chữ"
        ,"Overwrite" to "Ghi đè"
        ,"Save as Copy" to "Lưu bản sao"
        ,"Studio" to "Chỉnh sửa"
        ,"Share as image" to "Chia sẻ dưới dạng ảnh"
        ,"Delete Sticker" to "Xóa sticker"
        ,"New Category" to "Danh mục mới"
        ,"Category Name" to "Tên danh mục"
        ,"Create" to "Tạo"
        ,"Add category" to "Thêm danh mục"
        ,"Category name" to "Tên danh mục"
        ,"New category name" to "Tên danh mục mới"
        ,"Rename" to "Đổi tên"
        ,"Delete" to "Xóa"
        ,"Delete Category" to "Xóa danh mục"
        ,"Back to Settings" to "Quay lại Cài đặt"
        ,"Add" to "Thêm"
        ,"Rename category" to "Đổi tên danh mục"
        ,"All" to "Tất cả"
        ,"Frequent" to "Thường dùng"
        ,"Delete Selected Stickers" to "Xóa sticker đã chọn"
        ,"Retry" to "Thử lại"
        ,"Enable Quick Stickers" to "Bật Sticker nhanh"
        ,"Your library is empty" to "Thư viện của ngài đang trống"
        ,"Could not load your on-device sticker library." to "Không thể tải thư viện sticker trên thiết bị."
        ,"Library Layout" to "Bố cục thư viện"
        ,"Choose how stickers are displayed" to "Chọn cách hiển thị sticker"
        ,"Compact Grid" to "Lưới gọn"
        ,"4 columns, high density previews" to "4 cột, xem trước dày hơn"
        ,"Standard Grid" to "Lưới tiêu chuẩn"
        ,"3 columns, balanced card size" to "3 cột, kích thước cân đối"
        ,"Large Grid" to "Lưới lớn"
        ,"2 columns, prominent cards with titles" to "2 cột, thẻ lớn kèm tiêu đề"
        ,"Full-width rows with details and tags" to "Hàng toàn chiều rộng kèm chi tiết và tag"
    )[source] ?: source
}

fun stringsFor(language: AppLanguage): StickHubStrings = when (language) {
    AppLanguage.ENGLISH -> StickHubStrings(
        locale = AppLanguage.ENGLISH,
        settings = "Settings",
        settingsSubtitle = "Personalize your sticker studio",
        backToLibrary = "Back to Library",
        general = "General",
        quickStickers = "Quick Stickers",
        library = "Library",
        backupPrivacy = "Backup & Privacy",
        appearance = "APPEARANCE",
        colorTheme = "Color theme",
        themeMode = "Theme mode",
        language = "Language",
        english = "English",
        vietnamese = "Tiếng Việt",
        libraryLayout = "Library layout",
        quickStickersSection = "QUICK STICKERS",
        availability = "Availability",
        appearanceSection = "Appearance",
        presets = "Presets",
        bubble = "Bubble",
        popupComposition = "Popup composition",
        stickerClarity = "Sticker clarity",
        openingBehavior = "Opening behavior",
        popupContent = "Popup content",
        whatsappStickers = "WHATSAPP STICKERS",
        cloudBackup = "CLOUD BACKUP",
        libraryAndData = "LIBRARY & DATA",
        privacy = "PRIVACY",
        manageCategories = "Manage categories",
        categoriesAvailable = { count -> "$count categories available" },
        export = "Export",
        import = "Import",
        localFirstEncrypted = "Local-first & encrypted",
        light = "Light",
        dark = "Dark",
        system = "System",
        grant = "Grant",
        done = "Done",
        cancel = "Cancel"
    )
    AppLanguage.VIETNAMESE -> StickHubStrings(
        locale = AppLanguage.VIETNAMESE,
        settings = "Cài đặt",
        settingsSubtitle = "Cá nhân hóa không gian sticker",
        backToLibrary = "Quay lại thư viện",
        general = "Chung",
        quickStickers = "Sticker nhanh",
        library = "Thư viện",
        backupPrivacy = "Sao lưu & riêng tư",
        appearance = "GIAO DIỆN",
        colorTheme = "Chủ đề màu",
        themeMode = "Chế độ giao diện",
        language = "Ngôn ngữ",
        english = "English",
        vietnamese = "Tiếng Việt",
        libraryLayout = "Bố cục thư viện",
        quickStickersSection = "STICKER NHANH",
        availability = "Khả dụng",
        appearanceSection = "Hiển thị",
        presets = "Preset",
        bubble = "Nút nổi",
        popupComposition = "Thành phần popup",
        stickerClarity = "Độ rõ sticker",
        openingBehavior = "Cách mở popup",
        popupContent = "Nội dung popup",
        whatsappStickers = "STICKER WHATSAPP",
        cloudBackup = "SAO LƯU ĐÁM MÂY",
        libraryAndData = "THƯ VIỆN & DỮ LIỆU",
        privacy = "RIÊNG TƯ",
        manageCategories = "Quản lý danh mục",
        categoriesAvailable = { count -> "$count danh mục khả dụng" },
        export = "Xuất dữ liệu",
        import = "Nhập dữ liệu",
        localFirstEncrypted = "Ưu tiên cục bộ & mã hóa",
        light = "Sáng",
        dark = "Tối",
        system = "Theo hệ thống",
        grant = "Cấp quyền",
        done = "Xong",
        cancel = "Hủy"
    )
}
