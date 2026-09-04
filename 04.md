---
version: "alpha"
name: "Fraunces Typography"
description: "Render a 2D isolated text on a solid background. Ideal for branding and logos, headlines, hero sections, and editorial layouts.. AI-ready template."
colors:
  primary: "#C4B773"
  secondary: "#114A34"
typography:
  h1:
    fontFamily: Fraunces
    fontSize: 2.5rem
    fontWeight: 700
  body-md:
    fontFamily: Fraunces
    fontSize: 1rem
    fontWeight: 400
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    padding: 12px
---

## Overview

Render a 2D isolated text on a solid background. Ideal for branding and logos, headlines, hero sections, and editorial layouts.. AI-ready template. Fraunces is one of those typefaces that feels like it shouldn't exist in a variable font format — and that's exactly why it works. Designed by Phaedra Charles and published through Google Fonts, it draws from the tradition of old-style serifs: the kind you'd find on a 1920s whiskey label or a hand-lettered bookplate. High contrast strokes, soft terminals, and a deliberate irregularity that refuses to feel mechanical.

What makes Fraunces genuinely interesting is the WONK axis. Crank it up and the letterforms shift from conventional to expressive — optical sizes that feel hand-drawn without the pretense. The soft axis controls the curvature of terminals, letting you dial between sharp and pillowy. It's a typeface that rewards exploration rather than just picking a weight and moving on.

In digital editorial, Fraunces brings something rare: actual warmth. Not the sterile elegance of a Didone, not the safe neutrality of a geometric serif. It feels human, slightly imperfect, like ink pressed into good paper. That quality is hard to manufacture and impossible to fake with a lesser typeface.

- Density: 5/10 — Balanced
- Variance: 4/10 — Moderate
- Motion: 4/10 — Subtle

- **Style:** Premium, Old money style
- **Keywords:** Fraunces, classic serif, warm, offbeat personality, heritage, fintech, editorial
- **Era:** Classic Editorial
- **Light/Dark:** ✗ No / ✓ Full

## Colors

- **#C4B773** (#C4B773) — Primary surface or dominant color
- **#114A34** (#114A34) — Extended palette, decorative use


## Typography

- **Display / Hero:** Fraunces — Weight 700, tight tracking, used for headline impact
- **Body:** Fraunces — Weight 400, 16px/1.6 line-height, max 72ch per line
- **UI Labels / Captions:** Fraunces — 0.875rem, weight 500, slight letter-spacing
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

Tight tracking (-2%), 90% leading

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

- Do Fraunces Font
- Do Color: #C4B773
- Do Tracking -2%
- Do Background #114A34


## Use Case

Branding and logos, headlines, hero sections, and editorial layouts.

<!-- Source: https://designmd.app/library/fraunces-typography · designmd.app -->
