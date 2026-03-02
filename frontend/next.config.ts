import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    devIndicators: false,
    experimental: {
        proxyClientMaxBodySize: 10 * 1024 * 1024, // 10MB
    },
};

export default nextConfig;
