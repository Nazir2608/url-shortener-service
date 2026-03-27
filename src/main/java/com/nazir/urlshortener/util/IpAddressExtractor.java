package com.nazir.urlshortener.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extracts the real client IP address from HTTP requests,
 * handling reverse proxies and load balancers.
 * <p>
 * Checks headers in priority order:
 * X-Forwarded-For → X-Real-IP → Remote-Addr
 */
public final class IpAddressExtractor {

    private static final String[] PROXY_HEADERS = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_CLIENT_IP"
    };

    private IpAddressExtractor() {
        // Utility class
    }

    /**
     * Extract client IP from request, handling proxy headers.
     *
     * @param request the HTTP request
     * @return client IP address string
     */
    public static String extract(HttpServletRequest request) {
        for (String header : PROXY_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For may contain comma-separated list: client, proxy1, proxy2
                // First IP is the real client
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
