# Volzen — API Specification

All endpoints are prefixed with `/api/v1`.  
All request and response bodies are `application/json`.  
All monetary values are in **Vietnamese Dong (VND)**, represented as integers.  
Coordinates are **decimal degrees** (WGS84).  
Timestamps are **ISO 8601** strings (`2026-06-27T09:00:00+07:00`).

---

## Authentication

### `POST /api/v1/auth/signup`

Register a new account.

**Request**
```json
{
  "fullName": "Nguyen Van A",
  "email": "user@example.com",
  "password": "minimum8chars",
  "role": "driver" // "driver" | "provider"
}
```

**Response `201`**
```json
{
  "user": {
    "id": "usr_01j9x",
    "fullName": "Nguyen Van A",
    "email": "user@example.com",
    "role": "driver",
    "createdAt": "2026-06-27T08:00:00+07:00"
  },
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

---

### `POST /api/v1/auth/login`

**Request**
```json
{
  "email": "user@example.com",
  "password": "minimum8chars"
}
```

**Response `200`**
```json
{
  "user": {
    "id": "usr_01j9x",
    "fullName": "Nguyen Van A",
    "email": "user@example.com",
    "role": "driver",
    "createdAt": "2026-06-27T08:00:00+07:00"
  },
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

---

### `POST /api/v1/auth/refresh`

Exchange a refresh token for a new access token.

**Request**
```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

**Response `200`**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### `POST /api/v1/auth/logout`

Invalidate the current refresh token. Requires `Authorization: Bearer <accessToken>`.

**Request** _(empty body)_

**Response `204`** _(no body)_

---

## Current User

### `GET /api/v1/me`

Fetch the authenticated user's profile. Requires auth.

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

Update name or avatar. Requires auth.

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

List all vehicles registered to the authenticated driver.

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
  "connectorType": "CCS" // "Type 1" | "Type 2" | "CCS" | "CHAdeMO"
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
| `connectorType` | string | no | Filter by connector: `Type 1`, `Type 2`, `CCS`, `CHAdeMO` |
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
        "https://cdn.volzen.vn/stations/pvd_p1/photo_1.jpg",
        "https://cdn.volzen.vn/stations/pvd_p1/photo_2.jpg"
      ],
      "isAvailable": true
    }
  ]
}
```

---

### `GET /api/v1/providers/:providerId`

Fetch full provider detail including reviews. Used by the Provider Detail screen.

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

Return the provider's booked time slots for a given date so the frontend can block them out in the time picker.

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

Create a new order (booking). Triggers payment processing. Requires auth.

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
  "status": "confirmed", // "pending" | "confirmed" | "active" | "completed" | "cancelled"
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

**Error `409`** — time slot already booked
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

List all orders for the authenticated driver.

**Query parameters**

| Param | Type | Description |
|---|---|---|
| `status` | string | Filter by status (see status enum above) |
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

Cancel an order. Driver-initiated. Returns `403` if the order is already `active` or `completed`.

**Request** _(empty body)_

**Response `200`**
```json
{
  "id": "ord_xyz9",
  "status": "cancelled"
}
```

---

## Reviews

### `POST /api/v1/orders/:orderId/review`

Submit a review after an order is `completed`. One review per order. Requires auth.

**Request**
```json
{
  "rating": 5,        // integer 1–5
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

Fetch the authenticated provider's station profile.

**Response `200`**
```json
{
  "id": "pvd_p1",
  "address": "12 Nguyen Hue, District 1, Ho Chi Minh City",
  "lat": 10.7769,
  "lng": 106.7009,
  "pricePerHour": 25000,
  "connectorTypes": ["Type 2", "CCS"],
  "amenities": ["Coffee", "WiFi", "Air Conditioning", "Restroom"],
  "photoUrls": [
    "https://cdn.volzen.vn/stations/pvd_p1/photo_1.jpg"
  ],
  "isAvailable": true
}
```

---

### `PUT /api/v1/me/station`

Create or fully update the provider's station. Requires auth as `provider` role.

**Request**
```json
{
  "address": "12 Nguyen Hue, District 1, Ho Chi Minh City",
  "lat": 10.7769,
  "lng": 106.7009,
  "pricePerHour": 25000,
  "connectorTypes": ["Type 2", "CCS"],
  "amenities": ["Coffee", "WiFi", "Air Conditioning", "Restroom"],
  "photoUrls": [
    "https://cdn.volzen.vn/stations/pvd_p1/photo_1.jpg"
  ],
  "isAvailable": true
}
```

**Response `200`** — same shape as `GET /api/v1/me/station`

---

### `GET /api/v1/me/station/orders`

List incoming bookings for the authenticated provider.

**Query parameters** — same as `GET /api/v1/me/orders`

**Response `200`**
```json
{
  "total": 5,
  "orders": [
    {
      "id": "ord_xyz9",
      "startTime": "2026-06-28T09:00:00+07:00",
      "endTime": "2026-06-28T11:00:00+07:00",
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
        "connectorType": "CCS"
      }
    }
  ]
}
```

---

## Shared Types Reference

```
ConnectorType   = "Type 1" | "Type 2" | "CCS" | "CHAdeMO"

Role            = "driver" | "provider"

OrderStatus     = "pending"     // payment processing
                | "confirmed"   // payment success, not yet started
                | "active"      // driver has arrived, charging in progress
                | "completed"   // session ended
                | "cancelled"

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

Common HTTP status codes:

| Code | Meaning |
|---|---|
| `400` | Validation error — malformed request body or missing required fields |
| `401` | Missing or expired access token |
| `403` | Authenticated but not authorised (e.g. driver hitting provider-only endpoint) |
| `404` | Resource not found |
| `409` | Conflict (slot taken, duplicate review, etc.) |
| `422` | Business rule violation (e.g. end time before start time) |
| `500` | Internal server error |
