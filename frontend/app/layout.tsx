import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({ subsets: ["latin"], variable: "--font-sans" });

export const metadata: Metadata = {
  title: "Volzen — EV Charging, Anytime",
  description: "Find homeowner charging slots near you. Airbnb for EV charging.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      {/* ponytail: inline script prevents flash of wrong theme before React hydrates */}
      <head>
        <script
          dangerouslySetInnerHTML={{
            __html: `(function(){var t=localStorage.getItem('volzen-theme')||'dark';document.documentElement.setAttribute('data-theme',t);})();`,
          }}
        />
      </head>
      <body
        className={inter.variable}
        style={{ background: "var(--bg)", color: "var(--text)", minHeight: "100dvh" }}
      >
        {children}
      </body>
    </html>
  );
}
