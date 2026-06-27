"use client";
import { useParams, useRouter } from "next/navigation";
import { useState, useMemo } from "react";
import { mockProviders, mockVehicles, VN_DONG_FORMAT } from "@/lib/mock-data";
import { ArrowLeft, Star, MapPin, Car, Clock, Zap } from "lucide-react";

const SERVICE_FEE_RATE = 0.1;

function parseMinutes(timeStr: string) {
  const [h, m] = timeStr.split(":").map(Number);
  return h * 60 + m;
}

export default function OrderPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const p = mockProviders.find((x) => x.id === id);

  const today = new Date().toISOString().split("T")[0];
  const [date, setDate] = useState(today);
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("11:00");
  const [vehicleId, setVehicleId] = useState(mockVehicles[0]?.id ?? "");
  const [loading, setLoading] = useState(false);

  const hours = useMemo(() => {
    const diff = parseMinutes(endTime) - parseMinutes(startTime);
    return diff > 0 ? diff / 60 : 0;
  }, [startTime, endTime]);

  const subtotal = p ? hours * p.pricePerHour : 0;
  const fee = subtotal * SERVICE_FEE_RATE;
  const total = subtotal + fee;

  if (!p) return <div className="min-h-dvh flex items-center justify-center" style={{ background: "var(--bg)", color: "var(--text)" }}>Not found.</div>;

  function handleConfirm() {
    setLoading(true);
    // ponytail: mock payment
    setTimeout(() => router.push(`/route/${p!.id}`), 1500);
  }

  return (
    <div style={{ background: "var(--bg)", minHeight: "100dvh", color: "var(--text)" }}>
      {/* Header */}
      <div className="flex items-center gap-3 px-4 py-4" style={{ borderBottom: "1px solid rgba(74,222,128,0.12)" }}>
        <button onClick={() => router.back()} className="w-8 h-8 rounded-full flex items-center justify-center" style={{ background: "rgba(255,255,255,0.06)", border: "1px solid rgba(74,222,128,0.2)" }} aria-label="Go back">
          <ArrowLeft size={15} style={{ color: "var(--text)" }} />
        </button>
        <h1 className="font-bold text-lg" style={{ color: "var(--text)" }}>Order confirmation</h1>
      </div>

      <div className="max-w-xl mx-auto px-5 py-6 flex flex-col gap-5 pb-32">
        {/* Provider summary */}
        <div className="glass p-4 flex items-center gap-3">
          <img src={p.avatar} alt={p.name} className="w-12 h-12 rounded-full object-cover border-2" style={{ borderColor: "rgba(74,222,128,0.35)" }} />
          <div className="flex-1 min-w-0">
            <p className="font-semibold" style={{ color: "var(--text)" }}>{p.name}</p>
            <p className="text-xs flex items-center gap-1" style={{ color: "var(--text-muted)" }}>
              <MapPin size={11} /> {p.address}
            </p>
          </div>
          <span className="flex items-center gap-1 text-sm font-medium" style={{ color: "#facc15" }}>
            <Star size={13} fill="currentColor" /> {p.rating}
          </span>
        </div>

        {/* Date & time */}
        <div className="glass p-4 flex flex-col gap-4">
          <h2 className="font-semibold flex items-center gap-2" style={{ color: "var(--text)" }}>
            <Clock size={16} style={{ color: "var(--accent)" }} /> Charging time
          </h2>
          <div className="flex flex-col gap-2">
            <Field label="Date">
              <input type="date" value={date} min={today} onChange={(e) => setDate(e.target.value)} className="w-full px-3 py-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-green-400" style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)", colorScheme: "dark" }} />
            </Field>
            <div className="grid grid-cols-2 gap-3">
              <Field label="Start">
                <input type="time" value={startTime} onChange={(e) => setStartTime(e.target.value)} className="w-full px-3 py-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-green-400" style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)", colorScheme: "dark" }} />
              </Field>
              <Field label="End">
                <input type="time" value={endTime} onChange={(e) => setEndTime(e.target.value)} className="w-full px-3 py-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-green-400" style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)", colorScheme: "dark" }} />
              </Field>
            </div>
          </div>
          {hours > 0 ? (
            <p className="text-sm" style={{ color: "var(--text-muted)" }}>Duration: <strong style={{ color: "var(--accent)" }}>{hours.toFixed(1)} hrs</strong></p>
          ) : (
            <p className="text-xs" style={{ color: "#f87171" }}>End time must be after start time.</p>
          )}
        </div>

        {/* Vehicle */}
        <div className="glass p-4 flex flex-col gap-3">
          <h2 className="font-semibold flex items-center gap-2" style={{ color: "var(--text)" }}>
            <Car size={16} style={{ color: "var(--accent)" }} /> Vehicle
          </h2>
          {mockVehicles.map((v) => (
            <button key={v.id} onClick={() => setVehicleId(v.id)} className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition-all" style={{ background: vehicleId === v.id ? "rgba(74,222,128,0.12)" : "rgba(255,255,255,0.05)", border: `1px solid ${vehicleId === v.id ? "rgba(74,222,128,0.45)" : "rgba(74,222,128,0.15)"}`, color: "var(--text)" }}>
              <Car size={16} style={{ color: vehicleId === v.id ? "var(--accent)" : "var(--text-muted)" }} />
              {v.brand} {v.model} · {v.year} · {v.connector}
            </button>
          ))}
        </div>

        {/* Price breakdown */}
        <div className="glass p-4 flex flex-col gap-2">
          <h2 className="font-semibold flex items-center gap-2 mb-1" style={{ color: "var(--text)" }}>
            <Zap size={16} style={{ color: "var(--accent)" }} fill="currentColor" /> Price breakdown
          </h2>
          <Row label={`${VN_DONG_FORMAT(p.pricePerHour)} × ${hours.toFixed(1)} hrs`} value={VN_DONG_FORMAT(subtotal)} />
          <Row label={`Service fee (${SERVICE_FEE_RATE * 100}%)`} value={VN_DONG_FORMAT(fee)} />
          <div className="border-t mt-2 pt-2" style={{ borderColor: "rgba(74,222,128,0.18)" }}>
            <Row label="Total" value={VN_DONG_FORMAT(total)} bold />
          </div>
        </div>
      </div>

      {/* Sticky confirm */}
      <div className="fixed bottom-0 left-0 right-0 px-5 py-4" style={{ background: "rgba(10,15,13,0.95)", backdropFilter: "blur(12px)", borderTop: "1px solid rgba(74,222,128,0.15)" }}>
        <button onClick={handleConfirm} disabled={hours <= 0 || loading} className="w-full py-3.5 rounded-xl font-bold text-base hover:opacity-90 disabled:opacity-50 transition-opacity" style={{ background: "var(--accent)", color: "#0a0f0d" }}>
          {loading ? "Processing payment…" : `Confirm & Pay ${total > 0 ? VN_DONG_FORMAT(total) : ""}`}
        </button>
      </div>
    </div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1">
      <label className="text-xs font-medium" style={{ color: "var(--text-muted)" }}>{label}</label>
      {children}
    </div>
  );
}

function Row({ label, value, bold }: { label: string; value: string; bold?: boolean }) {
  return (
    <div className="flex justify-between items-center text-sm">
      <span style={{ color: bold ? "var(--text)" : "var(--text-muted)", fontWeight: bold ? 600 : 400 }}>{label}</span>
      <span style={{ color: bold ? "var(--accent)" : "var(--text)", fontWeight: bold ? 700 : 400 }}>{value}</span>
    </div>
  );
}
