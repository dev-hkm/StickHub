/* ==========================================================================
   StickHub Landing Page — Vintage Golden Earth Dark Mode Engine
   Real Cutout Memes, WindowManager Floating Popup, Smart Auto-Hide Topbar
   ========================================================================== */

(function () {
  'use strict';

  // ==========================================================================
  // 1. Comprehensive Bilingual Dictionary (100% Coverage)
  // ==========================================================================
  const translations = {
    vi: {
      langBtn: '🇻🇳 Tiếng Việt',
      langToggleTooltip: 'Chuyển sang English',
      pageTitle: 'StickHub — Sổ Sticker Nổi Cho Android | Giữ Trọn Cuộc Trò Chuyện',
      metaDesc: 'StickHub mang cả cuốn sổ sticker & meme bạn yêu thích bay lượn trên Messenger, Telegram, Zalo, WhatsApp. Chạm nhẹ là dán, ấm áp và không gián đoạn!',
      
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
      btnExploreFeatures: 'Khám Phá Tính Năng',
      stampOffline: '100% Không Cần Mạng',
      stampNoAds: 'Sạch Bóng Quảng Cáo',
      stampNoRoot: 'Không Cần Quyền Root',

      // Phone Simulator
      simChatTitle: 'Hội Bạn Cũ & Crush',
      simChatStatus: '● Đang online',
      simChatIncoming: 'Tối nay đi cà phê acoustic không mn ơi? Ai gửi meme chốt kèo nào! ☕✨',
      simChatOutgoing: 'Chờ xíu nè, mở StickHub thả chiếc meme hủy diệt này liền...',
      bubbleHint: 'Chạm mở popup! 👉',
      panelTitle: 'STICKHUB POPUP ✨',
      chipAll: 'Tất cả',
      chipPepe: 'Pepe 🐸',
      chipCheems: 'Cheems 🐶',
      chipCat: 'Mèo Bựa 🐱',
      chipTroll: 'Troll 😂',
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
      pillWindowManager: '#WindowManager',
      pillEdgeSnap: '#BámDínhMép',
      pillAutoDim: '#TựĐộngẨn',

      feat2Title: 'Tách Nền Tự Động On-Device',
      feat2Desc: 'Tích hợp ML Kit chạy trực tiếp trên máy. Biến những bức ảnh đời thường của bạn bè và thú cưng thành sticker không viền trong chớp mắt.',
      pillMLKit: '#MLKitAI',
      pillOnDevice: '#ChạyTrênMáy',

      feat3Title: 'Thu Hoạch Clipboard Thông Minh',
      feat3Desc: 'Chỉ cần nhấn Sao chép ảnh ở bất cứ đâu trên trình duyệt, StickHub sẽ tự cất vào kho và dùng mã SHA-256 để không bao giờ bị lưu trùng.',
      pillSHA256: '#MãHóaSHA256',
      pillAutoHarvest: '#ThuHoạchTựĐộng',

      feat4Title: 'Sắp Xếp Danh Mục Kéo Thả Tiện Lợi',
      feat4Desc: 'Tự do đổi vị trí danh mục Yêu thích, Thường dùng theo thói quen của bạn. 4 chế độ hiển thị từ lưới to đến danh sách mộc mạc.',
      pillDragDrop: '#KéoThảReorder',
      pill4Layouts: '#4ChếĐộHiểnThị',

      feat5Title: '100% Cục Bộ & Giữ Trọn Riêng Tư',
      feat5Desc: 'Toàn bộ dữ liệu nằm lại trong bộ nhớ máy của bạn. Không gửi ảnh lên mây, không theo dõi và không làm phiền bởi quảng cáo.',
      pillOffline: '#HoànToànOffline',
      pillPrivacy: '#BảoMậtRiêngTư',

      feat6Title: 'Giao Diện Vintage Mộc Mạc, Ấm Áp',
      feat6Desc: 'Tông màu giấy kraft, mực cà phê và vàng đất nung ấm áp. Tương thích chế độ Dark Mode nhẹ nhàng, êm dịu cho đôi mắt mỗi đêm.',
      pillVintageAesthetic: '#VintageẤmÁp',
      pillDarkMode: '#ÊmMắtĐêm',

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
      footerAuthor: 'Được phát triển bởi'
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
      btnExploreFeatures: 'Explore Features',
      stampOffline: '100% Offline',
      stampNoAds: 'Zero Advertisements',
      stampNoRoot: 'No Root Required',

      // Phone Simulator
      simChatTitle: 'Best Friends & Crush',
      simChatStatus: '● Online now',
      simChatIncoming: 'Coffee & acoustic songs tonight anyone? Send a meme to lock it in! ☕✨',
      simChatOutgoing: 'Hold on a second, opening StickHub to send a killer meme...',
      bubbleHint: 'Tap for popup! 👉',
      panelTitle: 'STICKHUB POPUP ✨',
      chipAll: 'All',
      chipPepe: 'Pepe 🐸',
      chipCheems: 'Cheems 🐶',
      chipCat: 'Cats 🐱',
      chipTroll: 'Troll 😂',
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
      pillWindowManager: '#WindowManager',
      pillEdgeSnap: '#EdgeSnapping',
      pillAutoDim: '#AutoDim',

      feat2Title: 'On-Device AI Subject Cutout',
      feat2Desc: 'Powered by on-device ML Kit. Transform casual photos of friends and beloved pets into borderless stickers in the blink of an eye.',
      pillMLKit: '#MLKitAI',
      pillOnDevice: '#OnDevice',

      feat3Title: 'Smart Clipboard Harvester',
      feat3Desc: 'Whenever you copy an image from your browser, StickHub gently archives it. SHA-256 hashing prevents duplicate clutter.',
      pillSHA256: '#SHA256Hashing',
      pillAutoHarvest: '#AutoHarvest',

      feat4Title: 'Drag-and-Drop Album Reordering',
      feat4Desc: 'Reorder your Favorite and Frequent categories freely to match your personal rhythm. Choose from 4 layout densities.',
      pillDragDrop: '#DragAndDrop',
      pill4Layouts: '#4LayoutModes',

      feat5Title: '100% Local & Privacy-Honoring',
      feat5Desc: 'Every piece of data stays strictly inside your device. No cloud leaks, no telemetry, and zero intrusive ads.',
      pillOffline: '#100%Offline',
      pillPrivacy: '#ZeroTracking',

      feat6Title: 'Warm Vintage & Dark Mode Harmony',
      feat6Desc: 'Cozy kraft paper tones, coffee ink, and golden earth accents. Perfectly soothing for late night conversations in Dark Mode.',
      pillVintageAesthetic: '#VintageWarmth',
      pillDarkMode: '#EyeCareNight',

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
      footerAuthor: 'Crafted with care by'
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
      if (translations[lang] && translations[lang][key]) {
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
  // 2. Real Transparent Meme Cutouts & Floating Popup Logic
  // ==========================================================================
  const simBubbleBtn = document.getElementById('simBubbleBtn');
  const simPanel = document.getElementById('simPanel');
  const simPanelClose = document.getElementById('simPanelClose');
  const simStickerGrid = document.getElementById('simStickerGrid');
  const simChatBody = document.getElementById('simChatBody');
  const simToast = document.getElementById('simToast');
  const simChipsRail = document.getElementById('simChipsRail');

  // Real Transparent Meme Stickers (Downloaded into public/stickers)
  const memeStickers = [
    { id: 1, cat: 'pepe', name: 'Pepe The Frog', img: 'stickers/pepe.png' },
    { id: 2, cat: 'cheems', name: 'Cheems Doge', img: 'stickers/cheems.png' },
    { id: 3, cat: 'troll', name: 'Trollface', img: 'stickers/trollface.png' },
    { id: 4, cat: 'cat', name: 'Mèo Crying Scream', img: 'stickers/catscream.png' },
    { id: 5, cat: 'cat', name: 'Mèo Đeo Kính Cool', img: 'stickers/kewlcat.png' },
    { id: 6, cat: 'cheems', name: 'Doge Classic', img: 'stickers/doge.png' }
  ];

  function renderStickers(category) {
    if (!simStickerGrid) return;
    simStickerGrid.innerHTML = '';
    const filtered = category === 'all' ? memeStickers : memeStickers.filter((s) => s.cat === category);

    filtered.forEach((item) => {
      const card = document.createElement('div');
      card.className = 'sim-sticker-card';
      card.title = item.name;

      const img = document.createElement('img');
      img.src = item.img;
      img.alt = item.name;
      img.loading = 'lazy';
      card.appendChild(img);

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

    // Close floating popup smoothly
    if (simPanel) {
      simPanel.classList.remove('active');
    }

    // Append outgoing transparent meme sticker to chat
    if (simChatBody) {
      const stickerMsg = document.createElement('div');
      stickerMsg.className = 'chat-bubble chat-sticker-pasted';
      
      const stickerImg = document.createElement('img');
      stickerImg.src = item.img;
      stickerImg.alt = item.name;
      stickerMsg.appendChild(stickerImg);

      simChatBody.appendChild(stickerMsg);
      simChatBody.scrollTop = simChatBody.scrollHeight;

      // Simulate Friend / Crush instant reaction
      clearTimeout(botReplyTimer);
      botReplyTimer = setTimeout(() => {
        const viReactions = [
          'Ủa meme Cheems này cắt nền chuẩn zậy! Xin link tải app gấp haha 🤣🔥',
          'Trollface kinh điển luôn trời ơi!! Đỉnh vcl 👏✨',
          'Ủa bấm cái dán liền luôn hả?? StickHub nổi trên màn hình xịn thế! 💯',
          'Con mèo gào khóc đúng tâm trạng tui lúc này luôn haha! 😂❤️'
        ];
        const enReactions = [
          'Whoa that Cheems cutout is so clean!! Send me the download link! 🤣🔥',
          'Classic Trollface OMG!! That hit hard 👏✨',
          'Wait, one tap and you pasted it right away?? StickHub overlay is so cool! 💯',
          'That screaming cat meme is literally my mood right now haha! 😂❤️'
        ];
        const pool = currentLang === 'vi' ? viReactions : enReactions;
        const text = pool[Math.floor(Math.random() * pool.length)];

        const incomingMsg = document.createElement('div');
        incomingMsg.className = 'chat-bubble incoming';
        incomingMsg.textContent = text;
        simChatBody.appendChild(incomingMsg);
        simChatBody.scrollTop = simChatBody.scrollHeight;
      }, 1000);
    }
  }

  // Mascot Bubble Toggle
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
  // 4. Smart Auto-Hide Topbar (Hide on scroll down, show on scroll up)
  // ==========================================================================
  let lastScrollY = window.scrollY;
  const siteHeader = document.getElementById('siteHeader');

  window.addEventListener(
    'scroll',
    () => {
      if (!siteHeader) return;
      const currentScrollY = window.scrollY;

      // Scrolled styling (background blur)
      if (currentScrollY > 40) {
        siteHeader.classList.add('scrolled');
      } else {
        siteHeader.classList.remove('scrolled');
      }

      // Hide on scroll down, show on scroll up
      if (currentScrollY > lastScrollY && currentScrollY > 120) {
        siteHeader.classList.add('header-hidden');
      } else {
        siteHeader.classList.remove('header-hidden');
      }

      lastScrollY = currentScrollY;
    },
    { passive: true }
  );

  // ==========================================================================
  // 5. Staggered Scroll Reveals (IntersectionObserver)
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
      { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
    );
    revealElements.forEach((el) => revealObserver.observe(el));
  } else {
    revealElements.forEach((el) => el.classList.add('revealed'));
  }

  // ==========================================================================
  // 6. 3D Perspective Card Tilt (Desktop Only)
  // ==========================================================================
  if (window.matchMedia('(min-width: 992px)').matches) {
    const tiltCards = document.querySelectorAll('.bento-card, .scrapbook-card, .step-card');
    tiltCards.forEach((card) => {
      card.addEventListener('mousemove', (e) => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left - rect.width / 2;
        const y = e.clientY - rect.top - rect.height / 2;
        const rotateX = (-y / rect.height) * 7;
        const rotateY = (x / rect.width) * 7;
        card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateY(-5px)`;
      });

      card.addEventListener('mouseleave', () => {
        card.style.transform = '';
      });
    });
  }

  // ==========================================================================
  // 7. Sticky Mobile Download Bar
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
  // 8. FAQs Accordion
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

  console.log('📖 StickHub Dark Mode loaded in ' + currentLang.toUpperCase());
})();
