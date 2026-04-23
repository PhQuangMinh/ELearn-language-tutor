package com.example.BTL_Mobile.model;

import com.example.BTL_Mobile.model.audit.AbstractAuditEntity;
import com.example.BTL_Mobile.model.enums.EMediaType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "media", schema = "btl_mobile")
public class Media extends AbstractAuditEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EMediaType type;

    @Size(max = 1000)
    @NotNull
    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @NotNull
    @Column(name = "size", nullable = false)
    private Integer size;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

}