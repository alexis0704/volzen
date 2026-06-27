# Volzen — API Specification

All endpoints are prefixed with `/api/v1`.  
All request and response bodies are `application/json`.  
All monetary values are in **Vietnamese Dong (VND)**, represented as integers.  
Coordinates are **decimal degrees** (WGS84).  
Timestamps are **ISO 8601** strings (`2026-06-27T09:00:00+07:00`).

> **Prototype note:** Auth is stubbed. All endpoints assume a hardcoded current user — no `Authorization` header required. Skip signup/login for now.

---

## Current User

### `GET /api/v1/me`

Fetch the current user's profile.

**Response `200`**
```json
{
  "id": "usr_01j9x",
  "fullName": "Nguyen Van A",
  "email": "user@example.com",
  "role": "driver",
  "avatarUrl": "https://cdn.volzen.vn/avatars/usr_01j9x.jpg",
  "createdAt": "2026-06-27T08:00:00+07:00"
}
```

---

### `PATCH /api/v1/me`

Update name or avatar.

**Request**
```json
{
  "fullName": "Nguyen Van B",
  "avatarUrl": "https://cdn.volzen.vn/avatars/usr_01j9x.jpg"
}
```

**Response `200`** — same shape as `GET /api/v1/me`

---

## Vehicles (Driver)

### `GET /api/v1/me/vehicles`

List all vehicles registered to the current driver.

**Response `200`**
```json
{
  "vehicles": [
    {
      "id": "veh_abc1",
      "brand": "VinFast",
      "model": "VF8",
      "year": 2024,
      "connectorType": "CCS",
      "isDefault": true
    },
    {
      "id": "veh_abc2",
      "brand": "Toyota",
      "model": "bZ4X",
      "year": 2023,
      "connectorType": "Type 2",
      "isDefault": false
    }
  ]
}
```

---

### `POST /api/v1/me/vehicles`

Add a new vehicle.

**Request**
```json
{
  "brand": "VinFast",
  "model": "VF8",
  "year": 2024,
  "connectorType": "CCS"
}
```

**Response `201`**
```json
{
  "id": "veh_abc1",
  "brand": "VinFast",
  "model": "VF8",
  "year": 2024,
  "connectorType": "CCS",
  "isDefault": true
}
```

---

### `PATCH /api/v1/me/vehicles/:vehicleId`

Update a vehicle or set it as default.

**Request**
```json
{
  "brand": "VinFast",
  "model": "VF9",
  "year": 2025,
  "connectorType": "CCS",
  "isDefault": true
}
```

**Response `200`** — same shape as a single vehicle object above

---

### `DELETE /api/v1/me/vehicles/:vehicleId`

Remove a vehicle. Returns `409` if it is the only vehicle on the account.

**Response `204`** _(no body)_

---

## Providers (Stations)

### `GET /api/v1/providers`

Search for providers near a location. Used by the map and list on the Explore screen.

**Query parameters**

| Param | Type | Required | Description |
|---|---|---|---|
| `lat` | float | yes | Centre latitude |
| `lng` | float | yes | Centre longitude |
| `radiusKm` | float | no | Search radius in km (default: `5`) |
| `connectorType` | string | no | Filter: `Type 1`, `Type 2`, `CCS`, `CHAdeMO` |
| `maxPricePerHour` | int | no | Upper price bound (VND) |
| `limit` | int | no | Max results (default: `20`, max: `50`) |
| `offset` | int | no | Pagination offset (default: `0`) |

**Response `200`**
```json
{
  "total": 4,
  "providers": [
    {
      "id": "pvd_p1",
      "name": "Minh Tuan",
      "avatarUrl": "https://cdn.volzen.vn/avatars/pvd_p1.jpg",
      "address": "12 Nguyen Hue, District 1, Ho Chi Minh City",
      "lat": 10.7769,
      "lng": 106.7009,
      "distanceKm": 0.8,
      "pricePerHour": 25000,
      "rating": 4.9,
      "reviewCount": 42,
      "connectorTypes": ["Type 2", "CCS"],
      "amenities": ["Coffee", "WiFi", "Air Conditioning", "Restroom"],
      "photoUrls": [
        "https://cdn.volzen.vn/stations/pvd_p1/photo_1.jpg"
      ],
      "isAvailable": true
    }
  ]
}
```

---

### `GET /api/v1/providers/:providerId`

