package com.example.BTL_Mobile.model;

import com.example.BTL_Mobile.model.audit.AbstractAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_device_tokens", schema = "btl_mobile")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceToken extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "fcm_token", nullable = false, unique = true, length = 512)
    private String fcmToken;

    @NotNull
    @Column(name = "platform", nullable = false, length = 30)
    @Builder.Default
    private String platform = "ANDROID";

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @NotNull
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;
}
