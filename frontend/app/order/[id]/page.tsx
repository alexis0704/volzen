"use client";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState, useMemo } from "react";
import { VN_DONG_FORMAT } from "@/lib/domain";
import type { Provider, Vehicle } from "@/lib/domain";
import { ArrowLeft, Star, MapPin, Car, Clock, Zap } from "lucide-react";
import { createOrder, getProvider, getProviderAvailability, listVehicles } from "@/lib/api";
import type { BookedSlot } from "@/lib/api";

const SERVICE_FEE_RATE = 0.1;

function parseMinutes(timeStr: string) {
  const [h, m] = timeStr.split(":").map(Number);
  return h * 60 + m;
}

export default function OrderPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [provider, setProvider] = useState<Provider | null>(null);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [providerLoading, setProviderLoading] = useState(true);
  const [error, setError] = useState("");
  const [bookedSlots, setBookedSlots] = useState<BookedSlot[] | null>(null);

  const today = new Date().toISOString().split("T")[0];
  const [date, setDate] = useState(today);
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("11:00");
  const [vehicleId, setVehicleId] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;

    getProvider(id)
      .then((data) => {
        if (!ignore) setProvider(data);
      })
      .catch(() => {
        if (!ignore) setProvider(null);
      })
      .finally(() => {
        if (!ignore) setProviderLoading(false);
      });

    listVehicles()
      .then((data) => {
        if (ignore) return;
        setVehicles(data);
        setVehicleId(data[0]?.id ?? "");
      })
      .catch(() => {
        if (ignore) return;
        setVehicles([]);
        setVehicleId("");
      });

    return () => {
      ignore = true;
    };
  }, [id]);

  useEffect(() => {
    let ignore = false;

    getProviderAvailability(id, date)
      .then((slots) => {
        if (!ignore) setBookedSlots(slots);
      })
      .catch(() => {
        if (!ignore) setBookedSlots([]);
      });

    return () => {
      ignore = true;
    };
  }, [id, date]);

  const hours = useMemo(() => {
    const diff = parseMinutes(endTime) - parseMinutes(startTime);
    return diff > 0 ? diff / 60 : 0;
  }, [startTime, endTime]);

  const p = provider;
  const subtotal = p ? hours * p.pricePerHour : 0;
  const fee = subtotal * SERVICE_FEE_RATE;
  const total = subtotal + fee;
  const compatibleVehicles = useMemo(
    () => (provider ? vehicles.filter((vehicle) => provider.connectors.includes(vehicle.connector)) : vehicles),
    [provider, vehicles],
  );
  const effectiveVehicleId = compatibleVehicles.some((vehicle) => vehicle.id === vehicleId)
    ? vehicleId
    : compatibleVehicles[0]?.id ?? "";
  const selectedVehicle = useMemo(
    () => vehicles.find((vehicle) => vehicle.id === effectiveVehicleId),
    [effectiveVehicleId, vehicles],
  );
  const selectedVehicleCompatible = Boolean(
    provider && selectedVehicle && provider.connectors.includes(selectedVehicle.connector),
  );
  const overlappingSlot = useMemo(
    () => bookedSlots?.find((slot) => overlapsSelectedTime(startTime, endTime, slot)),
    [bookedSlots, startTime, endTime],
  );
  const availabilityLoading = bookedSlots === null;
  const selectedSlotAvailable = hours > 0 && !overlappingSlot;

  if (providerLoading) return <div className="min-h-dvh flex items-center justify-center" style={{ background: "var(--bg)", color: "var(--text)" }}>Loading order…</div>;

  if (!p) return <div className="min-h-dvh flex items-center justify-center" style={{ background: "var(--bg)", color: "var(--text)" }}>Not found.</div>;

  async function handleConfirm() {
    if (!p) return;
    if (!selectedVehicleCompatible) {
      setError("Your selected vehicle is not compatible with this station.");
      return;
    }
    if (!selectedSlotAvailable) {
      setError("This time slot is already booked. Please choose another time.");
      return;
    }
    setLoading(true);
    setError("");

    try {
      const order = await createOrder({
        providerId: p.id,
        vehicleId: effectiveVehicleId,
        startTime: toOffsetDateTime(date, startTime),
        endTime: toOffsetDateTime(date, endTime),
      });
      router.push(`/route/${order.id}`);
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : "Could not create order.");
      setLoading(false);
    }
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
              <input type="date" value={date} min={today} onChange={(e) => {
                setBookedSlots(null);
                setDate(e.target.value);
              }} className="w-full px-3 py-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-green-400" style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)", colorScheme: "dark" }} />
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
          {availabilityLoading && <p className="text-xs" style={{ color: "var(--text-muted)" }}>Checking availability…</p>}
          {overlappingSlot && (
            <p className="text-xs leading-relaxed" style={{ color: "#fca5a5" }}>
              Already booked from {formatSlotTime(overlappingSlot.startTime)} to {formatSlotTime(overlappingSlot.endTime)}.
            </p>
          )}
          {bookedSlots && bookedSlots.length > 0 && (
            <div className="flex flex-wrap gap-1.5">
              {bookedSlots.map((slot) => (
                <span key={`${slot.startTime}-${slot.endTime}`} className="rounded-full px-2 py-1 text-[10px]" style={{ background: "rgba(248,113,113,0.12)", color: "#fca5a5", border: "1px solid rgba(248,113,113,0.2)" }}>
                  {formatSlotTime(slot.startTime)}-{formatSlotTime(slot.endTime)}
                </span>
              ))}
            </div>
          )}
        </div>

        {/* Vehicle */}
        <div className="glass p-4 flex flex-col gap-3">
          <h2 className="font-semibold flex items-center gap-2" style={{ color: "var(--text)" }}>
            <Car size={16} style={{ color: "var(--accent)" }} /> Vehicle
          </h2>
          {vehicles.length === 0 && (
            <p className="text-xs leading-relaxed" style={{ color: "#fca5a5" }}>
              No vehicles found. Add a vehicle before booking.
            </p>
          )}
          {vehicles.map((v) => {
            const compatible = p.connectors.includes(v.connector);
            return (
              <button
                key={v.id}
                onClick={() => compatible && setVehicleId(v.id)}
                disabled={!compatible}
                className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition-all disabled:opacity-55"
                style={{
                  background: effectiveVehicleId === v.id && compatible ? "rgba(74,222,128,0.12)" : "rgba(255,255,255,0.05)",
                  border: `1px solid ${effectiveVehicleId === v.id && compatible ? "rgba(74,222,128,0.45)" : "rgba(74,222,128,0.15)"}`,
                  color: "var(--text)",
                }}
              >
                <Car size={16} style={{ color: effectiveVehicleId === v.id && compatible ? "var(--accent)" : "var(--text-muted)" }} />
                <span className="min-w-0 flex-1 text-left">
                  {v.brand} {v.model} · {v.year} · {v.connector}
                </span>
                {!compatible && (
                  <span className="shrink-0 rounded-full px-2 py-0.5 text-[10px]" style={{ background: "rgba(248,113,113,0.12)", color: "#fca5a5" }}>
                    Not supported
                  </span>
                )}
              </button>
            );
          })}
          {compatibleVehicles.length === 0 && (
            <p className="text-xs leading-relaxed" style={{ color: "#fca5a5" }}>
              This station supports {p.connectors.join(", ")}. Add a compatible vehicle before booking.
            </p>
          )}
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

        {error && (
          <div className="glass p-4 text-sm" style={{ color: "#fca5a5", borderColor: "rgba(248,113,113,0.35)" }}>
            {error}
          </div>
        )}
      </div>

      {/* Sticky confirm */}
      <div className="fixed bottom-0 left-0 right-0 px-5 py-4" style={{ background: "rgba(10,15,13,0.95)", backdropFilter: "blur(12px)", borderTop: "1px solid rgba(74,222,128,0.15)" }}>
        <button onClick={handleConfirm} disabled={!selectedSlotAvailable || availabilityLoading || loading || !effectiveVehicleId || !selectedVehicleCompatible} className="w-full py-3.5 rounded-xl font-bold text-base hover:opacity-90 disabled:opacity-50 transition-opacity" style={{ background: "var(--accent)", color: "#0a0f0d" }}>
          {loading ? "Processing payment…" : `Confirm & Pay ${total > 0 ? VN_DONG_FORMAT(total) : ""}`}
        </button>
      </div>
    </div>
  );
}

function toOffsetDateTime(date: string, time: string) {
  return `${date}T${time}:00+07:00`;
}

function overlapsSelectedTime(startTime: string, endTime: string, slot: BookedSlot) {
  const selectedStart = parseMinutes(startTime);
  const selectedEnd = parseMinutes(endTime);
  const bookedStart = parseIsoTimeMinutes(slot.startTime);
  const bookedEnd = parseIsoTimeMinutes(slot.endTime);

  return selectedStart < bookedEnd && selectedEnd > bookedStart;
}

function parseIsoTimeMinutes(value: string) {
  const time = value.split("T")[1]?.slice(0, 5) ?? "00:00";
  return parseMinutes(time);
}

function formatSlotTime(value: string) {
  return value.split("T")[1]?.slice(0, 5) ?? value;
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
