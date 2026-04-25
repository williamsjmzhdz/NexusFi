# NexusFi — Design System

> Single source of truth for all UI decisions. Every page and component must follow this guide.

---

## Color Palette

| Role | Name | Tailwind Class | Hex | Usage |
|---|---|---|---|---|
| Primary | Indigo | `indigo-600` | `#4F46E5` | Buttons, links, brand accents, focus rings |
| Primary dark | Indigo dark | `indigo-700` | `#4338CA` | Button hover states |
| Primary light | Indigo light | `indigo-50` | `#EEF2FF` | Active nav backgrounds, badges |
| Income / Positive | Emerald | `emerald-500` | `#10B981` | Income amounts, positive balances, success states |
| Expense / Negative | Rose | `rose-500` | `#F43F5E` | Expense amounts, negative balances, error states |
| Page background | Slate 50 | `slate-50` | `#F8FAFC` | App-wide page background |
| Surface | White | `white` | `#FFFFFF` | Cards, modals, sidebar, inputs |
| Border | Slate 200 | `slate-200` | `#E2E8F0` | Input borders, dividers, card outlines |
| Text primary | Slate 900 | `slate-900` | `#0F172A` | Headings, important labels |
| Text secondary | Slate 500 | `slate-500` | `#64748B` | Subtitles, hints, timestamps, placeholders |
| Text disabled | Slate 300 | `slate-300` | `#CBD5E1` | Disabled labels |

---

## Typography

**Font family:** [Inter](https://fonts.google.com/specimen/Inter)  
**Import:** Via Google Fonts in `index.css`

| Scale | Tailwind | Usage |
|---|---|---|
| Display | `text-3xl font-bold` | Brand name / app title |
| Heading 1 | `text-2xl font-semibold` | Page titles |
| Heading 2 | `text-lg font-semibold` | Card headings, section titles |
| Body | `text-sm` | Default body text, labels |
| Caption | `text-xs text-slate-500` | Hints, timestamps, helper text |
| Number large | `text-2xl font-bold tabular-nums` | Balance amounts on dashboard |
| Number small | `text-sm font-medium tabular-nums` | Transaction amounts in lists |

> `tabular-nums` is critical for numbers — it prevents the layout from shifting as digits change.

---

## Spacing & Sizing

| Token | Value | Usage |
|---|---|---|
| Card padding | `p-6` | All card inner padding |
| Section gap | `gap-6` | Grid/flex gaps between cards |
| Form field gap | `space-y-4` | Vertical spacing between form fields |
| Page padding | `px-4 py-8` | Outer page wrapper |
| Max content width | `max-w-7xl mx-auto` | Dashboard content container |

---

## Border Radius

| Element | Class |
|---|---|
| Cards, modals | `rounded-xl` |
| Inputs, buttons, badges | `rounded-lg` |
| Avatar, icon containers | `rounded-full` |

---

## Shadows

| Level | Class | Usage |
|---|---|---|
| Subtle | `shadow-sm` | Cards on white background |
| Elevated | `shadow-md` | Dropdowns, popovers |
| Overlay | `shadow-xl` | Modals |

---

## Logo

File: `src/assets/logo.svg`  
Type: SVG vector — scalable to any size.

**Mark:** Three connected nodes forming an upward triangle — representing a "nexus" (hub of connections) with an upward growth direction. Clean, geometric, works at 16px and 512px alike.

**Usage:**
```tsx
import logo from '../assets/logo.svg';
<img src={logo} alt="NexusFi" className="h-8 w-8" />
```

---

## Icons

**Library:** [Lucide React](https://lucide.dev/)  
**Install:** `npm install lucide-react`

```tsx
import { TrendingUp, TrendingDown, ArrowLeftRight } from 'lucide-react';
```

**Size convention:**
- Nav icons: `size={20}`
- Inline / label icons: `size={16}`
- Feature icons in cards: `size={24}`

---

## Component Patterns

### Input
```tsx
<input className="w-full px-4 py-2.5 border border-slate-200 rounded-lg text-sm
  focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition" />
```

### Primary Button
```tsx
<button className="w-full bg-indigo-600 text-white py-2.5 rounded-lg text-sm font-semibold
  hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition">
```

### Card
```tsx
<div className="bg-white rounded-xl shadow-sm p-6">
```

### Income amount
```tsx
<span className="text-emerald-500 font-medium tabular-nums">+$1,200.00</span>
```

### Expense amount
```tsx
<span className="text-rose-500 font-medium tabular-nums">-$450.00</span>
```

---

## Design Principles

1. **Clarity over decoration** — every element must serve a purpose. No decorative gradients on data.
2. **Numbers are king** — always use `tabular-nums`, ensure high contrast, never truncate amounts.
3. **Semantic color** — never use emerald or rose for anything other than income/expense. Keep the mapping consistent.
4. **Light mode only** — dark mode adds engineering complexity with no current user demand.
5. **Mobile-first** — design for mobile width first, then expand for desktop.