Full provider detail including reviews. Used by the Provider Detail screen.

**Response `200`**
```json
{
  "id": "pvd_p1",
  "name": "Minh Tuan",
  "avatarUrl": "https://cdn.volzen.vn/avatars/pvd_p1.jpg",
  "address": "12 Nguyen Hue, District 1, Ho Chi Minh City",
  "lat": 10.7769,
  "lng": 106.7009,
  "distanceKm": 0.8,
  "pricePerHour": 25000,
  "rating": 4.9,
  "reviewCount": 42,
  "connectorTypes": ["Type 2", "CCS"],
  "amenities": ["Coffee", "WiFi", "Air Conditioning", "Restroom"],
  "photoUrls": [
    "https://cdn.volzen.vn/stations/pvd_p1/photo_1.jpg",
    "https://cdn.volzen.vn/stations/pvd_p1/photo_2.jpg"
  ],
  "isAvailable": true,
  "reviews": [
    {
      "id": "rev_r1",
      "authorName": "Lan Anh",
      "authorAvatarUrl": "https://cdn.volzen.vn/avatars/usr_lananh.jpg",
      "rating": 5,
      "comment": "Super convenient and fast charger. The host even brought out coffee!",
      "createdAt": "2026-06-10T14:22:00+07:00"
    }
  ]
}
```

---

### `GET /api/v1/providers/:providerId/availability`

Booked time slots for a given date. Used by the order time picker to block unavailable slots.

**Query parameters**

| Param | Type | Required | Description |
|---|---|---|---|
| `date` | string | yes | ISO date `2026-06-28` |

**Response `200`**
```json
{
  "date": "2026-06-28",
  "bookedSlots": [
    {
      "startTime": "2026-06-28T09:00:00+07:00",
      "endTime": "2026-06-28T11:00:00+07:00"
    },
    {
      "startTime": "2026-06-28T14:00:00+07:00",
      "endTime": "2026-06-28T15:30:00+07:00"
    }
  ]
}
```

---

## Orders

### `POST /api/v1/orders`

Create a new booking.

**Request**
```json
{
  "providerId": "pvd_p1",
  "vehicleId": "veh_abc1",
  "startTime": "2026-06-28T09:00:00+07:00",
  "endTime": "2026-06-28T11:00:00+07:00"
}
```

**Response `201`**
```json
{
  "id": "ord_xyz9",
  "providerId": "pvd_p1",
  "vehicleId": "veh_abc1",
  "driverId": "usr_01j9x",
  "startTime": "2026-06-28T09:00:00+07:00",
  "endTime": "2026-06-28T11:00:00+07:00",
  "durationHours": 2.0,
  "pricePerHour": 25000,
  "subtotal": 50000,
  "serviceFee": 5000,
  "total": 55000,
  "status": "confirmed",
  "createdAt": "2026-06-27T08:30:00+07:00",
  "provider": {
    "id": "pvd_p1",
    "name": "Minh Tuan",
    "address": "12 Nguyen Hue, District 1, Ho Chi Minh City",
    "lat": 10.7769,
    "lng": 106.7009,
    "avatarUrl": "https://cdn.volzen.vn/avatars/pvd_p1.jpg"
  }
}
```

**Error `409`** — slot already booked
```json
{
  "error": "SLOT_UNAVAILABLE",
  "message": "The requested time slot is no longer available."
}
```

---

### `GET /api/v1/orders/:orderId`

Fetch a single order. Used by the Order Confirmation and Route screens.

**Response `200`** — same shape as `POST /api/v1/orders` response

---

### `GET /api/v1/me/orders`

List all orders for the current driver.

**Query parameters**

| Param | Type | Description |
|---|---|---|
| `status` | string | Filter by status (see status enum below) |
| `limit` | int | Default `20` |
| `offset` | int | Default `0` |

**Response `200`**
```json
{
  "total": 3,
  "orders": [
    {
      "id": "ord_xyz9",
      "providerId": "pvd_p1",
      "vehicleId": "veh_abc1",
      "startTime": "2026-06-28T09:00:00+07:00",
      "endTime": "2026-06-28T11:00:00+07:00",
      "total": 55000,
      "status": "confirmed",
      "provider": {
        "id": "pvd_p1",
        "name": "Minh Tuan",
        "address": "12 Nguyen Hue, District 1, Ho Chi Minh City",
        "avatarUrl": "https://cdn.volzen.vn/avatars/pvd_p1.jpg"
      }
    }
  ]
}
```

