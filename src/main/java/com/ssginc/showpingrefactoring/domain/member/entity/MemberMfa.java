package com.ssginc.showpingrefactoring.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "member_mfa")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "memberNo")
public class MemberMfa {

    /** member 테이블 PK 값을 그대로 PK로 쓰되, @MapsId는 사용하지 않음 */
    @Id
    @Column(name = "member_no")
    private Long memberNo;

    /** 편의용 역참조(선택). PK 생성에는 관여하지 않도록 insertable/updatable=false */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no", insertable = false, updatable = false)
    private Member member;

    /** TOTP 시크릿(AES-GCM 암호화 바이트) */
    @Lob
    @Column(name = "secret_enc", nullable = false)
    private byte[] secretEnc;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @Column(name = "enrolled_at")
    private Instant enrolledAt;

    @Column(name = "last_verified_at")
    private Instant lastVerifiedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bound_device_id", columnDefinition = "BINARY(16)")
    private AdminDevice boundDevice;

    @PrePersist
    void prePersist() {
        if (enrolledAt == null) {
            enrolledAt = Instant.now();
        }
    }
}
