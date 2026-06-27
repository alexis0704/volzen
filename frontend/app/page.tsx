import Link from "next/link";
import { Zap, MapPin, Shield, Star, Users, Search, CalendarCheck, PlugZap, ChevronRight, Quote } from "lucide-react";
import ThemeToggle from "@/components/ThemeToggle";

/* ── Static data ──────────────────────────────────────────── */

const STATS = [
  { value: "2,400+", label: "Charging stations" },
  { value: "18", label: "Cities in Vietnam" },
  { value: "4.8", label: "Average rating" },
];

const STEPS = [
  {
    icon: Search,
    step: "01",
    title: "Find nearby hosts",
    desc: "Open the map, filter by connector type and price, and discover hosts in minutes.",
  },
  {
    icon: CalendarCheck,
    step: "02",
    title: "Book a time slot",
    desc: "Pick your date and hours. Pricing is shown upfront — no surprise fees.",
  },
  {
    icon: PlugZap,
    step: "03",
    title: "Plug in & charge",
    desc: "Follow turn-by-turn directions to the host's address and start charging.",
  },
];

const FEATURES = [
  {
    icon: MapPin,
    title: "Live map search",
    desc: "See available stations in real time, with distance and price visible at a glance.",
    wide: true,
  },
  {
    icon: Shield,
    title: "Upfront pricing",
    desc: "No hidden fees. Total cost shown before you book.",
    wide: false,
  },
  {
    icon: Star,
    title: "Verified reviews",
    desc: "Real ratings from real drivers help you choose the best spot.",
    wide: false,
  },
  {
    icon: Zap,
    title: "All connectors",
    desc: "Type 1, Type 2, CCS, CHAdeMO — filter by your car's plug.",
    wide: false,
  },
  {
    icon: Users,
    title: "Earn as a host",
    desc: "Turn your home charger into passive income. Set your own hours and rate.",
    wide: true,
  },
];

const TESTIMONIALS = [
  {
    name: "Lan Anh Nguyen",
    city: "Ho Chi Minh City",
    avatar: "https://i.pravatar.cc/64?img=5",
    rating: 5,
    text: "I was skeptical about EV charging in the city, but Volzen made it so easy. Found a host 0.8 km away with great amenities — coffee and AC included!",
  },
  {
    name: "Duc Huy Tran",
    city: "Hanoi",
    avatar: "https://i.pravatar.cc/64?img=8",
    rating: 5,
    text: "The map is super intuitive. I can see prices and connector types at a glance. Booked my first slot in under two minutes.",
  },
];

/* ── Page ─────────────────────────────────────────────────── */