---

### `PATCH /api/v1/orders/:orderId/cancel`

Driver cancels an order. Returns `403` if status is already `active` or `completed`.

**Response `200`**
```json
{ "id": "ord_xyz9", "status": "cancelled" }
```

---

## Reviews

### `POST /api/v1/orders/:orderId/review`

Submit a review after an order is `completed`. One review per order.

**Request**
```json
{
  "rating": 5,
  "comment": "Great host, fast charger, highly recommend."
}
```

**Response `201`**
```json
{
  "id": "rev_r99",
  "orderId": "ord_xyz9",
  "providerId": "pvd_p1",
  "authorName": "Nguyen Van A",
  "authorAvatarUrl": "https://cdn.volzen.vn/avatars/usr_01j9x.jpg",
  "rating": 5,
  "comment": "Great host, fast charger, highly recommend.",
  "createdAt": "2026-06-29T10:00:00+07:00"
}
```

---

## Provider Management (Host-side)

### `GET /api/v1/me/station`

Fetch the current provider's station profile.

**Response `200`**
```json
{
  "id": "pvd_p1",
  "name": "Tram sac Minh Tuan",
  "address": "12 Nguyen Hue, District 1, Ho Chi Minh City",
  "lat": 10.7769,
  "lng": 106.7009,
  "pricePerHour": 25000,
  "connectorTypes": ["Type 2", "CCS"],
  "amenities": ["Coffee", "WiFi", "Air Conditioning", "Restroom"],
  "photoUrls": ["https://cdn.volzen.vn/stations/pvd_p1/photo_1.jpg"],
  "isAvailable": true,
  "status": "Active"
}
```

---

### `PUT /api/v1/me/station`

Create or fully update the provider's station.

**Request**
```json
{
  "name": "Tram sac Minh Tuan",
  "address": "12 Nguyen Hue, District 1, Ho Chi Minh City",
  "lat": 10.7769,
  "lng": 106.7009,
  "pricePerHour": 25000,
  "connectorTypes": ["Type 2", "CCS"],
  "amenities": ["Coffee", "WiFi", "Air Conditioning", "Restroom"],
  "photoUrls": ["https://cdn.volzen.vn/stations/pvd_p1/photo_1.jpg"],
  "isAvailable": true
}
```

**Response `200`** — same shape as `GET /api/v1/me/station`

---

### `GET /api/v1/me/station/orders`

List incoming bookings for the current provider. Used by the Bookings screen.

**Query parameters**

| Param | Type | Description |
|---|---|---|
| `status` | string | Filter by status |
| `limit` | int | Default `20` |
| `offset` | int | Default `0` |

**Response `200`**
```json
{
  "total": 5,
  "orders": [
    {
      "id": "ord_xyz9",
      "startTime": "2026-06-28T09:00:00+07:00",
      "endTime": "2026-06-28T11:00:00+07:00",
      "durationHours": 2.0,
      "total": 55000,
      "status": "confirmed",
      "driver": {
        "id": "usr_01j9x",
        "fullName": "Nguyen Van A",
        "avatarUrl": "https://cdn.volzen.vn/avatars/usr_01j9x.jpg"
      },
      "vehicle": {
        "brand": "VinFast",
        "model": "VF8",
        "connectorType": "CCS",
        "plate": "51A-123.45",
        "batteryPercent": 24
      }
    }
  ]
}
```

---

### `PATCH /api/v1/me/station/orders/:orderId/status`

Provider updates the status of a booking. Used by the Bookings screen action buttons (Accept, Reject, Mark Charging, Complete).

**Request**
```json
{
  "status": "confirmed"
}
```

Valid provider-initiated transitions:

| From | To | Action button |
|---|---|---|
| `pending` | `confirmed` | Accept |
| `pending` | `cancelled` | Reject |
| `confirmed` | `active` | Mark Charging |
| `active` | `completed` | Complete |

**Response `200`**
```json
{ "id": "ord_xyz9", "status": "confirmed" }
```

**Error `422`** — invalid transition
```json
{
  "error": "INVALID_STATUS_TRANSITION",
  "message": "Cannot transition from 'completed' to 'confirmed'."
}
```

---

### `POST /api/v1/me/station/blocked-slots`

Block a time window on the provider's calendar (maintenance, personal, etc.). Used by the Block Time modal in the Bookings screen.

