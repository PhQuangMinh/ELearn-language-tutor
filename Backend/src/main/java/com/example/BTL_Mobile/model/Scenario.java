package com.example.BTL_Mobile.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "scenarios", schema = "btl_mobile")
public class Scenario {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 500)
    @NotNull
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Size(max = 50)
    @NotNull
    @Column(name = "ai_role", nullable = false, length = 50)
    private String aiRole;

    @Size(max = 50)
    @NotNull
    @Column(name = "user_role", nullable = false, length = 50)
    private String userRole;

}