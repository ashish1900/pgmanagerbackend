package com.ash.main.rpository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ash.main.entity.CityEntity;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, Long> {
    Optional<CityEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
	CityEntity findByName(String name);
}
