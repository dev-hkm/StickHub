# ANTIGRAVITY — StickHub 5.0: implementation handoff

Bạn chịu trách nhiệm triển khai, kiểm thử, sửa lỗi và giao APK của đợt nâng cấp này. Không chỉ trả kế hoạch. Đọc toàn bộ tài liệu trước khi sửa code.

## 1. Mục tiêu và quyền thực hiện

Nâng StickHub từ bản cơ bản đang dùng được thành một bản major release ổn định, chỉn chu, có chất lượng tương tác tốt. Ưu tiên dữ liệu người dùng, Quick Stickers popup, Settings, clipboard, cutout và thư viện. Cần hoàn thiện hành vi thật, không chỉ sửa hình thức hoặc thêm animation cho có.

Người dùng đã giao quyền tự quyết và yêu cầu không hỏi họ phải làm gì tiếp. Hãy tự giải quyết chi tiết kỹ thuật trong phạm vi dưới đây. Không hỏi người dùng sửa code, chạy lệnh, thiết lập emulator hoặc dùng ADB.

Đây là tiếp nối audit của Codex, KHÔNG phải bắt đầu từ một repo sạch. Người dùng dừng Codex vì quota, không phải vì họ yêu cầu xóa các thay đổi đang dở. Giữ và hoàn thiện những phần có giá trị; không coi các helper mới là tính năng đã tích hợp.

## 2. Trạng thái chính xác khi bàn giao

Workspace: `C:\Users\ASUS\AndroidStudioProjects\StickHub`.

- Branch: `codex/v5-quality`.
- Phiên bản app vẫn là `3.2.2`, versionCode `44`.
- Commit chức năng cuối trước đợt này: `7f88f4b`.
- Tag quay lại trước v5: `checkpoint/pre-v5-3.2.2`.
- HEAD: `bfca0cc`, chỉ commit tài liệu thiết kế/kế hoạch, KHÔNG phải commit hoàn thành v5.
- Đã có hai tài liệu: `docs/superpowers/specs/2026-09-04-v5-quality-design.md` và `docs/superpowers/plans/2026-09-04-v5-quality.md`.
- `03.md` đến `09.md` là tài liệu thiết kế của người dùng, chưa được Git theo dõi. Không xóa, ghi đè hoặc tự áp dụng thêm theme từ chúng trong đợt này.

Codex mới làm các việc sau, phần lớn CHƯA COMMIT:

1. Thêm vào `app/build.gradle.kts`:
   - `implementation("androidx.recyclerview:recyclerview:1.4.0")`;
   - `testImplementation("org.robolectric:robolectric:4.16.1")`;
   - `unitTests.isIncludeAndroidResources = true`.
2. Thêm regression tests, sửa ba test file overlay hiện hữu.
3. Thêm các helper/prototype bên dưới. Các luồng production cũ vẫn chưa được nối vào chúng:
   - `data/cutout/CutoutModelInputPlan.kt`;
   - `data/cutout/CutoutRequestGate.kt`;
   - `data/cutout/CutoutSaveSession.kt`;
   - `ui/library/CategoryDragSession.kt`;
   - `ui/settings/SliderInteractionState.kt` — chứa cả `PreviewRateLimiter`;
   - `service/OverlayAppearancePreset.kt`;
   - `service/OverlayAppearanceState.kt`;
   - `service/OverlayThumbnailPolicy.kt`;
   - `service/OverlayStickerAdapter.kt`.

Các file `StickerRepository`, `BackupHelper`, `OverlayService`, `StickHubApp`, `SettingsScreen`, `SubjectCutoutProcessor`, `SubjectCutoutSheet`, `CategoryChips` vẫn cần triển khai các sửa chữa chính. KHÔNG nói các lỗi đã được sửa chỉ vì tìm thấy helper tương ứng.

Codex đã dừng tác vụ Gradle đang chạy khi người dùng đổi yêu cầu. Chưa có kết quả hoàn tất cho regression suite mới, chưa build/giao APK v5. Một lần chạy trước đó lỗi biên dịch do helper được bổ sung sau khi compiler đã bắt đầu; một lỗi test gọi `RuntimeEnvironment.getApplication<T>()` đã được sửa thành `getApplication()`. Phải chạy lại hiện trạng, không dùng báo cáo XML cũ làm bằng chứng mới.

