package com.nazir.urlshortener.controller;

import com.nazir.urlshortener.event.ClickEventPayload;
import com.nazir.urlshortener.event.ClickEventPublisher;
import com.nazir.urlshortener.service.RedirectService;
import com.nazir.urlshortener.util.IpAddressExtractor;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;

@RestController
@Hidden  // hide from Swagger — this is the public redirect endpoint
public class RedirectController {

    private final RedirectService redirectService;
    private final ClickEventPublisher clickEventPublisher;

    public RedirectController(RedirectService redirectService,
                              ClickEventPublisher clickEventPublisher) {
        this.redirectService = redirectService;
        this.clickEventPublisher = clickEventPublisher;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Void> redirect(@PathVariable String slug,
                                         HttpServletRequest request) {

        // 1. Resolve slug → original URL (Redis cache → DB fallback)
        RedirectService.ResolvedUrl resolved = redirectService.resolve(slug);

        // 2. Fire async click event (non-blocking — does NOT delay the 302)
        ClickEventPayload payload = new ClickEventPayload(
            resolved.id(),
            slug,
            Instant.now(),
            IpAddressExtractor.extract(request),
            request.getHeader("User-Agent"),
            request.getHeader("Referer"),          // note: HTTP spec misspells it
            request.getHeader("Accept-Language")
        );
        clickEventPublisher.publish(payload);

        // 3. Return 302 redirect immediately
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(resolved.originalUrl()));
        // Prevent browser caching so every click is tracked
        headers.set(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate");
        headers.set(HttpHeaders.PRAGMA, "no-cache");

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
