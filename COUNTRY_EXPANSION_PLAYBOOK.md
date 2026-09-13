# NearWake Country Expansion Playbook

Last updated: 2026-09-13

**Purpose:** turn the Sri Lanka plan into a repeatable process so we can plan *any* country the same way — without rewriting the app or writing a full plan for markets that don't need one.

Read alongside:
- [MARKET_STRATEGY_PLAN.md](MARKET_STRATEGY_PLAN.md) §4 (one binary, region-adaptive) and §3 (pipeline)
- [SRI_LANKA_PRODUCTION_DATA_PLAN.md](SRI_LANKA_PRODUCTION_DATA_PLAN.md) — the fully worked example this template is abstracted from

---

## 1. The principle: one engine, per-country data packs

We do **not** build a new app per country. The arrival-assurance engine (staged alerts, confidence, offline mode, recovery) is location-agnostic; an eligible market can use Google Places where coverage, terms, billing, and local law permit it. Each approved country is a **data pack + localization layer** on top of that engine, produced by the same pipeline (MARKET_STRATEGY_PLAN §3).

So "plan for each country" = decide, per country, **how much local investment it earns**, then instantiate the template at that level.

---

## 2. Effort tiers — not every country gets a full plan

This is what makes per-country planning tractable. Eligible countries can receive Level 0 after release checks; we spend Level 1/2 effort only where the score (Section 3) justifies it.

| Level | What ships | Work required | Who gets it |
|-------|-----------|---------------|-------------|
| **L0 — Core only** | App available; Google Places search; full engine; English (or device locale) UI | Low after shared foundation | Eligible countries after API, legal, billing, and quality checks |
| **L1 — Light localization** | + UI language pack; curated landmark aliases for top cities; local-device battery test; localized store listing | Days–weeks | Countries with a language barrier or strong landmark culture but no data relationship yet |
| **L2 — Full local pack** | + route-number index, corridor validation, dead-zone profiles, official transport-data relationship, trilingual/multi-script search | Weeks–months, ongoing | Beachhead + proven-demand markets (Sri Lanka is here) |

**Rule:** a country only graduates L0 → L1 → L2 on evidence (installs, retention, requests), never speculatively. Sri Lanka is L2 because it's the beachhead; market #2 starts at L0 and earns its way up.

---

## 3. Country prioritization model

Score each candidate 1–5 on each factor, multiply by weight, and rank. This decides investment sequence; L0 availability still requires API, legal, billing, localization, and quality checks.

| Factor | Weight | 5 = strong signal | 1 = weak signal |
|--------|:------:|-------------------|-----------------|
| **Commuter pain** | ×3 | Long routes, nap/distraction culture, high transit dependence | Short trips, car-first, low transit use |
| **Architecture fit** | ×3 | Route-number + landmark navigation, no fixed timetables, dead zones common | Address-based, reliable schedules, full coverage |
| **Competition gap** | ×2 | No arrival-assurance incumbent | Strong local arrival app already loved |
| **Reachability** | ×2 | Founder/network access, feasible language(s), obtainable transport data | No access, many scripts, closed data |
| **Device reality** | ×1 | Budget Android dominant (our reliability edge matters most) | iOS-heavy or flagship-heavy |
| **Places quality** | ×1 | Google Places weak locally (our local index adds real value) | Places already excellent (less L2 upside) |
| **Market size** | ×1 | Large Android commuter base | Small |

Score = Σ(rating × weight). Shortlist the top few for L1/L2; other eligible markets remain L0 or unavailable until their release checks pass.

### 3.1 Preliminary shortlist (hypotheses to validate, not verified facts)

These are candidate markets that pattern-match Sri Lanka's reality. Ratings are first-guess and must be confirmed with the Section 4 template before any investment.

| Country | Why it fits the pattern | First-guess tier |
|---------|------------------------|:----------------:|
| **India** | Route-number + landmark navigation, huge budget-Android transit population, informal bus systems, dead zones; but large/fragmented, many languages | L2 candidate (start with one metro/state) |
| **Bangladesh** | Dense bus commuting, landmark-first, weak timetables, budget devices | L2 candidate |
| **Pakistan** | Similar transit reality, large Android base | L1 → L2 |
| **Kenya / Nigeria** | Matatu/danfo informal transit, variable schedules, landmark navigation, and budget phones may suit offline destination assurance; validate locally | L2 candidate |
| **Philippines / Indonesia / Vietnam** | Jeepney/angkot/informal + long commutes, budget Android, nap culture | L1 → L2 |
| **Egypt** | Microbus informal transit, landmark navigation | L1 |
| **Diaspora markets (UK, Gulf, Canada, Australia)** | Sri Lankan/South-Asian diaspora — L0 core already serves them; localized store listing only | L0 + listing |