Baseline lint trên mã cũ đã thực sự báo **7 errors, 103 warnings, 3 hints**. Báo cáo tại `app/build/intermediates/lint_intermediate_text_report/release/lintReportRelease/lint-results-release.txt`. Không tạo baseline/suppress toàn bộ để che chúng.

## 3. Ràng buộc sản phẩm — không được phá

- Giữ applicationId `com.hkm.stickhub`, signing identity hiện tại, database và toàn bộ sticker đã lưu. Người dùng phải update APK đè lên bản cũ, không uninstall/clear data.
- DB là **SQLiteOpenHelper**, không phải Room. `data/db/StickHubDbHelper.kt`, schema hiện tại v5. Không chuyển sang Room, không destructive migration.
- App screens dùng Kotlin + Jetpack Compose Material 3. Popup hiện dùng Android Views/WindowManager: giữ cấu trúc đó; chỉ thay grid bằng RecyclerView, không rewrite toàn overlay.
- Giữ 19 visual themes và light/dark/system mode. Không thêm theme 20, không thay icon/asset của người dùng.
- Lucide icons, không emoji. Dùng theme tokens; không hardcode tím hoặc nền trái theme.
- Giữ cả clipboard import trực tiếp từ Photos và cut subject riêng. Không gộp hai luồng, không bắt clipboard đi qua ML.
- Chạm sticker vẫn copy. Giữ favorites, category, reorder, layout modes, preset/default filter và after-copy behavior đang có.
- Giữ app local-first, không thêm tài khoản, analytics, cloud runtime, permission không cần thiết. Chưa làm share sticker pack.
- Không nâng hàng loạt dependencies/SDK để “mới nhất”. Stack hiện đã mới; chỉ thêm dependencies có lý do cụ thể.
- Không dựng Debug APK, không emulator, không ADB. JVM/Robolectric tests trên máy tính được dùng.

## 4. Cách triển khai và quản lý checkpoint

1. Đọc `git status`, `git diff`, helper/tests mới và production liên quan. Không reset/checkout đè working tree.
2. Tạo checkpoint rõ ràng cho trạng thái đang dở sau khi kiểm tra không có secrets/artifacts; chỉ stage file thuộc công việc, không `git add .` mù quáng. Ghi rõ đây là WIP test/helpers, không phải bản dùng được.
3. Thực hiện theo thứ tự: **data safety → overlay → cutout/library → Settings/lifecycle → integration/release**.
4. Mỗi khối phải chạy test liên quan, xem diff, rồi commit checkpoint có ý nghĩa. Không chỉ commit cuối cùng.
5. Viết regression trước hoặc chạy các regression đã có trên code chưa sửa để xác nhận chúng thật sự bắt lỗi. Sau đó sửa và chạy lại. Test draft cũng cần review: sửa fixture sai, không xóa assertion chỉ để xanh.
6. Không để helper không được production sử dụng rồi báo hoàn thành. Từng helper phải được nối vào luồng thật hoặc loại bỏ có giải thích nếu chọn giải pháp tốt hơn.

## 5. P0/P1 — bảo vệ dữ liệu và lưu ảnh

### 5.1. Commit thành công nhưng catch lại xóa ảnh

Root cause trong `data/repository/StickerRepository.kt`:

- `saveStickerBitmap`: insert DB khoảng dòng 142, gọi suspend `refresh()` khoảng 148, catch khoảng 150–152 xóa finalFile.
- `saveStickerFromStreamInternal` có cùng cấu trúc khoảng 344–355.
- Nếu refresh lỗi hoặc coroutine bị hủy SAU insert, DB giữ row nhưng file đã bị xóa.

Triển khai:

- Phân biệt rõ file tạm chưa commit và file đã thuộc sở hữu DB.
- Chỉ dọn file chưa commit. Snapshot publication lỗi không được phá dữ liệu đã commit.
- Rethrow `CancellationException`, không biến hủy thành “save failed” rồi xóa file.
- Phần file promotion + DB commit cần nhất quán; không gọi suspend có thể hủy vào giữa trạng thái nửa commit nếu không có rollback rõ ràng.
- Trả kết quả dựa trên bản ghi đã lưu, không phụ thuộc việc tìm lại nó trong một StateFlow có thể chưa refresh.
- Đảm bảo refresh/snapshot concurrent không đưa dữ liệu cũ đè snapshot mới.

### 5.2. Studio overwrite không được ghi trực tiếp đè file gốc

