package com.CaseTest.repository;

import com.CaseTest.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {

    List<Request> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

}
