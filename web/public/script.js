/* ==========================================================================
   StickHub Landing Page — Solarpunk Interactive Script
   Awwwards Reveal, Particle System & Realistic Floating Bubble Simulator
   ========================================================================== */

(function () {
  'use strict';

  // ==========================================
  // 1. Ambient Solarpunk Spores Canvas
  // ==========================================
  const canvas = document.getElementById('ambient-canvas');
  if (canvas) {
    const ctx = canvas.getContext('2d');
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    const colors = [
      'rgba(255, 183, 3, 0.45)',  // Solar Gold
      'rgba(111, 216, 156, 0.40)', // Sprout Green
      'rgba(127, 212, 236, 0.35)', // Sky Cyan
      'rgba(255, 201, 60, 0.35)'   // Solar Glow
    ];

    const particleCount = Math.min(Math.floor(width / 25), 55);
    const particles = [];

    for (let i = 0; i < particleCount; i++) {
      particles.push({
        x: Math.random() * width,
        y: Math.random() * height,
        radius: Math.random() * 2.2 + 0.8,
        color: colors[Math.floor(Math.random() * colors.length)],
        vx: (Math.random() - 0.5) * 0.45,
        vy: -Math.random() * 0.55 - 0.2, // Drifting upwards like spores
        pulse: Math.random() * Math.PI,
        pulseSpeed: 0.02 + Math.random() * 0.03
      });
    }

    function renderParticles() {
      ctx.clearRect(0, 0, width, height);

      particles.forEach((p) => {
        p.x += p.vx + Math.sin(p.pulse) * 0.3;
        p.y += p.vy;
        p.pulse += p.pulseSpeed;

        if (p.y < -10) {
          p.y = height + 10;
          p.x = Math.random() * width;
        }
        if (p.x < -10) p.x = width + 10;
        if (p.x > width + 10) p.x = -10;

        const currentRadius = Math.max(0.5, p.radius + Math.sin(p.pulse) * 0.6);
        ctx.beginPath();
        ctx.arc(p.x, p.y, currentRadius, 0, Math.PI * 2);
        ctx.fillStyle = p.color;
        ctx.shadowBlur = 10;
        ctx.shadowColor = p.color;
        ctx.fill();
      });

      requestAnimationFrame(renderParticles);
    }

    renderParticles();

    window.addEventListener('resize', () => {
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    });
  }

  // ==========================================
  // 2. Interactive Phone & Floating Bubble Demo
  // ==========================================
  const simBubbleBtn = document.getElementById('simBubbleBtn');
  const simPanel = document.getElementById('simPanel');
  const simPanelClose = document.getElementById('simPanelClose');
  const simStickerGrid = document.getElementById('simStickerGrid');
  const simChatBody = document.getElementById('simChatBody');
  const simToast = document.getElementById('simToast');
  const simChipsRail = document.getElementById('simChipsRail');

  // Vector Sticker Collection (Pepe, Cats, Cute, Memes)
  const stickers = [
    {
      id: 1,
      cat: 'pepe',
      name: 'Pepe Vui Sướng',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <ellipse cx="50" cy="50" rx="42" ry="38" fill="#5F9E43"/>
        <ellipse cx="36" cy="38" rx="14" ry="12" fill="#FFFFFF"/>
        <ellipse cx="64" cy="38" rx="14" ry="12" fill="#FFFFFF"/>
        <circle cx="38" cy="38" r="6" fill="#1A3324"/>
        <circle cx="62" cy="38" r="6" fill="#1A3324"/>
        <path d="M28 62 C 38 78, 62 78, 72 62" stroke="#2B5120" stroke-width="6" stroke-linecap="round" fill="#C93B3B"/>
        <ellipse cx="50" cy="65" rx="12" ry="6" fill="#E86A6A"/>
        <path d="M24 28 Q 36 20 48 28" stroke="#376825" stroke-width="4" stroke-linecap="round"/>
        <path d="M52 28 Q 64 20 76 28" stroke="#376825" stroke-width="4" stroke-linecap="round"/>
      </svg>`
    },
    {
      id: 2,
      cat: 'pepe',
      name: 'Pepe Suy Tư',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <ellipse cx="50" cy="50" rx="42" ry="38" fill="#5F9E43"/>
        <ellipse cx="35" cy="40" rx="13" ry="10" fill="#FFFFFF"/>
        <ellipse cx="65" cy="40" rx="13" ry="10" fill="#FFFFFF"/>
        <circle cx="36" cy="42" r="5" fill="#1A3324"/>
        <circle cx="64" cy="42" r="5" fill="#1A3324"/>
        <path d="M30 68 Q 50 56 70 68" stroke="#2B5120" stroke-width="5" stroke-linecap="round" fill="none"/>
        <path d="M25 32 Q 35 34 45 32" stroke="#2B5120" stroke-width="4" stroke-linecap="round"/>
        <path d="M55 32 Q 65 34 75 32" stroke="#2B5120" stroke-width="4" stroke-linecap="round"/>
        <circle cx="70" cy="55" r="4" fill="#7FD4EC"/>
      </svg>`
    },
    {
      id: 3,
      cat: 'cat',
      name: 'Mèo Lêu Lêu',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <circle cx="50" cy="50" r="40" fill="#FFC93C"/>
        <polygon points="20,25 32,8 44,25" fill="#FFB703"/>
        <polygon points="56,25 68,8 80,25" fill="#FFB703"/>
        <ellipse cx="36" cy="45" rx="5" ry="8" fill="#143324"/>
        <ellipse cx="64" cy="45" rx="5" ry="8" fill="#143324"/>
        <circle cx="38" cy="42" r="2" fill="#FFFFFF"/>
        <circle cx="66" cy="42" r="2" fill="#FFFFFF"/>
        <polygon points="50,56 46,51 54,51" fill="#E86A6A"/>
        <path d="M42 60 Q 50 64 58 60" stroke="#143324" stroke-width="3" stroke-linecap="round" fill="none"/>
        <ellipse cx="50" cy="67" rx="6" ry="8" fill="#FF6B6B"/>
      </svg>`
    },
    {
      id: 4,
      cat: 'cat',
      name: 'Mèo Bắn Tim',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <circle cx="50" cy="50" r="40" fill="#FFFFFF"/>
        <polygon points="18,28 30,10 42,28" fill="#FFAAA6"/>
        <polygon points="58,28 70,10 82,28" fill="#FFAAA6"/>
        <!-- Heart eyes -->
        <path d="M30 42 A 5 5 0 0 0 40 42 A 5 5 0 0 0 50 42 Q 50 50 40 56 Q 30 50 30 42 Z" fill="#E63946" transform="translate(-5, -6) scale(0.9)"/>
        <path d="M30 42 A 5 5 0 0 0 40 42 A 5 5 0 0 0 50 42 Q 50 50 40 56 Q 30 50 30 42 Z" fill="#E63946" transform="translate(25, -6) scale(0.9)"/>
        <ellipse cx="50" cy="56" rx="4" ry="3" fill="#FFAAA6"/>
        <path d="M42 63 Q 50 70 58 63" stroke="#2D6A4F" stroke-width="3" stroke-linecap="round" fill="none"/>
      </svg>`
    },
    {
      id: 5,
      cat: 'cute',
      name: 'Doge Đội Mầm',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <ellipse cx="50" cy="54" rx="38" ry="34" fill="#F4A261"/>
        <circle cx="36" cy="48" r="6" fill="#1D3557"/>
        <circle cx="64" cy="48" r="6" fill="#1D3557"/>
        <circle cx="38" cy="46" r="2" fill="#FFFFFF"/>
        <circle cx="66" cy="46" r="2" fill="#FFFFFF"/>
        <ellipse cx="50" cy="58" rx="8" ry="6" fill="#E76F51"/>
        <ellipse cx="50" cy="56" rx="5" ry="3" fill="#1D3557"/>
        <path d="M44 65 Q 50 70 56 65" stroke="#1D3557" stroke-width="3" stroke-linecap="round" fill="none"/>
        <!-- Sprout on Head -->
        <path d="M50 20 Q 50 10 50 5" stroke="#6FD89C" stroke-width="3" stroke-linecap="round"/>
        <path d="M50 10 Q 60 5 62 12 Q 55 16 50 10" fill="#6FD89C"/>
        <path d="M50 14 Q 40 10 38 17 Q 45 20 50 14" fill="#9EF0BD"/>
      </svg>`
    },
    {
      id: 6,
      cat: 'troll',
      name: 'Nụ Cười Hủy Diệt',
      svg: `<svg viewBox="0 0 100 100" fill="none">
        <circle cx="50" cy="50" r="42" fill="#FFB703"/>
        <circle cx="35" cy="38" r="8" fill="#0D2218"/>
        <circle cx="65" cy="38" r="8" fill="#0D2218"/>
        <circle cx="38" cy="36" r="3" fill="#FFFFFF"/>
        <circle cx="68" cy="36" r="3" fill="#FFFFFF"/>
        <path d="M22 55 Q 50 92 78 55 Z" fill="#FFFFFF" stroke="#0D2218" stroke-width="4"/>
        <path d="M30 62 L 70 62" stroke="#0D2218" stroke-width="3"/>
        <path d="M40 56 L 40 70" stroke="#0D2218" stroke-width="2.5"/>
        <path d="M50 56 L 50 72" stroke="#0D2218" stroke-width="2.5"/>
        <path d="M60 56 L 60 70" stroke="#0D2218" stroke-width="2.5"/>
      </svg>`
    }
  ];

  // Render Stickers in Panel Grid
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

  // Handle Sticker Click (Copy & Paste Simulation)
  let botReplyTimer = null;
  function handleStickerClick(item) {
    // Show Copy Toast
    if (simToast) {
      simToast.classList.add('show');
      setTimeout(() => simToast.classList.remove('show'), 2000);
    }

    // Close Panel after short delay
    setTimeout(() => {
      if (simPanel) simPanel.classList.remove('active');
    }, 280);

    // Append Outgoing Sticker to Simulated Chat
    if (simChatBody) {
      const stickerMsg = document.createElement('div');
      stickerMsg.className = 'chat-bubble chat-sticker-pasted';
      stickerMsg.innerHTML = item.svg;
      simChatBody.appendChild(stickerMsg);

      // Scroll to bottom
      simChatBody.scrollTop = simChatBody.scrollHeight;

      // Simulate Bot/Crush Reaction after 1.4s
      clearTimeout(botReplyTimer);
      botReplyTimer = setTimeout(() => {
        const reactions = [
          'Hahaha con sticker này đỉnh thế!! Xin link tải gấp! 🤣🔥',
          'U là trời lụm đâu ra meme bựa zữ v?? Gửi tiếp điii! 👏✨',
          'Vừa bấm cái gửi liền hả?? StickHub bay trên màn hình xịn z! 💯',
          'Meme hủy diệt cuộc trò chuyện luôn haha! 🐸❤️'
        ];
        const randomText = reactions[Math.floor(Math.random() * reactions.length)];
        const incomingMsg = document.createElement('div');
        incomingMsg.className = 'chat-bubble incoming';
        incomingMsg.textContent = randomText;
        simChatBody.appendChild(incomingMsg);
        simChatBody.scrollTop = simChatBody.scrollHeight;
      }, 1300);
    }
  }

  // Floating Bubble Toggle
  if (simBubbleBtn && simPanel) {
    simBubbleBtn.addEventListener('click', () => {
      const isActive = simPanel.classList.toggle('active');
      // Hide hint pulse once clicked
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

  // Category Chips Filter
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

  // Initial stickers render
  renderStickers('all');

  // ==========================================
  // 3. Awwwards Scroll Reveal (IntersectionObserver)
  // ==========================================
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
      {
        threshold: 0.12,
        rootMargin: '0px 0px -40px 0px'
      }
    );

    revealElements.forEach((el) => revealObserver.observe(el));
  } else {
    revealElements.forEach((el) => el.classList.add('revealed'));
  }

  // ==========================================
  // 4. Header Glassmorphism on Scroll
  // ==========================================
  const siteHeader = document.getElementById('siteHeader');
  window.addEventListener(
    'scroll',
    () => {
      if (!siteHeader) return;
      if (window.scrollY > 40) {
        siteHeader.classList.add('scrolled');
      } else {
        siteHeader.classList.remove('scrolled');
      }
    },
    { passive: true }
  );

  // ==========================================
  // 5. Sticky Mobile CTA Visibility
  // ==========================================
  const stickyMobileCta = document.getElementById('stickyMobileCta');
  const heroSection = document.getElementById('hero');

  if (stickyMobileCta && heroSection) {
    const mobileObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          // If hero is NOT intersecting, show mobile CTA bar
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

  // ==========================================
  // 6. FAQs Accordion
  // ==========================================
  const faqItems = document.querySelectorAll('.faq-item');
  faqItems.forEach((item) => {
    const questionBtn = item.querySelector('.faq-question');
    if (questionBtn) {
      questionBtn.addEventListener('click', () => {
        const wasActive = item.classList.contains('active');
        // Close others
        faqItems.forEach((other) => other.classList.remove('active'));
        // Toggle current
        if (!wasActive) {
          item.classList.add('active');
        }
      });
    }
  });

  // ==========================================
  // 7. Subtle 3D Card Hover Tilt (Desktop Only)
  // ==========================================
  if (window.matchMedia('(min-width: 992px)').matches) {
    const tiltCards = document.querySelectorAll('.bento-card, .vs-card, .step-card');
    tiltCards.forEach((card) => {
      card.addEventListener('mousemove', (e) => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left - rect.width / 2;
        const y = e.clientY - rect.top - rect.height / 2;
        const rotateX = (-y / rect.height) * 8;
        const rotateY = (x / rect.width) * 8;
        card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateY(-4px)`;
      });

      card.addEventListener('mouseleave', () => {
        card.style.transform = '';
      });
    });
  }

  // ==========================================
  // 8. Download Click Tracking / Feedback
  // ==========================================
  const downloadButtons = document.querySelectorAll('a[href$=".apk"]');
  downloadButtons.forEach((btn) => {
    btn.addEventListener('click', () => {
      // Gentle vibration feedback on supported mobile devices
      if (navigator.vibrate) {
        navigator.vibrate([30, 40, 30]);
      }
    });
  });

  console.log('🌿 StickHub Solarpunk Landing Page loaded successfully! Built by Khanh Minh (khanhminh.web.app)');
})();
