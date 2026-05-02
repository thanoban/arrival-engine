# Sri Lanka Production Data Plan

This file explains how NearWake should handle Sri Lanka transport data in a professional production path.

It answers:

- why we can start from public PDFs now
- why PDFs should not remain the long-term source of truth
- what official sources we can use
- what to ask NTC and Sri Lanka Railways for
- what production ops and compliance work must happen around the data
- how to build a scalable data pipeline without repainting the whole app later

## 1. Short Answer

Yes, we can start with the public NTC PDF and related public transport pages.

That is good enough for:

- first route-number import
- route-name normalization
- origin/destination seed data
- early Sri Lanka-focused search experiments

But for production, that should become:

- a bootstrap source
- a fallback source
- not the final long-term data contract

The professional target is:

1. use public documents now
2. normalize them into our own clean schema
3. ask the transport authorities for structured exports and usage permission
4. swap the importer later without changing the app's feature layer

## 2. Why PDF Is Enough To Start

PDF is enough to begin because we can already extract meaningful fields such as:

- route number
- origin
- destination
- service type
- permit validity markers

For example, the current public permit PDF already exposes route-oriented rows:

- [NTC valid permit details PDF](https://ntc.gov.lk/corporate/pdf/2023/permit-Info/Valied%20Permit%20Details.pdf)

That lets us build a first local seed dataset for:

- route-number-first search
- origin/destination lookup
- Sri Lanka-specific ranking
- local aliases and route hints

## 3. Why PDF Is Not Enough For Production

PDF is not a good long-term production source for five reasons.

### 3.1 Layout is unstable

PDFs are made for humans.

That means:

- rows can break across pages
- spacing can change
- columns can merge
- parsing quality can drop without warning

### 3.2 There is no clean update contract

A production app needs to know:

- when the source changed
- what changed
- whether the current local dataset is stale

Public PDFs rarely give us a reliable change contract.

### 3.3 Usage rights are unclear

For a public product, we should be able to answer:

- are we allowed to copy this data?
- are we allowed to transform it?
- are we allowed to redistribute it in-app?
- are we allowed to cache and ship it inside the APK?

That is the difference between "prototype okay" and "production safe."

### 3.4 PDFs are not enough for future quality

NearWake will eventually need more than route numbers:

- stop-level naming
- route aliases
- update cadence
- stable identifiers
- structured exports
- timetable references where available

### 3.5 Official relationships improve trust

If NearWake is positioned as a Sri Lanka-first commuter tool, it is stronger to say:

- data is based on official public transport sources
- update handling is defined
- usage permission is understood

than to silently rely on scraped public files forever.

## 4. Recommended Source Tiers

Use a layered source strategy.

### Tier 1: Official seed sources

Use these first for route and station truth.

#### National Transport Commission

- [NTC route map](https://www.ntc.gov.lk/Bus_info/route_map.php)
- [NTC valid permit details PDF](https://ntc.gov.lk/corporate/pdf/2023/permit-Info/Valied%20Permit%20Details.pdf)
- [NTC contact page](https://www.ntc.gov.lk/corporate/contact_us/contact_head_office.php)
- [NTC information officers](https://www.ntc.gov.lk/corporate/company_overview/information_officers.php)

Useful current contact details from NTC's public site:

- general line: `011 2587372`
- hotline: `1955`
- email: `info@ntc.gov.lk`

Why use NTC:

- route numbers
- route origin/destination naming
- permit and service context
- official baseline naming

#### Sri Lanka Railways

- [Sri Lanka Railways schedule search](https://eservices.railway.gov.lk/schedule/homeAction.action?lang=en)
- [Sri Lanka Railways main site](https://railway.gov.lk/web/)

Why use Sri Lanka Railways:

- official station names
- train schedule references
- route and fare context

### Tier 2: Map and coordinate sources

Use these for location geometry, not as the sole truth source for route identity.

- [OpenStreetMap public transport wiki](https://wiki.openstreetmap.org/wiki/Public_transport)
- [Overpass API](https://wiki.openstreetmap.org/Overpass_API)

Why use OSM:

- bus stop coordinates
- station coordinates
- terminal coordinates
- landmark geometry

### Tier 3: NearWake-owned local intelligence

This is where the app becomes locally excellent rather than just locally available.

We should own:

- route aliases
- landmark aliases
- Sinhala/Tamil/English spellings
- common typo variants
- commuter-friendly naming
- route-number ranking rules

Examples:

- `Fort`
- `Colombo Fort`
- `Pettah`
- `Pita Kotuwa`
- `Maharagama`
- `Maharagama town`
- `Makumbura`
- `Kottawa`

### Tier 4: Moderated community correction later

Not first.
Later.

This can improve:

- missing alias coverage
- local landmark naming
- route nickname conventions
- quality corrections

But it should be moderated, not open chaos.

## 5. What We Should Ask NTC For

For a production path, ask whether they can provide:

- route list in CSV or Excel
- timetable exports if available
- permit summary exports
- stable route identifiers if available
- update cadence
- usage permission or redistribution guidance

Ask these exact questions:

1. Can you provide the inter-provincial route list in CSV or Excel format?
2. Can you provide route number, origin, destination, service type, and permit status in a structured export?
3. Is there a regular update cycle for route or permit changes?
4. Can third-party apps store, transform, and display this data for passenger-facing use?
5. Is there any license, attribution, or approval requirement for redistributing this data in a mobile app?
6. Is there a more suitable planning or operations contact for transport data integration?

## 6. What We Should Ask Sri Lanka Railways For

If we want the train layer to feel first-party and local, ask whether they can provide:

- station list export
- schedule export
- route and line references
- update cadence
- usage permission for passenger-facing display

## 7. Copy-Paste Request Template

Use this as a starting point for email or formal inquiry.

```text
Subject: Request for structured public transport route data for passenger-facing mobile app

Hello,

We are building a Sri Lanka-focused commuter mobile application that helps passengers avoid missing their stop during bus and train journeys.

At the moment we are reviewing publicly available transport information and would like to ask whether the National Transport Commission can provide structured route data for passenger-facing use.

We are specifically looking for:

- route list in CSV or Excel format
- route number
- origin
- destination
- service type
- permit summary or route validity status
- timetable exports if available
- update frequency / update cadence

We would also like to clarify whether this data may be stored, transformed, and displayed inside a mobile application for public passenger use, and whether there are any attribution, licensing, or approval requirements.

If there is a more appropriate contact for data integration or planning-related transport data, we would appreciate being directed to that person or division.

Thank you.
```

## 8. Recommended Production Data Architecture

NearWake should not tie the app directly to one transport source format.

Use this shape:

```text
Official PDF / HTML / CSV / future API
-> importer
-> normalization pipeline
-> reviewed canonical dataset
-> packaged seed / app-sync payload
-> place and route search layer
```

This lets us begin with PDF parsing now and move to structured official feeds later without rewriting the app's search experience.

## 9. Recommended Canonical Schema

Even if the source is messy, our stored canonical model should be clean.

Suggested route fields:

- `routeNumber`
- `displayName`
- `origin`
- `destination`
- `serviceType`
- `operatorCategory`
- `source`
- `sourceReference`
- `sourceUpdatedAt`
- `ingestedAt`
- `isActive`
- `confidence`

Suggested stop or landmark fields:

- `name`
- `aliases`
- `transportMode`
- `locality`
- `district`
- `lat`
- `lng`
- `source`
- `sourceReference`
- `confidence`

## 10. How This Fits The App

NearWake should not replace Google Places entirely.

Instead:

```text
user query
-> Sri Lanka local transport index
-> route-number and landmark ranking
-> Google Places fallback
-> merged result list
```

That gives us:

- Sri Lanka-first relevance
- route-number awareness
- landmark-first search
- graceful fallback when local data is incomplete

## 11. Production Ops Around Google APIs

Transport data is only one part of production readiness.

Because NearWake already uses Google services for search and routing, also do this:

- [Google Maps Platform security best practices](https://developers.google.com/maps/api-security-best-practices)
- [Google API key security best practices](https://support.google.com/googleapi/answer/6310037?hl=en)
- [Google Cloud budgets and alerts](https://cloud.google.com/billing/docs/how-to/budgets)
- [Google Maps reporting and monitoring](https://developers.google.com/maps/reporting-and-monitoring/reporting)
- [Quota alerts](https://docs.cloud.google.com/docs/quotas/set-up-quota-alerts)

Production checklist around Google:

- restrict API keys
- split client and server use where needed
- disable unused APIs
- monitor usage
- set quotas and alerts
- rotate keys carefully

## 12. Production Play Store And Privacy Work

Because NearWake is location-heavy, production also means Play and privacy alignment.

Useful references:

- [Background location policy](https://support.google.com/googleplay/android-developer/answer/9799150)
- [Sensitive permissions and APIs](https://support.google.com/googleplay/android-developer/answer/16558241?hl=en-GB)
- [Google Play Data safety](https://support.google.com/googleplay/answer/11416267?hl=en&p=data-safety)

Production checklist around Play:

- ensure permission copy matches behavior
- ensure privacy policy matches behavior
- ensure background location justification is honest
- ensure Data safety answers match real app behavior

## 13. Recommended Launch Strategy

Do not launch with a fake "all Sri Lanka is fully covered" promise.

Start corridor-first:

- Colombo / Fort / Pettah
- major commuter rail stations
- a few validated bus corridors
- high-confidence local aliases

This gives better user trust than broad but weak coverage.

## 14. Final Recommendation

Best professional path:

1. use public NTC and railway sources now
2. parse them into a clean NearWake schema
3. build route-number and landmark search on top
4. contact NTC and Sri Lanka Railways for structured exports and permission
5. switch to structured official feeds when available

So the honest answer is:

- yes, the PDF is enough to start
- no, the PDF should not be the forever-source for production
- the right move is "PDF now, official structured feed later"
