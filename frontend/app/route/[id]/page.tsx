"use client";
import dynamic from "next/dynamic";
import { useParams, useRouter } from "next/navigation";
import type { Provider } from "@/lib/domain";
import { MapPin, Clock, CheckCircle } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { getOrder } from "@/lib/api";
import { getCurrentLocation, watchCurrentLocation } from "@/lib/location";
import type { Coordinates } from "@/lib/location";

// ponytail: cast needed — dynamic() loses prop types on ssr:false
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const RouteMap = dynamic(() => import("@/components/map/RouteMap"), { ssr: false }) as any;

export default function RoutePage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [provider, setProvider] = useState<Provider | null>(null);
  const [loading, setLoading] = useState(true);
  const [arrived, setArrived] = useState(false);
  const [origin, setOrigin] = useState<Coordinates | null>(null);
  const [locationSource, setLocationSource] = useState<"gps" | "fallback">("fallback");
  const [routeInfo, setRouteInfo] = useState<{ distanceKm: number; durationMinutes: number; source: "route" | "fallback" } | null>(null);

  useEffect(() => {
    let ignore = false;

    getOrder(id)
      .then((order) => {
        if (ignore) return;
        setProvider({
          id: order.provider.id,
          name: order.provider.name,
          avatar: imageOrFallback(order.provider.avatarUrl, `https://i.pravatar.cc/80?u=${order.provider.id}`),
          address: order.provider.address,
          lat: Number(order.provider.lat),
          lng: Number(order.provider.lng),
          distanceKm: 0,
          pricePerHour: order.pricePerHour,
          rating: 0,
          reviewCount: 0,
          connectors: [],
          amenities: [],
          photos: [],
          reviews: [],
        });
      })
      .catch(() => {
        if (!ignore) setProvider(null);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [id]);

  useEffect(() => {
    if (!provider) return;

    let ignore = false;
    const fallbackLocation = { lat: provider.lat - 0.012, lng: provider.lng - 0.009 };

    getCurrentLocation()
      .then((location) => {
        if (ignore) return;
        setOrigin(location);
        setLocationSource("gps");
      })
      .catch(() => {
        if (ignore) return;
        setOrigin(fallbackLocation);
        setLocationSource("fallback");
      });

    const watchId = watchCurrentLocation(
      (location) => {
        if (ignore) return;
        setOrigin(location);
        setLocationSource("gps");
      },
      () => {
        if (ignore) return;
        setOrigin((current) => current ?? fallbackLocation);
      },
    );

    return () => {
      ignore = true;
      if (watchId !== null) navigator.geolocation.clearWatch(watchId);
    };
  }, [provider]);

  const handleRouteInfo = useCallback((info: { distanceKm: number; durationMinutes: number; source: "route" | "fallback" }) => {
    setRouteInfo(info);
  }, []);

  const p = provider;
  const routeOrigin = useMemo<[number, number]>(
    () => origin && p ? [origin.lat, origin.lng] : [p ? p.lat - 0.012 : 10.775, p ? p.lng - 0.009 : 106.698],
    [origin, p],
  );
  const dest = useMemo<[number, number]>(() => [p?.lat ?? 10.775, p?.lng ?? 106.698], [p]);

  if (loading) return <div className="min-h-dvh flex items-center justify-center" style={{ background: "var(--bg)", color: "var(--text)" }}>Loading route…</div>;

  if (!p) return <div className="min-h-dvh flex items-center justify-center" style={{ background: "var(--bg)", color: "var(--text)" }}>Not found.</div>;

  return (
    <div style={{ background: "var(--bg)", height: "100dvh", display: "flex", flexDirection: "column" }}>
      <div className="flex-1 relative">
        <RouteMap origin={routeOrigin} destination={dest} onRouteInfo={handleRouteInfo} />

        {/* Payment success badge */}
        <div className="absolute top-4 left-1/2 -translate-x-1/2 flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium" style={{ background: "color-mix(in srgb, var(--accent) 12%, transparent)", border: "1px solid color-mix(in srgb, var(--accent) 35%, transparent)", backdropFilter: "blur(12px)", color: "var(--accent)" }}>
          <CheckCircle size={14} /> Payment successful
        </div>
      </div>

      {/* Bottom info sheet */}
      <div className="flex justify-center px-5 pt-5 pb-6" style={{ background: "var(--bg)", borderTop: "1px solid var(--glass-border)" }}>
        <div className="w-full max-w-lg">
        <div className="flex items-center gap-3 mb-4">
          <img src={p.avatar} alt={p.name} className="w-10 h-10 rounded-full object-cover border-2" style={{ borderColor: "color-mix(in srgb, var(--accent) 40%, transparent)" }} />
          <div>
            <p className="font-semibold" style={{ color: "var(--text)" }}>{p.name}</p>
            <p className="text-xs flex items-center gap-1" style={{ color: "var(--text-muted)" }}><MapPin size={11} /> {p.address}</p>
          </div>
          <div className="ml-auto text-right">
            <p className="text-xs" style={{ color: "var(--text-muted)" }}>ETA</p>
            <p className="font-bold" style={{ color: "var(--accent)" }}><Clock size={13} className="inline mr-0.5" />~{routeInfo?.durationMinutes ?? 8} min</p>
          </div>
        </div>
        <div className="mb-4 flex items-center justify-between gap-3 rounded-xl px-3 py-2 text-xs" style={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", color: "var(--text-muted)" }}>
          <span>{locationSource === "gps" ? "Using live GPS" : "Using approximate start"}</span>
          <span>{routeInfo ? `${routeInfo.distanceKm.toFixed(1)} km · ${routeInfo.source === "route" ? "road route" : "straight estimate"}` : "Calculating route…"}</span>
        </div>

        {arrived ? (
          <div className="text-center py-3">
            <p className="font-bold text-lg mb-1" style={{ color: "var(--accent)" }}>You&apos;ve arrived!</p>
            <p className="text-sm mb-4" style={{ color: "var(--text-muted)" }}>Plug in and start charging. Enjoy your stay.</p>
            <button onClick={() => router.push("/explore")} className="px-8 py-3 rounded-xl font-semibold text-sm" style={{ background: "var(--glass-bg)", color: "var(--text)", border: "1px solid var(--glass-border)" }}>
              Back to map
            </button>
          </div>
        ) : (
          <button onClick={() => setArrived(true)} className="w-full py-3.5 rounded-xl font-bold text-base hover:opacity-90 transition-opacity" style={{ background: "var(--accent)", color: "var(--accent-fg)" }}>
            I&apos;ve arrived
          </button>
        )}
      </div>
      </div>
    </div>
  );
}

function imageOrFallback(url: string | null | undefined, fallback: string) {
  if (!url || url.includes("cdn.volzen.vn")) {
    return fallback;
  }

  return url;
}
