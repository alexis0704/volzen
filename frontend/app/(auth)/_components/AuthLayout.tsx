"use client";
import type { ReactNode } from "react";
import Link from "next/link";


interface AuthLayoutProps {
  children: ReactNode;
  title: string;
  subtitle: string;
  altHref: string;
  altLabel: string;
  altLinkText: string;
}

export default function AuthLayout({
  children,
  title,
  subtitle,
  altHref,
  altLabel,
  altLinkText,
}: AuthLayoutProps) {
  return (
    <div className="min-h-dvh flex flex-col lg:flex-row" style={{ background: "var(--bg)" }}>
      {/* Brand panel — desktop */}
      <aside
        className="hidden lg:flex lg:w-[44%] relative flex-col justify-between p-12 overflow-hidden select-none"
        style={{ background: "linear-gradient(160deg, #060a08 0%, #0c1f16 40%, #060a08 100%)" }}
      >
        {/* Glow orbs */}
        <div
          className="absolute -top-32 -right-32 w-[500px] h-[500px] rounded-full opacity-10 blur-[140px] pointer-events-none"
          style={{ background: "radial-gradient(circle, #4ade80 0%, transparent 70%)" }}
          aria-hidden
        />
        <div
          className="absolute -bottom-40 -left-40 w-[600px] h-[600px] rounded-full opacity-[0.07] blur-[160px] pointer-events-none"
          style={{ background: "radial-gradient(circle, #4ade80 0%, transparent 70%)" }}
          aria-hidden
        />

        {/* Subtle grid */}
        <div
          className="absolute inset-0 opacity-[0.025] pointer-events-none"
          style={{
            backgroundImage:
              "linear-gradient(rgba(74,222,128,0.4) 1px, transparent 1px), linear-gradient(90deg, rgba(74,222,128,0.4) 1px, transparent 1px)",
            backgroundSize: "64px 64px",
          }}
          aria-hidden
        />

        {/* Top — logo */}
        <div className="relative z-10">
          <Link href="/" className="inline-flex items-center gap-2 group">
            <span className="text-lg font-bold tracking-tight" style={{ color: "var(--text)" }}>
              VOLZEN
            </span>
          </Link>
        </div>

        {/* Center — brand message */}
        <div className="relative z-10 pb-16">
          <p
            className="text-[13px] font-medium uppercase tracking-[0.15em] mb-4"
            style={{ color: "color-mix(in srgb, var(--accent) 60%, transparent)" }}
          >
            EV Charging Network
          </p>
          <h2 className="text-3xl font-bold leading-[1.15] mb-4" style={{ color: "var(--text)" }}>
            Power up,
            <br />
            <span style={{ color: "var(--accent)" }}>anywhere you park.</span>
          </h2>
          <p className="text-sm leading-relaxed max-w-xs" style={{ color: "var(--text-muted)" }}>
            Volzen connects EV drivers with homeowners across Vietnam. Find a spot, book in seconds, charge.
          </p>
        </div>

        {/* Bottom — copyright */}
        <div
          className="relative z-10 text-[11px]"
          style={{ color: "color-mix(in srgb, var(--text-muted) 40%, transparent)" }}
        >
          &copy; 2026 Volzen
        </div>
      </aside>

      {/* Form panel */}
      <main className="flex-1 flex items-center justify-center px-5 py-10 lg:py-0">
        <div className="w-full max-w-sm">
          {/* Mobile brand bar */}
          <div className="lg:hidden mb-10">
            <span className="text-lg font-bold tracking-tight" style={{ color: "var(--text)" }}>
              VOLZEN
            </span>
          </div>

          {/* Card */}
          <div className="relative">
            <div
              className="absolute -top-28 left-1/2 -translate-x-1/2 w-72 h-72 rounded-full opacity-[0.06] blur-[100px] pointer-events-none"
              style={{ background: "#4ade80" }}
              aria-hidden
            />
            <div className="glass p-8 relative">
              <h1 className="text-2xl font-bold mb-1" style={{ color: "var(--text)" }}>
                {title}
              </h1>
              <p className="text-sm mb-7" style={{ color: "var(--text-muted)" }}>
                {subtitle}
              </p>

              {children}

              <p className="text-sm text-center mt-6" style={{ color: "var(--text-muted)" }}>
                {altLabel}{" "}
                <Link
                  href={altHref}
                  className="font-medium hover:opacity-80 transition-opacity"
                  style={{ color: "var(--accent)" }}
                >
                  {altLinkText}
                </Link>
              </p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
