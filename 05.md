---
version: "alpha"
name: "Merriweather Typography"
description: "Render a 2D isolated text on a solid background. Ideal for blogs, news sites, long-form reading, and editorial platforms.. AI-ready template."
colors:
  primary: "#FFE3C3"
  secondary: "#5B3A30"
typography:
  h1:
    fontFamily: Merriweather
    fontSize: 2.5rem
    fontWeight: 700
  body-md:
    fontFamily: Merriweather
    fontSize: 1rem
    fontWeight: 400
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    padding: 12px
---

## Overview

Render a 2D isolated text on a solid background. Ideal for blogs, news sites, long-form reading, and editorial platforms.. AI-ready template. Before Merriweather, serifs on screen were a compromise. You either accepted blurry letterforms at body sizes or abandoned the genre entirely for sans-serifs. Eben Sorkin refused both options.

Released in 2011 through Google Fonts, Merriweather was drawn specifically for the constraints of backlit displays. The key insight was structural: an unusually large x-height paired with open counters and sturdy stroke contrast. Where traditional serifs like Georgia relied on hinting tricks to survive pixel grids, Sorkin designed the proportions themselves to remain legible. The serifs are slightly bracketed — thick enough to render cleanly at 16px but refined enough to not feel clunky at display sizes.

What made Merriweather genuinely important was timing. It arrived exactly when responsive design was forcing designers to serve the same type across wildly different screens. A serif that could hold its own at 14px on a Kindle and 18px on a Retina display wasn't just nice to have — it was infrastructure. The typeface proved that screen-first didn't have to mean sans-serif-only, and that readability and typographic warmth weren't mutually exclusive.

- Density: 5/10 — Balanced
- Variance: 4/10 — Moderate
- Motion: 4/10 — Subtle

- **Style:** Screen‑optimized classic serif
- **Keywords:** Merriweather, classic serif, large x-height, readable, screen-optimized, editorial
- **Era:** Contemporary Web
- **Light/Dark:** ✗ No / ✓ Full

## Colors

- **#FFE3C3** (#FFE3C3) — Primary surface or dominant color
- **#5B3A30** (#5B3A30) — Extended palette, decorative use


## Typography

- **Display / Hero:** Merriweather — Weight 700, tight tracking, used for headline impact
- **Body:** Merriweather — Weight 400, 16px/1.6 line-height, max 72ch per line
- **UI Labels / Captions:** Merriweather — 0.875rem, weight 500, slight letter-spacing
- **Monospace:** JetBrains Mono — Used for code, metadata, and technical values

Scale:
- Hero: clamp(2.5rem, 5vw, 4rem)
- H1: 2.25rem
- H2: 1.5rem
- Body: 1rem / 1.6
- Small: 0.875rem


## Layout

- **Grid:** CSS Grid primary. Max-width containment: 1280px centered with 1.5rem side padding.
- **Spacing rhythm:** Balanced. Base unit: 0.5rem (8px).
- **Section vertical gaps:** clamp(4rem, 8vw, 8rem).
- **Hero layout:** Split-screen (text left, visual right).
- **Feature sections:** Zig-zag alternating text+image rows. No 3-equal-columns.
- **Mobile collapse:** All multi-column layouts collapse below 768px. No horizontal overflow.
- **z-index contract:** base (0) / sticky-nav (100) / overlay (200) / modal (300) / toast (500).


## Elevation & Depth

Tight tracking (-3%), 90% leading

- **Physics:** Ease-out curves, 200-300ms duration. Smooth and predictable.
- **Entry animations:** Fade + translate-Y (16px → 0) over 420ms ease-out. Staggered cascades for lists: 80ms between items.
- **Hover states:** Subtle color shift + shadow adjustment over 200ms.
- **Page transitions:** Fade only (200ms).
- **Performance:** Only transform and opacity animated. No layout-triggering properties.


## Shapes

Base corner radius: 8px. See rounded tokens in front matter for the full scale.


## Components

- **Primary Button:** Subtly rounded (0.5rem) shape. Accent color fill. Hover: 8% darken + subtle lift shadow. Active: -1px translate tactile press. Font weight 600. No outer glows.
- **Secondary / Ghost Button:** Outline variant. 1.5px border in muted color. Text in primary color. Hover: subtle background fill.
- **Cards:** Subtly rounded (0.5rem) corners. Surface background. Subtle shadow (0 2px 12px rgba(0,0,0,0.06)). 1px border stroke.
- **Inputs:** Label above input. 1px border stroke. Focus ring: 2px accent color offset 2px. Error text below in semantic red. No floating labels.
- **Navigation:** Primary surface background. Active item: accent color indicator. Font weight 500 when active.
- **Skeletons:** Shimmer animation matching component dimensions. No circular spinners.
- **Empty States:** Icon-based composition with descriptive text and action button.


## Do's and Don'ts

- No emojis in UI — use icon system only (Lucide, Heroicons)
- No pure black (#000000) — use off-black or charcoal variants
- No oversaturated accent colors (saturation cap: 80%)
- No 3-column equal-width feature layouts — use zig-zag or asymmetric grid
- No `h-screen` — use `min-h-[100dvh]`
- No AI copywriting clichés: "Elevate", "Seamless", "Unleash", "Next-Gen"
- No broken external image links — use picsum.photos or inline SVG
- No generic lorem ipsum in demos

- Do Merriweather Font
- Do Color: #FFE3C3
- Do Tracking -3%
- Do Background #5B3A30


## Use Case

Blogs, news sites, long-form reading, and editorial platforms.

<!-- Source: https://designmd.app/library/merriweather-typography · designmd.app -->
