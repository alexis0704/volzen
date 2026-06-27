export type ConnectorType = "Type 1" | "Type 2" | "CCS" | "CHAdeMO";

export interface Provider {
  id: string;
  name: string;
  avatar: string;
  address: string;
  lat: number;
  lng: number;
  distanceKm: number;
  pricePerHour: number;
  rating: number;
  reviewCount: number;
  connectors: ConnectorType[];
  amenities: string[];
  photos: string[];
  reviews: Review[];
}

export interface Review {
  id: string;
  author: string;
  avatar: string;
  rating: number;
  text: string;
  date: string;
}

export interface Vehicle {
  id: string;
  brand: string;
  model: string;
  year: number;
  connector: ConnectorType;
}

export const AMENITIES_ICONS: Record<string, string> = {
  Coffee: "Coffee",
  WiFi: "Wifi",
  "Air Conditioning": "Wind",
  Restroom: "Bath",
  Parking: "ParkingSquare",
  Covered: "Home",
  Security: "Shield",
  Snacks: "Cookie",
};

export const VN_DONG_FORMAT = (n: number) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(n);
