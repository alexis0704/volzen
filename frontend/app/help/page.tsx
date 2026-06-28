"use client";

import Link from "next/link";
import { useState, useRef, useEffect } from "react";
import { Send, ArrowLeft, Bot, User } from "lucide-react";
import ThemeToggle from "@/components/ThemeToggle";

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "";

type Msg = { role: "user" | "assistant"; text: string };

const STARTERS = [
  "How do I find a charging spot near me?",
  "How does booking work?",
  "How do I become a host?",
  "What payment methods are accepted?",
];

export default function HelpPage() {
  const [messages, setMessages] = useState<Msg[]>([
    { role: "assistant", text: "Hi! I'm the VOLZEN assistant. Ask me anything about finding charging spots, booking, or hosting." },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  async function send(text: string) {
    if (!text.trim() || loading) return;
    const userMsg: Msg = { role: "user", text: text.trim() };
    setMessages((prev) => [...prev, userMsg]);
    setInput("");
    setLoading(true);

    try {
      const res = await fetch(`${API_BASE}/api/v1/advisor/chat`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: text.trim() }),
      });
      const json = await res.json();
      const reply = json?.answer ?? "Sorry, I couldn't get a response. Please try again.";
      setMessages((prev) => [...prev, { role: "assistant", text: reply }]);
    } catch {
      setMessages((prev) => [...prev, { role: "assistant", text: "Connection error. Please check that the server is running." }]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ background: "var(--bg)", color: "var(--text)", height: "100dvh", display: "flex", flexDirection: "column" }}>
      {/* Nav */}
      <header
        className="shrink-0"
        style={{
          borderBottom: "1px solid var(--glass-border)",
          background: "color-mix(in srgb, var(--bg) 80%, transparent)",
          backdropFilter: "blur(16px)",
        }}
      >
        <nav className="flex items-center justify-between px-6 py-4 max-w-3xl mx-auto w-full">
          <div className="flex items-center gap-3">
            <Link href="/" className="flex items-center gap-1.5 text-sm hover:opacity-70 transition-opacity" style={{ color: "var(--text-muted)" }}>
              <ArrowLeft size={16} />
              Back
            </Link>
            <div style={{ width: 1, height: 18, background: "var(--glass-border)" }} />
            <div className="flex items-center gap-2">
              <span className="font-bold" style={{ color: "var(--text)" }}>VOLZEN</span>
              <span className="text-xs px-2 py-0.5 rounded-full" style={{ background: "rgba(74,222,128,0.12)", color: "var(--accent)" }}>Help</span>
            </div>
          </div>
          <ThemeToggle />
        </nav>
      </header>

      {/* Chat area */}
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-3xl mx-auto px-4 py-6 flex flex-col gap-4">
          {messages.map((msg, i) => (
            <div key={i} className={`flex gap-3 ${msg.role === "user" ? "flex-row-reverse" : ""}`}>
              <div
                className="shrink-0 w-8 h-8 rounded-full flex items-center justify-center"
                style={{
                  background: msg.role === "assistant" ? "rgba(74,222,128,0.15)" : "var(--glass-bg)",
                  border: "1px solid var(--glass-border)",
                }}
              >
                {msg.role === "assistant"
                  ? <Bot size={14} style={{ color: "var(--accent)" }} />
                  : <User size={14} style={{ color: "var(--text-muted)" }} />}
              </div>
              <div
                className="max-w-[80%] px-4 py-3 rounded-2xl text-sm leading-relaxed"
                style={{
                  background: msg.role === "assistant" ? "var(--glass-bg)" : "rgba(74,222,128,0.12)",
                  border: "1px solid var(--glass-border)",
                  color: "var(--text)",
                  borderRadius: msg.role === "user" ? "18px 4px 18px 18px" : "4px 18px 18px 18px",
                }}
              >
                {msg.text}
              </div>
            </div>
          ))}

          {loading && (
            <div className="flex gap-3">
              <div
                className="shrink-0 w-8 h-8 rounded-full flex items-center justify-center"
                style={{ background: "rgba(74,222,128,0.15)", border: "1px solid var(--glass-border)" }}
              >
                <Bot size={14} style={{ color: "var(--accent)" }} />
              </div>
              <div
                className="px-4 py-3 rounded-2xl text-sm"
                style={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", borderRadius: "4px 18px 18px 18px" }}
              >
                <span style={{ color: "var(--text-muted)" }}>Thinking…</span>
              </div>
            </div>
          )}

          <div ref={bottomRef} />
        </div>
      </div>

      {/* Starter chips — only when only the greeting */}
      {messages.length === 1 && (
        <div className="shrink-0 px-4 pb-3 max-w-3xl mx-auto w-full">
          <div className="flex flex-wrap gap-2">
            {STARTERS.map((s) => (
              <button
                key={s}
                onClick={() => send(s)}
                className="text-xs px-3 py-1.5 rounded-full transition-opacity hover:opacity-80"
                style={{ border: "1px solid var(--glass-border)", color: "var(--text-muted)", background: "var(--glass-bg)" }}
              >
                {s}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Input */}
      <div
        className="shrink-0 px-4 py-4"
        style={{ borderTop: "1px solid var(--glass-border)", background: "color-mix(in srgb, var(--bg) 90%, transparent)", backdropFilter: "blur(12px)" }}
      >
        <form
          className="flex gap-2 max-w-3xl mx-auto"
          onSubmit={(e) => { e.preventDefault(); send(input); }}
        >
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask anything about VOLZEN…"
            disabled={loading}
            className="flex-1 text-sm px-4 py-3 rounded-2xl outline-none"
            style={{
              background: "var(--glass-bg)",
              border: "1px solid var(--glass-border)",
              color: "var(--text)",
            }}
          />
          <button
            type="submit"
            disabled={loading || !input.trim()}
            className="w-11 h-11 rounded-full flex items-center justify-center transition-opacity hover:opacity-80 disabled:opacity-40"
            style={{ background: "var(--accent)" }}
          >
            <Send size={16} style={{ color: "var(--accent-fg)" }} />
          </button>
        </form>
      </div>
    </div>
  );
}