export default function LandingPage() {
  return (
    <div style={{ background: "var(--bg)", color: "var(--text)" }} className="flex flex-col overflow-x-hidden">

      {/* ── Nav ──────────────────────────────────────────────── */}
      <header className="sticky top-0 z-50 w-full" style={{ borderBottom: "1px solid var(--divider)", backdropFilter: "blur(16px)", background: "color-mix(in srgb, var(--bg) 80%, transparent)" }}>
        <nav className="flex items-center justify-between px-6 py-4 max-w-6xl mx-auto w-full">
          <div className="flex items-center gap-2">
            <Zap size={20} fill="currentColor" style={{ color: "var(--accent)" }} />
            <span className="font-bold text-lg tracking-tight" style={{ color: "var(--text)" }}>Volzen</span>
          </div>

          <div className="hidden sm:flex items-center gap-6 text-sm" style={{ color: "var(--text-muted)" }}>
            <a href="#how-it-works" className="hover:opacity-80 transition-opacity">How it works</a>
            <a href="#features" className="hover:opacity-80 transition-opacity">Features</a>
            <Link href="/signup?role=provider" className="hover:opacity-80 transition-opacity">Become a host</Link>
          </div>

          <div className="flex items-center gap-2">
            <ThemeToggle />
            <Link href="/login" className="text-sm px-4 py-2 rounded-full transition-opacity hover:opacity-80" style={{ color: "var(--text-muted)" }}>
              Sign in
            </Link>
            <Link href="/signup" className="text-sm px-4 py-2 rounded-full font-medium transition-opacity hover:opacity-90" style={{ background: "var(--accent)", color: "var(--accent-fg)" }}>
              Get started
            </Link>
          </div>
        </nav>
      </header>

      {/* ── Hero ─────────────────────────────────────────────── */}
      <section className="relative flex flex-col items-center justify-center text-center px-6 pt-24 pb-20 overflow-hidden">
        {/* single subtle glow */}
        <div className="absolute inset-0 flex items-start justify-center pointer-events-none" aria-hidden>
          <div className="w-[500px] h-[500px] rounded-full opacity-[0.12] blur-[100px] mt-[-100px]" style={{ background: "var(--accent)" }} />
        </div>

        <div className="relative z-10 max-w-2xl">
          {/* pill badge */}
          <span className="inline-flex items-center gap-1.5 text-xs font-medium px-3 py-1.5 rounded-full mb-7 glass" style={{ color: "var(--accent)" }}>
            <Zap size={11} fill="currentColor" /> Vietnam&apos;s first P2P EV charging network
          </span>

          <h1 className="text-4xl sm:text-6xl font-bold leading-[1.08] tracking-tight mb-5" style={{ color: "var(--text)" }}>
            Charge your EV<br />
            <span style={{ color: "var(--accent)" }}>from your neighbour.</span>
          </h1>

          <p className="text-base sm:text-lg leading-relaxed mb-9 max-w-md mx-auto" style={{ color: "var(--text-muted)" }}>
            Volzen connects EV drivers with homeowners who share their chargers —
            like Airbnb, but for electricity. Find a spot, book in seconds, charge.
          </p>

          <div className="flex flex-col sm:flex-row gap-3 justify-center">
            <Link href="/signup" className="inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-full font-semibold text-base transition-all hover:opacity-90 hover:scale-[1.02]" style={{ background: "var(--accent)", color: "var(--accent-fg)" }}>
              Find a charger <ChevronRight size={16} />
            </Link>
            <Link href="/signup?role=provider" className="inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-full font-medium text-base glass glass-hover" style={{ color: "var(--text)" }}>
              List my charger
            </Link>
          </div>

          {/* trust line */}
          <p className="mt-6 text-xs" style={{ color: "var(--text-muted)" }}>
            Free to join · No commitment · Cancel anytime
          </p>
        </div>
      </section>

      {/* ── Stats strip ──────────────────────────────────────── */}
      <section className="max-w-2xl mx-auto w-full px-6 pb-20">
        <div className="glass grid grid-cols-3 divide-x" style={{ borderColor: "var(--glass-border)", "--tw-divide-opacity": "1" } as React.CSSProperties}>
          {STATS.map((s) => (
            <div key={s.label} className="flex flex-col items-center justify-center py-6 px-4">
              <span className="text-2xl sm:text-3xl font-bold tabular-nums" style={{ color: "var(--accent)" }}>
                {s.value}{s.label === "Average rating" && <span className="text-base ml-0.5">★</span>}
              </span>
              <span className="text-xs mt-1 text-center" style={{ color: "var(--text-muted)" }}>{s.label}</span>
            </div>
          ))}
        </div>
      </section>

      {/* ── How it works ─────────────────────────────────────── */}
      <section id="how-it-works" className="max-w-5xl mx-auto w-full px-6 pb-24">
        <div className="text-center mb-12">
          <h2 className="text-2xl sm:text-3xl font-bold mb-3" style={{ color: "var(--text)" }}>As easy as 1 – 2 – 3</h2>
          <p className="text-sm" style={{ color: "var(--text-muted)" }}>From finding a spot to fully charged — all in one app.</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
          {STEPS.map((s, i) => {
            const Icon = s.icon;
            return (
              <div key={s.step} className="glass glass-hover p-6 flex flex-col gap-4">
                <div className="flex items-center justify-between">
                  <div className="w-10 h-10 rounded-xl flex items-center justify-center" style={{ background: "color-mix(in srgb, var(--accent) 15%, transparent)", border: "1px solid color-mix(in srgb, var(--accent) 30%, transparent)" }}>
                    <Icon size={18} style={{ color: "var(--accent)" }} />
                  </div>
                  <span className="text-3xl font-black tabular-nums" style={{ color: "color-mix(in srgb, var(--text-muted) 30%, transparent)" }}>{s.step}</span>
                </div>
                <div>
                  <p className="font-semibold mb-1.5" style={{ color: "var(--text)" }}>{s.title}</p>
                  <p className="text-sm leading-6" style={{ color: "var(--text-muted)" }}>{s.desc}</p>
                </div>
                {/* connector line on desktop */}
                {i < STEPS.length - 1 && (
                  <div className="hidden sm:block absolute right-0 top-1/2 w-5 h-px" style={{ background: "var(--glass-border)" }} />
                )}
              </div>
            );
          })}
        </div>
      </section>

      {/* ── Features bento ───────────────────────────────────── */}
      <section id="features" className="max-w-5xl mx-auto w-full px-6 pb-24">
        <div className="text-center mb-12">
          <h2 className="text-2xl sm:text-3xl font-bold mb-3" style={{ color: "var(--text)" }}>Built for drivers, by drivers</h2>
          <p className="text-sm" style={{ color: "var(--text-muted)" }}>Everything you need to charge confidently, anywhere in Vietnam.</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {FEATURES.map((f) => {
            const Icon = f.icon;
            return (
              <div
                key={f.title}
                className={`glass glass-hover p-6 flex flex-col gap-3 ${f.wide ? "sm:col-span-2" : ""}`}
              >
                <div className="w-9 h-9 rounded-lg flex items-center justify-center" style={{ background: "color-mix(in srgb, var(--accent) 12%, transparent)", border: "1px solid color-mix(in srgb, var(--accent) 25%, transparent)" }}>
                  <Icon size={17} style={{ color: "var(--accent)" }} />
                </div>
                <div>
                  <p className="font-semibold mb-1" style={{ color: "var(--text)" }}>{f.title}</p>
                  <p className="text-sm leading-6" style={{ color: "var(--text-muted)" }}>{f.desc}</p>
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* ── Testimonials ─────────────────────────────────────── */}
      <section className="max-w-5xl mx-auto w-full px-6 pb-24">
        <div className="text-center mb-12">
          <h2 className="text-2xl sm:text-3xl font-bold mb-3" style={{ color: "var(--text)" }}>Drivers love it</h2>
          <p className="text-sm" style={{ color: "var(--text-muted)" }}>Real experiences from the Volzen community.</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
          {TESTIMONIALS.map((t) => (
            <div key={t.name} className="glass p-6 flex flex-col gap-4">
              <Quote size={20} style={{ color: "var(--accent)", opacity: 0.5 }} />
              <p className="text-sm leading-7" style={{ color: "var(--text)" }}>&ldquo;{t.text}&rdquo;</p>
              <div className="flex items-center gap-3 mt-auto pt-2" style={{ borderTop: "1px solid var(--divider)" }}>
                <img src={t.avatar} alt={t.name} className="w-9 h-9 rounded-full object-cover border-2" style={{ borderColor: "var(--glass-border)" }} width={36} height={36} />
                <div>
                  <p className="text-sm font-semibold" style={{ color: "var(--text)" }}>{t.name}</p>
                  <p className="text-xs" style={{ color: "var(--text-muted)" }}>{t.city}</p>
                </div>
                <div className="ml-auto flex gap-0.5">
                  {Array.from({ length: t.rating }).map((_, i) => (
                    <Star key={i} size={12} fill="currentColor" style={{ color: "#facc15" }} />
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ── CTA — host angle ─────────────────────────────────── */}
      <section className="max-w-3xl mx-auto w-full px-6 pb-24">
        <div className="glass p-10 text-center" style={{ background: "color-mix(in srgb, var(--accent) 6%, var(--glass-bg))", borderColor: "color-mix(in srgb, var(--accent) 30%, transparent)" }}>
          <div className="w-12 h-12 rounded-2xl flex items-center justify-center mx-auto mb-5" style={{ background: "color-mix(in srgb, var(--accent) 15%, transparent)", border: "1px solid color-mix(in srgb, var(--accent) 30%, transparent)" }}>
            <Zap size={22} fill="currentColor" style={{ color: "var(--accent)" }} />
          </div>
          <h2 className="text-2xl sm:text-3xl font-bold mb-3" style={{ color: "var(--text)" }}>Have a home charger?</h2>
          <p className="text-sm sm:text-base leading-relaxed mb-7 max-w-sm mx-auto" style={{ color: "var(--text-muted)" }}>
            Turn idle electricity into income. Set your own schedule and price.
            Volzen handles bookings and payments.
          </p>
          <div className="flex flex-col sm:flex-row gap-3 justify-center">
            <Link href="/signup?role=provider" className="inline-flex items-center justify-center gap-2 px-7 py-3.5 rounded-full font-semibold text-base hover:opacity-90 transition-all" style={{ background: "var(--accent)", color: "var(--accent-fg)" }}>
              Start earning <ChevronRight size={16} />
            </Link>
            <Link href="/signup" className="inline-flex items-center justify-center gap-2 px-7 py-3.5 rounded-full font-medium text-base glass glass-hover" style={{ color: "var(--text)" }}>
              Find a charger instead
            </Link>
          </div>
        </div>
      </section>

      {/* ── Footer ───────────────────────────────────────────── */}
      <footer className="max-w-6xl mx-auto w-full px-6 pb-10 pt-8" style={{ borderTop: "1px solid var(--divider)" }}>
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <Zap size={16} fill="currentColor" style={{ color: "var(--accent)" }} />
              <span className="font-bold" style={{ color: "var(--text)" }}>Volzen</span>
            </div>
            <p className="text-xs" style={{ color: "var(--text-muted)" }}>© 2026 Volzen · EV Charging Network · Vietnam</p>
          </div>
          <nav className="flex flex-wrap gap-5 text-xs" style={{ color: "var(--text-muted)" }}>
            <Link href="/signup?role=provider" className="hover:opacity-80">Become a host</Link>
            <Link href="/login" className="hover:opacity-80">Sign in</Link>
            <a href="#how-it-works" className="hover:opacity-80">How it works</a>
            <a href="#features" className="hover:opacity-80">Features</a>
          </nav>
        </div>
      </footer>
    </div>
  );
}
