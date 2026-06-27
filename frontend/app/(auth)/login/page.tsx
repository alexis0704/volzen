"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { Mail, Lock, Eye, EyeOff } from "lucide-react";
import AuthLayout from "../_components/AuthLayout";

export default function LoginPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    await new Promise((r) => setTimeout(r, 1000));
    router.push("/explore");
  }

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Sign in to your Volzen account"
      altHref="/signup"
      altLabel="No account?"
      altLinkText="Sign up free"
    >
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
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

        <Field
          label="Password"
          htmlFor="password"
          extra={
            <button
              type="button"
              className="text-xs font-medium hover:opacity-80 transition-opacity"
              style={{ color: "var(--accent)" }}
            >
              Forgot?
            </button>
          }
        >
          <Lock
            size={15}
            className="absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none"
            style={{ color: "var(--text-muted)" }}
          />
          <input
            id="password"
            type={showPassword ? "text" : "password"}
            autoComplete="current-password"
            required
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

        <button
          type="submit"
          disabled={loading}
          className="mt-1 h-11 rounded-xl font-semibold text-sm transition-all duration-200 hover:opacity-90 disabled:opacity-50 active:scale-[0.98]"
          style={{ background: "var(--accent)", color: "var(--accent-fg)" }}
        >
          {loading ? "Signing in\u2026" : "Sign in"}
        </button>
      </form>
    </AuthLayout>
  );
}

function Field({
  label,
  htmlFor,
  extra,
  children,
}: {
  label: string;
  htmlFor: string;
  extra?: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <div className="flex items-center justify-between">
        <label className="text-sm font-medium" style={{ color: "var(--text-muted)" }} htmlFor={htmlFor}>
          {label}
        </label>
        {extra}
      </div>
      <div className="relative">{children}</div>
    </div>
  );
}
