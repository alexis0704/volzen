"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { Car, Plus, Trash2, Check } from "lucide-react";
import type { ConnectorType, Vehicle } from "@/lib/domain";
import { createVehicle } from "@/lib/api";

const CONNECTORS: ConnectorType[] = ["Type 1", "Type 2", "CCS", "CHAdeMO"];
const BRANDS = ["VinFast", "Tesla", "Toyota", "Honda", "Hyundai", "Kia", "BMW", "Mercedes", "Audi", "BYD", "Other"];

function newVehicle(): Omit<Vehicle, "id"> {
  return { brand: "VinFast", model: "", year: 2024, connector: "CCS" };
}

export default function OnboardingPage() {
  const router = useRouter();
  const [step, setStep] = useState(0);
  const [vehicles, setVehicles] = useState<Omit<Vehicle, "id">[]>([newVehicle()]);
  const [defaultIdx, setDefaultIdx] = useState(0);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const steps = ["Add vehicle", "More vehicles", "Default vehicle"];

  function updateVehicle(i: number, patch: Partial<Omit<Vehicle, "id">>) {
    setVehicles((vs) => vs.map((v, idx) => (idx === i ? { ...v, ...patch } : v)));
  }

  async function finish() {
    setSaving(true);
    setError("");

    try {
      await Promise.all(
        vehicles
          .filter((vehicle) => vehicle.model.trim().length > 0)
          .map((vehicle, index) =>
            createVehicle({
              brand: vehicle.brand,
              model: vehicle.model,
              year: vehicle.year,
              connectorType: vehicle.connector,
              isDefault: index === defaultIdx,
            }),
          ),
      );
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : "Could not save vehicles. Continuing in demo mode.");
    }

    router.push("/explore");
  }

  return (
    <div className="min-h-dvh flex flex-col items-center justify-center px-4" style={{ background: "var(--bg)" }}>
      <div className="relative w-full max-w-md">
        <div className="absolute -top-20 left-1/2 -translate-x-1/2 w-72 h-72 rounded-full opacity-10 blur-[100px] pointer-events-none" style={{ background: "#4ade80" }} aria-hidden />
        <div className="glass p-8 relative">
          {/* Header */}
          <div className="mb-8">
            <span className="font-bold text-lg" style={{ color: "var(--text)" }}>VOLZEN</span>
          </div>

          {/* Progress dots */}
          <div className="flex gap-2 mb-7">
            {steps.map((_, i) => (
              <div key={i} className="h-1.5 flex-1 rounded-full transition-all" style={{ background: i <= step ? "var(--accent)" : "rgba(255,255,255,0.15)" }} />
            ))}
          </div>

          {/* Step 0 — first vehicle */}
          {step === 0 && (
            <div>
              <h1 className="text-2xl font-bold mb-1" style={{ color: "var(--text)" }}>Register your vehicle</h1>
              <p className="text-sm mb-6" style={{ color: "var(--text-muted)" }}>We need this to match you with compatible chargers.</p>
              <VehicleForm vehicle={vehicles[0]} onChange={(p) => updateVehicle(0, p)} />
              <button onClick={() => setStep(1)} className="mt-5 w-full py-3 rounded-xl font-semibold text-sm hover:opacity-90" style={{ background: "var(--accent)", color: "#0a0f0d" }}>Continue</button>
            </div>
          )}

          {/* Step 1 — add more */}
          {step === 1 && (
            <div>
              <h1 className="text-2xl font-bold mb-1" style={{ color: "var(--text)" }}>More vehicles?</h1>
              <p className="text-sm mb-5" style={{ color: "var(--text-muted)" }}>Add up to 4 vehicles to your account.</p>
              <div className="flex flex-col gap-3 mb-4">
                {vehicles.map((v, i) => (
                  <div key={i} className="flex items-center gap-3 p-3 rounded-xl" style={{ background: "rgba(255,255,255,0.05)", border: "1px solid rgba(74,222,128,0.15)" }}>
                    <Car size={18} style={{ color: "var(--accent)" }} />
                    <span className="text-sm flex-1" style={{ color: "var(--text)" }}>{v.brand} {v.model || "—"} · {v.connector}</span>
                    {i > 0 && (
                      <button onClick={() => setVehicles((vs) => vs.filter((_, idx) => idx !== i))} aria-label="Remove vehicle">
                        <Trash2 size={15} style={{ color: "var(--text-muted)" }} />
                      </button>
                    )}
                  </div>
                ))}
              </div>
              {vehicles.length < 4 && (
                <button onClick={() => setVehicles((vs) => [...vs, newVehicle()])} className="flex items-center gap-2 text-sm mb-5" style={{ color: "var(--accent)" }}>
                  <Plus size={15} /> Add another vehicle
                </button>
              )}
              <div className="flex gap-3">
                <button onClick={() => setStep(0)} className="flex-1 py-3 rounded-xl text-sm font-medium" style={{ border: "1px solid rgba(74,222,128,0.3)", color: "var(--text-muted)" }}>Back</button>
                <button onClick={() => (vehicles.length > 1 ? setStep(2) : finish())} disabled={saving} className="flex-1 py-3 rounded-xl font-semibold text-sm hover:opacity-90 disabled:opacity-60" style={{ background: "var(--accent)", color: "#0a0f0d" }}>
                  {saving ? "Saving…" : vehicles.length > 1 ? "Continue" : "Done"}
                </button>
              </div>
            </div>
          )}

          {/* Step 2 — pick default */}
          {step === 2 && (
            <div>
              <h1 className="text-2xl font-bold mb-1" style={{ color: "var(--text)" }}>Choose default</h1>
              <p className="text-sm mb-5" style={{ color: "var(--text-muted)" }}>Select the vehicle you drive most often.</p>
              <div className="flex flex-col gap-3 mb-6">
                {vehicles.map((v, i) => (
                  <button key={i} onClick={() => setDefaultIdx(i)} className="flex items-center gap-3 p-3 rounded-xl text-left transition-all" style={{ background: defaultIdx === i ? "rgba(74,222,128,0.12)" : "rgba(255,255,255,0.05)", border: `1px solid ${defaultIdx === i ? "rgba(74,222,128,0.5)" : "rgba(74,222,128,0.15)"}` }}>
                    <Car size={18} style={{ color: "var(--accent)" }} />
                    <span className="text-sm flex-1" style={{ color: "var(--text)" }}>{v.brand} {v.model || "—"} · {v.year} · {v.connector}</span>
                    {defaultIdx === i && <Check size={16} style={{ color: "var(--accent)" }} />}
                  </button>
                ))}
              </div>
              <div className="flex gap-3">
                <button onClick={() => setStep(1)} className="flex-1 py-3 rounded-xl text-sm font-medium" style={{ border: "1px solid rgba(74,222,128,0.3)", color: "var(--text-muted)" }}>Back</button>
                <button onClick={finish} disabled={saving} className="flex-1 py-3 rounded-xl font-semibold text-sm hover:opacity-90 disabled:opacity-60" style={{ background: "var(--accent)", color: "#0a0f0d" }}>
                  {saving ? "Saving…" : "Find chargers"}
                </button>
              </div>
            </div>
          )}

          {error && <p className="mt-4 text-xs" style={{ color: "#fca5a5" }}>{error}</p>}
        </div>
      </div>
    </div>
  );
}

