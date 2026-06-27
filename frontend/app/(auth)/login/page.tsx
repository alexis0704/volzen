"use client";
import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Zap, Mail, Lock } from "lucide-react";

export default function LoginPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    // ponytail: mock login — always succeed as driver
    setTimeout(() => router.push("/explore"), 1000);
  }

  return (
    <div className="min-h-dvh flex items-center justify-center px-4" style={{ background: "var(--bg)" }}>
      <div className="relative w-full max-w-sm">
        {/* glow */}
        <div className="absolute -top-20 left-1/2 -translate-x-1/2 w-72 h-72 rounded-full opacity-15 blur-[80px] pointer-events-none" style={{ background: "#4ade80" }} aria-hidden />
        <div className="glass p-8 relative">
          <div className="flex items-center gap-2 mb-8">
            <Zap className="text-green-400" size={20} fill="currentColor" />
            <span className="font-bold text-lg" style={{ color: "var(--text)" }}>Volzen</span>
          </div>
          <h1 className="text-2xl font-bold mb-1" style={{ color: "var(--text)" }}>Welcome back</h1>
          <p className="text-sm mb-6" style={{ color: "var(--text-muted)" }}>Sign in to your account</p>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium" style={{ color: "var(--text-muted)" }} htmlFor="email">Email</label>
              <div className="relative">
                <Mail size={15} className="absolute left-3 top-1/2 -translate-y-1/2" style={{ color: "var(--text-muted)" }} />
                <input
                  id="email"
                  type="email"
                  autoComplete="email"
                  required
                  placeholder="you@example.com"
                  className="w-full pl-9 pr-4 py-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-green-400"
                  style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)" }}
                />
              </div>
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium" style={{ color: "var(--text-muted)" }} htmlFor="password">Password</label>
              <div className="relative">
                <Lock size={15} className="absolute left-3 top-1/2 -translate-y-1/2" style={{ color: "var(--text-muted)" }} />
                <input
                  id="password"
                  type="password"
                  autoComplete="current-password"
                  required
                  placeholder="••••••••"
                  className="w-full pl-9 pr-4 py-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-green-400"
                  style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(74,222,128,0.2)", color: "var(--text)" }}
                />
              </div>
            </div>
            <button
              type="submit"
              disabled={loading}
              className="mt-2 py-3 rounded-xl font-semibold text-sm transition-opacity hover:opacity-90 disabled:opacity-60"
              style={{ background: "var(--accent)", color: "#0a0f0d" }}
            >
              {loading ? "Signing in…" : "Sign in"}
            </button>
          </form>

          <p className="text-sm text-center mt-5" style={{ color: "var(--text-muted)" }}>
            No account?{" "}
            <Link href="/signup" className="font-medium" style={{ color: "var(--accent)" }}>Sign up free</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
