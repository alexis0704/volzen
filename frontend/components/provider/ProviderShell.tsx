"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { BarChart3, CalendarDays, MapPinned, Plus } from "lucide-react";
import type { ReactNode } from "react";

const ACCENT = "#e2e8f0";
const ACCENT_FG = "#0a0f0d";

const navItems = [
  { href: "/host/financial", label: "Finance", icon: BarChart3 },
  { href: "/host/bookings", label: "Bookings", icon: CalendarDays },
  { href: "/host/spots", label: "Spots", icon: MapPinned },
];

export function ProviderShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const currentPage = navItems.find((item) => item.href === pathname)?.label ?? "Dashboard";

  return (
    <div className="min-h-dvh overflow-x-hidden" style={{ background: "var(--bg)", color: "var(--text)" }}>
      {/* Top bar */}
      <div
        className="fixed inset-x-0 top-0 z-40 lg:left-72"
        style={{ borderBottom: "1px solid var(--glass-border)", background: "color-mix(in srgb, var(--glass-bg) 80%, transparent)", backdropFilter: "blur(14px)" }}
      >
        <header className="flex h-14 items-center gap-3 px-3 sm:h-16 sm:px-6">
          <Link href="/" className="flex min-w-0 items-center gap-2 lg:hidden">
            <span className="font-bold tracking-tight" style={{ color: "var(--text)" }}>VOLZEN</span>
            <span
              className="truncate rounded px-2 py-0.5 text-[11px] font-semibold sm:hidden"
              style={{ background: "color-mix(in srgb, #e2e8f0 12%, transparent)", color: ACCENT }}
            >
              {currentPage}
            </span>
          </Link>
          <div className="ml-auto flex items-center gap-2">
            <span
              className="hidden rounded px-3 py-1 text-xs font-medium sm:inline-flex"
              style={{ background: "color-mix(in srgb, #e2e8f0 12%, transparent)", color: ACCENT }}
            >
              Provider account
            </span>
            <Link
              href="/host/spots"
              className="inline-flex h-10 items-center gap-2 rounded-lg px-3 text-sm font-semibold sm:px-4 transition-all duration-200 hover:opacity-90 active:scale-[0.98]"
              style={{ background: ACCENT, color: ACCENT_FG }}
            >
              <Plus size={16} /> <span className="hidden sm:inline">Add spot</span>
            </Link>
          </div>
        </header>
      </div>

      {/* Sidebar */}
      <aside
        className="fixed inset-y-0 left-0 z-50 hidden w-72 px-4 py-5 lg:block"
        style={{ borderRight: "1px solid var(--glass-border)", background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)", backdropFilter: "blur(14px)" }}
      >
        <Link href="/" className="mb-8 flex items-center gap-2 px-2">
          <div>
            <p className="font-bold tracking-tight" style={{ color: "var(--text)" }}>VOLZEN</p>
            <p className="text-xs" style={{ color: "var(--text-muted)" }}>Host dashboard</p>
          </div>
        </Link>
        <nav className="flex flex-col gap-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = pathname === item.href;
            return (
              <Link
                key={item.href}
                href={item.href}
                className="flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-semibold transition-all duration-200"
                style={{
                  background: active ? ACCENT : "transparent",
                  color: active ? ACCENT_FG : "var(--text-muted)",
                }}
              >
                <Icon size={18} />
                {item.label}
              </Link>
            );
          })}
        </nav>
      </aside>

      {/* Main content */}
      <main className="px-3 pb-28 pt-[70px] sm:px-6 sm:pt-20 lg:ml-72 lg:pb-10">
        <div className="mx-auto max-w-7xl min-w-0">{children}</div>
      </main>

      {/* Bottom mobile nav */}
      <nav
        className="fixed inset-x-3 bottom-3 z-50 grid grid-cols-3 p-2 rounded-xl shadow-2xl lg:hidden"
        style={{ border: "1px solid var(--glass-border)", background: "color-mix(in srgb, var(--glass-bg) 80%, transparent)", backdropFilter: "blur(14px)" }}
      >
        {navItems.map((item) => {
          const Icon = item.icon;
          const active = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className="flex min-h-14 flex-col items-center justify-center gap-1 rounded-lg text-[11px] font-semibold transition-all duration-200"
              style={{
                background: active ? ACCENT : "transparent",
                color: active ? ACCENT_FG : "var(--text-muted)",
              }}
            >
              <Icon size={18} />
              {item.label}
            </Link>
          );
        })}
      </nav>
    </div>
  );
}

export function ProviderCard({ children, className = "" }: { children: ReactNode; className?: string }) {
  return (
    <section
      className={`min-w-0 rounded-xl p-4 sm:p-5 ${className}`}
      style={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)" }}
    >
      {children}
    </section>
  );
}

export function StatusBadge({ tone, children }: { tone: "green" | "orange" | "red" | "slate" | "gray" | "white"; children: ReactNode }) {
  const styles: Record<string, React.CSSProperties> = {
    green: { background: "rgba(74,222,128,0.15)", color: "#4ade80" },
    orange: { background: "rgba(251,146,60,0.15)", color: "#fb923c" },
    red: { background: "rgba(239,68,68,0.15)", color: "#ef4444" },
    slate: { background: "rgba(226,232,240,0.12)", color: "#e2e8f0" },
    white: { background: "rgba(255,255,255,0.12)", color: "#ffffff" },
    gray: { background: "rgba(148,163,184,0.15)", color: "#94a3b8" },
  };

  return (
    <span className="inline-flex rounded-md px-2 py-0.5 text-[11px] font-semibold" style={styles[tone]}>
      {children}
    </span>
  );
}
