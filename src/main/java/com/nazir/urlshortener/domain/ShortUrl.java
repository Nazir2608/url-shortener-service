package com.nazir.urlshortener.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "short_urls")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String slug;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @ToString.Exclude
    private UrlGroup group;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "max_clicks")
    private Integer maxClicks;

    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private long clickCount = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── domain helpers ──

    /**
     * Checks if this URL has expired by time or click limit.
     */
    public boolean isExpired() {
        if (expiresAt != null && Instant.now().isAfter(Instant.from(expiresAt))) {
            return true;
        }
        return maxClicks != null && clickCount >= maxClicks;
    }

    public boolean hasReachedMaxClicks() {
        return maxClicks != null && clickCount >= maxClicks;
    }

    public boolean isAccessible() {
        return active && !isExpired() && !hasReachedMaxClicks();
    }

    public boolean isPasswordProtected() {
        return passwordHash != null && !passwordHash.isBlank();
    }

    public void incrementClickCount() {
        this.clickCount++;
    }
}
