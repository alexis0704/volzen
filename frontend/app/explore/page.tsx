"use client";
import dynamic from "next/dynamic";
import { useState } from "react";
import Link from "next/link";
import { MapPin, Zap, Star, ChevronRight, Navigation } from "lucide-react";
import { mockProviders, VN_DONG_FORMAT } from "@/lib/mock-data";
import type { Provider } from "@/lib/mock-data";

const LeafletMap = dynamic(() => import("@/components/map/LeafletMap"), { ssr: false, loading: () => <div className="w-full h-full flex items-center justify-center" style={{ background: "#0a0f0d", color: "#4ade80" }}>Loading map…</div> });

export default function ExplorePage() {
  const [selected, setSelected] = useState<string | null>(null);

  return (
    <div style={{ background: "var(--bg)", height: "100dvh", display: "flex", flexDirection: "column" }}>
      {/* Top bar */}
      <nav className="flex items-center gap-3 px-4 py-3 shrink-0" style={{ borderBottom: "1px solid rgba(74,222,128,0.12)", background: "rgba(10,15,13,0.9)", backdropFilter: "blur(12px)" }}>
        <Link href="/" className="flex items-center gap-1.5">
          <Zap size={18} style={{ color: "var(--accent)" }} fill="currentColor" />
          <span className="font-bold" style={{ color: "var(--text)" }}>Volzen</span>
        </Link>
        <span className="text-xs px-2 py-0.5 rounded-full" style={{ background: "rgba(74,222,128,0.12)", color: "var(--accent)" }}>Driver</span>
        <div className="ml-auto flex items-center gap-1.5 text-xs" style={{ color: "var(--text-muted)" }}>
          <Navigation size={13} /> Ho Chi Minh City
        </div>
      </nav>

      {/* Main split */}
      <div className="flex flex-1 overflow-hidden">
        {/* Map — 60% on lg, full on mobile */}
        <div className="relative flex-1 lg:flex-[6]">
          <LeafletMap providers={mockProviders} selected={selected} onSelect={setSelected} />
        </div>

        {/* Provider list — 40% on lg, bottom sheet on mobile */}
        <aside className="hidden lg:flex lg:flex-[4] flex-col overflow-y-auto" style={{ borderLeft: "1px solid rgba(74,222,128,0.12)", background: "rgba(10,15,13,0.97)" }}>
          <div className="px-4 py-4 shrink-0">
            <p className="text-sm font-semibold" style={{ color: "var(--text)" }}>{mockProviders.length} stations nearby</p>
            <p className="text-xs" style={{ color: "var(--text-muted)" }}>Sorted by distance</p>
          </div>
          <div className="flex flex-col gap-3 px-4 pb-6">
            {mockProviders.map((p) => (
              <ProviderCard key={p.id} provider={p} active={selected === p.id} onSelect={() => setSelected(p.id)} />
            ))}
          </div>
        </aside>
      </div>

      {/* Mobile bottom sheet */}
      <div className="lg:hidden shrink-0 overflow-x-auto" style={{ borderTop: "1px solid rgba(74,222,128,0.15)", background: "rgba(10,15,13,0.97)" }}>
        <div className="flex gap-3 px-4 py-4" style={{ minWidth: "max-content" }}>
          {mockProviders.map((p) => (
            <ProviderCardMini key={p.id} provider={p} active={selected === p.id} onSelect={() => setSelected(p.id)} />
          ))}
        </div>
      </div>
    </div>
  );
}

function ProviderCard({ provider: p, active, onSelect }: { provider: Provider; active: boolean; onSelect: () => void }) {
  return (
    <Link href={`/provider/${p.id}`} onClick={onSelect} className="block rounded-[16px] p-4 transition-all cursor-pointer" style={{ background: active ? "rgba(74,222,128,0.1)" : "rgba(255,255,255,0.04)", border: `1px solid ${active ? "rgba(74,222,128,0.45)" : "rgba(74,222,128,0.14)"}` }}>
      <div className="flex gap-3">
        <img src={p.avatar} alt={p.name} className="w-10 h-10 rounded-full shrink-0 object-cover" />
        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between">
            <p className="font-semibold text-sm truncate" style={{ color: "var(--text)" }}>{p.name}</p>
            <ChevronRight size={14} style={{ color: "var(--text-muted)" }} />
          </div>
          <div className="flex items-center gap-2 mt-0.5">
            <span className="text-xs flex items-center gap-0.5" style={{ color: "var(--text-muted)" }}><Star size={11} fill="currentColor" style={{ color: "#facc15" }} /> {p.rating}</span>
            <span className="text-xs" style={{ color: "var(--text-muted)" }}>· {p.distanceKm} km</span>
          </div>
          <p className="text-xs mt-1 truncate" style={{ color: "var(--text-muted)" }}><MapPin size={11} className="inline mr-0.5" />{p.address}</p>
        </div>
      </div>
      <div className="flex items-center justify-between mt-3">
        <div className="flex gap-1 flex-wrap">
          {p.connectors.map((c) => (
            <span key={c} className="text-[10px] px-2 py-0.5 rounded-full" style={{ background: "rgba(74,222,128,0.12)", color: "var(--accent)", border: "1px solid rgba(74,222,128,0.2)" }}>{c}</span>
          ))}
        </div>
        <p className="text-sm font-bold" style={{ color: "var(--accent)" }}>{VN_DONG_FORMAT(p.pricePerHour)}<span className="text-xs font-normal" style={{ color: "var(--text-muted)" }}>/hr</span></p>
      </div>
    </Link>
  );
}

function ProviderCardMini({ provider: p, active, onSelect }: { provider: Provider; active: boolean; onSelect: () => void }) {
  return (
    <Link href={`/provider/${p.id}`} onClick={onSelect} className="flex flex-col gap-2 rounded-2xl p-3 shrink-0 w-52 transition-all" style={{ background: active ? "rgba(74,222,128,0.1)" : "rgba(255,255,255,0.05)", border: `1px solid ${active ? "rgba(74,222,128,0.45)" : "rgba(74,222,128,0.14)"}` }}>
      <div className="flex items-center gap-2">
        <img src={p.avatar} alt={p.name} className="w-8 h-8 rounded-full object-cover" />
        <div>
          <p className="font-semibold text-xs" style={{ color: "var(--text)" }}>{p.name}</p>
          <p className="text-[10px]" style={{ color: "var(--text-muted)" }}>{p.distanceKm} km · <Star size={9} fill="currentColor" style={{ color: "#facc15", display: "inline" }} /> {p.rating}</p>
        </div>
      </div>
      <p className="text-sm font-bold" style={{ color: "var(--accent)" }}>{VN_DONG_FORMAT(p.pricePerHour)}<span className="text-[10px] font-normal" style={{ color: "var(--text-muted)" }}>/hr</span></p>
    </Link>
  );
}
