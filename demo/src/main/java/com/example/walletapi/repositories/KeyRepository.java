package com.example.walletapi.repositories;

import com.example.walletapi.models.KeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface KeyRepository extends JpaRepository<KeyEntity, Long> {
    
    Optional<KeyEntity> findByIdPerson(Long idPerson);
}
