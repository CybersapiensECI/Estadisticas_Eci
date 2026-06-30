package com.cybersapiens.estadisticaseci.infrastructure.persistence.adapter;

import com.cybersapiens.estadisticaseci.infrastructure.persistence.entity.ActivityRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ActivityRecordJpaRepository extends JpaRepository<ActivityRecordEntity, Long> {

    @Query("SELECT COUNT(DISTINCT a.userId) FROM ActivityRecordEntity a " +
           "WHERE a.activityType IN ('CONNECTION', 'PATCH') " +
           "AND a.createdAt BETWEEN :from AND :to " +
           "AND (:program IS NULL OR a.academicProgram = :program)")
    long countDistinctUsersWithActivity(@Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to,
                                        @Param("program") String program);

    @Query("SELECT COUNT(DISTINCT a.userId) FROM ActivityRecordEntity a " +
           "WHERE a.createdAt BETWEEN :from AND :to " +
           "AND (:program IS NULL OR a.academicProgram = :program)")
    long countDistinctUsers(@Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to,
                            @Param("program") String program);

}