Root cause: `overwriteStickerBitmap` fallback `copyTo(originalFile, overwrite=true)`. ENOSPC/lỗi I/O có thể cắt hỏng ảnh cũ. Đồng thời ghi PNG vào đường dẫn `.webp` làm sai định dạng.

Chốt hướng: copy-on-write. Ghi PNG mới hoàn chỉnh vào file mới, kiểm tra kết quả/hash, cập nhật file_path/hash trong DB transaction, chỉ dọn ảnh cũ sau commit an toàn. Khi bất kỳ bước trước commit lỗi, giữ nguyên bytes/path cũ. Xem xét URI clipboard còn được ứng dụng khác đọc; không xóa file cũ trước khi có chiến lược tương thích rõ ràng.

### 5.3. Category phải tồn tại

General có thể rename/delete qua UI nhưng save/import vẫn hardcode General. Resolve category từ DB ngay trong repository: ưu tiên category yêu cầu nếu còn tồn tại, sau đó default còn tồn tại; nếu không còn category nào, tạo fallback nhất quán. Không tạo sticker mang category “ma”. Áp dụng cho bitmap save, clipboard, edit metadata và restore. Không cấm ngược thao tác xóa/rename đang được người dùng sử dụng nếu có thể giữ bằng fallback đúng.

### 5.4. Một repository chung trong process

Thêm factory `StickerRepository.getInstance(context.applicationContext)` với khởi tạo thread-safe. MainActivity và OverlayService cùng dùng instance này để share StateFlows, DB owner và mutex dedup. Giữ constructor hoặc factory riêng phù hợp cho test isolation; có close cho test nếu cần. Không retain Activity/Service context. Không service.close() instance đang được Activity dùng.

## 6. Backup phải khôi phục đúng những gì đã xuất

Root causes trong `util/BackupHelper.kt` và `StickerRepository.restoreSticker`:

- Import giới hạn 500 ZIP entries, export không đối xứng: 500 sticker + metadata = 501 entries nên backup tự xuất không nhập lại được.
- Metadata `readText()` không giới hạn, entry lạ không tính ngân sách giải nén.
- Không giữ manual sort order.
- Dedup chỉ theo image hash làm mất hai sticker cùng ảnh nhưng title/category khác.
- Restore luôn đặt `.png` dù giữ nguyên bytes WebP/JPEG/GIF.
- Mutation từng phần trước khi xác thực toàn gói; lỗi giữa chừng vẫn trả count và UI báo “Merged”. Nhập lại toàn duplicates bị báo như file không hợp lệ.

Chốt thiết kế:

1. ZIP format v3, vẫn đọc backup v1/v2. Không cần đổi DB schema chỉ để đổi format backup.
2. Serialize thứ tự ổn định, metadata đầy đủ, basename/format, hash kiểm tra. Không mất favorites, tags, usageCount, createdAt, category order.
3. Dedup phải giữ multiplicity: trong một archive có hai row giống bytes vẫn phải bảo toàn nếu đó là hai sticker khác nhau. Merge lại cùng archive phải idempotent. Có thể match metadata+hash dưới dạng multiset, không dùng một set hash đơn giản.
4. Dùng một budget giải nén cho mọi entry; metadata có trần riêng (1 MiB là mốc regression hiện có), ảnh và tổng archive có trần rõ ràng. Mốc đề xuất cho thư viện hỗ trợ: tối đa 10.000 sticker, 32 MiB/ảnh, 1 GiB tổng bytes giải nén; metadata thực tế vượt trần phải fail rõ ràng ở export trước khi báo thành công. Có thể điều chỉnh giới hạn nếu test/memory cho thấy cần, nhưng export/import phải cùng contract.
5. Chặn zip slip, basename bất hợp lệ, duplicate ZIP entry, metadata trùng, manifest lỗi, hash mismatch, missing referenced image. Không decode toàn archive vào RAM.
6. Parse + validate + stage trước mutation. Bulk restore trong DB transaction; rollback các file mới nếu DB lỗi. Thư viện cũ luôn nguyên vẹn. Không tạo category/sắp xếp lại category hiện có trước khi biết archive hợp lệ.
7. Export preflight và stage trước khi ghi đích; thiếu file nguồn không được âm thầm xuất thiếu rồi báo success. Lỗi stream đích phải báo thất bại; không hứa remote document provider có atomic rename nếu không hỗ trợ.
8. MIME phải khớp bytes/format. Đồng bộ provider và clipboard cho PNG, WebP, JPEG, GIF, HEIC/HEIF nếu import hỗ trợ. Không đổi extension suông.
9. API kết quả đề xuất:

