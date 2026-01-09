package com.ash.main.rpository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ash.main.entity.CityEntity;
import com.ash.main.entity.UserDetailsEntity;


@Repository

public interface RepositoryO extends JpaRepository<UserDetailsEntity, Long> {

	// Custom findBy method
	UserDetailsEntity findByMoNumber(String moNumber);
	boolean existsByMoNumber(String moNumber);
	List<UserDetailsEntity> findByCity(CityEntity city);

}
