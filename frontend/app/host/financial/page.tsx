"use client";

import { ArrowDownToLine, CreditCard, Wallet } from "lucide-react";
import { useEffect, useState } from "react";
import {
  AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from "recharts";
import { ProviderCard, ProviderShell, StatusBadge } from "@/components/provider/ProviderShell";
import { fetchFinancialData } from "@/lib/host-api";
import type {
  ProviderSummaryItem, TimeSeriesItem, MonthlyRevenueItem, WeeklyRevenueItem,
  OccupancyRevenueItem, RevenueBreakdownItem, TransactionItem,
} from "@/lib/host-api";

const filters = ["Today", "This Week", "This Month", "This Year"];
const formatVND = (v: number) => `₫${(v / 1_000_000).toFixed(1)}M`;

export default function FinancialDashboardPage() {
  const [activeFilter, setActiveFilter] = useState("This Month");
  const [selectedTx, setSelectedTx] = useState<TransactionItem | null>(null);
  const [notice, setNotice] = useState("Showing this month's provider performance.");
  const [providerSummary, setProviderSummary] = useState<ProviderSummaryItem[]>([]);
  const [revenueTimeSeries, setRevenueTimeSeries] = useState<TimeSeriesItem[]>([]);
  const [monthlyRevenue, setMonthlyRevenue] = useState<MonthlyRevenueItem[]>([]);
  const [weeklyRevenue, setWeeklyRevenue] = useState<WeeklyRevenueItem[]>([]);
  const [occupancyRevenue, setOccupancyRevenue] = useState<OccupancyRevenueItem[]>([]);
  const [revenueBreakdown, setRevenueBreakdown] = useState<RevenueBreakdownItem[]>([]);
  const [transactions, setTransactions] = useState<TransactionItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchFinancialData().then((data) => {
      setProviderSummary(data.providerSummary);
      setRevenueTimeSeries(data.revenueTimeSeries);
      setMonthlyRevenue(data.monthlyRevenue);
      setWeeklyRevenue(data.weeklyRevenue);
      setOccupancyRevenue(data.occupancyRevenue);
      setRevenueBreakdown(data.revenueBreakdown);
      setTransactions(data.transactions);
      setLoading(false);
    });
  }, []);

  function handleWithdraw() {
    setNotice("Withdrawal request created for ₫7.2M. Payout status is now pending.");
  }

  return (
    <ProviderShell>
      <div className="flex flex-col gap-5">
        {/* Header */}
        <div className="flex min-w-0 flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div className="min-w-0">
            <p className="text-sm font-semibold" style={{ color: "var(--text-muted)" }}>Financial Dashboard</p>
            <h1 className="mt-1 text-2xl font-bold tracking-tight sm:text-4xl" style={{ color: "var(--text)" }}>Earnings overview</h1>
            <p className="mt-2 max-w-2xl text-sm leading-6" style={{ color: "var(--text-muted)" }}>Monitor revenue, payouts, and charging business trends.</p>
          </div>
          <div className="grid w-full grid-cols-4 gap-1 rounded-2xl p-1 sm:w-auto sm:gap-2 sm:bg-transparent sm:p-0"
            style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
            {filters.map((filter) => (
              <button
                key={filter}
                type="button"
                onClick={() => { setActiveFilter(filter); setNotice(`Revenue analytics updated for ${filter.toLowerCase()}.`); }}
                className="h-9 rounded-xl px-1 text-[11px] font-semibold transition-all duration-200 active:scale-95 sm:h-11 sm:rounded-full sm:px-4 sm:text-sm"
                style={{
                  background: activeFilter === filter ? "var(--accent)" : "transparent",
                  color: activeFilter === filter ? "var(--accent-fg)" : "var(--text-muted)",
                }}
              >
                <span className="sm:hidden">{filter.replace("This ", "").replace("Today", "Day")}</span>
                <span className="hidden sm:inline">{filter}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Notice */}
        <div className="rounded-lg px-4 py-3 text-sm font-semibold"
          style={{ background: "color-mix(in srgb, var(--text-muted) 8%, transparent)", border: "1px solid color-mix(in srgb, var(--text-muted) 20%, transparent)", color: "var(--text-muted)" }}>
          {notice}
        </div>

        {/* Summary cards */}
        <div className="grid grid-cols-2 gap-3 xl:grid-cols-4">
          {providerSummary.map((item) => (
            <ProviderCard key={item.label}>
              <p className="text-xs leading-4 sm:text-sm" style={{ color: "var(--text-muted)" }}>{item.label}</p>
              <div className="mt-3 flex items-end justify-between gap-3">
                <p className="text-xl font-bold tracking-tight sm:text-2xl" style={{ color: "var(--text)" }}>{item.value}</p>
                <StatusBadge tone={
                  item.delta === "Ready" ? "orange"
                  : item.delta.includes("%") ? "green"
                  : "slate"
                }>{item.delta}</StatusBadge>
              </div>
            </ProviderCard>
          ))}
        </div>

        {/* Charts row */}
        <div className="grid gap-5 xl:grid-cols-[1.5fr_1fr]">
          {/* Revenue Area Chart */}
          <ProviderCard>
            <div className="mb-5 flex items-center justify-between">
              <div>
                <h2 className="font-bold" style={{ color: "var(--text)" }}>Revenue Analytics</h2>
                <p className="text-sm" style={{ color: "var(--text-muted)" }}>Daily earnings trend</p>
              </div>
            </div>
            <div className="h-52 sm:h-64">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={revenueTimeSeries} margin={{ top: 4, right: 4, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(120,113,108,0.08)" />
                  <XAxis dataKey="day" tick={{ fill: "var(--text-muted)", fontSize: 10 }} tickLine={false} interval={4} />
                  <YAxis tick={{ fill: "var(--text-muted)", fontSize: 10 }} tickLine={false} tickFormatter={v => `${(v / 1_000_000).toFixed(0)}M`} />
                  <Tooltip
                    contentStyle={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", borderRadius: 12, color: "var(--text)" }}
                    formatter={(v) => [formatVND(Number(v)), "Revenue"]}
                  />
                  <Area type="monotone" dataKey="revenue" stroke="#a78bfa" strokeWidth={2.5} fill="rgba(167,139,250,0.04)" dot={false} activeDot={{ r: 4, strokeWidth: 0, fill: "#f472b6" }} />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </ProviderCard>

          {/* Weekly Revenue Bar Chart */}
          <ProviderCard>
            <div className="mb-5 flex items-center justify-between">
              <div>
                <h2 className="font-bold" style={{ color: "var(--text)" }}>Weekly Revenue</h2>
                <p className="text-sm" style={{ color: "var(--text-muted)" }}>Bar chart by day</p>
              </div>
            </div>
            <div className="h-52 sm:h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={weeklyRevenue} margin={{ top: 4, right: 4, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(120,113,108,0.08)" />
                  <XAxis dataKey="day" tick={{ fill: "var(--text-muted)", fontSize: 11 }} tickLine={false} />
                  <YAxis tick={{ fill: "var(--text-muted)", fontSize: 10 }} tickLine={false} tickFormatter={v => `${(v / 1_000_000).toFixed(0)}M`} />
                  <Tooltip
                    contentStyle={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", borderRadius: 12, color: "var(--text)" }}
                    formatter={(v) => [formatVND(Number(v)), "Revenue"]}
                  />
                  <Bar dataKey="revenue" radius={[8, 8, 0, 0]} fill="rgba(192,132,252,0.06)" stroke="#c084fc" strokeWidth={2} maxBarSize={40} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </ProviderCard>
        </div>

        {/* Second row */}
        <div className="grid gap-5 xl:grid-cols-2">
          {/* Occupancy vs Revenue Composed Chart */}
          <ProviderCard>
            <h2 className="font-bold" style={{ color: "var(--text)" }}>Occupancy vs Revenue</h2>
            <div className="mt-5 h-52 sm:h-56">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={occupancyRevenue} margin={{ top: 4, right: 4, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(120,113,108,0.08)" />
                  <XAxis dataKey="day" tick={{ fill: "var(--text-muted)", fontSize: 11 }} tickLine={false} />
                  <YAxis tick={{ fill: "var(--text-muted)", fontSize: 10 }} tickLine={false} />
                  <Tooltip
                    contentStyle={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", borderRadius: 12, color: "var(--text)" }}
                  />
                  <Bar dataKey="occupancy" radius={[6, 6, 0, 0]} fill="rgba(167,139,250,0.06)" stroke="#a78bfa" strokeWidth={2} maxBarSize={30} />
                  <Bar dataKey="revenue" radius={[6, 6, 0, 0]} fill="rgba(45,212,191,0.06)" stroke="#2dd4bf" strokeWidth={2} maxBarSize={30} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </ProviderCard>

          {/* Revenue Breakdown Pie */}
          <ProviderCard>
            <h2 className="font-bold" style={{ color: "var(--text)" }}>Revenue Breakdown</h2>
            <div className="mt-2 h-52 sm:h-56 flex items-center">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={revenueBreakdown} cx="50%" cy="50%" innerRadius={50} outerRadius={80} dataKey="value" paddingAngle={3}>
                    {revenueBreakdown.map((entry, i) => (
                      <Cell key={i} fill={entry.fill} stroke={entry.color} strokeWidth={1.5} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", borderRadius: 12, color: "var(--text)" }}
                    formatter={(v) => [formatVND(Number(v)), ""]}
                  />
                </PieChart>
              </ResponsiveContainer>
              <div className="shrink-0 space-y-2 text-xs" style={{ color: "var(--text-muted)" }}>
                {revenueBreakdown.map((item) => (
                  <div key={item.name} className="flex items-center gap-2">
                    <div className="w-2.5 h-2.5 rounded-sm shrink-0" style={{ background: item.color }} />
                    <span>{item.name}</span>
                  </div>
                ))}
              </div>
            </div>
          </ProviderCard>
        </div>

        {/* Monthly Revenue + Payout */}
        <div className="grid gap-5 xl:grid-cols-[1.4fr_0.8fr]">
          <ProviderCard>
            <h2 className="font-bold" style={{ color: "var(--text)" }}>Monthly Revenue</h2>
            <div className="mt-4 h-44 sm:h-52">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={monthlyRevenue} margin={{ top: 4, right: 4, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(120,113,108,0.08)" />
                  <XAxis dataKey="month" tick={{ fill: "var(--text-muted)", fontSize: 10 }} tickLine={false} />
                  <YAxis tick={{ fill: "var(--text-muted)", fontSize: 10 }} tickLine={false} tickFormatter={v => `${(v / 1_000_000).toFixed(0)}M`} />
                  <Tooltip
                    contentStyle={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", borderRadius: 12, color: "var(--text)" }}
                    formatter={(v) => [formatVND(Number(v)), "Revenue"]}
                  />
                  <Area type="monotone" dataKey="revenue" stroke="#a78bfa" strokeWidth={2.5} fill="rgba(167,139,250,0.04)" dot={{ r: 4, fill: "#f472b6" }} />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </ProviderCard>

          <ProviderCard>
            <Wallet size={20} style={{ color: "var(--text-muted)" }} />
            <h2 className="mt-4 font-bold" style={{ color: "var(--text)" }}>Payout</h2>
            <p className="mt-1 text-sm" style={{ color: "var(--text-muted)" }}>Available Balance</p>
            <p className="mt-3 text-4xl font-bold" style={{ color: "var(--text)" }}>₫7.2M</p>
            <div className="mt-5 rounded-3xl p-4" style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
              <p className="text-xs font-semibold uppercase tracking-wide" style={{ color: "color-mix(in srgb, var(--text-muted) 60%, transparent)" }}>Connected Bank</p>
              <p className="mt-1 font-semibold" style={{ color: "var(--text)" }}>Vietcombank •••• 8821</p>
            </div>
            <button
              type="button"
              onClick={handleWithdraw}
              className="mt-5 flex h-12 w-full items-center justify-center gap-2 rounded-lg font-bold transition-all duration-200 hover:opacity-90 active:scale-[0.98]"
              style={{ background: "var(--accent)", color: "var(--accent-fg)" }}
            >
              <ArrowDownToLine size={18} /> Withdraw
            </button>
          </ProviderCard>
        </div>

        {/* Transactions */}
        <div className="grid gap-5 xl:grid-cols-[1.4fr_0.8fr]">
          <ProviderCard>
            <h2 className="font-bold" style={{ color: "var(--text)" }}>Transaction History</h2>
            <div className="mt-4 space-y-3 lg:hidden">
              {transactions.map((tx, i) => (
                <TransactionCard key={`${tx.date}-${tx.driver}-${i}`} tx={tx} onSelect={() => { setSelectedTx(tx); setNotice(`Selected ${tx.driver}'s ${tx.amount} transaction.`); }} />
              ))}
            </div>
            <div className="mt-4 hidden overflow-x-auto rounded-3xl lg:block"
              style={{ border: "1px solid color-mix(in srgb, var(--glass-border) 50%, transparent)" }}>
              <table className="min-w-[720px] w-full text-left text-sm">
                <thead>
                  <tr style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)", color: "var(--text-muted)" }}>
                    {["Date", "Driver", "Vehicle", "Duration", "Amount", "Status"].map((h) => <th key={h} className="px-4 py-3 font-semibold">{h}</th>)}
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((tx, i) => (
                    <tr
                      key={`${tx.date}-${tx.driver}-${i}`}
                      onClick={() => { setSelectedTx(tx); setNotice(`Selected ${tx.driver}'s ${tx.amount} transaction.`); }}
                      className="cursor-pointer transition-all duration-200"
                      style={{ borderTop: "1px solid color-mix(in srgb, var(--glass-border) 40%, transparent)", color: "var(--text)" }}
                    >
                      <td className="px-4 py-4">{tx.date}</td>
                      <td className="px-4 py-4 font-semibold">{tx.driver}</td>
                      <td className="px-4 py-4" style={{ color: "var(--text-muted)" }}>{tx.vehicle}</td>
                      <td className="px-4 py-4" style={{ color: "var(--text-muted)" }}>{tx.duration}</td>
                      <td className="px-4 py-4 font-bold">{tx.amount}</td>
                      <td className="px-4 py-4"><StatusBadge tone={tx.status === "Paid" ? "green" : "orange"}>{tx.status}</StatusBadge></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </ProviderCard>

          {/* Transaction detail */}
          {selectedTx && (
            <div className="fixed inset-x-3 bottom-[88px] z-50 max-h-[62dvh] overflow-y-auto rounded-2xl p-4 shadow-2xl lg:left-auto lg:right-6 lg:w-96"
              style={{ background: "var(--glass-bg)", border: "1px solid var(--glass-border)", backdropFilter: "blur(14px)" }}>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-sm font-semibold" style={{ color: "var(--text-muted)" }}>Transaction selected</p>
                  <h3 className="mt-1 text-lg font-bold" style={{ color: "var(--text)" }}>{selectedTx.driver}</h3>
                </div>
                <button type="button" onClick={() => setSelectedTx(null)}
                  className="rounded-lg px-3 py-1 text-sm font-bold transition-opacity hover:opacity-80"
                  style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)", color: "var(--text-muted)" }}>
                  Close
                </button>
              </div>
              <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
                <Info label="Date" value={selectedTx.date} />
                <Info label="Amount" value={selectedTx.amount} />
                <Info label="Vehicle" value={selectedTx.vehicle} />
                <Info label="Duration" value={selectedTx.duration} />
              </div>
            </div>
          )}
        </div>
      </div>
    </ProviderShell>
  );
}

function TransactionCard({ tx, onSelect }: { tx: TransactionItem; onSelect: () => void }) {
  return (
    <button type="button" onClick={onSelect} className="w-full rounded-xl p-4 text-left transition-all duration-200 active:scale-[0.99]"
      style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="font-bold" style={{ color: "var(--text)" }}>{tx.driver}</p>
          <p className="text-sm" style={{ color: "var(--text-muted)" }}>{tx.date} · {tx.vehicle}</p>
        </div>
        <StatusBadge tone={tx.status === "Paid" ? "green" : "orange"}>{tx.status}</StatusBadge>
      </div>
      <div className="mt-4 flex items-center justify-between">
        <span className="flex items-center gap-2 text-sm" style={{ color: "var(--text-muted)" }}><CreditCard size={15} /> {tx.duration}</span>
        <span className="font-bold" style={{ color: "var(--text)" }}>{tx.amount}</span>
      </div>
    </button>
  );
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg p-3" style={{ background: "color-mix(in srgb, var(--glass-bg) 60%, transparent)" }}>
      <p className="text-xs" style={{ color: "var(--text-muted)" }}>{label}</p>
      <p className="mt-1 font-bold" style={{ color: "var(--text)" }}>{value}</p>
    </div>
  );
}
