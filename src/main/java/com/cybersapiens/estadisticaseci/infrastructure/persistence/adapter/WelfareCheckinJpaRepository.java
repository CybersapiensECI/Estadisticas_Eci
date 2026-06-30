package com.cybersapiens.estadisticaseci.infrastructure.persistence.adapter;

import com.cybersapiens.estadisticaseci.infrastructure.persistence.entity.WelfareCheckinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WelfareCheckinJpaRepository extends JpaRepository<WelfareCheckinEntity, Long> {

    @Query("SELECT w FROM WelfareCheckinEntity w " +
           "WHERE w.checkinDate BETWEEN :from AND :to " +
           "AND (:program IS NULL OR w.academicProgram = :program) " +
           "ORDER BY w.checkinDate")
    List<WelfareCheckinEntity> findByDateRange(@Param("from") LocalDate from,
                                               @Param("to") LocalDate to,
                                               @Param("program") String program);
}
