package com.ash.main.rpository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ash.main.entity.NoticeEntity;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

    long countByPgIdAndActiveTrue(Long pgId);

    List<NoticeEntity> findByPgIdAndActiveTrueOrderByUpdatedAtDesc(Long pgId);
    
    
}