```kotlin
sealed interface BackupImportResult {
    data class Success(val imported: Int, val alreadyPresent: Int) : BackupImportResult
    data class Invalid(val reason: String) : BackupImportResult
    data class Failed(val reason: String) : BackupImportResult
}
// BackupHelper.importBackupDetailed(context, uri, repository): BackupImportResult
```

Giữ wrapper cũ nếu cần tương thích callers, nhưng UI mới dùng structured result. `Success(0, n)` là “Already in your library”, không phải lỗi. Không nuốt cancellation.

## 7. Popup — lifecycle, rotation và hiệu năng

### 7.1. Tương thích Android và permissions

- `OverlayService.startForegroundServiceNotification` đang tạo NotificationChannel không guard; app minSdk24 nhưng API này yêu cầu26. Guard channel creation, vẫn có NotificationCompat cho API24/25.
- Bao quát lỗi startForeground/addView/updateViewLayout/removeView và permission bị thu hồi. Không để `isRunning=true` khi start thất bại. Không restart loop khi đã mất overlay permission; trả trạng thái không sticky phù hợp.
- Đóng service phải cancel job/debounce/reveal/animation, tháo views một lần, dọn listener và cache thực sự đang dùng. Không swallow cancellation.
- Khi Activity resume, phản ánh trạng thái permission/service thực, không chỉ giá trị remember ban đầu.

### 7.2. Geometry phải dùng cùng một nguồn tính toán

- Hiện `reflowOverlayViews` chỉ xử lý panel đang mở. Xoay lúc popup đóng rồi mở lại dùng kích thước cũ.
- Xoay khi panel mở chỉ đổi bounds, không tính lại cell widths.
- `OverlayLayoutPolicy` có thể trả minimum lớn hơn viewport.

Tạo một đường sync geometry dùng chung cho mở panel, đổi orientation/display bounds, resize và thay chrome. Cập nhật cả panel detached. Clamp effective min vào available viewport trước khi clamp requested size, cho cả width/height/bubble. Dùng đúng insets/display bounds; giữ fractional bubble position; không cho close/resize rơi ra ngoài màn hình.

Padding, grid content width, chrome visibility và minimum panel height phải được tính từ cùng snapshot hiện tại. Không capture minimum height cũ vĩnh viễn trong touch listener. Kiểm tra cả 8 tổ hợp title/search/category visibility. Hiding search phải cancel pending debounce, clear query VÀ EditText đồng bộ; không cho query ẩn lọc tiếp.

### 7.3. Tích hợp RecyclerView thật

Thay riêng `NestedScrollView + GridLayout` bằng RecyclerView + GridLayoutManager. Dùng/hoàn thiện `OverlayStickerAdapter.kt` đã tạo; helper này hiện CHƯA được service gọi.

- Toàn bộ sticker phải được adapter nhận, không `take(n)`/hard limit cắt thư viện.
- Chỉ load thumbnail cho ô đang attach/near viewport; concurrency bounded, hiện prototype dùng Semaphore(2).
- Stable IDs; khi reuse/detach/filter/close phải cancel request và kiểm tra generation trước gán ảnh.
- Capture render options bất biến cho mỗi request. Cache key gồm file identity/version, size, shadow, theme, density.
- Cancel trong lúc decode/shadow không được leak bitmap mới tạo. Không recycle bitmap đã đưa vào shared cache hoặc đang được ImageView khác dùng.
- Kiểm tra prototype adapter chỗ `notifyDataSetChanged`, holder tracking và cleanup. Dùng diff/payload khi đáng giá, không reset cả grid/scroll chỉ vì usageCount tăng sau copy.
- Đổi opacity tuyệt đối không reload ảnh. Đổi shadow chỉ invalidation/rebind cần thiết lúc release; giữ scroll anchor.
- Xử lý ảnh thiếu/hỏng bằng placeholder rõ ràng, không crash.
- Dọn cache không dùng; cache budget không đủ để chứng minh tổng bộ nhớ bounded nếu vẫn giữ ảnh ở mọi view.

### 7.4. Races/filter/interaction