**Do not spread thin.** Prove the template on market #2 end-to-end before opening #3. One deep win beats five shallow packs.

---

## 4. The per-country template (fill in the blanks)

To plan any country, copy this into `<COUNTRY>_MARKET_PLAN.md` and complete each slot. It's the Sri Lanka plan, parameterized.

```markdown
# <Country> Market Plan  — Target tier: L__

## 1. Market snapshot & why
- Transit dependence / commuter volume:
- Nap-and-distraction culture evidence:
- Prioritization score (Section 3):
- Beachhead corridor(s) chosen:

## 2. Incumbent landscape
- Existing transit/tracker/planner apps (name, what they do, review complaints):
- Arrival-assurance gap confirmed? (Y/N):
- Positioning vs incumbents (the layer they don't have):

## 3. Local reality → product requirements
| Local reality | Product requirement |
|---------------|---------------------|
| Primary navigation model (route # / landmark / address) | |
| Languages / scripts | |
| Timetable availability | |
| Dominant devices / OEM battery killers | |
| Data-cost sensitivity | |
| Rail vs bus mix | |
| Dead-zone corridors | |

## 4. Localization scope
- UI languages at launch:
- Search input scripts + romanization/typo tolerance:
- Alert audio/haptic tuning for local environment (loud buses, etc.):

## 5. Data sources (per data-plan tiers)
- Tier 1 official (transport authority / railway — name, URL, contact):
- Tier 2 map/coordinates (OSM/Overpass extract region):
- Tier 3 owned aliases (landmark/route nickname list owner):
- Tier 4 community corrections (later, moderated):
- Structured-export request sent? (adapt SRI_LANKA_PRODUCTION_DATA_PLAN §7 template):

## 6. Pipeline instantiation
- Importer for this source format:
- Alias-enrichment language set:
- Corridor coverage tiers assigned:
- Dead-zone profiles collected in field test:
- Seed pack: in-APK or on-demand delivery?

## 7. Corridor-first launch phases
- Phase 1 (launch corridors + rail):
- Phase 2 (intercity):
- Phase 3 (wider + official relationships):

## 8. Field-test device matrix
- Locally common devices × screen-off × battery saver × Doze:
- Delivery target: ≥99% on launch corridor before public listing.

## 9. Go-to-market
- Distribution channels (commuter communities, universities, etc.):
- Trust-language slogan in local language(s):
- Play Console: country targeting, staged rollout %, localized listing.

## 10. Success gates (before expanding this country's coverage)
- Alert delivery %:
- D7 / D30 retention:
- Rating:
- Then: expand corridors OR open next country.
```

---

## 5. How this shows up in the product & pipeline

- **Region detection** (SIM MCC / locale / user picker, overridable) selects the active pack; absent a pack, the country runs L0 (Places only) on the same code path. (MARKET_STRATEGY_PLAN §4.2)
- **Packs are modular and on-demand** — non-relevant packs are never downloaded; APK stays small.
- **Schema is additive** — route/stop/alias entities in `core:database` are additions only (frozen-entities constraint respected); a new country adds rows/packs, not migrations.
- **One file per L2 country** in the repo root, following the Section 4 template; L0/L1 countries need only a store-listing entry and (for L1) an alias list.

---

## 6. Sequenced expansion (the operating loop)

```
1. Build the reusable L0 foundation; enable only approved markets. [one-time]
2. Score candidates (Section 3).                                  [quarterly]
3. Pick ONE next market. Instantiate the template (Section 4).
4. Build pack via pipeline; field-test to ≥99% delivery.
5. Launch corridor-first in that country (Play country targeting).
6. Hit success gates (Section 4 §10) → expand its coverage.
7. Only then: return to step 2 for the next country.
```

**Bottom line:** we can evaluate each candidate country with the Sri Lanka template, but availability and investment are gated by evidence, platform coverage, compliance, and an effort tier. Sri Lanka is the fully worked instance; each approved market uses the same template at the level it has earned.
