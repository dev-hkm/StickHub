/* ==========================================================================
   StickHub Landing Page — Vintage & Warm Scrapbook Interactive Engine
   Bilingual Support (VI/EN) with Auto-Detection & Interactive Simulator
   ========================================================================== */

(function () {
  'use strict';

  // ==========================================================================
  // 1. Bilingual Dictionary (Tiếng Việt & English)
  // ==========================================================================
  const translations = {
    vi: {
      langBtn: '🇻🇳 Tiếng Việt',
      langToggleTooltip: 'Chuyển sang English',
      pageTitle: 'StickHub — Sổ Sticker Nổi Cho Android | Giữ Trọn Cuộc Trò Chuyện',
      metaDesc: 'StickHub mang cả cuốn sổ sticker & meme yêu thích bay lượn trên Messenger, Telegram, Zalo, WhatsApp. Chạm nhẹ là dán, ấm áp và không gián đoạn!',
      
      // Header
      navCompare: 'Chuyện Cũ & Mới',
      navFeatures: 'Tính Năng',
      navGuide: 'Cách Cài',
      navFaq: 'Tâm Sự & FAQ',
      btnHeaderDownload: 'Tải APK',

      // Hero
      heroBadge: 'Bản Cập Nhật v5.2.5 • Sổ Tay Android Mến Yêu',
      heroTitle: 'Kho Sticker Nổi.<br><span class="highlight-ink">Thả Cảm Xúc</span> Ấm Áp<br>Không Rời Cuộc Vui.',
      heroSubtitle: 'StickHub mang cả cuốn sổ sticker & meme bạn yêu thích bay lơ lửng trên Messenger, Telegram, Zalo hay WhatsApp. Chạm nhẹ là dán ngay, giữ trọn từng khoảnh khắc kết nối!',
      btnPrimaryCta: 'Tải APK Miễn Phí',
      btnSecondaryCta: 'Xem Trên GitHub',
      stampOffline: '100% Không Cần Mạng',
      stampNoAds: 'Sạch Bóng Quảng Cáo',
      stampNoRoot: 'Không Cần Quyền Root',

      // Simulator
      chatStatus: '● Đang online',
      chatIncoming: 'Tối nay đi cà phê acoustic không mn ơi? Ai gửi sticker chốt kèo nào! ☕✨',
      chatOutgoing: 'Chờ xíu nè, mở StickHub thả chiếc sticker ấm lòng này liền...',
      bubbleHint: 'Chạm mở sổ! 👉',
      panelTitle: 'SỔ STICKER GẦN ĐÂY 📒',
      chipAll: 'Tất cả',
      chipCat: 'Mèo 🐱',
      chipCute: 'Ấm Áp 🌸',
      chipPepe: 'Pepe 🐸',
      chipTroll: 'Vui Nhộn 😂',
      simToastText: 'Đã dán sticker vào chat! ✨',

      // Problem vs Solution
      problemTag: '// CHUYỆN CŨ & NỖI NIỀM',
      problemTitle: 'Gửi Sticker Sao Phải Mệt Mỏi Đến Thế?',
      problemDesc: 'Mỗi khi cuộc trò chuyện đang rôm rả, việc phải loay hoay đổi ứng dụng dễ làm nguội đi những cảm xúc chân thành.',
      oldWayBadge: 'CÁCH CŨ RƯỜM RÀ',
      oldWayTitle: 'Mất 5 bước & 15 giây cho 1 chiếc sticker',
      oldStep1: '<strong>Rời màn hình chat:</strong> Thoát ra ngoài tìm app Thư viện hay Telegram.',
      oldStep2: '<strong>Lục lọi hàng ngàn ảnh:</strong> Cuộn mỏi tay giữa ảnh chụp tài liệu, biên lai thanh toán.',
      oldStep3: '<strong>Nguội mất câu chuyện:</strong> Khi quay lại, bạn bè đã nói sang chuyện khác, lỡ mất nhịp vui.',
      oldStep4: '<strong>Bị chia rẽ ứng dụng:</strong> Sticker ở app này không thể gửi sang app kia một cách tiện lợi.',
      
      newWayBadge: 'CÙNG STICKHUB ẤM ÁP',
      newWayTitle: '1 Chạm Nhẹ Nhàng — Dán Liền Tay',
      newStep1: '<strong>Bong bóng nổi túc trực:</strong> Nằm gọn bên mép màn hình, luôn đồng hành trên mọi ứng dụng.',
      newStep2: '<strong>Chạm là dán ngay:</strong> Nạp thẳng sticker vào bộ nhớ đệm (Clipboard) siêu tốc.',
      newStep3: '<strong>Gom về một mái nhà:</strong> Mọi meme từ khắp nơi đều được sắp xếp gọn gàng trong sổ tay.',
      newStep4: '<strong>Tự động thu hoạch:</strong> Cứ copy bất kỳ ảnh nào trên mạng, StickHub tự lưu và chống trùng lặp!',

      // Bento Features
      featuresTag: '// ĐIỀU KỲ DIỆU TỪ STICKHUB',
      featuresTitle: 'Những Tính Năng Nhỏ Bé Làm Nên Cuộc Vui Lớn',
      featuresDesc: 'Được chế tác thủ công với tinh thần tối giản, tôn trọng tuyệt đối quyền riêng tư và chiếc điện thoại của bạn.',
      feat1Title: 'Bong Bóng Nổi WindowManager Êm Ái',
      feat1Desc: 'Tận dụng API gốc của Android. Bong bóng nổi lướt êm ru, tự động nép vào góc màn hình và tự ẩn khi bạn xem phim hay thưởng thức âm nhạc.',
      feat2Title: 'Tách Nền Tự Động On-Device',
      feat2Desc: 'Tích hợp ML Kit chạy trực tiếp trên máy. Biến những bức ảnh đời thường của bạn bè và thú cưng thành sticker không viền trong chớp mắt.',
      feat3Title: 'Thu Hoạch Clipboard Thông Minh',
      feat3Desc: 'Chỉ cần nhấn Sao chép ảnh ở bất cứ đâu trên trình duyệt, StickHub sẽ tự cất vào kho và dùng mã SHA-256 để không bao giờ bị lưu trùng.',
      feat4Title: 'Sắp Xếp Danh Mục Kéo Thả Tiện Lợi',
      feat4Desc: 'Tự do đổi vị trí danh mục Yêu thích, Thường dùng theo thói quen của bạn. 4 chế độ hiển thị từ lưới to đến danh sách mộc mạc.',
      feat5Title: '100% Cục Bộ & Giữ Trọn Riêng Tư',
      feat5Desc: 'Toàn bộ dữ liệu nằm lại trong bộ nhớ máy của bạn. Không gửi ảnh lên mây, không theo dõi và không làm phiền bởi quảng cáo.',
      feat6Title: 'Giao Diện Vintage Mộc Mạc, Ấm Áp',
      feat6Desc: 'Tông màu giấy kraft, mực cà phê và gốm nung đất ấm áp. Tương thích chế độ Dark Mode nhẹ nhàng, êm dịu cho đôi mắt mỗi đêm.',

      // Install Steps
      installTag: '// 3 BƯỚC ĐƠN GIẢN',
      installTitle: 'Mang StickHub Về Chiếc Điện Thoại Của Bạn',
      installDesc: 'Không cần tài khoản Google Play, không cần Root. Cài đặt trực tiếp file APK an toàn chỉ trong 30 giây.',
      step1Num: '01',
      step1Title: 'Tải File APK Nhẹ Nhàng',
      step1Desc: 'Nhấn nút tải từ máy chủ đám mây tốc độ cao. Gói cài đặt nhỏ gọn, chỉ 13.4 MB.',
      step2Num: '02',
      step2Title: 'Mở & Cấp Quyền Cài Đặt',
      step2Desc: 'Mở tệp đã tải và bấm Cài đặt. Cho phép "Cài đặt từ nguồn này" nếu máy bạn hỏi lần đầu.',
      step3Num: '03',
      step3Title: 'Bật Bong Bóng & Bắt Đầu',
      step3Desc: 'Mở StickHub, cấp quyền hiển thị nổi trên màn hình và thả hồn vào những cuộc trò chuyện!',

      // FAQs
      faqTag: '// TÂM SỰ & GIẢI ĐÁP',
      faqTitle: 'Những Điều Bạn Thắc Mắc, Chúng Mình Chia Sẻ',
      faqDesc: 'Câu trả lời chân thành và minh bạch nhất từ người tạo ra StickHub.',
      faq1Q: 'Tại sao StickHub chưa có mặt trên Google Play Store?',
      faq1A: 'Thành thật tâm sự cùng bạn: Mình là học sinh và hiện tại chưa có đủ 25$ để đóng phí mở tài khoản Google Play Developer! 😅 Tuy nhiên, file APK được đóng gói hoàn toàn sạch sẽ, không chèn mã theo dõi hay quảng cáo rác. Bạn có thể an tâm tải về sử dụng.',
      faq2Q: 'Cài đặt trực tiếp file APK có an toàn cho điện thoại không?',
      faq2A: 'Tuyệt đối an toàn. StickHub hoạt động hoàn toàn offline, không yêu cầu các quyền nhạy cảm như danh bạ hay vị trí. Ứng dụng chỉ cần quyền hiển thị bong bóng nổi và lưu trữ sticker cục bộ.',
      faq3Q: 'StickHub dùng được trên những ứng dụng trò chuyện nào?',
      faq3A: 'Tất cả mọi ứng dụng! Nhờ cơ chế bong bóng nổi trên hệ thống, khi bạn chạm chọn sticker, ảnh sẽ được đưa vào Clipboard chuẩn. Bạn chỉ cần mở bàn phím ở Messenger, Zalo, Telegram, WhatsApp hay Instagram rồi nhấn "Dán".',
      faq4Q: 'Bong bóng nổi có gây hao pin hay làm chậm máy không?',
      faq4A: 'Không hề nha! Ứng dụng được viết bằng Kotlin tối ưu sâu. Khi ở dạng bong bóng nhỏ, ứng dụng gần như tiêu thụ 0% CPU và chỉ tải bộ nhớ khi bạn chạm mở sổ chọn sticker.',

      // Master CTA
      masterTitle: 'Sẵn Sàng Cho Những Cuộc Trò Chuyện Ấm Áp?',
      masterDesc: 'Đừng để những chiếc sticker đong đầy cảm xúc bị lãng quên. Tải StickHub ngay hôm nay để sẻ chia niềm vui cùng những người bạn yêu thương!',
      btnMasterDownload: 'Tải StickHub APK (v5.2.5)',

      // Sticky Bar
      stickySubtitle: 'v5.2.5 • Miễn phí',
      btnSticky: 'Tải APK (13MB)',

      // Footer
      footerDesc: 'Sổ sticker nổi ấm áp dành cho Android. Tôn trọng quyền riêng tư và giữ trọn mạch trò chuyện.',
      footerAuthor: 'Được chăm chút bởi:'
    },
    en: {
      langBtn: '🇬🇧 English',
      langToggleTooltip: 'Switch to Vietnamese',
      pageTitle: 'StickHub — Cozy Floating Sticker Scrapbook for Android',
      metaDesc: 'StickHub brings your favorite sticker & meme scrapbook to hover over Messenger, Telegram, Zalo, WhatsApp. One tap to paste, cozy & interruption-free!',

      // Header
      navCompare: 'Old vs New',
      navFeatures: 'Features',
      navGuide: 'How to Install',
      navFaq: 'FAQ',
      btnHeaderDownload: 'Get APK',

      // Hero
      heroBadge: 'Update v5.2.5 • Cherished Android Scrapbook',
      heroTitle: 'Floating Sticker Album.<br><span class="highlight-ink">Drop Reactions</span><br>Without Leaving Chats.',
      heroSubtitle: 'StickHub brings your favorite sticker & meme scrapbook to hover gracefully over Messenger, Telegram, Zalo, or WhatsApp. One gentle tap to paste, keeping conversations lively and warm!',
      btnPrimaryCta: 'Download Free APK',
      btnSecondaryCta: 'View on GitHub',
      stampOffline: '100% Offline',
      stampNoAds: 'Zero Advertisements',
      stampNoRoot: 'No Root Required',

      // Simulator
      chatStatus: '● Online now',
      chatIncoming: 'Coffee & acoustic songs tonight anyone? Send a cozy sticker to confirm! ☕✨',
      chatOutgoing: 'Hold on a second, opening StickHub to send a heartwarming sticker...',
      bubbleHint: 'Tap to open! 👉',
      panelTitle: 'RECENT STICKERS 📒',
      chipAll: 'All',
      chipCat: 'Cats 🐱',
      chipCute: 'Cozy 🌸',
      chipPepe: 'Pepe 🐸',
      chipTroll: 'Funny 😂',
      simToastText: 'Pasted sticker to chat! ✨',

      // Problem vs Solution
      problemTag: '// THE STRUGGLE & THE COZY WAY',
      problemTitle: 'Why Was Sending Stickers Such A Hassle?',
      problemDesc: 'When a lively conversation is in full swing, switching back and forth between multiple apps cools down the genuine connection.',
      oldWayBadge: 'THE TEDIOUS OLD WAY',
      oldWayTitle: '5 Steps & 15 Seconds For A Single Sticker',
      oldStep1: '<strong>Leaving the chat:</strong> Jumping out to find your system Gallery or Telegram app.',
      oldStep2: '<strong>Drowning in photos:</strong> Scrolling through thousands of work documents, screenshots, and receipts.',
      oldStep3: '<strong>Losing the rhythm:</strong> By the time you return, friends have moved to another topic.',
      oldStep4: '<strong>Walled gardens:</strong> Stickers saved in one messenger cannot easily be shared to another.',

      newWayBadge: 'THE COZY STICKHUB WAY',
      newWayTitle: 'One Gentle Tap — Pasted Instantly',
      newStep1: '<strong>Gentle floating bubble:</strong> Resting quietly at the screen edge, ready over any chat app.',
      newStep2: '<strong>Tap to paste:</strong> Copies instantly into the standard Android clipboard.',
      newStep3: '<strong>Everything in one home:</strong> Memes and stickers from everywhere organized neatly in your album.',
      newStep4: '<strong>Automatic Harvester:</strong> Copy any image from the web and StickHub saves it with SHA-256 deduplication!',

      // Bento Features
      featuresTag: '// CRAFTED WITH CARE',
      featuresTitle: 'Thoughtful Features For Meaningful Conversations',
      featuresDesc: 'Handcrafted with a minimalist philosophy, honoring your privacy and treating your phone with gentle efficiency.',
      feat1Title: 'Smooth WindowManager Bubble',
      feat1Desc: 'Built directly on Android native WindowManager. Glides seamlessly, snaps gently to screen edges, and auto-hides when you enjoy movies or music.',
      feat2Title: 'On-Device AI Subject Cutout',
      feat2Desc: 'Powered by on-device ML Kit. Transform casual photos of friends and beloved pets into borderless stickers in the blink of an eye.',
      feat3Title: 'Smart Clipboard Harvester',
      feat3Desc: 'Whenever you copy an image from your browser, StickHub gently archives it. SHA-256 hashing prevents duplicate clutter.',
      feat4Title: 'Drag-and-Drop Album Reordering',
      feat4Desc: 'Reorder your Favorite and Frequent categories freely to match your personal rhythm. Choose from 4 layout densities.',
      feat5Title: '100% Local & Privacy-Honoring',
      feat5Desc: 'Every piece of data stays strictly inside your device. No cloud leaks, no telemetry, and zero intrusive ads.',
      feat6Title: 'Warm Vintage & Dark Mode Harmony',
      feat6Desc: 'Cozy kraft paper tones, coffee ink, and terracotta accents. Perfectly soothing for late night conversations in Dark Mode.',

      // Install Steps
      installTag: '// 3 EASY STEPS',
      installTitle: 'Bring StickHub To Your Android Device',
      installDesc: 'No Google Play account needed, no Root required. Safe direct installation in 30 seconds.',
      step1Num: '01',
      step1Title: 'Download the Lightweight APK',
      step1Desc: 'Tap the download button from our fast Cloudflare CDN. The APK file is only 13.4 MB.',
      step2Num: '02',
      step2Title: 'Open & Grant Permission',
      step2Desc: 'Open the downloaded file and choose Install. Allow "Install unknown apps" if prompted for the first time.',
      step3Num: '03',
      step3Title: 'Enable Bubble & Enjoy!',
      step3Desc: 'Launch StickHub, grant overlay permission, and let your cherished stickers flow into any conversation!',

      // FAQs
      faqTag: '// HONEST CONVERSATION & FAQ',
      faqTitle: 'Frequently Asked Questions, Answered Honestly',
      faqDesc: 'Candid and transparent answers directly from the student creator of StickHub.',
      faq1Q: 'Why is StickHub not on the Google Play Store yet?',
      faq1A: 'Being completely transparent: I am a student and currently don’t have the $25 fee to open a Google Play Developer account! 😅 However, the APK is 100% clean, contains zero tracking code or ads, and is safe to use. When finances allow, StickHub will definitely arrive on Play Store!',
      faq2Q: 'Is installing this APK directly safe for my phone?',
      faq2A: 'Completely safe. StickHub operates 100% offline and requires no sensitive permissions like Contacts or Location. It only asks for floating overlay permission and local storage to keep your stickers safe.',
      faq3Q: 'Which chat apps does StickHub support?',
      faq3A: 'All of them! Because StickHub uses a system-level overlay, selecting any sticker copies it to the Android standard Clipboard. Simply tap "Paste" inside Messenger, Zalo, Telegram, WhatsApp, Discord, or Instagram.',
      faq4Q: 'Does the floating bubble drain battery or lag the phone?',
      faq4A: 'Not at all! The app is written in optimized Kotlin. When minimized as a tiny bubble, it consumes practically 0% CPU and only loads memory when you actively tap to open the sticker picker.',

      // Master CTA
      masterTitle: 'Ready For Warm & Meaningful Chats?',
      masterDesc: 'Never let memorable moments and perfect memes slip away. Download StickHub today and share warmth with the people you love!',
      btnMasterDownload: 'Download StickHub APK (v5.2.5)',

      // Sticky Bar
      stickySubtitle: 'v5.2.5 • Free',
      btnSticky: 'Get APK (13MB)',

      // Footer
      footerDesc: 'A cozy floating sticker scrapbook for Android. Respecting privacy and keeping conversations unbroken.',
      footerAuthor: 'Crafted with care by:'
    }
  };

  // Language Detection & Persistence
  let currentLang = 'vi';
  const savedLang = localStorage.getItem('stickhub_lang');
  if (savedLang && (savedLang === 'vi' || savedLang === 'en')) {
    currentLang = savedLang;
  } else {
    const browserLang = (navigator.language || navigator.userLanguage || 'vi').toLowerCase();
    currentLang = browserLang.startsWith('vi') ? 'vi' : 'en';
  }

  // Apply Language to DOM
  function applyLanguage(lang) {
    currentLang = lang;
    localStorage.setItem('stickhub_lang', lang);
    document.documentElement.lang = lang;
    document.title = translations[lang].pageTitle;

    const metaDesc = document.querySelector('meta[name="description"]');
    if (metaDesc) metaDesc.setAttribute('content', translations[lang].metaDesc);

    // Update all data-i18n elements
    const elements = document.querySelectorAll('[data-i18n]');
    elements.forEach((el) => {
      const key = el.getAttribute('data-i18n');
      if (translations[lang][key]) {
        el.innerHTML = translations[lang][key];
      }
    });

    // Update lang toggle button text
    const langBtn = document.getElementById('langSwitcherBtn');
    if (langBtn) {
      langBtn.textContent = translations[lang].langBtn;
      langBtn.title = translations[lang].langToggleTooltip;
    }
  }

  // ==========================================================================
  // 2. Interactive Phone & Floating Bubble Demo
  // ==========================================================================
  const simBubbleBtn = document.getElementById('simBubbleBtn');
  const simPanel = document.getElementById('simPanel');
  const simPanelClose = document.getElementById('simPanelClose');
  const simStickerGrid = document.getElementById('simStickerGrid');
  const simChatBody = document.getElementById('simChatBody');
  const simToast = document.getElementById('simToast');
  const simChipsRail = document.getElementById('simChipsRail');

  // Handcrafted Cozy Vintage Stickers (Warm Earth Colors, Retro Lines)
  const stickers = [
    {
      id: 1,
      cat: 'cat',
      name: 'Mèo Ôm Cốc Trà',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <circle cx="50" cy="50" r="42" fill="#FAF2E6" stroke="#C4552A" stroke-width="2.5"/>
        <polygon points="26,26 36,10 46,24" fill="#E8A87C" stroke="#33261D" stroke-width="2"/>
        <polygon points="54,24 64,10 74,26" fill="#E8A87C" stroke="#33261D" stroke-width="2"/>
        <circle cx="50" cy="52" r="30" fill="#FFFDF8" stroke="#33261D" stroke-width="2.5"/>
        <ellipse cx="40" cy="46" rx="4" ry="6" fill="#33261D"/>
        <ellipse cx="60" cy="46" rx="4" ry="6" fill="#33261D"/>
        <circle cx="41" cy="44" r="1.5" fill="#FFFFFF"/>
        <circle cx="61" cy="44" r="1.5" fill="#FFFFFF"/>
        <!-- Cozy blush -->
        <ellipse cx="34" cy="52" rx="4" ry="2.5" fill="#F4B2A3"/>
        <ellipse cx="66" cy="52" rx="4" ry="2.5" fill="#F4B2A3"/>
        <!-- Tea mug -->
        <rect x="42" y="58" width="16" height="15" rx="3" fill="#C4552A" stroke="#33261D" stroke-width="2"/>
        <path d="M58 62 C62 62, 62 68, 58 68" stroke="#33261D" stroke-width="2" fill="none"/>
        <path d="M46 54 Q48 50 46 47" stroke="#D99B26" stroke-width="2" stroke-linecap="round"/>
        <path d="M54 54 Q56 50 54 47" stroke="#D99B26" stroke-width="2" stroke-linecap="round"/>
      </svg>`
    },
    {
      id: 2,
      cat: 'cute',
      name: 'Mầm Cây Ấm Áp',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <circle cx="50" cy="50" r="42" fill="#EEF4EE" stroke="#3D5A40" stroke-width="2.5"/>
        <!-- Terracotta Pot -->
        <polygon points="34,58 66,58 62,82 38,82" fill="#C4552A" stroke="#33261D" stroke-width="2.5"/>
        <rect x="31" y="54" width="38" height="6" rx="2" fill="#D86B42" stroke="#33261D" stroke-width="2"/>
        <!-- Face on pot -->
        <circle cx="44" cy="68" r="2.5" fill="#33261D"/>
        <circle cx="56" cy="68" r="2.5" fill="#33261D"/>
        <path d="M48 73 Q50 76 52 73" stroke="#33261D" stroke-width="2" stroke-linecap="round"/>
        <!-- Sprout -->
        <path d="M50 54 Q50 40 50 34" stroke="#3D5A40" stroke-width="3" stroke-linecap="round"/>
        <path d="M50 38 Q64 30 62 42 Q52 46 50 38" fill="#588157" stroke="#33261D" stroke-width="2"/>
        <path d="M50 34 Q36 26 38 38 Q48 42 50 34" fill="#74A57F" stroke="#33261D" stroke-width="2"/>
      </svg>`
    },
    {
      id: 3,
      cat: 'pepe',
      name: 'Pepe Tách Trà',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <ellipse cx="50" cy="50" rx="42" ry="38" fill="#84A98C" stroke="#33261D" stroke-width="2.5"/>
        <ellipse cx="36" cy="38" rx="13" ry="11" fill="#FFFFFF" stroke="#33261D" stroke-width="2"/>
        <ellipse cx="64" cy="38" rx="13" ry="11" fill="#FFFFFF" stroke="#33261D" stroke-width="2"/>
        <circle cx="38" cy="38" r="5" fill="#2C211B"/>
        <circle cx="62" cy="38" r="5" fill="#2C211B"/>
        <!-- Gentle gentle smile -->
        <path d="M30 62 Q 50 74 70 62" stroke="#2C211B" stroke-width="3.5" stroke-linecap="round" fill="#C4552A"/>
        <ellipse cx="32" cy="48" rx="4" ry="2" fill="#D47A5B"/>
        <ellipse cx="68" cy="48" rx="4" ry="2" fill="#D47A5B"/>
      </svg>`
    },
    {
      id: 4,
      cat: 'cat',
      name: 'Mèo Bánh Mì',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <ellipse cx="50" cy="50" rx="42" ry="40" fill="#FFF8EE" stroke="#D99B26" stroke-width="2.5"/>
        <!-- Loaf Body -->
        <rect x="25" y="36" width="50" height="36" rx="16" fill="#D99B26" stroke="#33261D" stroke-width="2.5"/>
        <polygon points="28,36 34,22 42,36" fill="#D99B26" stroke="#33261D" stroke-width="2"/>
        <polygon points="58,36 66,22 72,36" fill="#D99B26" stroke="#33261D" stroke-width="2"/>
        <!-- Happy Eyes -->
        <path d="M36 50 Q40 44 44 50" stroke="#33261D" stroke-width="2.5" stroke-linecap="round" fill="none"/>
        <path d="M56 50 Q60 44 64 50" stroke="#33261D" stroke-width="2.5" stroke-linecap="round" fill="none"/>
        <ellipse cx="50" cy="56" rx="3" ry="2" fill="#C4552A"/>
      </svg>`
    },
    {
      id: 5,
      cat: 'cute',
      name: 'Trái Tim Gỗ',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <circle cx="50" cy="50" r="42" fill="#FDF7EA" stroke="#D99B26" stroke-width="2.5"/>
        <path d="M50 78 C25 60, 20 40, 32 28 C42 18, 48 26, 50 30 C52 26, 58 18, 68 28 C80 40, 75 60, 50 78 Z" fill="#C4552A" stroke="#33261D" stroke-width="2.5"/>
        <path d="M38 32 Q44 26 48 34" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round"/>
      </svg>`
    },
    {
      id: 6,
      cat: 'troll',
      name: 'Nụ Cười Hóm Hỉnh',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <circle cx="50" cy="50" r="42" fill="#FDF7EA" stroke="#33261D" stroke-width="2.5"/>
        <ellipse cx="36" cy="40" rx="6" ry="8" fill="#33261D"/>
        <ellipse cx="64" cy="40" rx="6" ry="8" fill="#33261D"/>
        <circle cx="37" cy="38" r="2" fill="#FFFFFF"/>
        <circle cx="65" cy="38" r="2" fill="#FFFFFF"/>
        <path d="M26 56 Q50 86 74 56 Z" fill="#FFFFFF" stroke="#33261D" stroke-width="3"/>
        <line x1="34" y1="64" x2="66" y2="64" stroke="#33261D" stroke-width="2"/>
        <line x1="42" y1="58" x2="42" y2="70" stroke="#33261D" stroke-width="2"/>
        <line x1="50" y1="58" x2="50" y2="72" stroke="#33261D" stroke-width="2"/>
        <line x1="58" y1="58" x2="58" y2="70" stroke="#33261D" stroke-width="2"/>
      </svg>`
    }
  ];

  function renderStickers(category) {
    if (!simStickerGrid) return;
    simStickerGrid.innerHTML = '';
    const filtered = category === 'all' ? stickers : stickers.filter((s) => s.cat === category);

    filtered.forEach((item) => {
      const card = document.createElement('div');
      card.className = 'sim-sticker-card';
      card.title = item.name;
      card.innerHTML = item.svg;

      card.addEventListener('click', () => {
        handleStickerClick(item);
      });

      simStickerGrid.appendChild(card);
    });
  }

  let botReplyTimer = null;
  function handleStickerClick(item) {
    // Show toast
    if (simToast) {
      simToast.classList.add('show');
      setTimeout(() => simToast.classList.remove('show'), 2000);
    }

    // Close panel
    setTimeout(() => {
      if (simPanel) simPanel.classList.remove('active');
    }, 250);

    // Append outgoing sticker
    if (simChatBody) {
      const stickerMsg = document.createElement('div');
      stickerMsg.className = 'chat-bubble chat-sticker-pasted';
      stickerMsg.innerHTML = item.svg;
      simChatBody.appendChild(stickerMsg);
      simChatBody.scrollTop = simChatBody.scrollHeight;

      // Simulate Bot/Friend reaction
      clearTimeout(botReplyTimer);
      botReplyTimer = setTimeout(() => {
        const viReactions = [
          'Aww cưng xỉu!! Xin link tải app gấp ☕💖',
          'Trời ơi sticker gì mà dễ thương ấm áp dữ zậy! 🥺✨',
          'Bấm cái gửi liền luôn hả?? StickHub nổi trên màn hình xịn thế! 💯',
          'Chiếc meme làm ấm lòng giữa tiết trời se lạnh haha! 🐸❤️'
        ];
        const enReactions = [
          'Aww so adorable!! Send me the download link please! ☕💖',
          'OMG this sticker is so cozy and cute! 🥺✨',
          'One tap and you pasted it right away?? StickHub overlay is so cool! 💯',
          'This meme totally made my day haha! 🐸❤️'
        ];
        const pool = currentLang === 'vi' ? viReactions : enReactions;
        const text = pool[Math.floor(Math.random() * pool.length)];

        const incomingMsg = document.createElement('div');
        incomingMsg.className = 'chat-bubble incoming';
        incomingMsg.textContent = text;
        simChatBody.appendChild(incomingMsg);
        simChatBody.scrollTop = simChatBody.scrollHeight;
      }, 1200);
    }
  }

  // Floating Bubble Click
  if (simBubbleBtn && simPanel) {
    simBubbleBtn.addEventListener('click', () => {
      const isActive = simPanel.classList.toggle('active');
      const hint = simBubbleBtn.querySelector('.bubble-hint-pulse');
      if (hint) hint.style.display = 'none';
      if (isActive && simStickerGrid.children.length === 0) {
        renderStickers('all');
      }
    });
  }

  if (simPanelClose && simPanel) {
    simPanelClose.addEventListener('click', () => {
      simPanel.classList.remove('active');
    });
  }

  if (simChipsRail) {
    const chips = simChipsRail.querySelectorAll('.sim-chip');
    chips.forEach((chip) => {
      chip.addEventListener('click', () => {
        chips.forEach((c) => c.classList.remove('active'));
        chip.classList.add('active');
        const cat = chip.getAttribute('data-cat');
        renderStickers(cat);
      });
    });
  }

  // Initial render of stickers
  renderStickers('all');

  // ==========================================================================
  // 3. Language Switcher Button Listener
  // ==========================================================================
  const langSwitcherBtn = document.getElementById('langSwitcherBtn');
  if (langSwitcherBtn) {
    langSwitcherBtn.addEventListener('click', () => {
      const nextLang = currentLang === 'vi' ? 'en' : 'vi';
      applyLanguage(nextLang);
    });
  }

  // Run initial language setup
  applyLanguage(currentLang);

  // ==========================================================================
  // 4. Scroll Reveals (IntersectionObserver)
  // ==========================================================================
  const revealElements = document.querySelectorAll('.reveal-on-scroll');
  if ('IntersectionObserver' in window) {
    const revealObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('revealed');
            revealObserver.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12 }
    );
    revealElements.forEach((el) => revealObserver.observe(el));
  } else {
    revealElements.forEach((el) => el.classList.add('revealed'));
  }

  // ==========================================================================
  // 5. Header Scrolled Shadow
  // ==========================================================================
  const siteHeader = document.getElementById('siteHeader');
  window.addEventListener(
    'scroll',
    () => {
      if (!siteHeader) return;
      if (window.scrollY > 30) {
        siteHeader.classList.add('scrolled');
      } else {
        siteHeader.classList.remove('scrolled');
      }
    },
    { passive: true }
  );

  // ==========================================================================
  // 6. Sticky Mobile Download Bar
  // ==========================================================================
  const stickyMobileCta = document.getElementById('stickyMobileCta');
  const heroSection = document.getElementById('hero');

  if (stickyMobileCta && heroSection) {
    const mobileObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) {
            stickyMobileCta.classList.add('visible');
          } else {
            stickyMobileCta.classList.remove('visible');
          }
        });
      },
      { threshold: 0.1 }
    );
    mobileObserver.observe(heroSection);
  }

  // ==========================================================================
  // 7. FAQs Accordion
  // ==========================================================================
  const faqItems = document.querySelectorAll('.faq-item');
  faqItems.forEach((item) => {
    const questionBtn = item.querySelector('.faq-question');
    if (questionBtn) {
      questionBtn.addEventListener('click', () => {
        const wasActive = item.classList.contains('active');
        faqItems.forEach((other) => other.classList.remove('active'));
        if (!wasActive) {
          item.classList.add('active');
        }
      });
    }
  });

  console.log('📖 StickHub Vintage Scrapbook loaded in ' + currentLang.toUpperCase());
})();