- Opening refresh hiện có thể ghi đè filter user vừa chọn. Resolve initial filter trước tương tác hoặc guard bằng panel generation + selection revision. Hủy open-refresh cũ khi đóng/mở phiên khác.
- Frequent phải sort usageCount giảm dần, ties ổn định; hiện mới lọc `usageCount>0` mà giữ manual order.
- Khi tag row ẩn, vẫn tôn trọng default/last-used filter đã cấu hình; filter mất category phải fallback an toàn. Không để hai đường code một đường ép All, đường kia ghi đè lại.
- Nhấn X đóng; giữ X rồi kéo di chuyển, rung nhẹ một lần nhận long-press. Không click-close sau một drag. Hỗ trợ accessibility click và mô tả hành động.
- Giữ icon/viền mỏng nhưng mở rộng touch target trong vùng popup hợp lý. Kiểm tra ACTION_CANCEL/multitouch và orientation giữa gesture.
- Outside touch phải có hành vi focus/keyboard rõ ràng; flag WATCH_OUTSIDE_TOUCH không tự xử lý. Không biến vùng trong suốt thành lời hứa click-through toàn màn hình nếu WindowManager không hỗ trợ an toàn.

## 8. Opacity và Settings — live preview thật, không giật toàn screen

Root cause hiện tại: `StickHubApp.kt` khoảng 1446–1496 truyền các `onLivePreview... = { }`. Slider chỉ apply khi thả. Mỗi preview state lại nằm ở SettingsScreen lớn, có nguy cơ kéo một slider làm recompose cả section đồ sộ.

Chốt contract service:

```text
ACTION_PREVIEW_APPEARANCE = com.hkm.stickhub.PREVIEW_APPEARANCE
EXTRA_APPEARANCE_LAYER = appearance_layer
EXTRA_APPEARANCE_VALUE = appearance_value
layers: bubble, master, surface, stickers, chrome, close, resize
```

- Intent preview chứa layer/value, chỉ cập nhật transient `OverlayAppearanceState`; không ghi SharedPreferences lúc drag.
- Root throttle tối đa khoảng 30 Hz bằng `PreviewRateLimiter` hoặc latest-value coalescer tương đương. Không dùng Compose mutableState cho timestamp throttle vì gây recompose không cần thiết.
- Release luôn persist chính xác giá trị cuối, không dựa vào closure của frame cũ. Gửi ACTION_UPDATE_APPEARANCE để clear preview và apply committed state.
- Nếu gesture bị hủy/rời Settings/background, không để transient preview kẹt mãi: restore committed values. Callback cuối/queued preview không được chạy sau commit rồi đè lại.
- `SliderInteractionState` bảo vệ direct manipulation trước parent echoes; phải tích hợp vào slider dùng thật. State của mỗi slider và text phần trăm nên nằm trong composable nhỏ, không nâng toàn bộ preview lên root screen.
- Accessibility semantics: tên slider, đơn vị/phần trăm, state/disabled reason; thao tác bằng accessibility cũng phải persist, không chỉ pointer-up.
- Opacity = master × layer, không nhân alpha ở root thêm lần nữa. Surface=0 không làm sticker mờ; master=0 mới làm cả popup mờ. Bubble độc lập.
- Chặn NaN/infinity ở boundary/prefs/policy, fallback hữu hạn an toàn; `coerceIn` đơn thuần không xử lý NaN.
- Shadow là thao tác nặng, chỉ apply lúc release. Copy UI phải nói rõ, không giả vờ live preview nếu chưa làm.
- Reveal controls tồn tại dưới dạng deadline 5 giây: mở panel/đổi setting/kéo X trong thời gian đó vẫn thấy controls. Hết thời gian thì quay về saved appearance. Preview không được phá recovery.

### Presets và cấu trúc Settings

Tích hợp ba preset opt-in đã có trong `OverlayAppearancePreset`: Balanced, Floating stickers, Discreet. Áp dụng atomic các opacity/shadow trong một edit, cập nhật UI/service một lượt; KHÔNG đổi size, position, filter, sticker hay theme. Floating giữ sticker 100%, nền0%, có shadow và close/resize vẫn tìm được. Không tự áp preset khi update app; giữ toàn bộ giá trị custom cũ.

Settings vẫn là screen một lớp nền; giữ transition đang dùng tốt, không biến lại thành sheet. Giữ status/nav bars hòa vào theme. Có thể tách composable sections và thêm quick-jump tới Appearance/Quick Stickers/Library & Data để giảm cuộn; không thay toàn bộ visual design.

