---
version: "alpha"
name: "Hand-Drawn Sketch-Note"
description: "Hand drawn landing page, notebook paper background, sketch style, doodles, handwriting font, casual and fun design. Ideal for landing pages, modern websites. AI-ready template."
colors:
  primary: "#F4F1EA"
  secondary: "#0E2A5C"
  tertiary: "#D12828"
  neutral: "#333333"
  surface: "#FFFFA5"
  accent: "#0000FF"
typography:
  h1:
    fontFamily: Patrick Hand
    fontSize: 2.5rem
    fontWeight: 700
  body-md:
    fontFamily: Patrick Hand
    fontSize: 1rem
    fontWeight: 400
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.neutral}"
    padding: 12px
---

## Overview

Hand drawn landing page, notebook paper background, sketch style, doodles, handwriting font, casual and fun design. Ideal for landing pages, modern websites. AI-ready template. Mike Rohde didn't invent doodling in meetings — everyone does that. What he did with The Sketchnote Handbook in 2012 was give permission. He said: your notes don't need to be linear. They don't need to be pretty. They need to be yours. That single idea unlocked visual thinking for people who'd never picked up a Micron pen.

The hand-drawn aesthetic carries something typed text never will — vulnerability. A wobbly arrow says "I'm figuring this out with you." A boxed keyword says "this matters, but loosely." There's no pretense of finality. Dan Roam's Back of the Napkin work pushed this further into business contexts, proving that rough visuals communicate faster than polished decks.

Then Excalidraw arrived and changed the game for digital products. Suddenly every whiteboard tool wanted that sketchy, imperfect look. Miro, FigJam, tldraw — they all lean into hand-drawn strokes because users feel less precious about rough-looking canvases. The aesthetic lowers the barrier to contribution. That's not decoration. That's interaction design.

- Density: 5/10 — Balanced
- Variance: 8/10 — Expressive
- Motion: 4/10 — Subtle

- **Style:** Authentic, Informal, Creative
- **Keywords:** sketch, note, hand-drawn, doodle, paper, notebook, pencil, casual, fun
- **Era:** Modern Casual
- **Light/Dark:** ✓ Full / ✗ No

## Colors

- **Background** (#F4F1EA) — Primary background surface
- **Text** (#0E2A5C) — Primary text color
- **Accent** (#D12828) — Primary accent, CTAs and interactive elements
- **Graphite Grey** (#333333) — Secondary text, borders, muted elements
- **Highlighter Yellow** (#FFFFA5) — Warning states, attention indicators
- **Ballpoint Blue** (#0000FF) — Secondary accent


## Typography

- **Display / Hero:** Patrick Hand — Weight 700, tight tracking, used for headline impact
- **Body:** Patrick Hand — Weight 400, 16px/1.6 line-height, max 72ch per line
- **UI Labels / Captions:** Patrick Hand — 0.875rem, weight 500, slight letter-spacing
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

Spiral binding, coffee ring stains, arrows, stick figures, speech bubbles, matte paper grain, horizontal rule lines, ink bleed.

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

- Do Notebook paper background (lines/grid)
- Do Handwriting-style fonts
- Do Doodle/Sketch illustrations
- Do Imperfect/Organic shapes
- Do 'Sticker' or 'Tape' effects


## Use Case

Landing pages, Modern websites

<!-- Source: https://designmd.app/library/hand-drawn-sketch-note · designmd.app -->
