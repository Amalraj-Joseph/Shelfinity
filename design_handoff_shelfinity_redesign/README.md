# Handoff: Shelfinity UI/UX redesign

## Overview

Shelfinity is library management software. This package is a complete rewrite of its front-end
experience: one visual system, one layout grammar, one set of state rules, applied across
**19 routes** covering members, librarians and admins on **web and mobile**.

The scope is deliberately larger than the existing product. The client supplied 21 screenshots of
the live admin account; those screens are redesigned here, and the flows they imply but never
built — book detail, a staff "Desk" home, circulation, member records, join/registration,
notifications, account & card, system error pages — are specified as well.

**Read `design/Shelfinity UX Spec.dc.html` first.** It is the primary source of truth: the
diagnosis, seven design principles, the role model, component contracts with every state, the
status vocabulary, the copy deck, accessibility rules and mobile transform rules. This README
orients you and repeats the values you will type most often; the spec carries the reasoning and
the per-screen detail.

## About the design files

The files in `design/` are **design references authored in HTML** — prototypes showing intended
look, copy and behaviour. They are **not production code to lift**. They use a bespoke
streaming-template runtime (`support.js`, `.dc.html`) that exists only in the authoring tool; do
not try to port it.

Your task is to **recreate these designs in Shelfinity's existing codebase**, using its
established framework, component library, routing and data layer. Where Shelfinity already has a
primitive that can be restyled to match (a table, a dialog, a button), restyle it rather than
introducing a parallel one. If there is no front-end environment yet, pick the framework that
best fits the team and back end, and implement the designs there.

`design/_ds/modernist-.../styles.css` is the exception: it is real, plain CSS and **should be
adopted directly** as the token layer. Everything visual derives from it.

## Fidelity

**High fidelity.** Colours, type, spacing, copy, layout and interaction states are final and
should be matched closely. Two caveats:

- **Content is representative, not real.** Titles, member names, counts and dates are plausible
  fixtures. Bind them to real data; keep the *sentence shapes* exactly (see Copy, below).
- **Book covers are placeholders.** Every cover in the mockups is a flat grey block with the
  title set in it. That block is also the *real* production fallback for a title with no cover
  image (see Assets).

The mockups are laid out on a canvas as a set of framed screens with ids (`1a`, `1b`, … `1q`)
shown as badges. Those ids are the shared vocabulary — this README and the spec both cite them.

---

## Design tokens

Take every value from `design/_ds/modernist-.../styles.css`. Do not hard-code a hex, a font name
or a px value the tokens already carry. The table is a reading of that file for orientation only.

### Colour

| Token | Value | Role in Shelfinity |
|---|---|---|
| `--color-bg` | `#f3f2f2` | Page ground, every page |
| `--color-surface` | `#eae9e9` | Panels, table shells, nav rail, dialogs — one step only, never stacked |
| `--color-text` | `#201e1d` | All copy, all numbers, all headings |
| `--color-accent` | `#ec3013` | Primary action, current nav item, overdue, poster statements |
| `--color-accent-700` | `#ae1800` | Accent-coloured **text** and links (contrast-safe at body size) |
| `--color-accent-100/200` | `#fff2ef` / `#ffe0d9` | Tinted tag fills |
| `--color-divider` | `#201e1d` at 40% | 2px section rules, 1px row rules, input borders |
| `--color-neutral-200/300` | `#eae7e7` / `#d7d3d3` | Skeleton blocks, cover fallbacks, meter troughs |
| `--color-neutral-800/900` | `#444141` / `#2d2b2b` | Dark image grounds |

**Retire from the current build:** indigo `#4f46e5`, the blue-violet sign-in gradient, success
green, warning amber, info blue. All semantic meaning moves to the status vocabulary below.
Accent-to-ground is tuned to ~3:1 — fine for chrome, icons and large text, **not** for body
copy; use `--color-accent-700` there.

### Type

Archivo throughout (`--font-heading`, `--font-body`), weights 400 / 600 / 800.

