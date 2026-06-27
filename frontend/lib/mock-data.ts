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

export const mockProviders: Provider[] = [
  {
    id: "p1",
    name: "Minh Tuan",
    avatar: "https://i.pravatar.cc/80?img=11",
    address: "12 Nguyen Hue, District 1, Ho Chi Minh City",
    lat: 10.7769,
    lng: 106.7009,
    distanceKm: 0.8,
    pricePerHour: 25000,
    rating: 4.9,
    reviewCount: 42,
    connectors: ["Type 2", "CCS"],
    amenities: ["Coffee", "WiFi", "Air Conditioning", "Restroom"],
    photos: [
      "https://picsum.photos/seed/ev1/800/400",
      "https://picsum.photos/seed/ev2/800/400",
    ],
    reviews: [
      {
        id: "r1",
        author: "Lan Anh",
        avatar: "https://i.pravatar.cc/40?img=5",
        rating: 5,
        text: "Super convenient and fast charger. The host even brought out coffee!",
        date: "2026-06-10",
      },
      {
        id: "r2",
        author: "Duc Huy",
        avatar: "https://i.pravatar.cc/40?img=8",
        rating: 5,
        text: "Great spot, well maintained. Will definitely come back.",
        date: "2026-06-05",
      },
      {
        id: "r3",
        author: "Thu Ha",
        avatar: "https://i.pravatar.cc/40?img=20",
        rating: 4,
        text: "Charger was a bit slow at first but sorted out fine. Host was helpful.",
        date: "2026-05-28",
      },
    ],
  },
  {
    id: "p2",
    name: "Huong Giang",
    avatar: "https://i.pravatar.cc/80?img=25",
    address: "45 Le Loi, District 1, Ho Chi Minh City",
    lat: 10.7731,
    lng: 106.6983,
    distanceKm: 1.4,
    pricePerHour: 20000,
    rating: 4.7,
    reviewCount: 28,
    connectors: ["Type 2"],
    amenities: ["WiFi", "Restroom", "Parking"],
    photos: [
      "https://picsum.photos/seed/ev3/800/400",
      "https://picsum.photos/seed/ev4/800/400",
    ],
    reviews: [
      {
        id: "r4",
        author: "Phuong Nam",
        avatar: "https://i.pravatar.cc/40?img=3",
        rating: 5,
        text: "Best price in the area. Clean and secure parking.",
        date: "2026-06-12",
      },
      {
        id: "r5",
        author: "Khanh Linh",
        avatar: "https://i.pravatar.cc/40?img=15",
        rating: 4,
        text: "Reliable charger, friendly host. Good value.",
        date: "2026-06-01",
      },
    ],
  },
  {
    id: "p3",
    name: "Van Long",
    avatar: "https://i.pravatar.cc/80?img=33",
    address: "88 Nam Ky Khoi Nghia, District 3, Ho Chi Minh City",
    lat: 10.7835,
    lng: 106.6928,
    distanceKm: 2.1,
    pricePerHour: 30000,
    rating: 4.8,
    reviewCount: 61,
    connectors: ["CCS", "CHAdeMO", "Type 2"],
    amenities: ["Coffee", "Air Conditioning", "Security", "Covered", "Snacks"],
    photos: [
      "https://picsum.photos/seed/ev5/800/400",
      "https://picsum.photos/seed/ev6/800/400",
    ],
    reviews: [
      {
        id: "r6",
        author: "Bao Chau",
        avatar: "https://i.pravatar.cc/40?img=44",
        rating: 5,
        text: "Fastest CCS charger I've found on the platform. Premium experience.",
        date: "2026-06-15",
      },
      {
        id: "r7",
        author: "Tuan Kiet",
        avatar: "https://i.pravatar.cc/40?img=60",
        rating: 5,
        text: "Love the covered spot. No worrying about rain while charging.",
        date: "2026-06-08",
      },
    ],
  },
  {
    id: "p4",
    name: "Bich Ngoc",
    avatar: "https://i.pravatar.cc/80?img=47",
    address: "22 Tran Hung Dao, District 5, Ho Chi Minh City",
    lat: 10.757,
    lng: 106.681,
    distanceKm: 3.6,
    pricePerHour: 18000,
    rating: 4.5,
    reviewCount: 17,
    connectors: ["Type 1", "Type 2"],
    amenities: ["Parking", "WiFi"],
    photos: [
      "https://picsum.photos/seed/ev7/800/400",
    ],
    reviews: [
      {
        id: "r8",
        author: "Quoc Bao",
        avatar: "https://i.pravatar.cc/40?img=50",
        rating: 4,
        text: "Budget-friendly option. Does the job perfectly for overnight charging.",
        date: "2026-06-03",
      },
    ],
  },
];

export const mockVehicles: Vehicle[] = [
  {
    id: "v1",
    brand: "VinFast",
    model: "VF8",
    year: 2024,
    connector: "CCS",
  },
];

export const VN_DONG_FORMAT = (n: number) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(n);
