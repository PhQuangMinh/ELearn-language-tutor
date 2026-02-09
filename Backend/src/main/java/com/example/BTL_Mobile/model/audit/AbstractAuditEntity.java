package com.example.BTL_Mobile.model.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(CustomAuditingEntityListener.class)
public abstract class AbstractAuditEntity implements Serializable {

    @CreationTimestamp
    @Column(name = "created_at")
    protected LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by")
    protected Integer createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    protected Integer updatedBy;

}
