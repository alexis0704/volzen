"use client";

import { useState } from "react";
import type { ElementType } from "react";
import { BatteryCharging, CalendarDays, Car, Clock, X, UserRound, BarChart3 } from "lucide-react";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell,
} from "recharts";
import { ProviderCard, ProviderShell, StatusBadge } from "@/components/provider/ProviderShell";
import { bookingSlots, bookingHeatmapData, slotDistribution } from "@/lib/provider-data";

export default function BookingsPage() {
  const [slots, setSlots] = useState(bookingSlots);
  const [selected, setSelected] = useState<(typeof bookingSlots)[number] | null>(null);
  const [showBlockModal, setShowBlockModal] = useState(false);
  const [notice, setNotice] = useState("Calendar loaded for Nguyen Hue Home Charger.");

  function updateSelectedState(state: string) {
    if (!selected) return;
    const updated = { ...selected, state };
    setSlots((current) => current.map((slot) => (slot.id === selected.id ? updated : slot)));
    setSelected(updated);
    setNotice(`${selected.title} marked as ${state.toLowerCase()}.`);
  }

  function saveBlockTime() {
    const blockedSlot = {
      id: `blocked-${Date.now()}`,
      time: "14:00",
      end: "15:00",
      title: "Personal block",
      state: "Blocked",
      vehicle: "",
      plate: "",
      battery: "",
      cost: "",
    };
    setSlots((current) => [...current.filter((slot) => slot.time !== "14:00"), blockedSlot]);
    setSelected(blockedSlot);
    setShowBlockModal(false);
    setNotice("Blocked 2 PM - 3 PM for Nguyen Hue Home Charger.");
  }

  const heatmapData = bookingHeatmapData.flatMap(d =>
    d.hours.map(h => ({ day: d.day, hour: h.h, bookings: h.bookings }))
  );

  return (
    <ProviderShell>
      <div className="flex flex-col gap-5">
        {/* Header */}
        <div className="flex min-w-0 flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div className="min-w-0">
            <p className="text-sm font-semibold" style={{ color: "#e2e8f0" }}>Booking Calendar</p>
            <h1 className="mt-1 text-2xl font-bold tracking-tight sm:text-4xl" style={{ color: "var(--text)" }}>Schedule management</h1>
            <p className="mt-2 text-sm leading-6" style={{ color: "var(--text-muted)" }}>Manage reservations, station availability, and demand patterns.</p>
          </div>
          <div className="grid grid-cols-1 gap-2 sm:grid-cols-3 lg:flex">
            <select className="h-11 min-w-0 rounded-lg px-3 text-sm font-semibold outline-none"
              style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}>
              <option>Nguyen Hue Home Charger</option>
              <option>District 3 Fast Bay</option>
            </select>
            <input type="date" className="h-11 min-w-0 rounded-lg px-3 text-sm font-semibold outline-none"
              style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
              defaultValue="2026-06-28" />
            <button type="button" onClick={() => setShowBlockModal(true)}
              className="h-11 rounded-lg px-5 text-sm font-bold transition-all duration-200 hover:opacity-90 active:scale-[0.98]"
              style={{ background: "#e2e8f0", color: "#0a0f0d" }}>
              Block Time
            </button>
          </div>
        </div>

        {/* Notice */}
        <div className="rounded-lg px-4 py-3 text-sm font-semibold"
          style={{ background: "rgba(226,232,240,0.08)", border: "1px solid rgba(226,232,240,0.2)", color: "#e2e8f0" }}>
          {notice}
        </div>

        {/* Main row: timeline + drawer */}
        <div className="grid min-w-0 gap-4 sm:gap-5 xl:grid-cols-[minmax(0,1fr)_360px]">
          <div className="flex min-w-0 flex-col gap-5">
            {/* Daily Timeline */}
            <ProviderCard>
              <div className="mb-4 flex items-center justify-between">
                <div>
                  <h2 className="font-bold" style={{ color: "var(--text)" }}>Daily Timeline</h2>
                  <p className="text-sm" style={{ color: "var(--text-muted)" }}>All times for Nguyen Hue Home Charger</p>
                </div>
                <StatusBadge tone="slate">Live</StatusBadge>
              </div>
              <div className="grid grid-cols-1 gap-3 sm:hidden">
                {["08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00"].map((hour) => {
                  const slot = slots.find((item) => item.time === hour);
                  return <TimelineCard key={hour} hour={hour} slot={slot} onSelect={(item) => { setSelected(item); setNotice(`${item.time} slot selected.`); }} />;
                })}
              </div>
              <div className="hidden sm:block overflow-x-auto pb-2">
                <div className="flex min-w-[780px] gap-3">
                  {["08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00"].map((hour) => {
                    const slot = slots.find((item) => item.time === hour);
                    return <TimelineCard key={hour} hour={hour} slot={slot} horizontal onSelect={(item) => { setSelected(item); setNotice(`${item.time} slot selected.`); }} />;
                  })}
                </div>
              </div>
            </ProviderCard>

            {/* Charts row */}
            <div className="grid min-w-0 gap-5 lg:grid-cols-[minmax(0,1fr)_0.85fr]">
              <ProviderCard>
                <h2 className="font-bold" style={{ color: "var(--text)" }}>Booking Demand by Hour</h2>
                <div className="mt-4 h-48 sm:h-56">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={heatmapData} margin={{ top: 4, right: 4, left: -20, bottom: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="rgba(120,113,108,0.08)" />
                      <XAxis dataKey="hour" tick={{ fill: "var(--text-muted)", fontSize: 9 }} tickLine={false} interval={1} />
                      <YAxis tick={{ fill: "var(--text-muted)", fontSize: 10 }} tickLine={false} allowDecimals={false} />
                      <Tooltip
                        contentStyle={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", borderRadius: 12, color: "var(--text)" }}
                        formatter={(v) => [v, "Bookings"]}
                      />
                      <Bar dataKey="bookings" radius={[4, 4, 0, 0]} fill="rgba(167,139,250,0.06)" stroke="#a78bfa" strokeWidth={2} maxBarSize={20} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </ProviderCard>

              <div className="flex flex-col gap-5">
                <ProviderCard>
                  <h2 className="font-bold" style={{ color: "var(--text)" }}>Slot Distribution</h2>
                  <div className="mt-2 h-36 flex items-center justify-center">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie data={slotDistribution} cx="50%" cy="50%" innerRadius={28} outerRadius={48} dataKey="value" paddingAngle={2}>
                          {slotDistribution.map((entry, i) => (
                            <Cell key={i} fill={entry.fill} stroke={entry.color} strokeWidth={1.5} />
                          ))}
                        </Pie>
                      </PieChart>
                    </ResponsiveContainer>
                    <div className="shrink-0 space-y-1 text-[11px]" style={{ color: "var(--text-muted)" }}>
                      {slotDistribution.map((item) => (
                        <div key={item.name} className="flex items-center gap-1.5">
                          <div className="w-2 h-2 rounded-sm shrink-0" style={{ background: item.color }} />
                          <span>{item.name}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </ProviderCard>

                <ProviderCard>
                  <h2 className="font-bold" style={{ color: "var(--text)" }}>Insights</h2>
                  <div className="mt-4 grid grid-cols-2 gap-3">
                    {[
                      ["Occupancy Rate", "78%"],
                      ["Peak Hours", "5-8 PM"],
                      ["Busiest Day", "Friday"],
                      ["Bookings", "128"],
                    ].map(([label, value]) => (
                      <div key={label} className="rounded-lg p-3"
                        style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
                        <p className="text-xs" style={{ color: "var(--text-muted)" }}>{label}</p>
                        <p className="mt-1 font-bold" style={{ color: "var(--text)" }}>{value}</p>
                      </div>
                    ))}
                  </div>
                </ProviderCard>
              </div>
            </div>
          </div>

          {/* Booking detail drawer */}
          <div className="hidden xl:block">
            {selected ? (
              <div className="sticky top-24">
                <BookingPanel booking={selected} onClose={() => setSelected(null)} onAction={updateSelectedState} />
              </div>
            ) : (
              <ProviderCard className="sticky top-24">
                <p className="text-sm font-semibold" style={{ color: "#e2e8f0" }}>Booking Details</p>
                <h2 className="mt-2 text-xl font-bold" style={{ color: "var(--text)" }}>Select a time slot</h2>
                <p className="mt-2 text-sm leading-6" style={{ color: "var(--text-muted)" }}>Click any booked, charging, or blocked slot to manage the reservation.</p>
              </ProviderCard>
            )}
          </div>
        </div>

        {/* Mobile drawer */}
        {selected && (
          <div className="fixed inset-x-0 bottom-[84px] z-40 px-3 xl:hidden">
            <BookingPanel booking={selected} compact onClose={() => setSelected(null)} onAction={updateSelectedState} />
          </div>
        )}

        {/* Block Time modal */}
        {showBlockModal && (
          <div className="fixed inset-0 z-[60] flex items-end p-3 backdrop-blur-sm sm:items-center sm:justify-center"
            style={{ background: "color-mix(in srgb, #000 40%, transparent)" }}>
            <div className="max-h-[88dvh] w-full max-w-md overflow-y-auto rounded-[28px] p-5 shadow-2xl"
              style={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", backdropFilter: "blur(14px)" }}>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-sm font-semibold" style={{ color: "#e2e8f0" }}>Block Time</p>
                  <h2 className="mt-1 text-xl font-bold" style={{ color: "var(--text)" }}>Create unavailable slot</h2>
                </div>
                <button type="button" onClick={() => setShowBlockModal(false)}
                  className="grid size-9 place-items-center rounded-lg"
                  style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
                  <X size={17} style={{ color: "var(--text-muted)" }} />
                </button>
              </div>
              <div className="mt-5 grid gap-3">
                <select className="h-12 rounded-lg px-3 text-sm font-semibold outline-none"
                  style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}>
                  <option>Nguyen Hue Home Charger</option>
                  <option>District 3 Fast Bay</option>
                </select>
                <input type="date" defaultValue="2026-06-28" className="h-12 rounded-lg px-3 text-sm font-semibold outline-none"
                  style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }} />
                <div className="grid grid-cols-2 gap-3">
                  <input type="time" defaultValue="14:00" className="h-12 rounded-lg px-3 text-sm font-semibold outline-none"
                    style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }} />
                  <input type="time" defaultValue="15:00" className="h-12 rounded-lg px-3 text-sm font-semibold outline-none"
                    style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }} />
                </div>
                <select className="h-12 rounded-lg px-3 text-sm font-semibold outline-none"
                  style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}>
                  <option>Busy</option>
                  <option>Maintenance</option>
                  <option>Personal</option>
                  <option>Other</option>
                </select>
                <button type="button" onClick={saveBlockTime}
                  className="h-12 rounded-lg text-sm font-bold transition-all duration-200 hover:opacity-90 active:scale-[0.98]"
                  style={{ background: "#e2e8f0", color: "#0a0f0d" }}>
                  Save
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </ProviderShell>
  );
}

