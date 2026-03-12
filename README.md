# Loco Operator Integration Documentation

**Document Version:** 3.0  
**Last Updated:** March 2026  
**Owner:** Engineering & Product Team, Loco

---

## 📑 Table of Contents

| Section | Description |
|---------|-------------|
| **[1. Executive Summary](#1-executive-summary)** | Overview of integration capabilities and workflow |
| **[2. Loco Drops / Rewards](#2-loco-drops--rewards)** | Integration guide for Drops and Quest rewards |
| └─ [2.1 Feature Overview](#21-feature-overview) | What are Loco Drops and Rewards |
| └─ [2.2 Integration Workflow](#22-integration-workflow) | High level Integration steps for Loco Drops / Rewards |
| └─ [2.3 API Integration Steps](#23-api-integration-steps) | Step-by-step API integration workflow |
| **[3. Loco Battles](#3-loco-battles)** | Integration guide for tournament features |
| └─ [3.1 Feature Overview](#31-feature-overview) | What are Loco Battles |
| └─ [3.2 Integration Workflow](#32-integration-workflow) | High level Integration steps for Tournament |
| └─ [3.3 API Integration Steps](#33-api-integration-steps) | Step-by-step API integration workflow |
| └─ [3.4 Tournament APIs](#34-tournament-apis) | Operator-hosted tournament endpoints |

---

<a name="1-executive-summary"></a>
## 1. Executive Summary

Loco platform enables external operators to integrate two primary engagement features: **Loco Drops/Rewards** and **Loco Battles**. Loco Drops allow real-time bonus distribution during live streams, while Loco Battles enable tournament discovery and leaderboard tracking. Both features follow a common integration pattern: users are redirected from Loco to the operator's platform, accounts are linked, and workflows are tracked via standardized APIs. Operators implement user-facing redirect URLs, consume Loco's User Management APIs, and (for Battles) expose Tournament APIs for Loco to consume.

---

<a name="2-loco-drops--rewards"></a>
## 2. Loco Drops / Rewards

**Direction:** Operator → Loco (Operator calls Loco APIs)  
**Implementation:** Loco hosts User Management APIs; Operator implements redirect handler  
**Authentication:** Loco provides bearer token to Operator during onboarding

<a name="21-feature-overview"></a>
### 2.1 Feature Overview

**What are Loco Drops?**

Loco Drops are real-time bonus rewards triggered by streamers during live broadcasts. When a drop is activated, viewers are redirected to the operator's platform to claim rewards such as free spins, bonus cash, or quest completions. This feature drives user acquisition and engagement during peak attention moments.

---

<a name="22-integration-workflow"></a>
### 2.2 Integration Workflow

The Drops/Rewards integration follows a 4-step workflow:

```
Step 1: User clicks Drop on Loco
   ↓
Step 2: Loco redirects user to Operator platform
   ↓
Step 3: Operator fetches user details from Loco for login/signup
   ↓
Step 4: Operator links account and notifies Loco of workflow status
```

---

<a name="23-api-integration-steps"></a>
### 2.3 API Integration Steps

#### Step 1: User Redirection

**Overview:**

The operator must expose a **user-facing redirect URL** that Loco will use to redirect users from the Loco platform. This URL must accept specific query parameters, validate the URL, and handle user onboarding or reward fulfillment.

**What happens:**
1. User clicks on a Drop/Reward on Loco platform
2. Loco generates a redirect URL
3. User is redirected to operator's platform

**Operator must:**
1. Accept the redirect URL
2. Extract user information from URL parameters
3. Proceed to Step 2


#### Redirect URL Format

```
https://<operator-redirect-endpoint>/casino/register?utm_source=loco&utm_loco_uid=loco_12345678&utm_loco_uname=gamer123&utm_campaign=march_2026_drops&txn_id=txn_20260304_004&op_type=bonus&op_type_id=bundle_001&ts=1709548800&sig=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

#### Required Parameters

| Parameter | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| utm_source | `string` | ✅ Yes | Always "loco" for attribution tracking |
| utm_loco_uid | `string` | ✅ Yes | Unique Loco user identifier |
| utm_loco_uname | `string` | ✅ Yes | Loco username |
| utm_campaign | `string` | ✅ Yes | Campaign identifier for tracking |
| txn_id | `string` | ✅ Yes | Unique transaction ID for idempotency |
| op_type | `string` | ✅ Yes | Operation type: `"bonus"`, `"loco_link_account"` |
| op_type_id | `string` | ✅ Yes | Reward ID or bundle ID |
| ts | `integer` | ✅ Yes | Unix timestamp (seconds) |
| sig | `string` | ✅ Yes | HMAC-SHA256 signature for request validation |

> **NOTE:**  
> For Drops/Rewards integrations, `op_type` will be `"bonus"` or `"loco_link_account"`, and `op_type_id` corresponds to the specific reward identifier. These values along with `txn_id` are used for tracking and must be passed back to Loco in Step 4.

---

#### Step 2: Get User Details

**Endpoint:** `POST /api/v1/get-loco-user`

**Purpose:** Retrieve Loco user information for account linking

**Who calls:** Operator → Loco

**When to call:** After validating the redirect URL, before showing signup/login form to pre populate data in signup/login form

##### Request Headers

| Header | Type | Required | Value | Description |
|--------|------|----------|-------|-------------|
| Authorization | `string` | ✅ Yes | `Bearer {LOCO_TOKEN}` | Loco-provided authentication token |
| Content-Type | `string` | ✅ Yes | `application/json` | Request payload format |
| Accept | `string` | ✅ Yes | `application/json` | Expected response format |

##### Request Body

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| user_uid | `string` | ✅ Yes | Unique Loco user identifier (from redirect URL) | `"loco_12345678"` |
| txn_id | `string` | ✅ Yes | Unique transaction identifier (from redirect URL) | `"txn_20260304_001"` |

##### Sample Request

```json
{
  "user_uid": "loco_12345678",
  "txn_id": "txn_20260304_001"
}
```

##### Response Structure

**Response Object: UserDetails**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| loco_uid | `string` | ✅ Yes | Unique Loco user identifier |
| loco_username | `string` | ⬜ No | Loco username (null if not set) |
| email | `object` | ✅ Yes | Email verification details |
| phone | `object` | ✅ Yes | Phone verification details |
| screen_name | `string` | ⬜ No | Operator platform username (present if already linked) |
| ext_player_id | `string` | ⬜ No | Operator platform user ID (present if already linked) |
| country | `string` | ✅ Yes | ISO 3166-1 alpha-2 country code |

**Nested Object: email**

| Field | Type | Required | Possible Values | Description |
|-------|------|----------|-----------------|-------------|
| email_id | `string` | ✅ Yes | — | User's email address |
| verification_status | `enum` | ✅ Yes | `verified` \| `pending` \| `failed` | Current verification state |
| verification_mode | `enum` | ✅ Yes | `email_otp` \| `sms_otp` \| `social` | Method used for verification |

**Nested Object: phone**

| Field | Type | Required | Possible Values | Description |
|-------|------|----------|-----------------|-------------|
| phone_no | `string` | ✅ Yes | — | User's phone number |
| country_code | `string` | ✅ Yes | — | International dialing code (e.g., `"+91"`) |
| verification_status | `enum` | ✅ Yes | `verified` \| `pending` \| `failed` | Current verification state |
| verification_mode | `enum` | ✅ Yes | `sms_otp` \| `whatsapp_otp` | Method used for verification |


##### Implementation Notes

- `txn_id` must be unique per request for idempotency
- If `screen_name` and `ext_player_id` are present, the account is already linked
- Use the response data to pre-fill signup forms for better UX

---

#### Step 3: Link User Account

**Endpoint:** `POST /api/v1/link-account`

**Purpose:** Establish account federation between Loco and operator platforms

**Who calls:** Operator → Loco

**When to call:** After user completes signup/login on operator platform

##### Request Headers

| Header | Type | Required | Value | Description |
|--------|------|----------|-------|-------------|
| Authorization | `string` | ✅ Yes | `Bearer {LOCO_TOKEN}` | Loco-provided authentication token |
| Content-Type | `string` | ✅ Yes | `application/json` | Request payload format |
| Accept | `string` | ✅ Yes | `application/json` | Expected response format |

##### Request Body

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| loco_username | `string` | ⬜ No | Loco username (optional identifier) | `"gamer123"` |
| loco_uid | `string` | ✅ Yes | Unique Loco user identifier | `"loco_12345678"` |
| ext_player_id | `string` | ✅ Yes | Operator platform user ID | `"op_98765432"` |
| screen_name | `string` | ✅ Yes | Operator platform username | `"ProGamer123"` |
| source | `string` | ✅ Yes | UTM source parameter | `"loco"` |
| campaign | `string` | ✅ Yes | Campaign identifier | `"march_2026_drops"` |
| is_new_signup | `boolean` | ✅ Yes | `true` if user is new to operator platform | `true` |
| brand | `string` | ✅ Yes | Operator brand identifier | `"operator_brand"` |
| timestamp | `string` | ✅ Yes | ISO 8601 formatted timestamp | `"2026-03-04T12:30:00Z"` |
| loco_txn_id | `string` | ✅ Yes | Unique transaction ID (from redirect URL) | `"txn_20260304_002"` |

##### Sample Request

```json
{
  "loco_username": "gamer123",
  "loco_uid": "loco_12345678",
  "ext_player_id": "op_98765432",
  "screen_name": "ProGamer123",
  "source": "loco",
  "campaign": "march_2026_drops",
  "is_new_signup": true,
  "brand": "operator_brand",
  "timestamp": "2026-03-04T12:30:00Z",
  "loco_txn_id": "txn_20260304_002"
}
```

##### Response Structure

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| status | `string` | ✅ Yes | `ACKNOWLEDGED` if successful |
| message | `string` | ✅ Yes | Human-readable success message |

##### Implementation Notes

- Once linked, accounts cannot be unlinked without support intervention
- `is_new_signup` helps track user acquisition vs retention metrics

---

#### Step 4: Workflow Status Notification

**Endpoint:** `POST /api/v1/workflow-status`

**Purpose:** Notify Loco about workflow completion and bonus fulfillment

**Who calls:** Operator → Loco

**When to call:** After reward is granted (or workflow fails) on operator platform

##### Request Headers

| Header | Type | Required | Value | Description |
|--------|------|----------|-------|-------------|
| Authorization | `string` | ✅ Yes | `Bearer {LOCO_TOKEN}` | Loco-provided authentication token |
| Content-Type | `string` | ✅ Yes | `application/json` | Request payload format |
| Accept | `string` | ✅ Yes | `application/json` | Expected response format |

##### Request Body

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| loco_uid | `string` | ✅ Yes | Unique Loco user identifier | `"loco_12345678"` |
| ext_player_id | `string` | ⬜ No | Operator platform user ID | `"op_98765432"` |
| screen_name | `string` | ⬜ No | Operator platform username | `"ProGamer123"` |
| source | `string` | ✅ Yes | UTM source parameter | `"loco"` |
| campaign | `string` | ✅ Yes | Campaign identifier | `"march_2026_drops"` |
| brand | `string` | ✅ Yes | Operator brand identifier | `"operator_brand"` |
| timestamp | `string` | ✅ Yes | ISO 8601 formatted timestamp | `"2026-03-04T12:45:00Z"` |
| loco_txn_id | `string` | ✅ Yes | Unique transaction ID (from redirect URL) | `"txn_20260304_003"` |
| status | `enum` | ✅ Yes | `COMPLETE` \| `FAILED` \| `LINKING_FAILED` | `"COMPLETE"` |
| failure_reason | `string` | Conditional | Required if status is `FAILED` or `LINKING_FAILED` | `"Invalid bonus code"` |
| category | `string` | ⬜ No | User categorization for segmentation | `"high_value"` |
| is_bonus_offered | `boolean` | ⬜ No | `true` if reward was provided to user | `true` |


##### Sample Request

```json
{
  "loco_uid": "loco_12345678",
  "ext_player_id": "op_98765432",
  "screen_name": "ProGamer123",
  "source": "loco",
  "campaign": "march_2026_drops",
  "brand": "operator_brand",
  "timestamp": "2026-03-04T12:45:00Z",
  "loco_txn_id": "txn_20260304_003",
  "status": "COMPLETE",
  "category": "high_value",
  "is_bonus_offered": true
}
```

##### Response Structure

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| status | `string` | ✅ Yes | `ACKNOWLEDGED` if received successfully |
| message | `string` | ✅ Yes | Success or failure message |

##### Implementation Notes

- Use `status: COMPLETE` when workflow finishes successfully and reward is granted
- Use `status: FAILED` when workflow fails at operator side (e.g., bonus code invalid)
- Use `status: LINKING_FAILED` specifically for account linking failures
- `failure_reason` is mandatory when status is not `COMPLETE`

---

<a name="3-loco-battles"></a>
## 3. Loco Battles

**Direction:** Bidirectional (Operator calls Loco User Management APIs; Loco calls Operator Tournament APIs)  
**Implementation:** Loco hosts User Management APIs; Operator hosts Tournament APIs  
**Authentication:** Mutual token exchange during onboarding

<a name="31-feature-overview"></a>
### 3.1 Feature Overview

**What are Loco Battles?**

Loco Battles enable operators to run tournaments that are discoverable within the Loco platform. Users can view tournament details, track live leaderboards, and deep-link to the operator's platform to participate. This feature drives engagement by showcasing operator tournaments to Loco's audience.

---

<a name="32-integration-workflow"></a>
### 3.2 Integration Workflow

```
Step 1: User clicks "Join Tournament" on Loco
   ↓
Step 2: Loco redirects user to Operator platform (with redirect URL)
   ↓
Step 3: Operator validates URL and fetches user details from Loco
   ↓
Step 4: Operator links account (If not already linked)
   ↓
Step 5: User moves to operator tournament page and notifies Loco of workflow status
```
---

<a name="33-api-integration-steps"></a>
### 3.3 API Integration Steps

#### Step 1: User Redirection

**Overview:**

Similar to Drops/Rewards, the operator must expose a **user-facing redirect URL** for Battles. When a user clicks "Join Tournament" on Loco, they are redirected to the operator's platform with tournament-specific parameters.

**Who implements:** Loco generates, Operator consumes

**What happens:**
- User clicks "Join Tournament" on Loco platform
- Loco generates a signed redirect URL  
- User is redirected to operator's tournament page

**Operator must:**
1. Accept the redirect URL
3. Extract tournament information from URL parameters (`op_type_id` contains tournament ID)
4. Proceed to Step 2

#### Redirect URL Format

```
https://<operator-redirect-endpoint>/casino/tournament?utm_source=loco&utm_loco_uid=loco_12345678&utm_loco_uname=gamer123&utm_campaign=weekly_tournament&txn_id=txn_20260304_005&op_type=tournament&op_type_id=tourn_2024_001&ts=1709548800&sig=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

#### Required Parameters

The parameter structure is **identical** to Drops/Rewards (see Section 2.2), with the following semantic difference:

| Parameter | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| op_type | `string` | ✅ Yes | Always `"tournament"` for Battles |
| op_type_id | `string` | ✅ Yes | Tournament identifier (e.g., `"tourn_2024_001"`) |

> **IMPORTANT NOTE:**  
> If the operator has **already integrated Drops/Rewards**, they do **NOT** need to re-integrate the User Management APIs (Steps 2-4) for Battles. The same APIs are used for both features.
> 
> **What needs to be updated:**
> - Ensure the redirect URL handler correctly processes `op_type="tournament"` and `op_type_id={tournament_id}`
> - Update the **Workflow Status Notification API** (Step 4) to pass tournament-specific parameters:
>   - `op_type="tournament"`
>   - `op_type_id={tournament_id}` (the tournament the user joined)
 
> **If this is your first integration**, proceed with all 4 steps below.

---

#### Step 2: Get User Details

**Endpoint:** `POST /api/v1/get-loco-user`

**Same API as Drops/Rewards.** See Section 2.3, Step 2 for complete details (Operator → Loco)

**Tournament-specific note:** Use this API to determine if the user already has a linked account before showing the tournament signup form or pre populate details in form.

---

#### Step 3: Link User Account

**Endpoint:** `POST /api/v1/link-account`

**Same API as Drops/Rewards.** See Section 2.3, Step 3 for complete details (Operator → Loco)

**Tournament-specific note:** Set `campaign` parameter to the tournament name or identifier for tracking purposes.

---

#### Step 4: Workflow Status Notification

**Endpoint:** `POST /api/v1/workflow-status`

**Same API as Drops/Rewards, with tournament-specific parameters.**

See Section 2.3, Step 4 for complete API details. (Operator → Loco)

**Tournament-specific parameters:**

| Field | Value for Battles |
|-------|-------------------|
| status | `COMPLETE` if user successfully joined tournament |
| op_type | Ensure this matches `"tournament"` from redirect URL |
| op_type_id | Ensure this matches the tournament ID from redirect URL |

**Sample Request (Battles):**

```json
{
  "loco_uid": "loco_12345678",
  "ext_player_id": "op_98765432",
  "screen_name": "ProGamer123",
  "source": "loco_app",
  "campaign": "weekly_tournament_2026",
  "brand": "operator_brand",
  "timestamp": "2026-03-04T13:00:00Z",
  "loco_txn_id": "txn_20260304_006",
  "status": "COMPLETE",
  "category": "tournament_participant",
  "is_bonus_offered": false
}
```

---

<a name="34-tournament-apis"></a>
### 3.4 Tournament APIs

**Direction:** Loco → Operator  
**Implementation:** **Operator must host these APIs**  
**Authentication:** Operator provides bearer token to Loco; Loco uses this token when calling operator endpoints

#### Integration Requirements

**Operator must:**
1. **Implement** all endpoints specified in this section
2. **Host** these APIs on operator's infrastructure
3. **Provide** base URL to Loco team (production and staging)
4. **Generate** authentication tokens and provide to Loco during onboarding

#### Endpoint Summary

| Endpoint | Method | Purpose | Call Frequency |
|----------|--------|---------|----------------|
| `/tournament/getTournamentSchedule` | GET | List all active/scheduled tournaments | Every 60 minutes (polling) |
| `/tournament/getTournamentConfigDetails` | GET | Get tournament details and rules | On-demand (user views details) |
| `/tournament/getTournamentLeaderBoard` | GET | Get tournament leaderboard | On-demand (user views leaderboard) |
| `/tournament/getPlayerToRankingDetails` | GET | Get player's rank in tournament | On-demand (user views progress) |

---

#### 3.4.1 Get Tournament Schedule

**Operator must implement this endpoint**

**Purpose:** Retrieve list of active and scheduled tournaments for discovery on Loco platform.

##### Endpoint

```
GET /tournament/getTournamentSchedule
```

**Full URL Example:**  
`https://api.operator-domain.com/tournament/getTournamentSchedule?limit=20&offset=0`

##### Description

Loco will call this endpoint every 60 minutes to sync tournament data. This endpoint should return all tournaments that are currently active, scheduled, or recently completed.

##### Request Headers

**Loco will send:**

| Header | Type | Value | Description |
|--------|------|-------|-------------|
| Authorization | `string` | `Bearer {OPERATOR_TOKEN}` | Operator-provided authentication token |
| Content-Type | `string` | `application/json` | Request content type |
| Accept | `string` | `application/json` | Expected response format |

##### Query Parameters

**Loco will send:**

| Parameter | Type | Required | Default | Range | Description |
|-----------|------|----------|---------|-------|-------------|
| limit | `integer` | ⬜ No | `10` | 1-100 | Number of records per page |
| offset | `integer` | ⬜ No | `0` | ≥ 0 | Number of records to skip for pagination |

##### Response Structure

**Operator must return:**

**Top-level Response:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| status | `string` | ✅ Yes | `"SUCCESS"` or `"ERROR"` |
| error_code | `string` | ⬜ No | Error code if status is `"ERROR"` |
| error_id | `integer` | ⬜ No | Error identifier if status is `"ERROR"` |
| error_description | `string` | ⬜ No | Error description if status is `"ERROR"` |
| data | `array` | ✅ Yes | List of Tournament objects |

**Object: Tournament**

| Field | Type | Required | Format/Values | Description | Example |
|-------|------|----------|---------------|-------------|---------|
| tournament_id | `string` | ✅ Yes | UUID or custom format | Unique tournament identifier | `"tourn_2024_001"` |
| title | `string` | ✅ Yes | Max 200 chars | Tournament display name | `"Weekly Championship"` |
| start_time | `integer` | ✅ Yes | Unix timestamp (seconds) | Tournament start time | `1714521600` |
| end_time | `integer` | ✅ Yes | Unix timestamp (seconds) | Tournament end time | `1715126400` |
| tournament_parent_id | `string` | ⬜ No | UUID or custom format | Parent tournament ID for linked tournaments | `"tourn_parent_001"` |
| icon_image | `string (URL)` | ✅ Yes | HTTPS URL | Tournament icon (128x128px recommended) | `"https://cdn.example.com/icon.png"` |
| background_image | `string (URL)` | ✅ Yes | HTTPS URL | Tournament background (1920x1080px recommended) | `"https://cdn.example.com/bg.png"` |
| tournament_criteria | `integer` | ✅ Yes | Enum ID | Scoring rules enum identifier | `13` |
| campaign_status | `enum` | ✅ Yes | `ACTIVE` \| `INACTIVE` \| `SCHEDULED` \| `COMPLETED` | Current tournament state | `"ACTIVE"` |
| campaignType | `enum` | ✅ Yes | `LEADER_BOARD` \| `QUALIFIER` \| `FINAL` | Tournament type | `"LEADER_BOARD"` |
| campaign_sub_type | `enum` | ✅ Yes | `REGULAR` \| `SPECIAL` \| `PREMIUM` | Tournament subtype | `"REGULAR"` |
| campaign_category | `enum` | ✅ Yes | `LIVE_CASINO` \| `SLOTS` \| `SPORTS_BETTING` \| `TABLE_GAMES` | Tournament category | `"LIVE_CASINO"` |

---

#### 3.4.2 Get Tournament Details

**Operator must implement this endpoint**

**Purpose:** Retrieve detailed configuration for a specific tournament including rules and prize breakdown.

##### Endpoint

```
GET /tournament/getTournamentConfigDetails
```

**Full URL Example:**  
`https://api.operator-domain.com/tournament/getTournamentConfigDetails?tournament_id=tourn_2024_001`

##### Description

Loco will call this endpoint on-demand when users view tournament detail pages. This endpoint should return comprehensive tournament information including prize structure and rules.

##### Request Headers

**Loco will send:**

| Header | Type | Value | Description |
|--------|------|-------|-------------|
| Authorization | `string` | `Bearer {OPERATOR_TOKEN}` | Operator-provided authentication token |
| Content-Type | `string` | `application/json` | Request content type |
| Accept | `string` | `application/json` | Expected response format |

##### Query Parameters

**Loco will send:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| tournament_id | `string` | ✅ Yes | Unique tournament identifier |

##### Response Structure

**Operator must return:**

**Top-level Response:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| status | `string` | ✅ Yes | `"SUCCESS"` or `"ERROR"` |
| error_code | `string` | ⬜ No | Error code if status is `"ERROR"` |
| error_id | `integer` | ⬜ No | Error identifier if status is `"ERROR"` |
| error_description | `string` | ⬜ No | Error description if status is `"ERROR"` |
| data | `object` | ✅ Yes | TournamentDetail object |

**Object: TournamentDetail**

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| tournament_id | `string` | ✅ Yes | Unique tournament identifier | `"tourn_2024_001"` |
| title | `string` | ✅ Yes | Tournament display name | `"Weekly Championship"` |
| reward_pool | `string` | ✅ Yes | Total prize pool with currency | `"$10,000"` |
| start_time | `integer` | ✅ Yes | Tournament start time (Unix seconds) | `1714521600` |
| end_time | `integer` | ✅ Yes | Tournament end time (Unix seconds) | `1715126400` |
| tournament_criteria | `integer` | ✅ Yes | Scoring rules enum identifier | `13` |
| guidelines | `object` | ✅ Yes | Tournament rules and prize breakdown | See below |
| tournament_parent_id | `string` | ⬜ No | Parent tournament ID | `"tourn_parent_001"` |
| created_at | `integer` | ✅ Yes | Tournament creation timestamp (Unix seconds) | `1714521600` |
| updated_at | `integer` | ✅ Yes | Last update timestamp (Unix seconds) | `1714521600` |
| campaign_status | `enum` | ✅ Yes | `ACTIVE` \| `INACTIVE` \| `SCHEDULED` \| `COMPLETED` | `"ACTIVE"` |

**Nested Object: guidelines**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| title | `string` | ✅ Yes | Guidelines section title (e.g., "Rules") |
| sub_title | `string` | ✅ Yes | Guidelines description or subtitle |
| data | `array` | ✅ Yes | List of PrizeRank objects (prize breakdown) |

**Nested Object: PrizeRank** (within guidelines.data)

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| rank | `string` | ✅ Yes | Position or rank range | `"1"`, `"2-5"`, `"6-10"` |
| prize | `string` | ✅ Yes | Prize amount with currency symbol | `"$5,000"` |
| user_avatar | `string (URL)` | ✅ Yes | URL of avatar/badge for this rank tier | `"https://cdn.example.com/gold.png"` |

---

#### 3.4.3 Get Tournament Leaderboard

**Operator must implement this endpoint**

**Purpose:** Retrieve paginated leaderboard rankings for a specific tournament.

##### Endpoint

```
GET /tournament/getTournamentLeaderBoard
```

**Full URL Example:**  
`https://api.operator-domain.com/tournament/getTournamentLeaderBoard?tournament_id=tourn_2024_001&limit=50&offset=0`

##### Description

Loco will call this endpoint on-demand when users view tournament leaderboards. This endpoint should return current player rankings, scores, and prizes.

##### Request Headers

**Loco will send:**

| Header | Type | Value | Description |
|--------|------|-------|-------------|
| Authorization | `string` | `Bearer {OPERATOR_TOKEN}` | Operator-provided authentication token |
| Content-Type | `string` | `application/json` | Request content type |
| Accept | `string` | `application/json` | Expected response format |

##### Query Parameters

**Loco will send:**

| Parameter | Type | Required | Default | Range | Description |
|-----------|------|----------|---------|-------|-------------|
| tournament_id | `string` | ✅ Yes | — | — | Unique tournament identifier |
| limit | `integer` | ⬜ No | `10` | 1-100 | Number of records per page |
| offset | `integer` | ⬜ No | `0` | ≥ 0 | Number of records to skip |

##### Response Structure

**Operator must return:**

**Top-level Response:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| status | `string` | ✅ Yes | `"SUCCESS"` or `"ERROR"` |
| error_code | `string` | ⬜ No | Error code if status is `"ERROR"` |
| error_id | `integer` | ⬜ No | Error identifier if status is `"ERROR"` |
| error_description | `string` | ⬜ No | Error description if status is `"ERROR"` |
| data | `object` | ✅ Yes | LeaderboardData object |

**Object: LeaderboardData**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| tournament_id | `string` | ✅ Yes | Unique tournament identifier |
| leaderboard | `array` | ✅ Yes | List of LeaderboardEntry objects |

**Object: LeaderboardEntry** (within leaderboard array)

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| rank | `integer` | ✅ Yes | Player's current rank position | `1` |
| username | `string` | ✅ Yes | Player's display username | `"TopPlayer001"` |
| score | `string` | ✅ Yes | Player's score (string to support large numbers) | `"15000"` |
| prize | `string` | ✅ Yes | Prize amount (numeric string) | `"1000"` |
| prize_currency | `string` | ✅ Yes | Prize currency code | `"USD"`, `"INR"` |
| user_avatar | `string (URL)` | ✅ Yes | URL of player's avatar image | `"https://cdn.example.com/avatar.png"` |

---

#### 3.4.4 Get Player Tournament Rank

**Operator must implement this endpoint**

**Purpose:** Retrieve individual player's rank and performance in a specific tournament.

##### Endpoint

```
GET /tournament/getPlayerToRankingDetails
```

**Full URL Example:**  
`https://api.operator-domain.com/tournament/getPlayerToRankingDetails?user_id=op_98765432&tournament_id=tourn_2024_001`

##### Description

Loco will call this endpoint on-demand when users view their tournament progress. This endpoint should return the player's current standing and projected prize.

##### Request Headers

**Loco will send:**

| Header | Type | Value | Description |
|--------|------|-------|-------------|
| Authorization | `string` | `Bearer {OPERATOR_TOKEN}` | Operator-provided authentication token |
| Content-Type | `string` | `application/json` | Request content type |
| Accept | `string` | `application/json` | Expected response format |

##### Query Parameters

**Loco will send:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| user_id | `string` | ✅ Yes | Operator platform user ID (external_player_id) |
| tournament_id | `string` | ✅ Yes | Unique tournament identifier |

##### Response Structure

**Operator must return:**

**Response Object: PlayerRank**

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| tournament_id | `string` | ✅ Yes | Unique tournament identifier | `"tourn_2024_001"` |
| external_player_id | `string` | ✅ Yes | Player's ID on operator platform | `"op_98765432"` |
| screen_name | `string` | ✅ Yes | Display name on operator platform | `"ProGamer123"` |
| user_name | `string` | ✅ Yes | Loco username of the player | `"gamer123"` |
| user_avatar | `string (URL)` | ✅ Yes | URL of player's profile picture | `"https://cdn.example.com/avatar.png"` |
| rank | `integer` | ✅ Yes | Player's current rank (0 if not ranked) | `42` |
| score | `integer` | ✅ Yes | Player's current tournament score | `1500` |
| prize | `string` | ✅ Yes | Prize amount won or projected | `"50"` |
| prize_currency | `string` | ✅ Yes | Prize currency code | `"USD"`, `"INR"` |

---