| Role | Size / weight | Where |
|---|---|---|
| Poster | 52–76px / 800 | Sign-in statement, system pages |
| Page title | 42px / 800 | One per page, flush left |
| Answer line | 22–32px / 800 | The sentence answering the page's question |
| Section | 20px / 800 | Panel headings (`h4` at 16–17px for sub-panels) |
| Figure | 38–42px / 800, `tabular-nums` | Counters — always with a noun beneath |
| Body | 15px / 400 | Prose |
| UI / table | 13–14px / 400 | Rows, fields, buttons |
| Label | 10–11px / 600, `letter-spacing: .08em`, uppercase | Column heads, group labels, kickers |

Line-height: `1.0–1.05` for poster and page titles, `1.15` for tile titles, `1.3` for answer
lines, `1.5–1.6` for body. All headings and copy **flush left** — never centred.

### Spacing & geometry

- `--space-1…8` = 4 / 8 / 12 / 16 / 24 / 32px. Page gutters **40px** desktop, **20px** mobile.
- **`border-radius: 0` everywhere.** Buttons, inputs, tags, dialogs, avatars, images, covers.
  `--radius-md` is `0px` on purpose. This is the single easiest rule to break by accident.
- Section separation is a **2px** rule (`.hr`), not whitespace alone. Row separation is 1px.
- Shadows only on things that genuinely float — dialogs `--shadow-lg`, menus/toasts
  `--shadow-md`. Panels are flat, with a 1px divider border.
- Content max width 1240px in a 12-column grid, 24px gutters.

### Design-system classes

The stylesheet ships `.btn` (`.btn-primary` / `.btn-secondary` / `.btn-ghost` / `.btn-icon` /
`.btn-block`), `.tag` (`.tag-accent` / `.tag-neutral` / `.tag-outline`), `.field` + `.input` +
`.radio` + `.dot`, `.seg` + `.seg-opt`, `.card`, `.nav`, `.table`, `.dialog`, `.hr`,
`.grayscale`. Map these onto your framework's components rather than re-authoring them.
**Button labels are flush left** — a button wider than its label starts its text at the left
padding edge, never centred.

### Icons