function BookingPanel({
  booking, compact = false, onClose, onAction,
}: {
  booking: (typeof bookingSlots)[number];
  compact?: boolean;
  onClose: () => void;
  onAction: (state: string) => void;
}) {
  const disabled = booking.state === "Available" || booking.state === "Blocked";

  return (
    <ProviderCard className={compact ? "max-h-[58dvh] overflow-y-auto rounded-[28px] p-4 shadow-2xl" : "sticky top-24"}>
      <div className="mx-auto mb-3 h-1.5 w-12 rounded-lg xl:hidden"
        style={{ background: "color-mix(in srgb, var(--text-muted) 20%, transparent)" }} />
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm font-semibold" style={{ color: "#e2e8f0" }}>Booking Details</p>
          <h2 className="mt-1 text-xl font-bold" style={{ color: "var(--text)" }}>{booking.title}</h2>
        </div>
        <StatusBadge tone={booking.state === "Charging" ? "orange" : booking.state === "Booked" ? "slate" : booking.state === "Completed" ? "gray" : booking.state === "Available" ? "gray" : "gray"}>
          {booking.state}
        </StatusBadge>
        <button type="button" onClick={onClose} className="grid size-8 place-items-center rounded-lg xl:hidden"
          style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
          <X size={15} style={{ color: "var(--text-muted)" }} />
        </button>
      </div>

      <div className="mt-5 grid grid-cols-2 gap-2 text-sm sm:gap-3">
        <DetailBox icon={UserRound} label="Driver" value={disabled ? "No driver" : booking.title} />
        <DetailBox icon={Car} label="Vehicle" value={booking.vehicle || "None"} />
        <DetailBox icon={BarChart3} label="Plate" value={booking.plate || "-"} />
        <DetailBox icon={BatteryCharging} label="Battery" value={booking.battery || "-"} />
        <DetailBox icon={Clock} label="Duration" value={`${booking.time} - ${booking.end}`} />
        <DetailBox icon={CalendarDays} label="Cost" value={booking.cost || "-"} />
      </div>

      <div className="mt-5 grid grid-cols-2 gap-2">
        <button type="button" onClick={() => onAction("Booked")}
          className="h-11 rounded-lg text-sm font-bold transition-all duration-200 hover:opacity-90 disabled:opacity-40 active:scale-[0.98]"
          style={{ background: "#e2e8f0", color: "#0a0f0d" }} disabled={disabled}>Accept</button>
        <button type="button" onClick={() => onAction("Blocked")}
          className="h-11 rounded-lg text-sm font-bold transition-all duration-200 hover:opacity-80 disabled:opacity-40"
          style={{ background: "rgba(239,68,68,0.12)", color: "#ef4444" }} disabled={disabled}>Reject</button>
        <button type="button" onClick={() => onAction("Charging")}
          className="h-11 rounded-lg text-sm font-bold transition-all duration-200 hover:opacity-80 disabled:opacity-40"
          style={{ background: "rgba(251,146,60,0.12)", color: "#fb923c" }} disabled={disabled}>Mark Charging</button>
        <button type="button" onClick={() => onAction("Completed")}
          className="h-11 rounded-lg text-sm font-bold transition-all duration-200 hover:opacity-90 disabled:opacity-40 active:scale-[0.98]"
          style={{ background: "#e2e8f0", color: "#0a0f0d" }} disabled={disabled}>Completed</button>
      </div>
    </ProviderCard>
  );
}

