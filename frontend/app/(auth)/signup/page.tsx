"use client";
import { useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Zap, Mail, Lock, User } from "lucide-react";
import { Suspense } from "react";

type Role = "driver" | "provider";

function SignupForm() {
  const router = useRouter();
  const params = useSearchParams();
  const [role, setRole] = useState<Role>((params.get("role") as Role) ?? "driver");
  const [loading, setLoading] = useState(false);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    // ponytail: mock signup — driver goes to onboarding, provider TBD
    setTimeout(() => {
      if (role === "driver") router.push("/onboarding");
      else router.push("/explore");
    }, 1000);
  }

  return (
    <div className="min-h-dvh flex items-center justify-center px-4" style={{ background: "var(--bg)" }}>
      <div className="relative w-full max-w-sm">
        <div className="absolute -top-20 left-1/2 -translate-x-1/2 w-72 h-72 rounded-full opacity-15 blur-[80px] pointer-events-none" style={{ background: "#4ade80" }} aria-hidden />
        <div className="glass p-8 relative">
          <div className="flex items-center gap-2 mb-8">
            <Zap className="text-green-400" size={20} fill="currentColor" />
            <span className="font-bold text-lg" style={{ color: "var(--text)" }}>Volzen</span>
          </div>
          <h1 className="text-2xl font-bold mb-1" style={{ color: "var(--text)" }}>Create account</h1>
          <p className="text-sm mb-5" style={{ color: "var(--text-muted)" }}>Join the green charging network</p>

          {/* Role toggle */}
          <div className="flex mb-5 p-1 rounded-xl gap-1" style={{ background: "rgba(255,255,255,0.06)", border: "1px solid rgba(74,222,128,0.15)" }}>
            {(["driver", "provider"] as Role[]).map((r) => (
              <button
                key={r}
                type="button"
                onClick={() => setRole(r)}
                className="flex-1 py-2 rounded-lg text-sm font-medium capitalize transition-all"
                style={{
                  background: role === r ? "var(--accent)" : "transparent",
                  color: role === r ? "#0a0f0d" : "var(--text-muted)",
                }}
              >
                {r === "driver" ? "🚗 Driver" : "🏠 Host"}
              </button>
            ))}
          </div>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium" style={{ color: "var(--text-muted)" }} htmlFor="name">Full name</label>
              <div className="relative">
                <User size={15} className="absolute left-3 top-1/2 -translate-y-1/2" style={{ color: "var(--text-muted)" }} />
                <input id="name" type="text" autoComplete="name" required placeholder="Nguyen Van A" className="w-full pl-9 pr-4 py-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-green-400" style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)" }} />
              </div>
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium" style={{ color: "var(--text-muted)" }} htmlFor="email">Email</label>
              <div className="relative">
                <Mail size={15} className="absolute left-3 top-1/2 -translate-y-1/2" style={{ color: "var(--text-muted)" }} />
                <input id="email" type="email" autoComplete="email" required placeholder="you@example.com" className="w-full pl-9 pr-4 py-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-green-400" style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)" }} />
              </div>
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium" style={{ color: "var(--text-muted)" }} htmlFor="password">Password</label>
              <div className="relative">
                <Lock size={15} className="absolute left-3 top-1/2 -translate-y-1/2" style={{ color: "var(--text-muted)" }} />
                <input id="password" type="password" autoComplete="new-password" required placeholder="••••••••" minLength={8} className="w-full pl-9 pr-4 py-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-green-400" style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)" }} />
              </div>
            </div>
            <button type="submit" disabled={loading} className="mt-2 py-3 rounded-xl font-semibold text-sm transition-opacity hover:opacity-90 disabled:opacity-60" style={{ background: "var(--accent)", color: "#0a0f0d" }}>
              {loading ? "Creating account…" : `Sign up as ${role}`}
            </button>
          </form>

          <p className="text-sm text-center mt-5" style={{ color: "var(--text-muted)" }}>
            Already have an account?{" "}
            <Link href="/login" className="font-medium" style={{ color: "var(--accent)" }}>Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default function SignupPage() {
  return (
    <Suspense>
      <SignupForm />
    </Suspense>
  );
}
