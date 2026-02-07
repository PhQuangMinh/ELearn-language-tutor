package com.example.BTL_Mobile.model;

import com.example.BTL_Mobile.model.audit.AbstractAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dictionary_words", schema = "btl_mobile")
public class DictionaryWord extends AbstractAuditEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "word", nullable = false, length = 50)
    private String word;

    @Size(max = 50)
    @NotNull
    @Column(name = "pronunciation", nullable = false, length = 50)
    private String pronunciation;

    @Size(max = 255)
    @NotNull
    @Column(name = "meaning", nullable = false)
    private String meaning;

}