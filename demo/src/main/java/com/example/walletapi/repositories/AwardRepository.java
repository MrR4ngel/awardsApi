package com.example.walletapi.repositories;

import com.example.walletapi.models.AwardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface AwardRepository extends JpaRepository<AwardEntity, Long> {
    
    List<AwardEntity> findByType(String type);
}
