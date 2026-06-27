"use client";
import dynamic from "next/dynamic";
import { useParams, useRouter } from "next/navigation";
import { mockProviders } from "@/lib/mock-data";
import { MapPin, Clock, CheckCircle } from "lucide-react";
import { useState } from "react";

// ponytail: cast needed — dynamic() loses prop types on ssr:false
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const RouteMap = dynamic(() => import("@/components/map/RouteMap"), { ssr: false }) as any;

export default function RoutePage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const p = mockProviders.find((x) => x.id === id);
  const [arrived, setArrived] = useState(false);

  if (!p) return <div className="min-h-dvh flex items-center justify-center" style={{ background: "var(--bg)", color: "var(--text)" }}>Not found.</div>;

  // Mock current location slightly offset from provider
  const origin: [number, number] = [p.lat - 0.012, p.lng - 0.009];
  const dest: [number, number] = [p.lat, p.lng];

  return (
    <div style={{ background: "var(--bg)", height: "100dvh", display: "flex", flexDirection: "column" }}>
      <div className="flex-1 relative">
        <RouteMap origin={origin} destination={dest} />

        {/* Payment success badge */}
        <div className="absolute top-4 left-1/2 -translate-x-1/2 flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium" style={{ background: "rgba(74,222,128,0.15)", border: "1px solid rgba(74,222,128,0.4)", backdropFilter: "blur(12px)", color: "var(--accent)" }}>
          <CheckCircle size={14} /> Payment successful
        </div>
      </div>

      {/* Bottom info sheet */}
      <div className="px-5 pt-5 pb-6" style={{ background: "rgba(10,15,13,0.97)", borderTop: "1px solid rgba(74,222,128,0.15)" }}>
        <div className="flex items-center gap-3 mb-4">
          <img src={p.avatar} alt={p.name} className="w-10 h-10 rounded-full object-cover border-2" style={{ borderColor: "rgba(74,222,128,0.4)" }} />
          <div>
            <p className="font-semibold" style={{ color: "var(--text)" }}>{p.name}</p>
            <p className="text-xs flex items-center gap-1" style={{ color: "var(--text-muted)" }}><MapPin size={11} /> {p.address}</p>
          </div>
          <div className="ml-auto text-right">
            <p className="text-xs" style={{ color: "var(--text-muted)" }}>ETA</p>
            <p className="font-bold" style={{ color: "var(--accent)" }}><Clock size={13} className="inline mr-0.5" />~8 min</p>
          </div>
        </div>

        {arrived ? (
          <div className="text-center py-3">
            <p className="font-bold text-lg mb-1" style={{ color: "var(--accent)" }}>You&apos;ve arrived!</p>
            <p className="text-sm mb-4" style={{ color: "var(--text-muted)" }}>Plug in and start charging. Enjoy your stay.</p>
            <button onClick={() => router.push("/explore")} className="px-8 py-3 rounded-xl font-semibold text-sm" style={{ background: "rgba(255,255,255,0.08)", color: "var(--text)", border: "1px solid rgba(74,222,128,0.2)" }}>
              Back to map
            </button>
          </div>
        ) : (
          <button onClick={() => setArrived(true)} className="w-full py-3.5 rounded-xl font-bold text-base hover:opacity-90 transition-opacity" style={{ background: "var(--accent)", color: "#0a0f0d" }}>
            I&apos;ve arrived
          </button>
        )}
      </div>
    </div>
  );
}
