---
version: "alpha"
name: "Cordel Digital"
description: "Brazilian cordel literature style landing page. Ideal for landing pages culturais, projetos nordestinos, sites artísticos. AI-ready template."
colors:
  primary: "#000000"
  secondary: "#FFFFFF"
  tertiary: "#F5DEB3"
  neutral: "#8B4513"
  surface: "#8B0000"
  accent: "#4682B4"
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

Brazilian cordel literature style landing page. Ideal for landing pages culturais, projetos nordestinos, sites artísticos. AI-ready template. Literatura de cordel is Brazil's chapbook tradition — small pamphlets sold hanging from strings (cordéis) at open-air markets across the Northeast since the late 1800s. The stories range from epic bandit tales to political satire, always printed cheap, always passed hand to hand. What makes them visually unforgettable is the xilogravura: woodcut illustrations carved into blocks with pocket knives, printed in stark black ink on rough paper. No gradients. No half-measures. Every line is a commitment.

The woodcut aesthetic carries an inherent boldness that translates remarkably well to screens. High contrast, decisive marks, flattened perspective — these aren't limitations, they're a graphic language refined over a century of constraints. The carvers worked fast and small, which forced an economy of form that feels almost modernist in retrospect.

Digitally, cordel woodcuts offer something rare: immediate cultural specificity without decorative excess. The style reads at any size. It carries narrative weight. It refuses to be mistaken for generic illustration. When you bring xilogravura into digital design, you're not borrowing an aesthetic — you're channeling a living tradition that still sells millions of pamphlets annually in Brazilian street markets.

- Density: 5/10 — Balanced
- Variance: 4/10 — Moderate
- Motion: 8/10 — Cinematic

- **Style:** Narrative, Illustrated, Cultural, Poetic
- **Keywords:** cordel, woodcut, xylography, Brazilian folk, narrative, illustrated, cultural, poetic, handcrafted, traditional, northeastern, sertão
- **Era:** Tradição Nordestina Digital
- **Light/Dark:** ✓ Full / ✗ No

## Colors

- **Preto** (#000000) — Dark surface, primary background
- **Branco** (#FFFFFF) — Light surface, card backgrounds
- **Amarelo Pergaminho** (#F5DEB3) — Warning states, attention indicators
- **Marrom Rústico** (#8B4513) — Supporting palette color
- **Vermelho Sangue** (#8B0000) — Error states, destructive actions
- **Azul Sertão** (#4682B4) — Secondary accent
- **Verde Caatinga** (#556B2F) — Success states, positive indicators
- **Laranja Crepúsculo** (#CD853F) — Warm accent, call-to-action secondary


## Typography

- **Display / Hero:** Playfair Display — Weight 700, tight tracking, used for headline impact
- **Accent:** Merriweather — Used for decorative or emphasis text
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
- **Hero layout:** Split-screen (text left, visual right).
- **Feature sections:** Zig-zag alternating text+image rows. No 3-equal-columns.
- **Mobile collapse:** All multi-column layouts collapse below 768px. No horizontal overflow.
- **z-index contract:** base (0) / sticky-nav (100) / overlay (200) / modal (300) / toast (500).


## Elevation & Depth

Ilustrações em estilo xilogravura como elementos visuais principais, tipografia serifada que remete a folhetos de cordel, layouts que simulam páginas de livreto, bordas decorativas com padrões de cordel, fundos com textura de papel envelhecido, divisores de seção com motivos xilográficos, composições narrativas que contam uma história visual, elementos decorativos de moldura artesanal.

- **Physics:** Spring — stiffness 120, damping 20. Confident, weighted transitions.
- **Entry animations:** Fade + translate-Y (16px → 0) over 540ms ease-out. Staggered cascades for lists: 120ms between items.
- **Hover states:** Scale(1.03) + shadow lift over 200ms.
- **Page transitions:** Fade + slide (300ms).
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

- Do Xilogravura como elementos visuais
- Do Tipografia serifada de cordel
- Do Layout de livreto
- Do Bordas decorativas
- Do Textura de papel envelhecido
- Do Divisores xilográficos.


## Use Case

Cultural landing pages, Regional Brazilian projects, Artistic websites

<!-- Source: https://designmd.app/library/cordel-digital · designmd.app -->
