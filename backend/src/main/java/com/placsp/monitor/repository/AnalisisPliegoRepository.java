package com.placsp.monitor.repository;

import com.placsp.monitor.model.AnalisisPliego;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalisisPliegoRepository extends JpaRepository<AnalisisPliego, Long> {

    Optional<AnalisisPliego> findByEntryId(String entryId);
}
