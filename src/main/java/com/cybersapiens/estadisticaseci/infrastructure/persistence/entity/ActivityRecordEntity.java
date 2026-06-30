package com.cybersapiens.estadisticaseci.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_records")
public class ActivityRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "activity_type", nullable = false)
    private String activityType;

    @Column(name = "academic_program")
    private String academicProgram;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ActivityRecordEntity() {
    }

    public ActivityRecordEntity(String userId, String activityType, String academicProgram, LocalDateTime createdAt) {
        this.userId = userId;
        this.activityType = activityType;
        this.academicProgram = academicProgram;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getActivityType() {
        return activityType;
    }

    public String getAcademicProgram() {
        return academicProgram;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