**Request**
```json
{
  "startTime": "2026-06-29T10:00:00+07:00",
  "endTime": "2026-06-29T14:00:00+07:00",
  "reason": "Maintenance"
}
```

`reason` values: `"Busy" | "Maintenance" | "Personal" | "Other"`

**Response `201`**
```json
{
  "id": "blk_001",
  "startTime": "2026-06-29T10:00:00+07:00",
  "endTime": "2026-06-29T14:00:00+07:00",
  "reason": "Maintenance"
}
```

**Error `409`** — overlaps an existing confirmed booking
```json
{
  "error": "SLOT_CONFLICT",
  "message": "The blocked window overlaps with an existing confirmed booking."
}
```

---

### `DELETE /api/v1/me/station/blocked-slots/:blockId`

Remove a blocked slot.

**Response `204`** _(no body)_

---

### `GET /api/v1/me/station/analytics`

KPI summary and chart data for the Financial dashboard.

**Query parameters**

| Param | Type | Description |
|---|---|---|
| `year` | int | Year for monthly series (default: current year) |

**Response `200`**
```json
{
  "summary": [
    { "label": "Total Revenue",       "value": "₫4,200,000", "delta": "+12%" },
    { "label": "Revenue This Month",  "value": "₫840,000",   "delta": "+8%" },
    { "label": "Pending Payout",      "value": "₫210,000",   "delta": null },
    { "label": "Completed Sessions",  "value": "23",          "delta": "+3" }
  ],
  "revenueSeries": [120000, 180000, 95000, 210000, 175000, 300000, 260000, 400000, 320000, 510000, 480000, 840000],
  "weeklyRevenue": [95000, 130000, 80000, 160000, 200000, 310000, 260000],
  "occupancyRevenue": [
    { "day": "Mon", "occupancy": 60, "revenue": 150000 },
    { "day": "Tue", "occupancy": 45, "revenue": 110000 },
    { "day": "Wed", "occupancy": 80, "revenue": 200000 },
    { "day": "Thu", "occupancy": 55, "revenue": 135000 },
    { "day": "Fri", "occupancy": 90, "revenue": 225000 },
    { "day": "Sat", "occupancy": 95, "revenue": 240000 },
    { "day": "Sun", "occupancy": 70, "revenue": 175000 }
  ],
  "transactions": [
    {
      "date": "2026-06-27T09:00:00+07:00",
      "driverName": "Lan Anh Nguyen",
      "vehicle": "VinFast VF8",
      "durationHours": 2.0,
      "amount": 55000,
      "status": "completed"
    }
  ]
}
```

---

### `POST /api/v1/me/provider/verify-licence`

Upload business registration document for provider verification. Used by the returning-provider onboarding flow (`/provider-onboarding?returning=true`).

**Request** — `multipart/form-data`

| Field | Type | Description |
|---|---|---|
| `file` | file | Image (JPG/PNG) or PDF of the approved business licence |

**Response `200`**
```json
{
  "verified": true,
  "verifiedAt": "2026-06-28T10:00:00+07:00"
}
```

**Response `202`** — file received but manual review required (edge case)
```json
{
  "verified": false,
  "message": "Document submitted for manual review. You will be notified within 24 hours."
}
```

---

## Shared Types Reference

```
ConnectorType   = "Type 1" | "Type 2" | "CCS" | "CHAdeMO"

Role            = "driver" | "provider"

OrderStatus     = "pending"     // awaiting provider acceptance
                | "confirmed"   // provider accepted
                | "active"      // driver arrived, charging in progress
                | "completed"   // session ended
                | "cancelled"

BlockReason     = "Busy" | "Maintenance" | "Personal" | "Other"

Amenity         = "Coffee" | "WiFi" | "Air Conditioning" | "Restroom"
                | "Parking" | "Covered" | "Security" | "Snacks"
```

---

## Error Response Shape

All error responses follow this structure:

```json
{
  "error": "MACHINE_READABLE_CODE",
  "message": "Human-readable description of what went wrong."
}
```

| Code | Meaning |
|---|---|
| `400` | Validation error — malformed request or missing required fields |
| `403` | Not authorised for this action (e.g. driver hitting provider-only endpoint) |
| `404` | Resource not found |
| `409` | Conflict (slot taken, duplicate review, etc.) |
| `422` | Business rule violation (e.g. invalid status transition, end before start) |
| `500` | Internal server error |
