"use client";

import { Bath, Camera, Car, Coffee, Edit3, Home, ParkingSquare, Plus, Star, Trash2, Wifi, X } from "lucide-react";
import { useState } from "react";
import type { ElementType } from "react";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from "recharts";
import { ProviderCard, ProviderShell, StatusBadge } from "@/components/provider/ProviderShell";
import { chargingSpots, stationPerformance } from "@/lib/provider-data";

const amenities = [
  { label: "Covered Area", icon: Home },
  { label: "Security Camera", icon: Camera },
  { label: "Waiting Area", icon: Coffee },
  { label: "Wi-Fi", icon: Wifi },
  { label: "Restroom", icon: Bath },
  { label: "Parking", icon: ParkingSquare },
];

type Spot = (typeof chargingSpots)[number];

export default function SpotsPage() {
  const [spots, setSpots] = useState<Spot[]>(chargingSpots);
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [editingIndex, setEditingIndex] = useState<number | null>(null);
  const [chargerType, setChargerType] = useState<"Fast Charging" | "Slow Charging">("Fast Charging");
  const [isActive, setIsActive] = useState(true);
  const [uploaded, setUploaded] = useState(false);
  const [notice, setNotice] = useState("Tap a station card to view details.");

  const selectedSpot = selectedIndex === null ? null : spots[selectedIndex];
  const editingSpot = editingIndex === null ? null : spots[editingIndex];

  const totalRevenue = spots.reduce((sum, s) => sum + s.sessions * 42000, 0);
  const activeCount = spots.filter((s) => s.status === "Active").length;
  const totalSessions = spots.reduce((sum, s) => sum + s.sessions, 0);

  function openEdit(index: number) {
    setEditingIndex(index);
    setSelectedIndex(null);
    setIsActive(spots[index].status === "Active");
    setUploaded(false);
    setNotice(`Editing ${spots[index].name}.`);
  }

  function addNewSpot() {
    const draft: Spot = {
      name: "New Community Charger",
      address: "Enter station address",
      status: "Inactive",
      rating: "New",
      sessions: 0,
      price: "₫40,000/hr",
      slots: "0/1",
      image: "https://picsum.photos/seed/volzen-station-new/640/420",
    };
    setSpots((current) => [draft, ...current]);
    setEditingIndex(0);
    setSelectedIndex(null);
    setIsActive(false);
    setUploaded(false);
    setNotice("New station draft created.");
  }

  function deleteSpot(index: number) {
    const removed = spots[index];
    setSpots((current) => current.filter((_, itemIndex) => itemIndex !== index));
    setSelectedIndex(null);
    setEditingIndex(null);
    setNotice(`${removed.name} deleted.`);
  }

  function saveStation() {
    if (editingIndex === null || !editingSpot) return;
    setSpots((current) =>
      current.map((spot, index) =>
        index === editingIndex
          ? { ...spot, status: isActive ? "Active" : "Inactive", price: chargerType === "Fast Charging" ? "₫55,000/hr" : "₫35,000/hr", slots: chargerType === "Fast Charging" ? "1/2" : "2/3" }
          : spot,
      ),
    );
    setNotice(`${editingSpot.name} saved.`);
    setEditingIndex(null);
  }

  return (
    <ProviderShell>
      <div className="flex flex-col gap-5">
        {/* Header */}
        <div className="flex min-w-0 flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div className="min-w-0">
            <p className="text-sm font-semibold" style={{ color: "#e2e8f0" }}>My Charging Spots</p>
            <h1 className="mt-1 text-2xl font-bold tracking-tight sm:text-4xl" style={{ color: "var(--text)" }}>Station management</h1>
            <p className="mt-2 max-w-2xl text-sm leading-6" style={{ color: "var(--text-muted)" }}>Create, edit, activate, and monitor every charger listed on Volzen.</p>
          </div>
          <button type="button" onClick={addNewSpot}
            className="flex h-12 w-full items-center justify-center gap-2 rounded-lg px-5 text-sm font-bold transition-all duration-200 hover:opacity-90 active:scale-[0.98] sm:w-auto"
            style={{ background: "#e2e8f0", color: "#0a0f0d" }}>
            <Plus size={18} /> Add New Charging Spot
          </button>
        </div>

        {/* Notice */}
        <div className="rounded-lg px-4 py-3 text-sm font-semibold"
          style={{ background: "rgba(226,232,240,0.08)", border: "1px solid rgba(226,232,240,0.2)", color: "#e2e8f0" }}>
          {notice}
        </div>

        {/* Quick stats row */}
        <div className="grid grid-cols-3 gap-3 sm:grid-cols-3">
          <ProviderCard>
            <p className="text-xs" style={{ color: "var(--text-muted)" }}>Total Stations</p>
            <p className="mt-2 text-2xl font-bold" style={{ color: "var(--text)" }}>{spots.length}</p>
          </ProviderCard>
          <ProviderCard>
            <p className="text-xs" style={{ color: "var(--text-muted)" }}>Active</p>
            <p className="mt-2 text-2xl font-bold" style={{ color: "#e2e8f0" }}>{activeCount}</p>
          </ProviderCard>
          <ProviderCard>
            <p className="text-xs" style={{ color: "var(--text-muted)" }}>Total Sessions</p>
            <p className="mt-2 text-2xl font-bold" style={{ color: "var(--text)" }}>{totalSessions}</p>
          </ProviderCard>
        </div>

        {/* Station cards */}
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {spots.map((spot, index) => (
            <ProviderCard key={`${spot.name}-${index}`} className="overflow-hidden p-0">
              <div className="h-1.5 w-full" style={{ background: spot.status === "Active" ? "#e2e8f0" : "#94a3b8" }} />
              <button type="button" onClick={() => setSelectedIndex(index)} className="block w-full text-left">
                <img src={spot.image} alt={spot.name} className="h-40 w-full object-cover" />
                <div className="p-4">
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <h2 className="truncate font-bold" style={{ color: "var(--text)" }}>{spot.name}</h2>
                      <p className="mt-1 line-clamp-2 text-sm leading-5" style={{ color: "var(--text-muted)" }}>{spot.address}</p>
                    </div>
                    <StatusBadge tone={spot.status === "Active" ? "slate" : "gray"}>{spot.status}</StatusBadge>
                  </div>
                  <div className="mt-4 grid grid-cols-2 gap-2">
                    <Metric icon={Star} label="Rating" value={spot.rating} />
                    <Metric icon={Car} label="Sessions" value={String(spot.sessions)} />
                    <Metric icon={ParkingSquare} label="Price" value={spot.price} />
                    <Metric icon={Home} label="Slots" value={spot.slots} />
                  </div>
                </div>
              </button>
              <div className="grid grid-cols-2 gap-2 p-3"
                style={{ borderTop: "1px solid var(--glass-border)", background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
                <Action icon={Edit3} label="Edit" onClick={() => openEdit(index)} />
                <Action icon={Trash2} label="Delete" danger onClick={() => deleteSpot(index)} />
              </div>
            </ProviderCard>
          ))}
        </div>

        {/* Station Performance Chart */}
        <ProviderCard>
          <h2 className="font-bold" style={{ color: "var(--text)" }}>Station Performance</h2>
          <div className="mt-4 h-48 sm:h-56">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={stationPerformance} margin={{ top: 4, right: 20, left: -20, bottom: 0 }} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(120,113,108,0.08)" />
                <XAxis type="number" tick={{ fill: "var(--text-muted)", fontSize: 10 }} tickLine={false} tickFormatter={v => `${(v / 1_000_000).toFixed(0)}M`} />
                <YAxis type="category" dataKey="name" tick={{ fill: "var(--text-muted)", fontSize: 11 }} tickLine={false} width={140} />
                <Tooltip
                  contentStyle={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", borderRadius: 12, color: "var(--text)" }}
                  formatter={(v) => [`₫${(Number(v) / 1_000_000).toFixed(1)}M`, "Revenue"]}
                />
                <Bar dataKey="revenue" radius={[0, 8, 8, 0]} fill="rgba(45,212,191,0.06)" stroke="#2dd4bf" strokeWidth={2} maxBarSize={28} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </ProviderCard>

        {/* Spot detail modal */}
        {selectedSpot && (
          <SpotModal title="Charging Spot Details" onClose={() => setSelectedIndex(null)}>
            <img src={selectedSpot.image} alt={selectedSpot.name} className="h-44 w-full rounded-xl object-cover" />
            <div className="mt-4 flex items-start justify-between gap-3">
              <div>
                <h2 className="text-xl font-bold" style={{ color: "var(--text)" }}>{selectedSpot.name}</h2>
                <p className="mt-1 text-sm leading-6" style={{ color: "var(--text-muted)" }}>{selectedSpot.address}</p>
              </div>
              <StatusBadge tone={selectedSpot.status === "Active" ? "slate" : "gray"}>{selectedSpot.status}</StatusBadge>
            </div>
            <div className="mt-5 grid grid-cols-2 gap-3">
              <Metric icon={Star} label="Rating" value={selectedSpot.rating} />
              <Metric icon={Car} label="Sessions" value={String(selectedSpot.sessions)} />
              <Metric icon={ParkingSquare} label="Price" value={selectedSpot.price} />
              <Metric icon={Home} label="Slots" value={selectedSpot.slots} />
            </div>
            <div className="mt-5 grid grid-cols-2 gap-2">
              {amenities.map((item) => {
                const Icon = item.icon;
                return (
                  <div key={item.label} className="flex min-h-12 items-center gap-2 rounded-lg px-3 text-sm font-semibold"
                    style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
                    <Icon size={16} style={{ color: "#94a3b8" }} />
                    {item.label}
                  </div>
                );
              })}
            </div>
          </SpotModal>
        )}

        {/* Edit modal */}
        {editingSpot && (
          <SpotModal title="Edit Charging Spot" onClose={() => setEditingIndex(null)}>
            <div className="space-y-5">
              <div>
                <p className="mb-3 text-sm font-bold" style={{ color: "var(--text)" }}>Basic Information</p>
                <div className="grid gap-3">
                  <input className="h-12 rounded-lg px-4 text-sm outline-none transition-all duration-200 focus:ring-1"
                    style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                    placeholder="Station Name" defaultValue={editingSpot.name} />
                  <input className="h-12 rounded-lg px-4 text-sm outline-none transition-all duration-200 focus:ring-1"
                    style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                    placeholder="Address" defaultValue={editingSpot.address} />
                  <textarea className="min-h-24 rounded-lg px-4 py-3 text-sm outline-none transition-all duration-200 focus:ring-1"
                    style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                    placeholder="Description" defaultValue="Covered home charger with secure parking and waiting area." />
                  <button type="button" onClick={() => setUploaded(true)}
                    className="h-12 rounded-lg text-sm font-bold transition-all duration-200 hover:opacity-80"
                    style={{ border: "2px dashed rgba(226,232,240,0.4)", background: "rgba(226,232,240,0.08)", color: "#e2e8f0" }}>
                    {uploaded ? "Images Uploaded" : "Upload Images"}
                  </button>
                </div>
              </div>
              <div>
                <p className="mb-3 text-sm font-bold" style={{ color: "var(--text)" }}>Charging Information</p>
                <div className="grid grid-cols-1 gap-3 min-[380px]:grid-cols-2">
                  <button type="button" onClick={() => setChargerType("Fast Charging")}
                    className={`h-12 rounded-lg text-sm font-bold transition-all duration-200 ${chargerType === "Fast Charging" ? "" : "opacity-60"}`}
                    style={{ background: chargerType === "Fast Charging" ? "#e2e8f0" : "var(--glass-bg)", color: chargerType === "Fast Charging" ? "#0a0f0d" : "var(--text-muted)" }}>
                    Fast Charging
                  </button>
                  <button type="button" onClick={() => setChargerType("Slow Charging")}
                    className={`h-12 rounded-lg text-sm font-bold transition-all duration-200 ${chargerType === "Slow Charging" ? "" : "opacity-60"}`}
                    style={{ background: chargerType === "Slow Charging" ? "#e2e8f0" : "var(--glass-bg)", color: chargerType === "Slow Charging" ? "#0a0f0d" : "var(--text-muted)" }}>
                    Slow Charging
                  </button>
                  <input className="h-12 rounded-lg px-4 text-sm outline-none transition-all duration-200"
                    style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                    type="number" placeholder="Slots" defaultValue={3} />
                  <input className="h-12 rounded-lg px-4 text-sm outline-none transition-all duration-200"
                    style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                    placeholder="Price / Hour" defaultValue="42000" />
                </div>
              </div>
              <div>
                <p className="mb-3 text-sm font-bold" style={{ color: "var(--text)" }}>Amenities</p>
                <div className="grid grid-cols-1 gap-2 min-[380px]:grid-cols-2">
                  {amenities.map((item) => {
                    const Icon = item.icon;
                    return (
                      <label key={item.label} className="flex min-h-12 items-center gap-2 rounded-lg px-3 text-sm font-semibold"
                        style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
                        <input type="checkbox" defaultChecked className="accent-slate-300" />
                        <Icon size={16} style={{ color: "#94a3b8" }} />
                        {item.label}
                      </label>
                    );
                  })}
                </div>
              </div>
              <div className="flex items-center justify-between gap-3 rounded-xl p-4"
                style={{ background: "rgba(226,232,240,0.08)" }}>
                <div>
                  <p className="font-bold" style={{ color: "var(--text)" }}>Station Status</p>
                  <p className="text-sm" style={{ color: "var(--text-muted)" }}>Active stations are bookable by drivers.</p>
                </div>
                <button type="button" onClick={() => setIsActive((current) => !current)}
                  className={`h-8 w-14 rounded-lg p-1 transition ${isActive ? "" : "opacity-40"}`}
                  style={{ background: isActive ? "#e2e8f0" : "var(--glass-bg)" }}
                  aria-pressed={isActive}>
                  <span className={`block size-6 rounded-md shadow-sm transition ${isActive ? "translate-x-6" : "translate-x-0"}`}
                    style={{ background: "#0a0f0d" }} />
                </button>
              </div>
              <button type="button" onClick={saveStation}
                className="h-12 w-full rounded-lg text-sm font-bold transition-all duration-200 hover:opacity-90 active:scale-[0.98]"
                style={{ background: "#e2e8f0", color: "#0a0f0d" }}>
                Save Station
              </button>
            </div>
          </SpotModal>
        )}
      </div>
    </ProviderShell>
  );
}

function SpotModal({ title, children, onClose }: { title: string; children: React.ReactNode; onClose: () => void }) {
  return (
    <div className="fixed inset-0 z-[60] flex items-end p-3 backdrop-blur-sm sm:items-center sm:justify-center"
      style={{ background: "color-mix(in srgb, #000 40%, transparent)" }}>
      <div className="max-h-[88dvh] w-full max-w-2xl overflow-y-auto rounded-[28px] p-5 shadow-2xl"
        style={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", backdropFilter: "blur(14px)" }}>
        <div className="mb-5 flex items-start justify-between gap-3">
          <div>
            <p className="text-sm font-semibold" style={{ color: "#e2e8f0" }}>Charging Spot</p>
            <h2 className="mt-1 text-xl font-bold" style={{ color: "var(--text)" }}>{title}</h2>
          </div>
          <button type="button" onClick={onClose}
            className="grid size-9 place-items-center rounded-lg transition-opacity hover:opacity-80"
            style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
            <X size={17} style={{ color: "var(--text-muted)" }} />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}

function Metric({ icon: Icon, label, value }: { icon: ElementType; label: string; value: string }) {
  return (
    <div className="rounded-lg p-3" style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
      <Icon size={15} style={{ color: "#94a3b8" }} />
      <p className="mt-2 text-[11px]" style={{ color: "var(--text-muted)" }}>{label}</p>
      <p className="mt-1 truncate text-xs font-bold sm:text-sm" style={{ color: "var(--text)" }}>{value}</p>
    </div>
  );
}

function Action({ icon: Icon, label, danger = false, onClick }: { icon: ElementType; label: string; danger?: boolean; onClick: () => void }) {
  return (
    <button type="button" onClick={onClick}
      className="flex h-11 items-center justify-center gap-1.5 rounded-lg text-xs font-bold transition-all duration-200 active:scale-[0.98]"
      style={{
        background: danger ? "rgba(239,68,68,0.1)" : "var(--glass-bg)",
        color: danger ? "#ef4444" : "var(--text-muted)",
        border: danger ? "none" : "1px solid var(--glass-border)",
      }}>
      <Icon size={14} />
      {label}
    </button>
  );
}
