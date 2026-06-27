"use client";
import { useParams, useRouter } from "next/navigation";
import { mockProviders, VN_DONG_FORMAT, AMENITIES_ICONS } from "@/lib/mock-data";
import { Star, MapPin, ArrowLeft, Wifi, Coffee, Wind, Bath, ParkingSquare, Home, Shield, Cookie, ChevronRight } from "lucide-react";

const ICON_MAP: Record<string, React.ElementType> = {
  Wifi, Coffee, Wind, Bath, ParkingSquare, Home, Shield, Cookie,
};

export default function ProviderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const p = mockProviders.find((x) => x.id === id);

  if (!p) return (
    <div className="min-h-dvh flex items-center justify-center" style={{ background: "var(--bg)", color: "var(--text)" }}>
      Provider not found.
    </div>
  );

  return (
    <div style={{ background: "var(--bg)", minHeight: "100dvh", color: "var(--text)" }}>
      {/* Photo hero */}
      <div className="relative h-56 sm:h-72 overflow-hidden">
        <img src={p.photos[0]} alt={p.name} className="w-full h-full object-cover" />
        <div className="absolute inset-0" style={{ background: "linear-gradient(to bottom, rgba(10,15,13,0.3) 0%, rgba(10,15,13,0.85) 100%)" }} />
        <button onClick={() => router.back()} className="absolute top-4 left-4 w-9 h-9 rounded-full flex items-center justify-center" style={{ background: "rgba(10,15,13,0.7)", backdropFilter: "blur(8px)", border: "1px solid rgba(74,222,128,0.2)" }} aria-label="Go back">
          <ArrowLeft size={16} style={{ color: "var(--text)" }} />
        </button>
        {p.photos[1] && (
          <div className="absolute bottom-4 right-4 w-20 h-14 rounded-xl overflow-hidden border-2" style={{ borderColor: "rgba(74,222,128,0.3)" }}>
            <img src={p.photos[1]} alt="" className="w-full h-full object-cover" />
          </div>
        )}
      </div>

      {/* Content */}
      <div className="max-w-xl mx-auto px-5 py-6 pb-28 flex flex-col gap-6">
        {/* Host info */}
        <div className="flex items-center gap-4">
          <img src={p.avatar} alt={p.name} className="w-14 h-14 rounded-full object-cover border-2" style={{ borderColor: "rgba(74,222,128,0.4)" }} />
          <div>
            <h1 className="text-xl font-bold" style={{ color: "var(--text)" }}>{p.name}</h1>
            <div className="flex items-center gap-2 mt-0.5">
              <span className="flex items-center gap-1 text-sm" style={{ color: "#facc15" }}>
                <Star size={13} fill="currentColor" /> {p.rating}
              </span>
              <span className="text-sm" style={{ color: "var(--text-muted)" }}>({p.reviewCount} reviews)</span>
            </div>
            <p className="text-xs mt-1 flex items-center gap-1" style={{ color: "var(--text-muted)" }}>
              <MapPin size={11} /> {p.address}
            </p>
          </div>
        </div>

        {/* Pricing */}
        <div className="glass p-4 flex items-center justify-between">
          <div>
            <p className="text-xs mb-0.5" style={{ color: "var(--text-muted)" }}>Price per hour</p>
            <p className="text-2xl font-bold" style={{ color: "var(--accent)" }}>{VN_DONG_FORMAT(p.pricePerHour)}</p>
          </div>
          <div className="flex gap-1.5 flex-wrap justify-end">
            {p.connectors.map((c) => (
              <span key={c} className="text-xs px-2.5 py-1 rounded-full" style={{ background: "rgba(74,222,128,0.12)", color: "var(--accent)", border: "1px solid rgba(74,222,128,0.25)" }}>{c}</span>
            ))}
          </div>
        </div>

        {/* Amenities */}
        <div>
          <h2 className="font-semibold mb-3" style={{ color: "var(--text)" }}>Amenities</h2>
          <div className="grid grid-cols-3 sm:grid-cols-4 gap-3">
            {p.amenities.map((a) => {
              const iconKey = AMENITIES_ICONS[a];
              const Icon = ICON_MAP[iconKey] ?? Wifi;
              return (
                <div key={a} className="glass p-3 flex flex-col items-center gap-1.5 text-center">
                  <Icon size={20} style={{ color: "var(--accent)" }} />
                  <span className="text-xs" style={{ color: "var(--text-muted)" }}>{a}</span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Reviews */}
        <div>
          <h2 className="font-semibold mb-3" style={{ color: "var(--text)" }}>Reviews</h2>
          <div className="flex flex-col gap-3">
            {p.reviews.map((r) => (
              <div key={r.id} className="glass p-4">
                <div className="flex items-center gap-3 mb-2">
                  <img src={r.avatar} alt={r.author} className="w-8 h-8 rounded-full object-cover" />
                  <div>
                    <p className="text-sm font-medium" style={{ color: "var(--text)" }}>{r.author}</p>
                    <div className="flex gap-0.5">
                      {Array.from({ length: 5 }).map((_, i) => (
                        <Star key={i} size={10} fill={i < r.rating ? "currentColor" : "none"} style={{ color: "#facc15" }} />
                      ))}
                    </div>
                  </div>
                  <span className="ml-auto text-xs" style={{ color: "var(--text-muted)" }}>{r.date}</span>
                </div>
                <p className="text-sm leading-relaxed" style={{ color: "var(--text-muted)" }}>{r.text}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Sticky book button */}
      <div className="fixed bottom-0 left-0 right-0 px-5 py-4" style={{ background: "rgba(10,15,13,0.95)", backdropFilter: "blur(12px)", borderTop: "1px solid rgba(74,222,128,0.15)" }}>
        <button onClick={() => router.push(`/order/${p.id}`)} className="w-full py-3.5 rounded-xl font-bold text-base flex items-center justify-center gap-2 hover:opacity-90 transition-opacity" style={{ background: "var(--accent)", color: "#0a0f0d" }}>
          Book now <ChevronRight size={18} />
        </button>
      </div>
    </div>
  );
}
