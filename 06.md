---
version: "alpha"
name: "Tenor Sans Typography"
description: "Render a 2D isolated text on a solid background. Ideal for magazines, minimalist brands, and content-heavy layouts with a sophisticated tone.. AI-ready template."
colors:
  primary: "#FFFFFF"
  secondary: "#E67D54"
typography:
  h1:
    fontFamily: Tenor Sans
    fontSize: 2.5rem
    fontWeight: 700
  body-md:
    fontFamily: Tenor Sans
    fontSize: 1rem
    fontWeight: 400
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    padding: 12px
---

## Overview

Render a 2D isolated text on a solid background. Ideal for magazines, minimalist brands, and content-heavy layouts with a sophisticated tone.. AI-ready template. Tenor Sans occupies a peculiar and deliberate space in type design — it's a sans-serif that refuses to abandon the gestural memory of serif letterforms. Designed by Denis Masharov, it carries the proportional DNA of classical Roman inscriptions while stripping away the serifs themselves. The result is something that feels literate without being decorative, modern without being cold.

What makes Tenor Sans genuinely interesting is its stroke modulation. Unlike geometric sans-serifs that flatten everything into uniform weight, Tenor retains subtle thick-thin contrast — the kind you'd find in a Didone or transitional serif, just whispered rather than stated. This gives headlines a sense of refinement that Helvetica or Futura simply cannot deliver. It reads as cultured.

The typeface works because it understands restraint. It doesn't shout 'luxury' through ornament or extreme contrast. Instead, it communicates sophistication through proportion, through the careful calibration of x-height to cap-height, through terminals that resolve cleanly without calling attention to themselves. It's the typographic equivalent of a well-cut garment — the quality is in the construction, not the embellishment.

- Density: 3/10 — Airy
- Variance: 3/10 — Restrained
- Motion: 4/10 — Subtle

- **Style:** Refined, editorial sans
- **Keywords:** Tenor Sans, refined, editorial sans, minimalist brands, narrow proportions
- **Era:** Contemporary Web
- **Light/Dark:** ✗ No / ✓ Full

## Colors

- **#FFFFFF** (#FFFFFF) — Primary surface or dominant color
- **#E67D54** (#E67D54) — Extended palette, decorative use


## Typography

- **Display / Hero:** Tenor Sans — Weight 700, tight tracking, used for headline impact
- **Body:** Tenor Sans — Weight 400, 16px/1.6 line-height, max 72ch per line
- **UI Labels / Captions:** Tenor Sans — 0.875rem, weight 500, slight letter-spacing
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

Tight tracking (-4%), 90% leading

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
- No decorative gradients — flat color only
- No shadows heavier than 0 2px 8px rgba(0,0,0,0.08)
- No pure black (#000000) — use off-black or charcoal variants
- No oversaturated accent colors (saturation cap: 80%)
- No 3-column equal-width feature layouts — use zig-zag or asymmetric grid
- No `h-screen` — use `min-h-[100dvh]`
- No AI copywriting clichés: "Elevate", "Seamless", "Unleash", "Next-Gen"
- No broken external image links — use picsum.photos or inline SVG
- No generic lorem ipsum in demos

- Do Tenor Sans Font
- Do Color: #FFFFFF
- Do Tracking -4%
- Do Background #E67D54


## Use Case

Magazines, minimalist brands, and content-heavy layouts with a sophisticated tone.

<!-- Source: https://designmd.app/library/tenor-sans-typography · designmd.app -->
