package com.placsp.monitor.repository;

import com.placsp.monitor.model.Licitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LicitacionRepository extends JpaRepository<Licitacion, String>, JpaSpecificationExecutor<Licitacion> {
}