function TimelineCard({
  hour, slot, onSelect, horizontal = false,
}: {
  hour: string;
  slot: (typeof bookingSlots)[number] | undefined;
  onSelect: (slot: (typeof bookingSlots)[number]) => void;
  horizontal?: boolean;
}) {
  const colors = slot
    ? slot.state === "Available" ? { bg: "rgba(148,163,184,0.1)", text: "#94a3b8", border: "rgba(148,163,184,0.25)" }
      : slot.state === "Booked" ? { bg: "rgba(226,232,240,0.1)", text: "#e2e8f0", border: "rgba(226,232,240,0.25)" }
      : slot.state === "Blocked" ? { bg: "rgba(148,163,184,0.1)", text: "#94a3b8", border: "rgba(148,163,184,0.25)" }
      : slot.state === "Charging" ? { bg: "rgba(251,146,60,0.1)", text: "#fb923c", border: "rgba(251,146,60,0.25)" }
      : { bg: "rgba(148,163,184,0.08)", text: "#94a3b8", border: "rgba(148,163,184,0.15)" }
    : { bg: "rgba(148,163,184,0.04)", text: "var(--text-muted)", border: "rgba(148,163,184,0.1)" };

  if (horizontal) {
    return (
      <button type="button" onClick={() => slot && onSelect(slot)} disabled={!slot}
        className="flex w-40 shrink-0 flex-col rounded-xl text-left transition-all duration-200 hover:opacity-90 active:scale-[0.98] disabled:opacity-60"
        style={{ background: colors.bg, border: `1px solid ${colors.border}` }}>
        <div className="flex items-center justify-between px-4 pt-3 pb-2"
          style={{ borderBottom: `1px solid ${colors.border}` }}>
          <span className="text-sm font-bold" style={{ color: colors.text }}>{hour}</span>
          {slot && (
            <span className="text-[10px] font-semibold rounded-md px-1.5 py-0.5"
              style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)", color: colors.text }}>
              {slot.state}
            </span>
          )}
        </div>
        <div className="flex min-w-0 flex-col px-4 py-3">
          <p className="truncate text-sm font-bold" style={{ color: "var(--text)" }}>{slot?.title ?? "Open slot"}</p>
          <p className="mt-0.5 text-xs" style={{ color: "var(--text-muted)" }}>{slot ? `${slot.time} - ${slot.end}` : "No booking"}</p>
          {slot?.vehicle && (
            <p className="mt-1 truncate text-xs" style={{ color: "var(--text-muted)" }}>{slot.vehicle}</p>
          )}
        </div>
      </button>
    );
  }

  return (
    <button type="button" onClick={() => slot && onSelect(slot)} disabled={!slot}
      className="w-full rounded-xl text-left transition-all duration-200 active:scale-[0.99] disabled:opacity-60"
      style={{ background: colors.bg, border: `1px solid ${colors.border}` }}>
      <div className="grid grid-cols-[56px_1fr] min-h-[72px]">
        <div className="flex items-center justify-center text-sm font-bold rounded-l-xl"
          style={{ color: colors.text, background: "color-mix(in srgb, rgba(255,255,255,0.03), transparent)" }}>
          {hour}
        </div>
        <div className="flex min-w-0 flex-col justify-center px-4 py-3">
          <div className="flex min-w-0 items-center justify-between gap-2">
            <p className="truncate font-bold text-sm" style={{ color: "var(--text)" }}>{slot?.title ?? "Open slot"}</p>
            {slot && (
              <span className="shrink-0 rounded-md px-2 py-0.5 text-[11px] font-semibold"
                style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)", color: colors.text }}>
                {slot.state}
              </span>
            )}
          </div>
          <p className="mt-0.5 text-xs" style={{ color: "var(--text-muted)" }}>{slot ? `${slot.time} - ${slot.end}` : "No booking"}</p>
          {slot?.vehicle && <p className="mt-1 truncate text-xs" style={{ color: "var(--text-muted)" }}>{slot.vehicle}</p>}
        </div>
      </div>
    </button>
  );
}

function DetailBox({ icon: Icon, label, value }: { icon: ElementType; label: string; value: string }) {
  return (
    <div className="rounded-lg p-3"
      style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
      <Icon size={15} style={{ color: "#94a3b8" }} />
      <p className="mt-2 text-xs" style={{ color: "var(--text-muted)" }}>{label}</p>
      <p className="mt-0.5 truncate font-bold" style={{ color: "var(--text)" }}>{value}</p>
    </div>
  );
}
