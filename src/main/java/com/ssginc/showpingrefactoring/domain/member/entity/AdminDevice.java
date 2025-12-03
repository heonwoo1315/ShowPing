package com.ssginc.showpingrefactoring.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "admin_device",
        indexes = {
                @Index(name = "idx_admin_device_member_status", columnList = "member_no, status"),
                @Index(name = "uid_admin_device_credential", columnList = "credential_id", unique = true)
        }
)
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class AdminDevice {
    /** Hibernate 6+는 UUID를 BINARY(16)로 자동 매핑합니다.
     *  (만약 Hibernate 5.x이면 @Type(type="uuid-binary") 추가를 고려하세요) */
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_no", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private AdminDeviceType type; // WEB_AUTHN | MTLS | ATTEST ...

    /** WebAuthn Credential ID (allowCredentials 식별자로 사용) */
    @Lob
    @Column(name = "credential_id", nullable = false, unique = true)
    private byte[] credentialId;

    /** WebAuthn COSE Public Key */
    @Lob
    @Column(name = "public_key_cose", nullable = false)
    private byte[] publicKeyCose;

    /** WebAuthn Authenticator AAGUID (없으면 null 허용) */
    @Lob
    @Column(name = "aaguid")
    private byte[] aaguid;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AdminDeviceStatus status; // PENDING | APPROVED | REVOKED

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = AdminDeviceStatus.PENDING;
        if (type == null) type = AdminDeviceType.WEB_AUTHN;
    }
}
