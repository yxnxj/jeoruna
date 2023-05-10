package com.prography1.eruna.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public abstract class BaseTimeEntity {

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime modifiedDate;

}