Các toggle chỉ phát haptic một nơi. Hiện Settings toggle overlay và root toggleOverlay đều có thể rung: bỏ duplicate owner. Giữ tiered haptics đang có; không thêm VIBRATE waveform mạnh. Slider không rung mỗi pixel, chỉ tick nhẹ khi hoàn tất/điểm mốc hợp lý.

## 9. Clipboard, long operations và lifecycle

- Thay polling mỗi2s không phụ thuộc lifecycle bằng listener + kiểm tra ngay khi RESUMED. Tháo listener khi pause/dispose. Đọc/validate stream off-main nhưng truy cập lifecycle/Compose state đúng thread.
- Không mất offer mới khi picker đang mở; cần phân biệt clipboard event mới, resume lại cùng clip và clip đã consume. API24/25 không có timestamp API26: guard và có fallback/event revision rõ ràng.
- Own-provider clip vẫn không được offer import ngược. Không phá dedup hash.
- Khóa thao tác import/export đang chạy: repeated tap không tạo nhiều job. Hiển thị trạng thái tiến trình thật, completion/failure/duplicate counts rõ ràng.
- Dùng ViewModel hoặc lifecycle owner thích hợp cho clipboard/backup operations cần sống qua rotation. Không giữ Activity trong singleton; không dùng GlobalScope. Đừng để xoay màn hình hủy nửa restore hoặc reset tiến trình nhưng job vẫn chạy.
- Có error state và retry tại nguồn thao tác, không chỉ printStackTrace. Không báo success khi một phần chưa commit. UI không hiển thị “library empty” thay cho loading/failure.
- Chỉ refresh/reconcile cần thiết khi resume, không decode/hash lại toàn thư viện mỗi lần chạm tab.
- Nếu start overlay yêu cầu permission: hướng dẫn rõ ràng, xử lý quay lại sau cấp/từ chối, không show bật thành công trước khi service thực sự chạy. Không tự bật lại khi người dùng chủ động tắt.

## 10. Cut subject — save, cancellation và kích thước model

### 10.1. Save đúng một lần, thất bại không mất kết quả

`SubjectCutoutSheet` hiện không có saving state; callback không suspend. Root launch mỗi lần tap và luôn đóng sheet kể cả save thất bại.

Đổi contract:

```kotlin
onSaveSticker: suspend (Bitmap, String, String, String) -> Boolean
```

Sheet dùng `CutoutSaveSession` hoặc state tương đương: một save tại một thời điểm, disabled Save/Close/Back/change image trong đoạn commit, loading rõ ràng. Failure giữ candidate/title/category/tags và hiện lỗi retry. Success mới dismiss. Root không tự clear activeUri khi failure. Giải phóng bitmap đúng owner, không recycle khi repository còn đang compress. Nếu rotation hủy sheet, xử lý cancellation/storage an toàn; không khẳng định UI-retention nếu chưa thực hiện.

### 10.2. Chặn kết quả cũ

`SubjectCutoutProcessor` hiện check requestId trước CPU extraction rồi publish CandidatesReady sau extraction mà không recheck. Dùng `CutoutRequestGate` xuyên suốt; kiểm tra trước mỗi state publication sau suspension/CPU; ensureActive trong vòng pixel. Request bị thay thế phải dispose candidates/resources không còn dùng. Không để ảnh A trả kết quả vào phiên ảnh B.

### 10.3. Ảnh dài/hẹp

Hiện scale cạnh ngắn lên512 không giới hạn cạnh dài. Ảnh 2048×16 có thể thành65536×512. Bitmap allocation còn nằm ngoài try.

Tích hợp `CutoutModelInputPlan`: giữ aspect, giới hạn cạnh dài2048, dùng padding để model canvas tối thiểu512 thay vì phóng vô hạn. Đặt allocation trong khối có error handling. Ánh xạ subject bounds/mask loại bỏ padding về tọa độ ảnh gốc chính xác; chỉ thêm canvas mà không đổi mapping là sai. Output vẫn normalize sticker theo canvas đang có, crop sát chủ thể, không giữ tỷ lệ nhỏ xíu trong ảnh gốc. Test ảnh cực ngang/dọc, nhỏ, nhiều subject, near-edge subject và mask rỗng.

Thêm cách chọn Subject 1…N bằng thumbnail/button có semantics cho TalkBack/keyboard; giữ chọn trực tiếp bằng touch trên ảnh.

## 11. Library interaction và chi tiết

