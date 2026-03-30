package com.nazir.urlshortener.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extracts the real client IP address, handling reverse proxies and load balancers.
 *
 * Priority:
 *   1. X-Forwarded-For (first IP in comma-separated chain)
 *   2. X-Real-IP
 *   3. request.getRemoteAddr()
 */
public final class IpAddressExtractor {

    private static final String[] HEADER_CANDIDATES = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_REAL_IP"
    };

    private IpAddressExtractor() {
        // utility class — no instantiation
    }

    public static String extract(HttpServletRequest request) {
        for (String header : HEADER_CANDIDATES) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                // X-Forwarded-For: "client, proxy1, proxy2" → take first
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Returns true if the IP is private/loopback.
     * GeoIP databases have no data for these addresses.
     */
    public static boolean isPrivateIp(String ip) {
        if (ip == null) return true;
        return ip.startsWith("127.")
            || ip.startsWith("10.")
            || ip.startsWith("192.168.")
            || ip.startsWith("172.16.") || ip.startsWith("172.17.")
            || ip.startsWith("172.18.") || ip.startsWith("172.19.")
            || ip.startsWith("172.2")   || ip.startsWith("172.30.")
            || ip.startsWith("172.31.")
            || "0:0:0:0:0:0:0:1".equals(ip)
            || "::1".equals(ip)
            || "0.0.0.0".equals(ip);
    }
}
