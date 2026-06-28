"use client";

import { Bath, Camera, Car, Coffee, Edit3, Home, LocateFixed, MapPin, ParkingSquare, Plus, Star, Trash2, Wifi, X } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import type { ElementType } from "react";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from "recharts";
import { ProviderCard, ProviderShell, StatusBadge } from "@/components/provider/ProviderShell";
import { createMyStation, deleteMyStation, fetchSpotsData, updateMyStation } from "@/lib/host-api";
import type { ChargingSpot, StationPerformanceEntry } from "@/lib/host-api";
import { getCurrentLocation } from "@/lib/location";
import "leaflet/dist/leaflet.css";

const amenities = [
  { label: "Covered Area", apiValue: "Covered", icon: Home },
  { label: "Security Camera", apiValue: "Security", icon: Camera },
  { label: "Waiting Area", apiValue: "Air Conditioning", icon: Coffee },
  { label: "Wi-Fi", apiValue: "WiFi", icon: Wifi },
  { label: "Restroom", apiValue: "Restroom", icon: Bath },
  { label: "Parking", apiValue: "Parking", icon: ParkingSquare },
];

type Spot = ChargingSpot;
type SpotForm = {
  name: string;
  address: string;
  description: string;
  image: string;
  lat: string;
  lng: string;
  pricePerHour: string;
  slots: string;
  chargerType: "Fast Charging" | "Slow Charging";
  amenities: string[];
  isActive: boolean;
};

function parsePrice(price: string) {
  const value = Number(price.replace(/[^\d]/g, ""));
  return Number.isFinite(value) && value > 0 ? value : 40000;
}

function parseSlots(slots: string) {
  const value = Number(slots.split("/")[0]);
  return Number.isFinite(value) && value > 0 ? value : 1;
}

function buildSpotForm(spot: Spot): SpotForm {
  const price = parsePrice(spot.price);
  return {
    name: spot.name,
    address: spot.address,
    description: "Covered home charger with secure parking and waiting area.",
    image: spot.image,
    lat: String(spot.lat ?? 10.878072),
    lng: String(spot.lng ?? 106.801247),
    pricePerHour: String(price),
    slots: String(parseSlots(spot.slots)),
    chargerType: price >= 30000 ? "Fast Charging" : "Slow Charging",
    amenities: amenities.map((item) => item.apiValue),
    isActive: spot.status === "Active",
  };
}

function stationPayloadFromForm(spotForm: SpotForm, image: string, pricePerHour: number, connectorTypes: string[]) {
  return {
    name: spotForm.name.trim() || "Community Charger",
    address: spotForm.address.trim() || "Station address",
    lat: Number(spotForm.lat) || 10.878072,
    lng: Number(spotForm.lng) || 106.801247,
    pricePerHour,
    connectorTypes,
    amenities: spotForm.amenities,
    photoUrls: [image],
    isAvailable: spotForm.isActive,
  };
}

function stationResponseToSpot(saved: Awaited<ReturnType<typeof createMyStation>>, fallback: Spot): Spot {
  if (!saved) return fallback;
  return {
    ...fallback,
    id: saved.id,
    name: saved.name,
    address: saved.address,
    lat: Number(saved.lat),
    lng: Number(saved.lng),
    status: saved.isAvailable ? "Active" : "Inactive",
    price: formatVnd(saved.pricePerHour),
    slots: `${saved.connectorTypes.length}/4`,
    image: saved.photoUrls[0] ?? fallback.image,
    isPersisted: true,
  };
}

function formatVnd(value: number) {
  return `₫${value.toLocaleString("vi-VN")}/hr`;
}

function createDraftSpot(): Spot {
  return {
    name: "New Community Charger",
    address: "Enter station address",
    status: "Inactive",
    lat: 10.878072,
    lng: 106.801247,
    rating: "New",
    sessions: 0,
    price: "₫40,000/hr",
    slots: "0/1",
    image: "/stations/pvd-p1-2.svg",
  };
}

