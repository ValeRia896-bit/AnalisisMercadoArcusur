package com.umss.sistemas.repository;

import com.umss.sistemas.model.TituladoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TituladoRepository extends JpaRepository<TituladoEntity, Long> {
}