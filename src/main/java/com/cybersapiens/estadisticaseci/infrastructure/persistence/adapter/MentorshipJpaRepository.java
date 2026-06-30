package com.cybersapiens.estadisticaseci.infrastructure.persistence.adapter;

import com.cybersapiens.estadisticaseci.domain.model.MentorshipByProgram;
import com.cybersapiens.estadisticaseci.infrastructure.persistence.entity.MentorshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MentorshipJpaRepository extends JpaRepository<MentorshipEntity, Long> {

    @Query("SELECT new com.cybersapiens.estadisticaseci.domain.model.MentorshipByProgram(" +
           "m.programCode, m.programName, COUNT(m)) FROM MentorshipEntity m " +
           "WHERE m.startedAt BETWEEN :from AND :to " +
           "AND (:program IS NULL OR m.programCode = :program) " +
           "GROUP BY m.programCode, m.programName " +
           "ORDER BY COUNT(m) DESC")
    List<MentorshipByProgram> countByProgramGrouped(@Param("from") LocalDate from,
                                                    @Param("to") LocalDate to,
                                                    @Param("program") String program);
}
