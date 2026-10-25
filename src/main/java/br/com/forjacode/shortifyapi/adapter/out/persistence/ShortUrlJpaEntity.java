package br.com.forjacode.shortifyapi.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;

@Entity
@DynamicUpdate
@Table(name = "short_url")
public class ShortUrlJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true)
    private String shortCode;

    @Column(name = "access_count", nullable = false)
    private Long accessCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ShortUrlJpaEntity() {
    }

    public ShortUrlJpaEntity(Long id, String originalUrl, String shortCode, Long accessCount,
                             Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.accessCount = accessCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateOriginalUrl(String originalUrl, Instant updatedAt) {
        this.originalUrl = originalUrl;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