Lucide (https://lucide.dev), 16px in nav and rows, 18–20px in the top bar and mobile tabs.

---

## App shell

### Web, ≥1024px (`1c`, `1e`, `1g`, `1i`, `1k`, `1l`, `1n`, `1o`, `1p`)

- **Nav rail**, 232–240px, `--color-surface`, full height, 2px right rule.
  - Brand block at top: 20px Lucide book-open glyph in accent + "Shelfinity" 18px/800, on an
    18px/16px pad, 2px bottom rule.
  - Group label (11px uppercase, 55% ink) above each group; a 2px top rule separates groups.
  - Items: 40px tall (`padding: 9px 16px`), 14px label, 16px icon, `gap: 10px`, transparent 3px
    left border. **Current item**: 3px accent left border, accent label, weight 600, **no fill**
    — no pill, no rounded highlight. Hover: `color-mix(in srgb, var(--color-text) 6%, transparent)`.
  - Badge counts (`.tag-accent`) push right with `margin-left: auto`.
  - Rail foot: 34×34 square ink avatar with the initial in ground colour, name 13px/600, card
    number + role beneath at 11px, then Settings / Sign out as a ghost link. **The avatar menu
    lives here, not in the top bar.**
- **Top bar**, 60–64px, 2px bottom rule: global search field (max 420–520px, 1px divider border,
  surface fill, leading search glyph, `⌘K` hint right) and a notifications bell. Nothing else.
- **Content**: `--color-bg`, 40px gutters, 32px top pad.

### Mobile, <768px (`1b`, `1d`, `1f`, `1h`, `1j`, `1m`)

Frames are drawn at **390 × 812**.

- **52px header**: page title 19px/800 (or back chevron + 15px/600 title when deep), one trailing
  action.
- **Bottom tab bar**: 2px top rule, surface fill, **max 4 tabs** — Home / Browse / Shelf / More.
  Staff swap Shelf for Desk and find Shelf under More. Each tab: 20px icon over a 10px label,
  `padding: 9px 0 14px`, transparent 2px top border pulled up with `margin-top: -2px`. Current
  tab: accent icon, accent label, weight 600, 2px accent top border. **Never a filled pill.**
- All touch targets **≥44px**. Row actions become a trailing 44px button plus a `•••` sheet — never
  a 20px icon in a table cell.
- **Tablet 768–1023px**: rail collapses to a 64px icon rail with tooltips; content at 2 columns.

### Page header — mandatory and identical on every page

1. Context row: breadcrumb or group label, 10–11px uppercase accent-700; page actions pushed right.
2. **Page title**, 42px/800.
3. **Answer line**, 22px/800 — *one sentence stating the page's finding*, with the urgent clause
   in `--color-accent-700`. Not a description of the page. This is the core pattern of the
   redesign: `"You have 3 books out. 1984 is due Friday."`, `"4 requests waiting, 2 holds to pull,
   nothing overdue."`, `"Nothing is overdue. 4 loans come due this week."`
4. 2px rule.
5. Then, and only then, controls — search, `.seg` tabs, filters. **Never above the title.**

---

## Status vocabulary

Every status word in the product. **Meaning is carried by the word; colour only ranks it** — so
the UI survives colour-blindness and greyscale. Implement as one module that maps state → label +
variant; no component invents its own label or colour. Sentence case, never SHOUTING CAPS.

| Domain | Label | Variant | Lucide glyph |
|---|---|---|---|
| Copies | `On the shelf` | `tag-neutral` | check |
| | `Last copy` | `tag-outline` | alert-circle |
| | `All out` | `tag-neutral` | clock |
| Requests | `Waiting on the library` | `tag-accent` | hourglass |
| | `Approved` | `tag-neutral` | check |
| | `Declined` | `tag-outline` | x |
| | `Withdrawn` | `tag-neutral` | — |
| Loans | `Due in N days` | `tag-neutral` | calendar |
| | `Due Friday` (≤3 days — name the day) | `tag-accent` | calendar |
| | `N days overdue` | **solid accent** (`background: var(--color-accent); color: var(--color-bg)`) | alert-triangle |
| Holds | `Ready for pickup` | `tag-accent` | bookmark |
| | `Nth in queue` | `tag-neutral` | users |
| | `Expired` | `tag-neutral` | — |
| People | `Active` | `tag-neutral` | — |
| | `Blocked` | `tag-accent` | ban |
| Roles | `Librarian` / `Admin` | `tag-outline` (roles are always outline) | — |

The solid-accent overdue tag is the **only** solid-accent tag in the product.

---

## Role model

**One account, capability-scoped.** Admin is a role on an ordinary member account, not a separate
app — so a librarian is also a borrower and never loses the member surfaces. The member
experience is literally this app **with the `MANAGE LIBRARY` group removed from the rail**
(compare the two rails side by side in `1q`).

The rail is **two groups, never three**: `MY LIBRARY` always, then `MANAGE LIBRARY` if the role
grants it. Admin-only settings sit behind a single `Settings` entry at the rail foot, not as five
siblings. A member's rail is four items with no dividers.

| Role | Adds |
|---|---|
| **Member** (everyone) | Home, Browse, Book detail, My Shelf, Lists, Notifications, Account & card |
| **Librarian** | Desk, Requests, Circulation, Catalogue admin, Holds, Overdue, Members |
| **Admin** | Reports, Roles & permissions, Loan policy, Email & notifications, Integrations, Audit log |

---

## Routes and screens

Mockup ids in the last column are frames in `design/Shelfinity Screens.dc.html`. Per-screen
layout detail — including screens with no mockup — is in the spec, §11.1–§11.19.

| Route | Screen | Role | Question it answers | Mockups | Spec |
|---|---|---|---|---|---|
| `/signin` | Sign in | public | Is this a real library, and how do I get in? | `1a` `1b` | §11.1 |
| `/join` | Request a card | public | How do I become a member? | — | §11.2 |
| `/` | Home | member | What do I have, and what needs me today? | `1c` `1d` | §11.3 |
| `/books` | Browse | member | What is worth borrowing? | `1e` `1f` | §11.4 |
| `/books/:id` | Book detail | member | Should I borrow this, and can I? | `1g` `1h` | §11.5 |
| `/shelf` | My Shelf (replaces My Activity) | member | What's out, due, requested, waiting? | `1i` `1j` | §11.6 |
| `/shelf/history` | Reading history | member | What have I read? | — | §11.6 |
| `/lists` | Lists & saved searches | member | What did I mean to read? | — | §11.7 |
| `/notifications` | Notifications | member | What changed while I was away? | — | §11.8 |
| `/account` | Profile, card, preferences | member | Who am I here? | — | §11.9 |
| `/desk` | Desk — staff home | librarian | What must the library do today? | `1k` | §11.10 |
| `/desk/requests` | Requests queue | librarian | Who is waiting on a decision? | `1l` `1m` | §11.11 |
| `/desk/circulation` | Check out / check in | librarian | Hand over or take back, fast | — | §11.12 |
| `/desk/catalogue` | Catalogue admin + import | librarian | Is the catalogue true? | — | §11.13 |
| `/desk/catalogue/:id/edit` | Add / edit a title | librarian | — | — | §11.13 |
| `/desk/holds` | Reservations & holds | librarian | What must be pulled and shelved? | — | §11.14 |
| `/desk/overdue` | Overdue & recalls | librarian | What is late, who do we nudge? | `1n` | §11.15 |
| `/desk/members` `/:id` | Members, member record | librarian | Who is this person, what do they have? | — | §11.16 |
| `/reports` | Reports | admin | How is the library doing? | `1o` | §11.17 |
| `/settings/*` | Policy, email, roles, integrations, audit | admin | How does the library behave? | `1p` | §11.18 |
| `/404` `/500` `/offline` | System pages | any | Something broke; now what? | — | §11.19 |

`1q` is a **state gallery**: loading skeletons, both empty-state flavours, an inline error, the
full tag set, the button set, and the member and staff rails compared. Build from it directly.

---

## Components to build

Full anatomy and state lists in spec §7.1–§7.16. The ones with non-obvious geometry:

**FigureBlock** (§7.3) — replaces the stat card. Equal cells of **one** panel divided by 1px
rules (`display: grid` + `border-right` on all but the last), *not* four floating cards. Number
38–42px/800 `tabular-nums`; noun beneath at 11px uppercase, 55% ink, 6px above. Max 4 per row, 3
tablet, 2 mobile. On dashboards it goes at the **foot**, not the top — a page whose first row is
four counters is the thing being fixed.

**BookTile** (§7.4) — cover 2:3 through `.grayscale`, title 16px/800 line-height 1.15, author
12px at 55% ink, availability *sentence* at 12px ("All 3 copies on the shelf", "4 of 5 on the
shelf", "All out — 3 people waiting"), one `.btn-block` action. Whole tile is the link. Grid: 5
columns at 1440, 4 at 1200, 3 at 1024, 2 at mobile; gap 24px/20px.

**DataTable** (§7.5) — surface shell with a 1px divider border. 11px uppercase heads on a 2px
rule; 1px row rules; 44px rows. **First column is human identity** — name 600 weight with email
or author beneath at 12px/55% ink. **No UUIDs anywhere** (the live Reservations screen prints
`6f160ada-81f9-…` under the title; expose ids only behind a copy-id affordance on a detail view).
Right-most column is actions, right-aligned, `white-space: nowrap`, always present for keyboard
and touch. Foot bar: "1–4 of 4" at 13px plus keyboard hints.

**DueMeter** (§7.15) — the one piece of information design here. 4px-tall bar,
`--color-neutral-300` trough, ink fill; the fill switches to `--color-accent` once inside 3 days
or overdue. Percentage = elapsed / loan length. Always paired with a text label that spells the
days, so it reads without colour: `"Due Friday — 3 days left"`.

**Skeleton** (§7.10) — solid `--color-neutral-300` blocks matching the real geometry, 3–6 rows,
1.4s opacity pulse. **Never a spinner over stale content.**

**EmptyState** (§7.9) — left-aligned inside the panel that would have held the data. 17–20px/800
headline saying what the place is *for*, one 13–14px sentence, one action. Never centred, never an
illustration, never the word "No".

**Toast + Undo** (§7.11) — bottom-left, surface, `--shadow-lg`, 3px accent left border,
`padding: 14px 16px`, 380px, one sentence in 14px, ghost "Undo" (+ "Add a note" where relevant),
8s, stacks to 3. This is the *only* confirmation most actions need.

---

## Interactions & behaviour

### Optimistic and reversible (§9.4) — the biggest behavioural change

- **Approve / Decline act on click.** No dialog. The row restyles immediately, a toast offers
  **Undo** for 8s. The optional remark — which today is the entire content of a modal — moves
  into the toast ("Add a note") and the request detail. It never gates the decision.
- **Decline requires a reason**, chosen from four options (`Not a member yet`, `No copies free`,
  `Duplicate request`, `Other` + note) as an **inline expansion of the row** (see `1l`, row 3),
  or a bottom sheet on mobile (`1m`). The member sees the reason.
- **Reserve / Cancel / Renew / Withdraw** act immediately with undo.
- **Dialogs only for the irreversible**: delete a title, delete a member, revoke a role. Title
  states the consequence — "Delete 1984 and its 3 copies?" — body names what is lost, primary
  button repeats the verb.
- **Bulk**: selecting rows raises a bar with the count and the 2–3 verbs that apply; a bulk
  approval undoes as one batch.

### Loading (§9.1)

First paint of a route: page header renders immediately (the title is known), skeletons in the
real geometry below. Refetch of existing data: **keep the data**, show a 2px accent progress line
under the top bar — never dim, never spinner-over-table. A single row acting: that row's action
area becomes an inline "Working…" / "Approving…" label and the row stays readable. Nothing blocks
the whole screen after first paint.

### Empty (§9.2) — three distinct flavours, each with its own copy

1. **Nothing yet** — invite: *"Nothing reserved yet. Reserve any title that's all out and we'll
   hold a copy for you at the desk."* + Browse.
2. **Nothing matched** — offer to relax: *"Nothing matched 'dune messiah'. The closest we have
   are Dune and Children of Dune."* + Clear filters + Ask us to buy it.
3. **Nothing left** — congratulate. Overdue at zero is *good news* and must read that way
   (`1n`): the answer line becomes "Nothing is overdue. 4 loans come due this week.", the figures
   show 0 / 0 / 4, and the table is replaced by the due-this-week list with a "Send a nudge" per
   row. The page stays useful when it has nothing to complain about.

### Error (§9.3)

Inline where the failure happened; page-level only when the page can't render. Say what failed,
what is intact, what to do, and offer Retry: *"We couldn't reach the catalogue. Your loans and
holds are safe. Nothing was lost."* Field errors sit beneath the field in `--color-accent-700`
with the fix ("Port must be between 1 and 65535"). Never a stack, a code, or "error" alone.

### Motion budget (§14)

120ms for state changes on the element you touched; 180ms ease-out translate for entering
surfaces (sheet, dialog, toast). No page transitions, no parallax, no animated numbers. The only
looping animations in the product are the skeleton pulse and the 2px refetch line. Honour
`prefers-reduced-motion` by dropping both.

### Keyboard

`⌘K` global search · `/` focuses the page search · `g` then `h`/`b`/`s`/`d` jumps to Home /
Browse / Shelf / Desk. In the requests queue — where staff live — `j`/`k` move, `a` approve, `d`
decline, `u` undo. Escape closes the topmost surface only.

### Mobile transform rules (§15)

Apply these to derive any phone screen from its desktop counterpart, including screens with no
mobile mockup:

| Desktop | Mobile |
|---|---|
| 240px nav rail | 4-tab bottom bar, remainder under More |
| DataTable | Cards: identity line, two facts, one primary verb, `•••` sheet |
| FigureBlock row of 4 | 2 × 2 grid, same divided panel |
| Facet column | "Filter (2)" button → bottom sheet with Apply / Clear |
| Right-hand secondary column | Below the primary content, or into More if reference |
| Dialog | Bottom sheet, full-width 44px buttons, primary last for thumb reach |

Breakpoints: <768 phone · 768–1023 tablet · 1024–1439 desktop · ≥1440 wide (5-column browse
grid, content capped 1240px). Test the requests queue and the browse grid at every one — they are
the two that break.

---

## State management

Per-route data plus these cross-cutting concerns:

- **Session**: account, capability set (drives which rail groups render), card number.
- **Per-route fetch state**: `idle | loading | refetching | ready | error` — `refetching` must be
  distinguishable from `loading` because they render differently (see Loading).
- **Optimistic mutation queue**: each entry holds the target id, the previous state, the applied
  state and an 8s undo timer. Undo restores the previous state and cancels the server call (or
  issues the inverse). Bulk actions hold an array and undo as one.
- **Toast stack**: max 3, FIFO, each with optional undo and note handlers.
- **Row-level busy flags**, so one row can act while the rest of the table stays interactive.
- **Facet/search state in the URL** — a Browse search must be shareable and savable ("Save this
  search" appears in the facet column).
- **Conflict handling**: a request decided by someone else refreshes that row in place with
  *"This was already decided by Priya 2 minutes ago."* — never a full-page reload.
- **Derived, not stored**: the answer line on every page, and every status label (both computed
  from data through the vocabulary module).

Data requirements the current API may not cover: copy-level records with condition and shelf
location; hold queue positions and expiry; per-loan elapsed/total for the DueMeter; relative
waiting time on requests; decline reasons; "readers also borrowed" adjacency; cover images.

---

## Copy

Tone: a good librarian — plain, warm, specific, second person, never system-voice. Name the day,
not the timestamp. Sentence case everywhere, including tags and buttons. Numbers live inside
sentences. Full before/after deck in spec §13; the pattern:

| Today | Rewrite |
|---|---|
| `Welcome, Amalraj` / `Here's what's happening in your library.` | `Good afternoon, Amalraj` / **`You have 3 books out. 1984 is due Friday.`** |
| `3 of 3 copies available` | `All 3 copies on the shelf` |
| `Request to Borrow` | `Borrow this` |
| `PENDING` | `Waiting on the library` · *since Monday* |
| `ACTIVE · Expires 1/9/2026` | `Ready for you at the front desk until Tuesday` |
| `No reservations yet.` | `Nothing reserved. Reserve any title that's all out and we'll hold it for you.` |
| `No rows` (overdue) | `Nothing overdue. The shelves are square.` |
| `No email configuration set up yet.` | `Email isn't set up, so members aren't being told when a hold is ready.` |
| `Approve request` + optional-remark modal | *(no modal)* `1984 returned. Tom's copy is back on the shelf.` · Undo · Add a note |

**Never** in user-facing copy: ALL-CAPS status words, exclamation marks, emoji, "Oops!",
"Something went wrong" with no object, UUIDs, HTTP codes, field names, or domain jargon on member
surfaces (circulation, patron, bibliographic record). In-app notifications and their matching
emails must use the **same sentence**.

---

## Accessibility

- Body copy ≥ 4.5:1 — accent text is always `--color-accent-700`, never `--color-accent` at 14px.
- Status is word-first; colour is redundant everywhere (see the vocabulary table).
- Focus: `outline: 2px solid var(--color-accent); outline-offset: 2px` on **every** interactive
  element including table rows and book tiles. Never the default blue ring.
- Targets ≥44px touch, ≥32px pointer.
- Real `<th scope>`, table captions, and row action labels that name the row: *"Approve Amalraj's
  request for 1984"*.
- Dialogs trap focus, restore it on close, are labelled by their title.
- Toasts `aria-live="polite"`; errors `assertive`.
- Alt text describes the room, not the mood: *"The main reading room, looking toward the tall
  windows."*

---

## Assets

| File | Use | Notes |
|---|---|---|
| `design/assets/reading-room.png` | Sign-in, web + mobile (`1a`, `1b`) | **Stand-in.** Drawn to the design system — flat, architectural, greyscale, ink silhouettes. Replace with a real photograph of the library's own reading room. |
| `design/assets/desk-card.png` | Join / request a card (spec §12) | **Stand-in**, same treatment. Two hands passing a card across the desk. |
| `design/image-slot.js` | Authoring-tool placeholder component | **Do not ship.** It exists so the client can drop photos into the mockups. |

Photography rules (spec §12): **five surfaces, one photograph each** — sign-in, join, first-run
Home, the Browse "Staff picks" rail, and the printed Reports header. Everywhere else the imagery
is **book covers**, which do more humanising work per pixel than stock photography. Always
through `.grayscale`, always zero radius, never tinted. Use the library's own building and staff —
no stock smiling students, no AI-generated shelves, no illustration set. A photograph never
carries meaning that isn't also in text.

**Cover fallback** (needed in production — 6 of 103 titles have no cover): a flat
`--color-neutral-200` block with a 1px divider border, the title in Archivo 800 bottom-left at
2:3. Never a grey box with a broken-image glyph. Real covers get the same 1px divider edge so
pale covers keep an edge against the ground.

Icons: Lucide, linked not vendored. Archivo loads from Google Fonts in `styles.css` — vendor it
if the deployment needs to be offline.

---

## Build order

1. **Tokens first.** Adopt `styles.css`; delete every hard-coded hex, every `border-radius` and
   the sign-in gradient. Nothing else starts until this lands.
2. **Shell.** Nav rail with the two-group role rule, top bar, PageHeader with the answer line.
   Every route adopts it before any route is redesigned.
3. **Primitives** (§7.1–7.16) with all their states — skeletons and empty states built in from
   the start, not retrofitted.
4. **Status vocabulary module.** One mapping, imported everywhere.
5. **Member path**: Home → Browse → Book detail → My Shelf. This is where the product is judged.
6. **Staff path**: Desk → Requests (optimistic + bulk) → Circulation → Catalogue.
7. **Admin path**: Reports → Settings sections.
8. **System pages, notifications, email templates** — last, but before launch.

### Definition of done, per route

Shell ✓ · PageHeader with a real answer line ✓ · all four data states ✓ · statuses from the
vocabulary ✓ · mobile per the transform rules ✓ · keyboard + focus ring ✓ · copy passes the tone
rules ✓ · no token violations ✓.

### Easy to break by accident

No `border-radius`, anywhere. No new colour — reaching for green or amber means the vocabulary
was missed. No spinner over existing content. No modal for anything reversible. No UUID, HTTP
code or field name in user-facing copy. No centred text. No dashboard whose first row is four
counters. Every table needs its **filtered-empty** state written, not just its empty state.

---

## Open questions — do not guess in code

1. **Fines and fees** — in scope? The design reserves a slot on Holds and the member record, but
   no policy was given.
2. **Single branch or multi-branch?** Shelf location ("Floor 2 · Bay 14 · Fiction HER") assumes
   one building.
3. **Loan length, limits, renewal policy** — the UI shows Renew and computes due dates; the rules
   are unknown. Settings → Loan policy is specified as the place they live.
4. **Can the IBM App ID sign-in page be themed?** The design mitigates by announcing the hand-off
   before the jump ("You'll finish signing in with your institution, then land back here") and
   passing a return URL to Home. If theming *is* possible: ground, Archivo, zero radius, accent
   button, and remove the default-social-login warning before launch.
5. **Is there a notification/email queue, or is email fire-and-forget?** Affects what Settings →
   Notifications & email can report about delivery.

---

## Files

```
design/
  Shelfinity UX Spec.dc.html      ← PRIMARY SOURCE. Read first. §1–§16.
  Shelfinity Screens.dc.html      ← 17 mockups, ids 1a–1q, web + mobile.
  support.js                      ← authoring runtime. Needed to open the two files
                                    above in a browser. Do not port.
  image-slot.js                   ← authoring placeholder. Do not ship.
  assets/reading-room.png         ← stand-in artwork, sign-in
  assets/desk-card.png            ← stand-in artwork, join
  _ds/modernist-.../styles.css    ← ADOPT DIRECTLY. The token layer.
  _ds/modernist-.../readme.md     ← design-system guidance (do / don't, components)
  _ds/modernist-.../_ds_bundle.js ← design-system component bundle (reference)
```

Open either `.dc.html` file directly in a browser — `support.js` renders it. The mockup file is a
pan-and-zoom canvas; frame ids are the badges at each frame's top-left.
