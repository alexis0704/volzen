import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    const apiBaseUrl = process.env.API_BASE_URL ?? "http://localhost:8080";

    return [
      {
        source: "/api/v1/:path*",
        destination: `${apiBaseUrl}/api/v1/:path*`,
      },
      {
        source: "/api/ai",
        destination: `${apiBaseUrl}/api/ai`,
      },
      {
        source: "/api/ai/:path*",
        destination: `${apiBaseUrl}/api/ai/:path*`,
      },
    ];
  },
};

export default nextConfig;
