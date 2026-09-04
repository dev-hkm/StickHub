---
version: "alpha"
name: "Space Grotesk Typography"
description: "Render a 2D isolated text on a solid background. Ideal for developer tools, web3/crypto, tech startups, and data-driven interfaces.. AI-ready template."
colors:
  primary: "#FFE3C3"
  secondary: "#73001C"
typography:
  h1:
    fontFamily: Space Grotesk
    fontSize: 2.5rem
    fontWeight: 700
  body-md:
    fontFamily: Space Grotesk
    fontSize: 1rem
    fontWeight: 400
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    padding: 12px
---

## Overview

Render a 2D isolated text on a solid background. Ideal for developer tools, web3/crypto, tech startups, and data-driven interfaces.. AI-ready template. Space Grotesk emerged from Florian Karsten's 2018 reworking of Space Mono — itself a monospaced typeface commissioned by Google Fonts from Colophon Foundry. Where Space Mono was rigid and utilitarian, Karsten saw the bones of something more versatile. He stripped the monospacing constraint, opened up the proportions, and let the letterforms breathe into a proper proportional sans-serif. The result kept all the personality — those slightly squared curves, the geometric DNA, the subtle nod to 1950s space-age optimism — while becoming genuinely usable for body text and UI.

What makes Space Grotesk interesting isn't novelty. It's restraint with character. The typeface sits in a lineage of geometric grotesks (think Eurostile, Bank Gothic, Microgramma) that defined mid-century techno-futurism, but it doesn't cosplay as vintage. The open apertures and generous x-height are thoroughly contemporary. It reads clean at small sizes on screens — a non-negotiable that many "character" typefaces fumble.

The variable font axis (weight 300–700) shipped from day one, which tells you Karsten was thinking about systems, not specimens. This is a typeface built for interfaces, not posters hanging in design studios.

- Density: 5/10 — Balanced
- Variance: 4/10 — Moderate
- Motion: 4/10 — Subtle

- **Style:** Techy, retro‑futuristic sans
- **Keywords:** Space Grotesk, techy, retro-futuristic, Web3, crypto, data-driven, code DNA
- **Era:** Retro-Futuristic
- **Light/Dark:** ✗ No / ✓ Full

## Colors

- **#FFE3C3** (#FFE3C3) — Primary surface or dominant color
- **#73001C** (#73001C) — Extended palette, decorative use


## Typography

- **Display / Hero:** Space Grotesk — Weight 700, tight tracking, used for headline impact
- **Body:** Space Grotesk — Weight 400, 16px/1.6 line-height, max 72ch per line
- **UI Labels / Captions:** Space Grotesk — 0.875rem, weight 500, slight letter-spacing
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

- Do Space Grotesk Font
- Do Color: #FFE3C3
- Do Tracking -3%
- Do Background #73001C


## Use Case

Developer tools, Web3/crypto, tech startups, and data-driven interfaces.

<!-- Source: https://designmd.app/library/space-grotesk-typography · designmd.app -->
