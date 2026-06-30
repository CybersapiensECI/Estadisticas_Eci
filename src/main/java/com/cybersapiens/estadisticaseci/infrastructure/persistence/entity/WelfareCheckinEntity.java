package com.cybersapiens.estadisticaseci.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "welfare_checkins")
public class WelfareCheckinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "intervention_type")
    private String interventionType;

    @Column(name = "academic_program")
    private String academicProgram;

    protected WelfareCheckinEntity() {
    }

    public WelfareCheckinEntity(String userId, LocalDate checkinDate, String interventionType, String academicProgram) {
        this.userId = userId;
        this.checkinDate = checkinDate;
        this.interventionType = interventionType;
        this.academicProgram = academicProgram;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDate getCheckinDate() {
        return checkinDate;
    }

    public String getInterventionType() {
        return interventionType;
    }

    public String getAcademicProgram() {
        return academicProgram;
    }
}