- `CategoryChips.pointerInput(filters)` dùng list thứ tự thay đổi làm key. Swap đầu đổi key → restart gesture → onDragCancel lại persist dở. Đổi key ổn định, dùng updated state cho callbacks/data; tích hợp `CategoryDragSession` và offset compensation. Cancel rollback, end mới commit. General ghim đầu theo contract repository; không cho drag nhìn như reorder được nhưng lưu lại nhảy về.
- Test một lần giữ kéo qua ít nhất3 tag, kéo ra mép, cancel, external category delete giữa drag, restart app kiểm thứ tự.
- Thư viện đang tính draggedOffset chủ yếu để hit-test; sticker không nhất thiết đi theo ngón tay. Nối transform/zIndex cho item đang drag, compensate slot movement, không animate vị trí trực tiếp dưới ngón tay bằng tween gây trễ. Những ô khác animate placement. Auto-scroll dựa frame/edge distance, không spam write DB mỗi pointer event.
- Các layout ngoài standard cần reachable long-press/select/details, không có callback nhưng không nối. Giữ tap-to-copy và không cho drag phát copy khi nhấc tay.
- Detail sheet đang giữ snapshot StickerItem nên favorite icon không cập nhật. Resolve selected ID từ flow hiện tại; form state keyed theo sticker.id, không theo cả object, tránh favorite/usage refresh xóa draft title/tags.
- Semantics selected/role cho chips, checkbox/selection và accessible Details/Select actions. Controls nhỏ về hình thức vẫn có hit target dùng được.
- Không đổi theme chỉ để sửa layout. Không thêm animation vô hạn cho mọi sticker; cache/scroll performance quan trọng hơn hiệu ứng phô diễn.

## 12. Lint, security và compatibility

Các baseline errors cụ thể:

1. `BitmapDecodeUtil.kt` khoảng137: `MediaStore.openFileDescriptor` cần API36 hoặc R extension15. Check SDK extension đúng API theo tài liệu; Android>=33 đơn thuần không đủ. Giữ fallback ContentResolver phù hợp.
2. `ClipboardHelper.kt` khoảng70: ClipDescription.timestamp cần26, guard cho min24.
3. NotificationChannel trong OverlayService: bốn NewApi errors liên quan API26.
4. `StickHubApp.kt` khoảng720: Scaffold innerPadding không được dùng. Sửa inset ownership thực sự, không chồng double padding/status/nav hoặc suppress cho qua. App đang dùng edge-to-edge nên kiểm toàn bộ route.

Provider đang exported=true và grantUriPermissions=true. Audit quyền read của ứng dụng khác; ưu tiên exported=false + temporary URI grants theo chuẩn Android nếu clipboard/share contract được bảo toàn. Không đổi mù quáng làm các bàn phím/app chat không đọc được sticker. Giữ canonical path validation, chỉ read mode, không lộ DB/file ngoài stickers. Test URI permission contract và ghi rõ giới hạn kiểm chứng nếu không có thiết bị thật.

Không log hình ảnh, URI nhạy cảm, credentials hay signing passwords. Không thêm INTERNET/runtime network chỉ để phục vụ upload APK; release upload là tooling ngoài app.

## 13. Test gates bắt buộc

Đọc và hoàn thiện các test mới, đặc biệt:

- `DataSafetyRegressionTest` — postcommit/cancellation, failed overwrite, General rename, duplicate metadata/order, WebP MIME, missing image, oversized metadata,500 sticker roundtrip, DB failure giữa restore, thiếu file export;
- `DatabaseUpgradeIntegrationTest` — fixture SQLite v1…v5 giữ IDs/metadata;
- `CutoutModelInputTest`, `CutoutModelInputPlanTest`, `CutoutRequestGateTest`, `CutoutSaveSessionTest`;
- `CategoryDragSessionTest`;
- `OverlayLayoutPolicyTest`, `OverlayStickerFilterTest`, `OverlayOpacityPolicyTest`;
- `OverlayAppearanceStateTest`, `OverlayThumbnailPolicyTest`, `OverlayServiceIntegrationTest`, `OverlayStickerAdapterTest`;
- `SettingsInteractionPolicyTest`.

Robolectric dùng SDK34 cho đa số integration tests; API25 cho regression NotificationChannel nếu test tương ứng đặt vậy. Không tùy tiện test SDK37 nếu Robolectric4.16.1 chưa hỗ trợ. Tests phải exercise code dùng thật; không tạo bản mock thuật toán ở trong test rồi assert chính bản đó.

