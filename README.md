# Table of Contents

| Section | Description |
|---------|-------------|
| **[Executive Summary](#executive-summary)** | Overview of integration capabilities and benefits |
| **[1. User Management APIs](#1-user-management-apis)** | APIs for account linking and user data retrieval |
| └─ [1.1 Authentication](#11-authentication) | Token-based authentication mechanism |
| └─ [1.2 Get User Details](#12-get-user-details) | Retrieve Loco user information |
| └─ [1.3 Link User Account](#13-link-user-account) | Establish account federation |
| **[2. Workflow Management APIs](#2-workflow-management-apis)** | Workflow status and completion tracking |
| └─ [2.1 Workflow Status Notification](#21-workflow-status-notification) | Notify workflow completion status |
| **[3. Loco Battles - Tournament APIs](#3-loco-battles---tournament-apis)** | Tournament integration endpoints (Operator implements) |
| └─ [3.1 Get Tournament Schedule](#31-get-tournament-schedule) | List all active tournaments |
| └─ [3.2 Get Tournament Details](#32-get-tournament-details) | Retrieve tournament configuration |
| └─ [3.3 Get Tournament Leaderboard](#33-get-tournament-leaderboard) | Fetch tournament rankings |
| └─ [3.4 Get Player Tournament Rank](#34-get-player-tournament-rank) | Get individual player standing |
| **[4. Redirection Management](#4-redirection-management)** | Deep link generation and validation |
| **[5. Appendices](#5-appendices)** | Reference materials and conventions |

---

## Loco Operator Integration API Documentation

**Document Version:** 2.0  
**Last Updated:** March 2026  
**Owner:** Engineering & Product Team, Loco

---

<a name="executive-summary"></a>
## Executive Summary

This document provides comprehensive API specifications for integrating external operators with the Loco platform. The integration enables account linking, reward distribution, tournament management, and real-time engagement features.

<a name="key-integration-capabilities"></a>
### Key Integration Capabilities

- **Account Linking**: Seamless user account federation between platforms
- **Reward Distribution**: Automated fulfillment and tracking across platforms
- **Quest Integration**: Gamified user engagement workflows
- **Loco Drops**: Real-time operator-triggered bonus drops during live streams
- **Loco Battles**: Tournament discovery, leaderboard tracking, and deep-link integration
- **Real-time Notifications**: SSE-based instant feedback mechanisms
- **Geo-fencing**: Region-specific feature availability

<a name="strategic-benefits"></a>
### Strategic Benefits

- **Reduced Integration Time**: 15-20 business days from kickoff to production
- **Scalable Architecture**: Support for multiple concurrent operators
- **Risk Mitigation**: Built-in security, compliance, and rollback mechanisms
- **Performance**: < 200ms latency targets for critical paths

<a name="1-user-management-apis"></a>
## 1. User Management APIs

**Direction:** Operator → Loco  
**Implementation:** Loco hosts these APIs  
**Action Required:** Operator integrates and calls these endpoints   
**Authentication:** Use Loco-provided bearer token in Authorization header  
**High level design:** [High level Account linking and bonus distribution flow](https://i.ibb.co/Kj3q8z6X/image-3.png)  
**Entity diagram:** [Entity Relationship Overview](https://i.ibb.co/5gqwT0Kp/image-7.png)


<a name="11-authentication"></a>
### 1.1 Authentication

```
Authorization: Bearer {LOCO_PROVIDED_TOKEN}
```

**Token Management:**
- Loco provides authentication tokens during onboarding
- Multiple active tokens supported per operator
- Token rotation capability without service downtime
- Tokens are environment-specific (staging vs production)
- Token expiry: Tokens do not expire but can be revoked


<a name="12-get-user-details"></a>
### 1.2 Get User Details

Retrieve Loco user information for account linking purposes.

<a name="endpoint"></a>
#### Endpoint

```
POST /api/v1/get-loco-user
```

<a name="description"></a>
#### Description

Fetches comprehensive user details from Loco platform using the user's unique identifier. Used during account linking workflows to retrieve verified contact information and existing linkage status.

<a name="request-headers"></a>
#### Request Headers

| Header | Type | Mandatory | Description |
|--------|------|-----------|-------------|
| Authorization | string | Yes | Bearer {ACCESS_TOKEN} |
| Content-Type | string | Yes | application/json |
| Accept | string | Yes | application/json |

<a name="request-body"></a>
#### Request Body

| Field | Type | Mandatory | Description |
|-------|------|-----------|-------------|
| user_uid | string | Yes | Unique Loco user identifier |
| txn_id | string | Yes | Unique transaction identifier for idempotency |

**Sample Request:**

```json
{
  "user_uid": "loco_12345678",
  "txn_id": "txn_20260304_001"
}
```

<a name="response-structure"></a>
#### Response Structure

**Response Object: UserDetails**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| loco_uid | string | Yes | Unique Loco user identifier |
| loco_username | string | No | Loco username (null if not set) |
| email | object | Yes | Email verification details |
| phone | object | Yes | Phone verification details |
| screen_name | string | No | Operator platform username (present if already linked) |
| ext_player_id | string | No | Operator platform user ID (present if already linked) |
| country | string | Yes | ISO 3166-1 alpha-2 country code |

**Nested Object: email**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| email_id | string | Yes | User's email address |
| verification_status | string | Yes | verified \| pending \| failed |
| verification_mode | string | Yes | email_otp \| sms_otp \| social |

**Nested Object: phone**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| phone_no | string | Yes | User's phone number |
| country_code | string | Yes | International dialing code (e.g., "+91") |
| verification_status | string | Yes | verified \| pending \| failed |
| verification_mode | string | Yes | sms_otp \| whatsapp_otp |

<a name="notes"></a>
#### Notes

- `txn_id` must be unique per request for idempotency
- Linked accounts will return `screen_name` and `ext_player_id`

---

<a name="13-link-user-account"></a>
### 1.3 Link User Account

Establish account federation between Loco and operator platforms.

<a name="endpoint-1"></a>
#### Endpoint

```
POST /api/v1/link-account
```

<a name="description-1"></a>
#### Description

Creates a bidirectional link between a Loco user account and an operator platform account. This enables cross-platform reward distribution, tournament participation, and unified user experience.

<a name="request-headers-1"></a>
#### Request Headers

| Header | Type | Mandatory | Description |
|--------|------|-----------|-------------|
| Authorization | string | Yes | Bearer {ACCESS_TOKEN} |
| Content-Type | string | Yes | application/json |
| Accept | string | Yes | application/json |

<a name="request-body-1"></a>
#### Request Body

| Field | Type | Mandatory | Description |
|-------|------|-----------|-------------|
| loco_username | string | No | Loco username (optional identifier) |
| loco_uid | string | Yes | Unique Loco user identifier |
| ext_player_id | string | Yes | Operator platform user ID |
| screen_name | string | Yes | Operator platform username |
| source | string | Yes | UTM source parameter for tracking |
| campaign | string | Yes | Campaign identifier for attribution |
| is_new_signup | boolean | Yes | true if user is new to operator platform |
| brand | string | Yes | Operator brand identifier |
| timestamp | string | Yes | ISO 8601 formatted timestamp |
| loco_txn_id | string | Yes | Unique transaction ID for idempotency |

**Sample Request:**

```json
{
  "loco_username": "gamer123",
  "loco_uid": "loco_12345678",
  "ext_player_id": "op_98765432",
  "screen_name": "ProGamer123",
  "source": "loco_app",
  "campaign": "march_2026_signup",
  "is_new_signup": true,
  "brand": "operator_brand",
  "timestamp": "2026-03-04T12:30:00Z",
  "loco_txn_id": "txn_20260304_002"
}
```

<a name="response-structure-1"></a>
#### Response Structure

**Response Object: LinkAccountResponse**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| status | string | Yes | ACKNOWLEDGED if successful |
| message | string | Yes | Human-readable success message |

<a name="notes-1"></a>
#### Notes

- Idempotent operation: Duplicate requests with same `loco_txn_id` return success
- Once linked, accounts cannot be unlinked without support intervention
- `is_new_signup` helps track user acquisition vs retention metrics
- `timestamp` must be in ISO 8601 format with timezone

---

<a name="2-workflow-management-apis"></a>
## 2. Workflow Management APIs

<a name="21-workflow-status-notification"></a>
### 2.1 Workflow Status Notification

Notify Loco about workflow completion status and bonus fulfillment.

<a name="endpoint-2"></a>
#### Endpoint

```
POST /api/v1/workflow-status
```

<a name="description-2"></a>
#### Description

Sends status updates to Loco after user completes workflows on the operator platform. Used to track signup completion, bonus fulfillment, and battles redirection. This endpoint supports multiple reward types including quests, drops, and link account.

<a name="request-headers-2"></a>
#### Request Headers

| Header | Type | Mandatory | Description |
|--------|------|-----------|-------------|
| Authorization | string | Yes | Bearer {ACCESS_TOKEN} |
| Content-Type | string | Yes | application/json |
| Accept | string | Yes | application/json |

<a name="request-body-2"></a>
#### Request Body

| Field | Type | Mandatory | Description |
|-------|------|-----------|-------------|
| loco_uid | string | Yes | Unique Loco user identifier |
| ext_player_id | string | No | Operator platform user ID |
| screen_name | string | No | Operator platform username |
| source | string | Yes | UTM source parameter |
| campaign | string | Yes | Campaign identifier |
| brand | string | Yes | Operator brand identifier |
| timestamp | string | Yes | ISO 8601 formatted timestamp |
| loco_txn_id | string | Yes | Unique transaction ID for idempotency |
| status | string | Yes | COMPLETE \| FAILED \| LINKING_FAILED |
| failure_reason | string | Conditional | Required if status is FAILED or LINKING_FAILED |
| category | string | No | User categorization for segmentation |
| is_bonus_offered | boolean | No | true if reward was provided to user |
| loco_reward_type | string | No | quest \| drops \| lucky_draw (provided by Loco in redirection) |

**Sample Request:**

```json
{
  "loco_uid": "loco_12345678",
  "ext_player_id": "op_98765432",
  "screen_name": "ProGamer123",
  "source": "loco_app",
  "campaign": "march_2026_signup",
  "brand": "operator_brand",
  "timestamp": "2026-03-04T12:45:00Z",
  "loco_txn_id": "txn_20260304_003",
  "status": "COMPLETE",
  "category": "high_value",
  "is_bonus_offered": true,
  "loco_reward_type": "drops"
}
```

<a name="response-structure-2"></a>
#### Response Structure

**Response Object: WorkflowStatusResponse**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| status | string | Yes | ACKNOWLEDGED if received successfully |
| message | string | Yes | Success or Failure message |

<a name="notes-2"></a>
#### Notes

- `loco_reward_type` should match the value provided in the redirection URL

---

<a name="3-loco-battles-tournament-apis"></a>
## 3. Loco Battles - Tournament APIs

**Direction:** Loco → Operator  
**Implementation:** **Operator hosts these APIs**  
**Action Required:** Operator expose endpoints to Loco  
**First-Time User Journey:** [Tournament Join Flow (Non-Linked User)](https://i.ibb.co/pvhpHn5V/image-4.png)  
**Returning User Journey:** [Tournament Join Flow (Linked User)](https://i.ibb.co/CpbvS2fg/image-5.png)  
**System Integration Architecture:** [Tournament Integration Flow](https://i.ibb.co/Pzxqnx7b/image-6.png)

<a name="integration-requirements"></a>
### Integration Requirements

Operator must:
1. **Expose** all endpoints specified in this section
2. **Host** these APIs on operator's infrastructure
3. **Provide** base URL to Loco team (production and staging)
4. **Generate** authentication tokens and provide to Loco

**Authentication:** Operator provides bearer token to Loco. Loco will use this token when calling operator endpoints.

<a name="endpoint-summary"></a>
### Endpoint Summary

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/tournament/getTournamentSchedule` | GET | List all tournaments |
| `/tournament/getTournamentConfigDetails` | GET | Get single tournament details |
| `/tournament/getTournamentLeaderBoard` | GET | Get tournament leaderboard |
| `/tournament/getPlayerToRankingDetails` | GET | Get player's rank in tournament |

---

<a name="31-get-tournament-schedule"></a>
### 3.1 Get Tournament Schedule

**Operator must implement this endpoint**

Retrieve list of active and scheduled tournaments.

<a name="endpoint-3"></a>
#### Endpoint

```
GET /tournament/getTournamentSchedule
```

**Full URL Example:** `https://api.operator-domain.com/tournament/getTournamentSchedule?limit=10&offset=0`

<a name="description-3"></a>
#### Description

Loco will call this endpoint to fetch paginated list of tournaments. This data is used for tournament discovery and display within the Loco platform.

**Call Frequency:** Loco polls this endpoint every 60 minutes to sync tournament data.

<a name="request-headers-3"></a>
#### Request Headers

**Loco will send:**

| Header | Type | Value | Description |
|--------|------|-------|-------------|
| Authorization | string | Bearer {OPERATOR_TOKEN} | Operator-provided authentication token |
| Content-Type | string | application/json | Request content type |
| Accept | string | application/json | Expected response format |

<a name="query-parameters"></a>
#### Query Parameters

**Loco will send:**

| Parameter | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| limit | integer | No | Number of records per page (default: 10, max: 100) |
| offset | integer | No | Number of records to skip (default: 0) |

<a name="response-structure-3"></a>
#### Response Structure

**Operator must return:**

**Top-level Response:**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
|status|string|Yes|"SUCCESS" or "ERROR"|
|error_code|string|Yes|Error code if status is ERROR|
|error_id|string|No|Error identifier if status is ERROR|
|error_description|string|No|Error description if status is ERROR|
|data|Array of Tournament|Yes|List of Tournament objects|

**Object: Tournament**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| tournament_id | string | Yes | Unique tournament identifier |
| title | string | Yes | Tournament display name |
| start_time | integer | Yes | Tournament start time (Unix timestamp in seconds) |
| end_time | integer | Yes | Tournament end time (Unix timestamp in seconds) |
| tournament_parent_id | string | No | Parent tournament ID for linked tournaments |
| icon_image | string | Yes | URL of tournament icon (small) |
| background_image | string | Yes | URL of tournament background image (large) |
| tournament_criteria | integer | Yes | Scoring rules enum identifier |
| campaign_status | string | Yes | ACTIVE \| INACTIVE \| SCHEDULED \| COMPLETED |
| campaignType | string | Yes | LEADER_BOARD \| QUALIFIER \| FINAL |
| campaign_sub_type | string | Yes | REGULAR \| SPECIAL \| PREMIUM |
| campaign_category | string | Yes | LIVE_CASINO \| SLOTS \| SPORTS_BETTING \| TABLE_GAMES |

---

<a name="32-get-tournament-details"></a>
### 3.2 Get Tournament Details

**Operator must implement this endpoint**

Retrieve detailed configuration for a specific tournament.

<a name="endpoint-4"></a>
#### Endpoint

```
GET /tournament/getTournamentConfigDetails
```

**Full URL Example:** `https://api.operator-domain.com/tournament/getTournamentConfigDetails?tournament_id=tourn_2024_001`

<a name="description-4"></a>
#### Description

Loco will call this endpoint to fetch comprehensive tournament information including rules, prize breakdown, and configuration. This data is displayed on tournament detail pages within the Loco platform.

**Call Frequency:** Loco fetches this on-demand when users view tournament details.

<a name="request-headers-4"></a>
#### Request Headers

**Loco will send:**

| Header | Type | Value | Description |
|--------|------|-------|-------------|
| Authorization | string | Bearer {OPERATOR_TOKEN} | Operator-provided authentication token |
| Content-Type | string | application/json | Request content type |
| Accept | string | application/json | Expected response format |

<a name="query-parameters-1"></a>
#### Query Parameters

**Loco will send:**

| Parameter | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| tournament_id | string | Yes | Unique tournament identifier |

<a name="response-structure-4"></a>
#### Response Structure

**Operator must return:**

**Top-level Response:**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
|status|string|Yes|"SUCCESS" or "ERROR"|
|error_code|string|Yes|Error code if status is ERROR|
|error_id|string|No|Error identifier if status is ERROR|
|error_description|string|No|Error description if status is ERROR|
|data|Tournament (object)|Yes|Tournament object|

**Object: TournamentDetail**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| tournament_id | string | Yes | Unique tournament identifier |
| title | string | Yes | Tournament display name |
| reward_pool | string | Yes | Total prize pool with currency (e.g., "$10,000") |
| start_time | integer | Yes | Tournament start time (Unix timestamp in seconds) |
| end_time | integer | Yes | Tournament end time (Unix timestamp in seconds) |
| tournament_criteria | integer | Yes | Scoring rules enum identifier |
| guidelines | object | Yes | Tournament rules and prize breakdown |
| tournament_parent_id | string | No | Parent tournament ID for linked tournaments |
| created_at | integer | Yes | Tournament creation timestamp (Unix seconds) |
| updated_at | integer | Yes | Last update timestamp (Unix seconds) |
| campaign_status | string | Yes | ACTIVE \| INACTIVE \| SCHEDULED \| COMPLETED |

**Nested Object: guidelines**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| title | string | Yes | Guidelines section title |
| sub_title | string | Yes | Guidelines description or subtitle |
| data | array | Yes | List of PrizeRank objects |

**Nested Object: PrizeRank** (within guidelines.data)

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| rank | string | Yes | Position or rank range (e.g., "1", "2-5", "6-10") |
| prize | string | Yes | Prize amount with currency symbol |
| user_avatar | string | Yes | URL of avatar/badge for this rank tier |

---

<a name="33-get-tournament-leaderboard"></a>
### 3.3 Get Tournament Leaderboard

**Operator must implement this endpoint**

Retrieve leaderboard rankings for a specific tournament.

<a name="endpoint-5"></a>
#### Endpoint

```
GET /tournament/getTournamentLeaderBoard
```

**Full URL Example:** `https://api.operator-domain.com/tournament/getTournamentLeaderBoard?tournament_id=tourn_2024_001&limit=50&offset=0`

<a name="description-5"></a>
#### Description

Loco will call this endpoint to fetch paginated leaderboard rankings showing player positions, scores, and prizes. This data is displayed as live tournament standings within the Loco platform.

**Call Frequency:** Loco fetches this on-demand when users view tournament details.

<a name="request-headers-5"></a>
#### Request Headers

**Loco will send:**

| Header | Type | Value | Description |
|--------|------|-------|-------------|
| Authorization | string | Bearer {OPERATOR_TOKEN} | Operator-provided authentication token |
| Content-Type | string | application/json | Request content type |
| Accept | string | application/json | Expected response format |

<a name="query-parameters-2"></a>
#### Query Parameters

**Loco will send:**

| Parameter | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| tournament_id | string | Yes | Unique tournament identifier |
| limit | integer | No | Number of records per page (default: 10, max: 100) |
| offset | integer | No | Number of records to skip (default: 0) |

<a name="response-structure-5"></a>
#### Response Structure

**Operator must return:**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
|status|string|Yes|"SUCCESS" or "ERROR"|
|error_code|string|Yes|Error code if status is ERROR|
|error_id|string|No|Error identifier if status is ERROR|
|error_description|string|No|Error description if status is ERROR|
|data|LeaderboardData (object)|Yes|LeaderboardData object|

**Object: LeaderboardData**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| tournament_id | string | Yes | Unique tournament identifier |
| leaderboard | array | Yes | List of LeaderboardEntry objects |

**Object: LeaderboardEntry** (within leaderboard array)

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| rank | integer | Yes | Player's current rank position |
| username | string | Yes | Player's display username |
| score | string | Yes | Player's current score (string to support large numbers) |
| prize | string | Yes | Prize amount (numeric string) |
| prize_currency | string | Yes | Prize currency code (e.g., "USD", "INR") |
| user_avatar | string | Yes | URL of player's avatar image |

---

<a name="34-get-player-tournament-rank"></a>
### 3.4 Get Player Tournament Rank

**Operator must implement this endpoint**

Retrieve individual player's rank and performance in a specific tournament.

<a name="endpoint-6"></a>
#### Endpoint

```
GET /tournament/getPlayerToRankingDetails
```

**Full URL Example:** `https://api.operator-domain.com/tournament/getPlayerToRankingDetails?user_id=op_98765432&tournament_id=tourn_2024_001`

<a name="description-6"></a>
#### Description

Loco will call this endpoint to fetch a specific player's current standing in a tournament. This data is used to show personalized "My Tournament Progress" views for logged-in users on Loco platform.

**Call Frequency:** Loco fetches this on-demand when users view their tournament progress.

<a name="request-headers-6"></a>
#### Request Headers

**Loco will send:**

| Header | Type | Value | Description |
|--------|------|-------|-------------|
| Authorization | string | Bearer {OPERATOR_TOKEN} | Operator-provided authentication token |
| Content-Type | string | application/json | Request content type |
| Accept | string | application/json | Expected response format |

<a name="query-parameters-3"></a>
#### Query Parameters

**Loco will send:**

| Parameter | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| user_id | string | Yes | Operator platform user ID (external_player_id) |
| tournament_id | string | Yes | Unique tournament identifier |

<a name="response-structure-6"></a>
#### Response Structure

**Operator must return:**

**Response Object: PlayerRank**

| Field Name | Type | Mandatory | Description |
|------------|------|-----------|-------------|
| tournament_id | string | Yes | Unique tournament identifier |
| external_player_id | string | Yes | Player's ID on operator platform |
| screen_name | string | Yes | Display name on operator platform |
| user_name | string | Yes | Loco username of the player |
| user_avatar | string | Yes | URL of player's profile picture |
| rank | integer | Yes | Player's current rank position (0 if not ranked) |
| score | integer | Yes | Player's current tournament score |
| prize | string | Yes | Prize amount won or projected |
| prize_currency | string | Yes | Prize currency code (e.g., "USD", "INR") |

---

<a name="4-redirection-management"></a>
## 4. Redirection Management

**Direction:** Loco → Operator  
**Implementation:** Loco generates these deep links  
**Action Required:** Operator must accept and validate these deep links

<a name="41-deep-link-format"></a>
### 4.1 Deep Link Format

Loco uses the following deep link format to redirect users from the Loco platform to the operator's platform. Operator must implement a redirect handler to accept and validate these links.

<a name="deep-link-structure"></a>
#### Deep Link Structure

**Generated by Loco, consumed by Operator:**

```
https://redirect.operator.com/{BRAND}/register?utm_source=loco&utm_loco_uid={user_uid}&utm_loco_uname={username}&utm_campaign={campaign_name}&txn_id={unique_transaction_id}&op_type={bonus}&op_type_id={bundle_id|tournament_id}&ts={timestamp}&sig={hmac_signature}
```

**Operator must:**
1. Configure redirect endpoint: `https://redirect.{operator-domain}.com/{BRAND}/register`
2. Accept all query parameters listed below

<a name="url-parameters"></a>
#### URL Parameters

| Parameter | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| BRAND | string | Yes | Operator brand identifier (path parameter) |
| utm_source | string | Yes | Always "loco" for attribution tracking |
| utm_loco_uid | string | Yes | Unique Loco user identifier |
| utm_loco_uname | string | Yes | Loco username |
| utm_campaign | string | Yes | Campaign identifier for tracking |
| txn_id | string | Yes | Unique transaction ID for idempotency |
| op_type | string | Yes | Operation type (e.g., "bonus", "tournament", "quest") |
| op_type_id | string | Yes | Reward ID or tournament ID|
| ts | integer | Yes | Unix timestamp (seconds) |
| sig | string | Yes | HMAC-SHA256 signature for request validation |

<a name="hmac-signature-generation"></a>
#### HMAC Signature Generation

**Algorithm:** HMAC-SHA256

**By Loco (for signing):**
- Loco constructs query string and generates signature using shared secret
- Signature is appended to URL as `sig` parameter

**Example Query String for Hashing:**

```
op_type=bonus&op_type_id=123&ts=1709548800&txn_id=txn_001&utm_campaign=march&utm_loco_uid=loco_123&utm_loco_uname=gamer&utm_source=loco
```

**Shared Secret:**
- Exchanged securely during onboarding (not in this document)
- Same secret used by both Loco (signing) and Operator (validation)

<a name="example-deep-link"></a>
#### Example Deep Link

```
https://redirect.operator.com/casino/register?utm_source=loco&utm_loco_uid=loco_12345678&utm_loco_uname=gamer123&utm_campaign=march_2026_drops&txn_id=txn_20260304_004&op_type=bonus&op_type_id=bundle_001&ts=1709548800&sig=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

---

<a name="5-appendices"></a>
## 5. Appendices

<a name="appendix-a-standard-http-error-codes"></a>
### Appendix A: Standard HTTP Error Codes

| Code | Meaning | Description | Recommended Action |
|------|---------|-------------|-------------------|
| 400 | Bad Request | Malformed request syntax or invalid parameters | Validate request format and parameters |
| 401 | Unauthorized | Missing or invalid authentication token | Check Authorization header and token validity |
| 403 | Forbidden | Valid token but insufficient permissions | Verify operator permissions with Loco team |
| 404 | Not Found | Resource does not exist | Verify resource ID and endpoint path |
| 409 | Conflict | Resource already exists or state conflict | Check for duplicate operations or stale data |
| 422 | Unprocessable Entity | Valid syntax but semantic errors | Review business logic constraints |
| 429 | Too Many Requests | Rate limit exceeded | Implement exponential backoff and retry logic |
| 500 | Internal Server Error | Server-side processing failure | Retry with exponential backoff; contact support if persistent |
| 502 | Bad Gateway | Upstream service unavailable | Retry after delay; check Loco status page |
| 503 | Service Unavailable | Temporary service disruption | Retry with exponential backoff |
| 504 | Gateway Timeout | Request timeout | Increase timeout or retry; contact support if persistent |

<a name="appendix-b-data-type-conventions"></a>
### Appendix B: Data Type Conventions

| Type | Description | Format/Example |
|------|-------------|----------------|
| string | UTF-8 text | "example_text" |
| integer | Whole number | 12345 |
| boolean | True/false value | true or false |
| timestamp | Unix timestamp | 1709548800 (seconds since epoch) |
| iso8601 | ISO 8601 datetime | "2026-03-04T12:30:00Z" |
| array | Ordered list | [item1, item2, item3] |
| object | Key-value structure | {"key": "value"} |

---
