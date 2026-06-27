"use client";
import { useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { User, Mail, Lock, Eye, EyeOff, Car, House } from "lucide-react";
import AuthLayout from "../_components/AuthLayout";

type Role = "driver" | "provider";

const ROLE_OPTIONS: { value: Role; label: string; icon: typeof Car }[] = [
  { value: "driver", label: "Driver", icon: Car },
  { value: "provider", label: "Host", icon: House },
];

function SignupForm() {
  const router = useRouter();
  const params = useSearchParams();
  const [role, setRole] = useState<Role>((params.get("role") as Role) ?? "driver");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    await new Promise((r) => setTimeout(r, 1000));
    if (role === "driver") router.push("/onboarding");
    else router.push("/provider-onboarding");
  }

  return (
    <AuthLayout
      title="Create account"
      subtitle={"Join Vietnam\u2019s EV charging network"}
      altHref="/login"
      altLabel="Already have an account?"
      altLinkText="Sign in"
    >
      {/* Role toggle */}
      <div
        className="flex p-1 rounded-xl gap-1 mb-6"
        style={{ background: "rgba(255,255,255,0.05)", border: "1px solid rgba(74,222,128,0.12)" }}
      >
        {ROLE_OPTIONS.map(({ value, label, icon: Icon }) => (
          <button
            key={value}
            type="button"
            onClick={() => setRole(value)}
            className="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-lg text-sm font-medium capitalize transition-all duration-200"
            style={{
              background: role === value ? "var(--accent)" : "transparent",
              color: role === value ? "var(--accent-fg)" : "var(--text-muted)",
            }}
          >
            <Icon size={15} />
            {label}
          </button>
        ))}
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <Field label="Full name" htmlFor="name">
          <User
            size={15}
            className="absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none"
            style={{ color: "var(--text-muted)" }}
          />
          <input
            id="name"
            type="text"
            autoComplete="name"
            required
            placeholder="Nguyen Van A"
            className="w-full h-10 pl-9 pr-4 rounded-xl text-sm outline-none transition-all duration-200 focus:ring-1 focus:ring-green-400"
            style={{
              background: "rgba(255,255,255,0.06)",
              border: "1px solid rgba(74,222,128,0.18)",
              color: "var(--text)",
            }}
          />
        </Field>

        <Field label="Email" htmlFor="email">
          <Mail
            size={15}
            className="absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none"
            style={{ color: "var(--text-muted)" }}
          />
          <input
            id="email"
            type="email"
            autoComplete="email"
            required
            placeholder="you@example.com"
            className="w-full h-10 pl-9 pr-4 rounded-xl text-sm outline-none transition-all duration-200 focus:ring-1 focus:ring-green-400"
            style={{
              background: "rgba(255,255,255,0.06)",
              border: "1px solid rgba(74,222,128,0.18)",
              color: "var(--text)",
            }}
          />
        </Field>

        <Field label="Password" htmlFor="password">
          <Lock
            size={15}
            className="absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none"
            style={{ color: "var(--text-muted)" }}
          />
          <input
            id="password"
            type={showPassword ? "text" : "password"}
            autoComplete="new-password"
            required
            minLength={8}
            placeholder={"••••••••"}
            className="w-full h-10 pl-9 pr-10 rounded-xl text-sm outline-none transition-all duration-200 focus:ring-1 focus:ring-green-400"
            style={{
              background: "rgba(255,255,255,0.06)",
              border: "1px solid rgba(74,222,128,0.18)",
              color: "var(--text)",
            }}
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 transition-opacity hover:opacity-70"
            style={{ color: "var(--text-muted)" }}
            tabIndex={-1}
          >
            {showPassword ? <EyeOff size={15} /> : <Eye size={15} />}
          </button>
        </Field>
        <p
          className="text-xs -mt-1"
          style={{ color: "color-mix(in srgb, var(--text-muted) 60%, transparent)" }}
        >
          At least 8 characters
        </p>

        <button
          type="submit"
          disabled={loading}
          className="mt-1 h-11 rounded-xl font-semibold text-sm transition-all duration-200 hover:opacity-90 disabled:opacity-50 active:scale-[0.98]"
          style={{ background: "var(--accent)", color: "var(--accent-fg)" }}
        >
          {loading ? "Creating account\u2026" : `Sign up as ${role}`}
        </button>
      </form>
    </AuthLayout>
  );
}

function Field({
  label,
  htmlFor,
  children,
}: {
  label: string;
  htmlFor: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-sm font-medium" style={{ color: "var(--text-muted)" }} htmlFor={htmlFor}>
        {label}
      </label>
      <div className="relative">{children}</div>
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