Chạy PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon --max-workers=1 :app:testDebugUnitTest
.\gradlew.bat --no-daemon --max-workers=1 :app:lintRelease
.\gradlew.bat --no-daemon --max-workers=1 :app:assembleRelease
```

`testDebugUnitTest` là tên task JVM hiện có, KHÔNG phải yêu cầu assembleDebug. `testReleaseUnitTest` không tồn tại trong cấu hình hiện tại, đừng mất thời gian gọi lại. Không build APK Debug.

Lượt verification cuối cần thực thi tests mới thật sự, không chỉ UP-TO-DATE từ báo cáo cũ. Đọc số tests/failures/errors và exit code. Lint errors=0, triage warning rủi ro thực. Không claim smooth60fps, update-install trên điện thoại hoặc toàn bộ19 theme đã visual QA nếu chưa có bằng chứng. Ghi rõ phần device-only chưa kiểm nhưng không bắt người dùng dựng emulator.

## 14. Release, upload và notification — thuộc phạm vi task

Sau khi implementation/test/lint đạt:

1. Bump `versionName="5.0.0"`, versionCode lớn hơn44 và lớn hơn mọi bản hiện tại nếu repo đã đổi. Giữ applicationId/signing identity.
2. Build signed Release với HKM Keystore tại `D:\Downloads\Web Projects\HKM Keystore`; dùng signing credentials người dùng đã cung cấp/cấu hình riêng. Gradle hiện đọc env `STICKHUB_STORE_FILE`, `STICKHUB_STORE_PASSWORD`, `STICKHUB_KEY_ALIAS`, `STICKHUB_KEY_PASSWORD`, hoặc file signing properties đã ignore. Không ghi credentials vào source/prompt/changelog/log.
3. Verify chữ ký bằng apksigner và package/version từ APK; không giao unsigned APK. Tính SHA-256, ghi tên và kích thước artifact.
4. Upload APK hoàn chỉnh lên **Personal R2**, bucket `filesend`, ưu tiên tuyệt đối. Public base: `https://pub-66aa1ff95abc4f6198b4ab5def83a4a0.r2.dev`. Dùng credentials đã được người dùng cấp qua kênh/cấu hình bí mật, không lặp lại chúng trong báo cáo.
5. Chỉ coi upload thành công sau khi verify public URL tải được, size/hash phù hợp artifact. Không dùng response200 của API upload làm bằng chứng duy nhất cho public link.
6. Chỉ nếu R2 thật sự thất bại sau chẩn đoán hợp lý mới fallback storage.to. Đọc docs hiện hành, workflow Init → upload presigned URL → Confirm → lấy file.url. Không tự đoán API/multipart.
7. Sau khi có link cuối cùng hợp lệ, gửi **đúng một ntfy** tới topic `filesend`, gồm tên APK, link và changelog ngắn nhưng đầy đủ. Không gửi secrets, không gửi nhiều message tiến độ.
8. Commit checkpoint hoàn thành, ghi version/artifact/checks vào release notes. Không commit APK, keystore, credentials, build output.

Nếu thật sự không có quyền truy cập signing/upload credentials trong môi trường của bạn: kiểm tra cấu hình bí mật hiện hữu trước, không bịa link hoặc bỏ qua delivery rồi báo xong. Báo chính xác ranh giới bị chặn và giữ APK/code an toàn. Tuyệt đối không tìm lại secrets bằng cách in toàn bộ lịch sử chat, env hoặc thư mục bí mật vào log.

## 15. Báo cáo cuối cho người dùng

Trả kết quả ngắn, bằng tiếng Việt:

- Link APK signed Release đã verify.
- Các cải thiện thực sự đã nối vào app, nhóm theo data/popup/Settings/cutout/library.
- Kết quả tests/lint/build, số test và lỗi còn lại nếu có.
- Checkpoint cuối và tag trước v5 để fallback source. Không hướng dẫn downgrade APK bằng uninstall làm mất dữ liệu.
- Device-only checks chưa thực hiện, nói đúng mức độ kiểm chứng.
- Xác nhận R2 hoặc fallback nào đã dùng và ntfy đã gửi một lần.

Đừng kết thúc ở “đã thêm helper”, “đã lập kế hoạch”, “build có vẻ ổn” hoặc “người dùng tự build”. Hoàn thiện các vertical flows bên trên, verify, checkpoint và giao artifact.
