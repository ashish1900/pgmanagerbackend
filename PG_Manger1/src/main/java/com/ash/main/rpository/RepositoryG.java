package com.ash.main.rpository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ash.main.entity.GuestDetailEntity;

@Repository
public interface RepositoryG extends JpaRepository<GuestDetailEntity, Long> {

	GuestDetailEntity findByMoNumber(String moNumber);
	boolean existsByMoNumber(String moNumber);
	
}
