package com.cybersapiens.estadisticaseci.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "mentorships")
public class MentorshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mentor_id", nullable = false)
    private String mentorId;

    @Column(name = "mentee_id", nullable = false)
    private String menteeId;

    @Column(name = "program_code")
    private String programCode;

    @Column(name = "program_name")
    private String programName;

    @Column(name = "started_at", nullable = false)
    private LocalDate startedAt;

    protected MentorshipEntity() {
    }

    public MentorshipEntity(String mentorId, String menteeId, String programCode,
                            String programName, LocalDate startedAt) {
        this.mentorId = mentorId;
        this.menteeId = menteeId;
        this.programCode = programCode;
        this.programName = programName;
        this.startedAt = startedAt;
    }

    public Long getId() {
        return id;
    }

    public String getMentorId() {
        return mentorId;
    }

    public String getMenteeId() {
        return menteeId;
    }

    public String getProgramCode() {
        return programCode;
    }

    public String getProgramName() {
        return programName;
    }

    public LocalDate getStartedAt() {
        return startedAt;
    }
}