export default function SpotsPage() {
  const [spots, setSpots] = useState<Spot[]>([]);
  const [stationPerformance, setStationPerformance] = useState<StationPerformanceEntry[]>([]);
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [editingIndex, setEditingIndex] = useState<number | null>(null);
  const [spotForm, setSpotForm] = useState<SpotForm | null>(null);
  const [uploaded, setUploaded] = useState(false);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState("Tap a station card to view details.");

  useEffect(() => {
    fetchSpotsData().then((data) => {
      setSpots(data.spots);
      setStationPerformance(data.stationPerformance);
    });
  }, []);

  const selectedSpot = selectedIndex === null ? null : spots[selectedIndex];
  const isAddingSpot = editingIndex === -1;
  const editingSpot = editingIndex === null ? null : isAddingSpot ? createDraftSpot() : spots[editingIndex];

  const totalRevenue = spots.reduce((sum, s) => sum + s.sessions * 42000, 0);
  const activeCount = spots.filter((s) => s.status === "Active").length;
  const totalSessions = spots.reduce((sum, s) => sum + s.sessions, 0);

  function openEdit(index: number) {
    setEditingIndex(index);
    setSelectedIndex(null);
    setSpotForm(buildSpotForm(spots[index]));
    setUploaded(false);
    setNotice(`Editing ${spots[index].name}.`);
  }

  function addNewSpot() {
    const draft = createDraftSpot();
    setEditingIndex(-1);
    setSelectedIndex(null);
    setSpotForm(buildSpotForm(draft));
    setUploaded(false);
    setNotice("New station draft ready.");
  }

  async function useCurrentStationLocation() {
    try {
      const location = await getCurrentLocation();
      setSpotForm((current) => current && {
        ...current,
        lat: location.lat.toFixed(6),
        lng: location.lng.toFixed(6),
      });
      setNotice("Current GPS location selected.");
    } catch {
      setNotice("Could not access GPS. You can click the map or enter coordinates manually.");
    }
  }

  async function saveStation() {
    if (editingIndex === null || !editingSpot || !spotForm) return;
    setSaving(true);
    const pricePerHour = Math.max(1, Number(spotForm.pricePerHour) || 40000);
    const slotCount = Math.max(1, Number(spotForm.slots) || 1);
    const connectorTypes = spotForm.chargerType === "Fast Charging" ? ["CCS", "Type 2"] : ["Type 2"];
    const image = spotForm.image.trim() || "/stations/pvd-p1-1.svg";
    const updatedSpot: Spot = {
      ...editingSpot,
      name: spotForm.name.trim() || "Community Charger",
      address: spotForm.address.trim() || "Station address",
      lat: Number(spotForm.lat) || 10.878072,
      lng: Number(spotForm.lng) || 106.801247,
      status: spotForm.isActive ? "Active" : "Inactive",
      price: formatVnd(pricePerHour),
      slots: `${Math.min(slotCount, 4)}/4`,
      image,
    };

    const payload = stationPayloadFromForm(spotForm, image, pricePerHour, connectorTypes);
    const saved = editingSpot.id
      ? await updateMyStation(editingSpot.id, payload)
      : await createMyStation(payload);

    if (saved) {
      const persistedSpot = stationResponseToSpot(saved, updatedSpot);
      setSpots((current) =>
        isAddingSpot
          ? [persistedSpot, ...current]
          : current.map((spot, index) => (index === editingIndex ? persistedSpot : spot)),
      );
      setNotice(`${saved.name} saved to API.`);
    } else {
      setNotice(`${updatedSpot.name} could not be saved. Please try again.`);
    }
    setSaving(false);
    setEditingIndex(null);
    setSpotForm(null);
  }

  async function deleteSpot(index: number) {
    const removed = spots[index];
    if (removed.id) {
      const deleted = await deleteMyStation(removed.id);
      if (!deleted) {
        setNotice(`${removed.name} could not be deleted. Please try again.`);
        return;
      }
    }
    setSpots((current) => current.filter((_, itemIndex) => itemIndex !== index));
    setSelectedIndex(null);
    setEditingIndex(null);
    setNotice(`${removed.name} deleted.`);
  }

  return (
    <ProviderShell>
      <div className="flex flex-col gap-5">
        {/* Header */}
        <div className="flex min-w-0 flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div className="min-w-0">
            <p className="text-sm font-semibold" style={{ color: "var(--text-muted)" }}>My Charging Spots</p>
            <h1 className="mt-1 text-2xl font-bold tracking-tight sm:text-4xl" style={{ color: "var(--text)" }}>Station management</h1>
            <p className="mt-2 max-w-2xl text-sm leading-6" style={{ color: "var(--text-muted)" }}>Create, edit, activate, and monitor every charger listed on Volzen.</p>
          </div>
          <button type="button" onClick={addNewSpot}
            className="flex h-12 w-full items-center justify-center gap-2 rounded-lg px-5 text-sm font-bold transition-all duration-200 hover:opacity-90 active:scale-[0.98] sm:w-auto"
            style={{ background: "var(--accent)", color: "var(--accent-fg)" }}>
            <Plus size={18} /> Add New Charging Spot
          </button>
        </div>

        {/* Notice */}
        <div className="rounded-lg px-4 py-3 text-sm font-semibold"
          style={{ background: "color-mix(in srgb, var(--text-muted) 8%, transparent)", border: "1px solid color-mix(in srgb, var(--text-muted) 20%, transparent)", color: "var(--text-muted)" }}>
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
            <p className="mt-2 text-2xl font-bold" style={{ color: "var(--text)" }}>{activeCount}</p>
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
              <div className="h-1.5 w-full" style={{ background: spot.status === "Active" ? "var(--accent)" : "var(--text-muted)" }} />
              <button type="button" onClick={() => setSelectedIndex(index)} className="block w-full text-left">
                <img src={spot.image} alt={spot.name} className="h-40 w-full object-cover" onError={(e) => { e.currentTarget.src = "/stations/pvd-p1-1.svg"; e.currentTarget.style.objectFit = "cover"; }} />
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
            <img src={selectedSpot.image} alt={selectedSpot.name} className="h-44 w-full rounded-xl object-cover" onError={(e) => { e.currentTarget.src = "/stations/pvd-p1-1.svg"; e.currentTarget.style.objectFit = "cover"; }} />
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
                    <Icon size={16} style={{ color: "var(--text-muted)" }} />
                    {item.label}
                  </div>
                );
              })}
            </div>
          </SpotModal>
        )}

        {/* Edit modal */}
        {editingSpot && spotForm && (
          <SpotModal title={editingSpot.rating === "New" ? "Add Charging Spot" : "Edit Charging Spot"} onClose={() => { setEditingIndex(null); setSpotForm(null); }}>
            <div className="space-y-5">
              <div>
                <p className="mb-3 text-sm font-bold" style={{ color: "var(--text)" }}>Basic Information</p>
                <div className="grid gap-3">
                  <input className="h-12 rounded-lg px-4 text-sm outline-none transition-all duration-200 focus:ring-1"
                    style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                    placeholder="Station Name" value={spotForm.name}
                    onChange={(e) => setSpotForm((current) => current && { ...current, name: e.target.value })} />
                  <input className="h-12 rounded-lg px-4 text-sm outline-none transition-all duration-200 focus:ring-1"
                    style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                    placeholder="Address" value={spotForm.address}
                    onChange={(e) => setSpotForm((current) => current && { ...current, address: e.target.value })} />
                  <textarea className="min-h-24 rounded-lg px-4 py-3 text-sm outline-none transition-all duration-200 focus:ring-1"
                    style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                    placeholder="Description" value={spotForm.description}
                    onChange={(e) => setSpotForm((current) => current && { ...current, description: e.target.value })} />
                  <input className="h-12 rounded-lg px-4 text-sm outline-none transition-all duration-200 focus:ring-1"
                    style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                    placeholder="Image URL" value={spotForm.image}
                    onChange={(e) => setSpotForm((current) => current && { ...current, image: e.target.value })} />
                  <button type="button" onClick={() => setUploaded(true)}
                    className="h-12 rounded-lg text-sm font-bold transition-all duration-200 hover:opacity-80"
                    style={{ border: "2px dashed color-mix(in srgb, var(--text-muted) 40%, transparent)", background: "color-mix(in srgb, var(--text-muted) 8%, transparent)", color: "var(--text-muted)" }}>
                    {uploaded ? "Images Uploaded" : "Upload Images"}
                  </button>
                </div>
              </div>
              <div>
                <div className="mb-3 flex items-center justify-between gap-3">
                  <p className="text-sm font-bold" style={{ color: "var(--text)" }}>Location</p>
                  <button type="button" onClick={useCurrentStationLocation}
                    className="flex h-9 items-center gap-2 rounded-lg px-3 text-xs font-bold transition-opacity hover:opacity-80"
                    style={{ background: "color-mix(in srgb, var(--glass-bg) 70%, transparent)", color: "var(--text)" }}>
                    <LocateFixed size={14} /> Use GPS
                  </button>
                </div>
                <div className="grid gap-3">
                  <div className="grid grid-cols-2 gap-3">
                    <label className="grid gap-1">
                      <span className="text-xs font-semibold" style={{ color: "var(--text-muted)" }}>Latitude</span>
                      <input className="h-10 rounded-lg px-3 text-xs font-mono outline-none transition-all duration-200"
                        style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                        placeholder="10.878072" value={spotForm.lat}
                        onChange={(e) => setSpotForm((current) => current && { ...current, lat: e.target.value })} />
                    </label>
                    <label className="grid gap-1">
                      <span className="text-xs font-semibold" style={{ color: "var(--text-muted)" }}>Longitude</span>
                      <input className="h-10 rounded-lg px-3 text-xs font-mono outline-none transition-all duration-200"
                        style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                        placeholder="106.801247" value={spotForm.lng}
                        onChange={(e) => setSpotForm((current) => current && { ...current, lng: e.target.value })} />
                    </label>
                  </div>
                  <div className="overflow-hidden rounded-2xl" style={{ border: "1px solid var(--glass-border)" }}>
                    <div className="flex items-center gap-2 px-3 py-2 text-xs font-semibold"
                      style={{ background: "color-mix(in srgb, var(--glass-bg) 70%, transparent)", color: "var(--text-muted)" }}>
                      <MapPin size={14} /> Click the map to pin the charging spot for this address.
                    </div>
                    <div className="h-64">
                      <StationLocationPicker
                        key={`${editingSpot.id ?? "draft"}-${editingIndex}`}
                        lat={Number(spotForm.lat) || 10.878072}
                        lng={Number(spotForm.lng) || 106.801247}
                        onChange={(lat, lng) => setSpotForm((current) => current && {
                          ...current,
                          lat: lat.toFixed(6),
                          lng: lng.toFixed(6),
                        })}
                      />
                    </div>
                  </div>
                </div>
              </div>
              <div>
                <p className="mb-3 text-sm font-bold" style={{ color: "var(--text)" }}>Charging Information</p>
                <div className="grid grid-cols-1 gap-3 min-[380px]:grid-cols-2">
                  <button type="button" onClick={() => setSpotForm((current) => current && { ...current, chargerType: "Fast Charging" })}
                    className={`h-12 rounded-lg text-sm font-bold transition-all duration-200 ${spotForm.chargerType === "Fast Charging" ? "" : "opacity-60"}`}
                    style={{ background: spotForm.chargerType === "Fast Charging" ? "var(--accent)" : "var(--glass-bg)", color: spotForm.chargerType === "Fast Charging" ? "var(--accent-fg)" : "var(--text-muted)" }}>
                    Fast Charging
                  </button>
                  <button type="button" onClick={() => setSpotForm((current) => current && { ...current, chargerType: "Slow Charging" })}
                    className={`h-12 rounded-lg text-sm font-bold transition-all duration-200 ${spotForm.chargerType === "Slow Charging" ? "" : "opacity-60"}`}
                    style={{ background: spotForm.chargerType === "Slow Charging" ? "var(--accent)" : "var(--glass-bg)", color: spotForm.chargerType === "Slow Charging" ? "var(--accent-fg)" : "var(--text-muted)" }}>
                    Slow Charging
                  </button>
                  <label className="grid gap-1">
                    <span className="text-xs font-semibold" style={{ color: "var(--text-muted)" }}>Connectors</span>
                    <input className="h-10 rounded-lg px-3 text-xs outline-none transition-all duration-200"
                      style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                      type="number" min={1} max={4} placeholder="e.g. 2" value={spotForm.slots}
                      onChange={(e) => setSpotForm((current) => current && { ...current, slots: e.target.value })} />
                  </label>
                  <label className="grid gap-1">
                    <span className="text-xs font-semibold" style={{ color: "var(--text-muted)" }}>Price (₫/hr)</span>
                    <input className="h-10 rounded-lg px-3 text-xs outline-none transition-all duration-200"
                      style={{ border: "1px solid var(--glass-border)", background: "var(--glass-bg)", color: "var(--text)" }}
                      type="number" min={1} placeholder="e.g. 42000" value={spotForm.pricePerHour}
                      onChange={(e) => setSpotForm((current) => current && { ...current, pricePerHour: e.target.value })} />
                  </label>
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
                        <input type="checkbox" checked={spotForm.amenities.includes(item.apiValue)}
                          onChange={() => setSpotForm((current) => {
                            if (!current) return current;
                            const nextAmenities = current.amenities.includes(item.apiValue)
                              ? current.amenities.filter((value) => value !== item.apiValue)
                              : [...current.amenities, item.apiValue];
                            return { ...current, amenities: nextAmenities };
                          })}
                          className="accent-slate-300" />
                        <Icon size={16} style={{ color: "var(--text-muted)" }} />
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
                <button type="button" onClick={() => setSpotForm((current) => current && { ...current, isActive: !current.isActive })}
                  className={`h-8 w-14 rounded-lg p-1 transition ${spotForm.isActive ? "" : "opacity-40"}`}
                  style={{ background: spotForm.isActive ? "var(--accent)" : "var(--glass-bg)" }}
                  aria-pressed={spotForm.isActive}>
                  <span className={`block size-6 rounded-md shadow-sm transition ${spotForm.isActive ? "translate-x-6" : "translate-x-0"}`}
                    style={{ background: "var(--accent-fg)" }} />
                </button>
              </div>
              <button type="button" onClick={saveStation} disabled={saving}
                className="h-12 w-full rounded-lg text-sm font-bold transition-all duration-200 hover:opacity-90 active:scale-[0.98] disabled:opacity-60"
                style={{ background: "var(--accent)", color: "var(--accent-fg)" }}>
                {saving ? "Saving..." : "Save Station"}
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
            <p className="text-sm font-semibold" style={{ color: "var(--text-muted)" }}>Charging Spot</p>
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

type LeafletContainer = HTMLDivElement & {
  _leaflet_id?: number;
};

function resetLeafletContainer(container: LeafletContainer) {
  container.replaceChildren();
  delete container.dataset.leafletInitialized;
  delete container._leaflet_id;
}

function StationLocationPicker({
  lat,
  lng,
  onChange,
}: {
  lat: number;
  lng: number;
  onChange: (lat: number, lng: number) => void;
}) {
  const containerRef = useRef<LeafletContainer>(null);
  const mapRef = useRef<import("leaflet").Map | null>(null);
  const markerRef = useRef<import("leaflet").Marker | null>(null);

  useEffect(() => {
    const container = containerRef.current;
    let disposed = false;
    if (!container || mapRef.current) return;

    import("leaflet").then((L) => {
      if (disposed || mapRef.current) return;

      if (container.dataset.leafletInitialized === "true" || container._leaflet_id) {
        resetLeafletContainer(container);
      }

      const map = L.map(container, {
        center: [lat, lng],
        zoom: 15,
        zoomControl: false,
      });
      container.dataset.leafletInitialized = "true";

      const isDark = document.documentElement.dataset.theme !== "light";
      const tileUrl = isDark
        ? "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        : "https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png";

      L.tileLayer(tileUrl, {
        attribution: "© OpenStreetMap © CartoDB",
        subdomains: "abcd",
        maxZoom: 19,
      }).addTo(map);

      L.control.zoom({ position: "bottomright" }).addTo(map);

      const icon = L.divIcon({
        className: "",
        html: `<div style="
          width: 34px; height: 34px;
          border-radius: 50% 50% 50% 0;
          background: #4ade80;
          border: 3px solid #ffffff;
          transform: rotate(-45deg);
          box-shadow: 0 8px 24px rgba(74,222,128,.35);
        "></div>`,
        iconSize: [34, 34],
        iconAnchor: [17, 34],
      });

      markerRef.current = L.marker([lat, lng], { icon, draggable: true }).addTo(map);
      markerRef.current.on("dragend", () => {
        const next = markerRef.current?.getLatLng();
        if (next) onChange(next.lat, next.lng);
      });
      map.on("click", (event: import("leaflet").LeafletMouseEvent) => {
        markerRef.current?.setLatLng(event.latlng);
        onChange(event.latlng.lat, event.latlng.lng);
      });

      mapRef.current = map;
      const refreshMapSize = () => {
        if (!disposed) {
          map.invalidateSize();
          map.panTo([lat, lng], { animate: false });
        }
      };
      requestAnimationFrame(refreshMapSize);
      window.setTimeout(refreshMapSize, 120);
      window.setTimeout(refreshMapSize, 350);
      window.setTimeout(refreshMapSize, 700);
    });

    return () => {
      disposed = true;
      markerRef.current = null;
      mapRef.current?.remove();
      mapRef.current = null;
      if (container) resetLeafletContainer(container);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const nextLatLng: [number, number] = [lat, lng];
    markerRef.current?.setLatLng(nextLatLng);
    mapRef.current?.invalidateSize();
    mapRef.current?.panTo(nextLatLng);
  }, [lat, lng]);

  return <div ref={containerRef} className="h-full w-full" style={{ minHeight: 256, width: "100%" }} />;
}

function Metric({ icon: Icon, label, value }: { icon: ElementType; label: string; value: string }) {
  return (
    <div className="rounded-lg p-3" style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
      <Icon size={15} style={{ color: "var(--text-muted)" }} />
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
