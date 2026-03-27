package com.nazir.urlshortener.service;

import com.nazir.urlshortener.exception.InvalidUrlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

/**
 * Validates URLs before shortening.
 * <p>
 * Checks performed:
 * <ol>
 *   <li>Not blank</li>
 *   <li>Valid URL format (parseable)</li>
 *   <li>Allowed scheme (http or https)</li>
 *   <li>Has a valid host</li>
 *   <li>Not exceeding max length</li>
 *   <li>Not a blocked/dangerous domain (extensible)</li>
 * </ol>
 */
@Service
public class UrlValidationService {

    private static final Logger log = LoggerFactory.getLogger(UrlValidationService.class);

    private static final int MAX_URL_LENGTH = 2048;

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    /**
     * Domains that should not be shortened (prevent redirect loops, abuse).
     */
    private static final Set<String> BLOCKED_HOSTS = Set.of(
        "localhost",
        "127.0.0.1",
        "0.0.0.0",
        "::1"
    );

    /**
     * Validate and normalize a URL.
     *
     * @param rawUrl the URL string to validate
     * @return the validated, normalized URL string
     * @throws InvalidUrlException if validation fails
     */
    public String validateAndNormalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new InvalidUrlException("(blank)", "URL must not be blank");
        }

        String url = rawUrl.trim();

        // Auto-prepend https:// if no scheme provided
        if (!url.contains("://")) {
            url = "https://" + url;
            log.debug("Auto-prepended https:// to URL: {}", url);
        }

        // Check length
        if (url.length() > MAX_URL_LENGTH) {
            throw new InvalidUrlException(url, "URL exceeds maximum length of " + MAX_URL_LENGTH);
        }

        // Parse and validate
        URL parsedUrl;
        try {
            URI uri = new URI(url);
            parsedUrl = uri.toURL();
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            throw new InvalidUrlException(url, "Malformed URL: " + e.getMessage());
        }

        // Check scheme
        String scheme = parsedUrl.getProtocol().toLowerCase();
        if (!ALLOWED_SCHEMES.contains(scheme)) {
            throw new InvalidUrlException(url, "Only HTTP and HTTPS URLs are allowed, got: " + scheme);
        }

        // Check host
        String host = parsedUrl.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidUrlException(url, "URL must have a valid host");
        }

        if (BLOCKED_HOSTS.contains(host.toLowerCase())) {
            throw new InvalidUrlException(url, "URLs pointing to '%s' are not allowed".formatted(host));
        }

        // Block private IP ranges (basic check)
        if (isPrivateIp(host)) {
            throw new InvalidUrlException(url, "URLs pointing to private IP addresses are not allowed");
        }

        log.debug("URL validated successfully: {}", url);
        return url;
    }

    /**
     * Basic check for private/internal IP addresses.
     * Not exhaustive — covers most common cases.
     */
    private boolean isPrivateIp(String host) {
        return host.startsWith("10.")
            || host.startsWith("172.16.") || host.startsWith("172.17.")
            || host.startsWith("172.18.") || host.startsWith("172.19.")
            || host.startsWith("172.2") // covers 172.20-172.29
            || host.startsWith("172.30.") || host.startsWith("172.31.")
            || host.startsWith("192.168.");
    }
}
