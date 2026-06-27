import type { ConnectorType, Provider, Review, Vehicle } from "@/lib/domain";
import { DEFAULT_LOCATION } from "@/lib/location";
import type { Coordinates } from "@/lib/location";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";
const API_V1 = `${API_BASE_URL}/api/v1`;

type ApiProvider = {
  id: string;
  name: string;
  avatarUrl: string | null;
  address: string;
  lat: number | string;
  lng: number | string;
  distanceKm: number | null;
  pricePerHour: number;
  rating: number;
  reviewCount: number;
  connectorTypes: string[];
  amenities: string[];
  photoUrls: string[];
  isAvailable: boolean;
};

type ApiReview = {
  id: string;
  authorName: string;
  authorAvatarUrl: string | null;
  rating: number;
  comment: string;
  createdAt: string;
};

type ApiProviderDetail = ApiProvider & {
  reviews: ApiReview[];
};

type ApiVehicle = {
  id: string;
  brand: string;
  model: string;
  year: number;
  connectorType: string;
  isDefault: boolean;
};

type CreateVehicleInput = {
  brand: string;
  model: string;
  year: number;
  connectorType: string;
  isDefault?: boolean;
};

type CreateOrderInput = {
  providerId: string;
  vehicleId: string;
  startTime: string;
  endTime: string;
};

type ApiOrder = {
  id: string;
  providerId: string;
  vehicleId: string;
  startTime: string;
  endTime: string;
  durationHours: number;
  pricePerHour: number;
  subtotal: number;
  serviceFee: number;
  total: number;
  status: string;
  provider: {
    id: string;
    name: string;
    address: string;
    lat: number | string;
    lng: number | string;
    avatarUrl: string | null;
  };
};

type ApiAvailability = {
  date: string;
  bookedSlots: Array<{
    startTime: string;
    endTime: string;
  }>;
};

export type DriverOrder = ApiOrder;
export type BookedSlot = ApiAvailability["bookedSlots"][number];

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_V1}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
  });

  if (!response.ok) {
    let message = `Request failed with status ${response.status}.`;
    try {
      const body = await response.json();
      message = body?.message ?? body?.error?.message ?? message;
    } catch {
      // Keep the generic message when the server returns no JSON body.
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export async function searchProviders(location: Coordinates = DEFAULT_LOCATION) {
  const params = new URLSearchParams({
    lat: String(location.lat),
    lng: String(location.lng),
    radiusKm: "20",
    limit: "20",
    offset: "0",
  });
  const data = await request<{ total: number; providers: ApiProvider[] }>(`/providers?${params}`);
  return data.providers.map(mapProvider);
}

export async function getProvider(providerId: string) {
  const data = await request<ApiProviderDetail>(`/providers/${providerId}`);
  return mapProviderDetail(data);
}

export async function getProviderAvailability(providerId: string, date: string) {
  const params = new URLSearchParams({ date });
  const data = await request<ApiAvailability>(`/providers/${providerId}/availability?${params}`);
  return data.bookedSlots;
}

export async function listVehicles() {
  const data = await request<{ vehicles: ApiVehicle[] }>("/me/vehicles");
  return data.vehicles.map(mapVehicle);
}

export async function createVehicle(input: CreateVehicleInput) {
  const data = await request<ApiVehicle>("/me/vehicles", {
    method: "POST",
    body: JSON.stringify(input),
  });
  return mapVehicle(data);
}

export async function createOrder(input: CreateOrderInput) {
  return request<ApiOrder>("/orders", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export async function getOrder(orderId: string) {
  return request<ApiOrder>(`/orders/${orderId}`);
}

function mapProvider(provider: ApiProvider): Provider {
  return {
    id: provider.id,
    name: provider.name,
    avatar: imageOrFallback(provider.avatarUrl, `https://i.pravatar.cc/80?u=${provider.id}`),
    address: provider.address,
    lat: Number(provider.lat),
    lng: Number(provider.lng),
    distanceKm: provider.distanceKm ?? 0,
    pricePerHour: provider.pricePerHour,
    rating: provider.rating,
    reviewCount: provider.reviewCount,
    connectors: provider.connectorTypes as ConnectorType[],
    amenities: provider.amenities,
    photos: provider.photoUrls.length > 0
      ? provider.photoUrls.map((url, index) => imageOrFallback(url, `https://picsum.photos/seed/${provider.id}-${index}/800/400`))
      : [`https://picsum.photos/seed/${provider.id}/800/400`],
    reviews: [],
  };
}

function mapProviderDetail(provider: ApiProviderDetail): Provider {
  return {
    ...mapProvider(provider),
    reviews: provider.reviews.map(mapReview),
  };
}

function mapReview(review: ApiReview): Review {
  return {
    id: review.id,
    author: review.authorName,
    avatar: imageOrFallback(review.authorAvatarUrl, `https://i.pravatar.cc/40?u=${review.id}`),
    rating: review.rating,
    text: review.comment,
    date: new Intl.DateTimeFormat("en", { month: "short", day: "numeric", year: "numeric" }).format(new Date(review.createdAt)),
  };
}

function imageOrFallback(url: string | null | undefined, fallback: string) {
  if (!url || url.includes("cdn.volzen.vn")) {
    return fallback;
  }

  return url;
}

function mapVehicle(vehicle: ApiVehicle): Vehicle {
  return {
    id: vehicle.id,
    brand: vehicle.brand,
    model: vehicle.model,
    year: vehicle.year,
    connector: vehicle.connectorType as ConnectorType,
  };
}
