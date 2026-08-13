package com.horabase.api.terminal;

import com.horabase.api.business.Business;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "terminal_devices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_terminal_identifier",
                columnNames = "identifier"
        )
)
public class TerminalDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "business_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_terminal_business")
    )
    private Business business;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 80)
    private String identifier;

    @Column(name = "secret_hash", nullable = false, length = 100)
    private String secretHash;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_seen_at")
    private OffsetDateTime lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSecretHash() {
        return secretHash;
    }

    public void setSecretHash(String secretHash) {
        this.secretHash = secretHash;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(OffsetDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