function VehicleForm({ vehicle, onChange }: { vehicle: Omit<Vehicle, "id">; onChange: (p: Partial<Omit<Vehicle, "id">>) => void }) {
  return (
    <div className="flex flex-col gap-4">
      <Field label="Brand">
        <select value={vehicle.brand} onChange={(e) => onChange({ brand: e.target.value })} className="w-full px-3 py-2.5 rounded-xl text-sm outline-none" style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)" }}>
          {BRANDS.map((b) => <option key={b} value={b} style={{ background: "#0a0f0d" }}>{b}</option>)}
        </select>
      </Field>
      <Field label="Model">
        <input type="text" value={vehicle.model} onChange={(e) => onChange({ model: e.target.value })} placeholder="e.g. VF8, Model 3" required className="w-full px-3 py-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-green-400" style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)" }} />
      </Field>
      <Field label="Year">
        <input type="number" value={vehicle.year} onChange={(e) => onChange({ year: Number(e.target.value) })} min={2000} max={2030} className="w-full px-3 py-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-green-400" style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)" }} />
      </Field>
      <Field label="Connector type">
        <div className="flex gap-2 flex-wrap">
          {CONNECTORS.map((c) => (
            <button key={c} type="button" onClick={() => onChange({ connector: c })} className="px-3 py-1.5 rounded-full text-xs font-medium transition-all" style={{ background: vehicle.connector === c ? "var(--accent)" : "rgba(255,255,255,0.07)", color: vehicle.connector === c ? "#0a0f0d" : "var(--text-muted)", border: `1px solid ${vehicle.connector === c ? "transparent" : "rgba(74,222,128,0.2)"}` }}>
              {c}
            </button>
          ))}
        </div>
      </Field>
    </div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-sm font-medium" style={{ color: "var(--text-muted)" }}>{label}</label>
      {children}
    </div>
  );
}
