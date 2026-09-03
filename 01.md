---
version: "alpha"
name: "Vintage Botanical Scientific Illustration"
description: "Vintage botanical landing page, scientific illustration style, aged paper background, plant diagram aesthetics, scholarly look, organic colors. Ideal for landing pages, modern websites. AI-ready template."
colors:
  primary: "#F0E5D3"
  secondary: "#1A1512"
  tertiary: "#BF6B63"
  neutral: "#4A6741"
  surface: "#5D4037"
  accent: "#2F2F2F"
typography:
  h1:
    fontFamily: Playfair Display
    fontSize: 2.25rem
    fontWeight: 700
  body-md:
    fontFamily: Playfair Display
    fontSize: 1rem
    fontWeight: 400
  label-caps:
    fontFamily: Playfair Display
    fontSize: 0.75rem
    fontWeight: 500
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.neutral}"
    padding: 12px
---

## Overview

Vintage botanical landing page, scientific illustration style, aged paper background, plant diagram aesthetics, scholarly look, organic colors. Ideal for landing pages, modern websites. AI-ready template. Before photography existed, science depended on artists who could render a pistil with the accuracy of a surgeon. Maria Sibylla Merian sailed to Suriname in 1699 and came back with illustrations that rewrote entomology. Ernst Haeckel turned radiolarians into Art Nouveau fever dreams. These weren't decorations — they were data. Every vein on a leaf, every stamen count, carried taxonomic weight.

That obsession with fidelity never really left us. It just migrated. When Aesop built a skincare empire on brown apothecary bottles and line-drawn botanicals, they weren't inventing a new aesthetic — they were borrowing centuries of scientific credibility. The implicit message: we understand plants at a molecular level. Trust us with your skin.

Now the style sits at a fascinating intersection. It signals both intellectual rigor and sensory luxury. A hand-rendered Echinacea specimen on packaging says 'we did the research' in a way no stock photo ever could. The tradition carries weight precisely because it was never meant to be pretty — it was meant to be true.

- Density: 5/10 — Balanced
- Variance: 8/10 — Expressive
- Motion: 4/10 — Subtle

- **Style:** Scholarly, Organic, Timeless
- **Keywords:** botanical, vintage, scientific, natural, plants, illustration, parchment, academic
- **Era:** Victorian Science
- **Light/Dark:** ✓ Full / ✗ No

## Colors

- **Background** (#F0E5D3) — Primary background surface
- **Text** (#1A1512) — Primary text color
- **Accent** (#BF6B63) — Primary accent, CTAs and interactive elements
- **Leaf Green** (#4A6741) — Success states, positive indicators
- **Earth Brown** (#5D4037) — Extended palette, decorative use
- **Faded Ink** (#2F2F2F) — Primary text color


## Typography

- **Display / Hero:** Playfair Display — Weight 700, tight tracking, used for headline impact
- **Body:** Playfair Display — Weight 400, 16px/1.6 line-height, max 72ch per line
- **UI Labels / Captions:** Playfair Display — 0.875rem, weight 500, slight letter-spacing
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
- **Hero layout:** Asymmetric composition.
- **Feature sections:** Asymmetric grid with varied card sizes. No 3-equal-columns.
- **Mobile collapse:** All multi-column layouts collapse below 768px. No horizontal overflow.
- **z-index contract:** base (0) / sticky-nav (100) / overlay (200) / modal (300) / toast (500).


## Elevation & Depth

Hand-drawn botanical anatomy, ornate vine borders, aged parchment, coffee stains, watercolor wash, ink bleed.

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

- Do Aged paper/parchment texture
- Do Detailed botanical illustrations
- Do Serif typography
- Do Scientific labels/diagram style
- Do Earthy color palette


## Use Case

Landing pages, Modern websites

<!-- Source: https://designmd.app/library/vintage-botanical-scientific-illustration · designmd.app -->
