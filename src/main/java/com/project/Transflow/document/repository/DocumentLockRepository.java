package com.project.Transflow.document.repository;

import com.project.Transflow.document.entity.DocumentLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentLockRepository extends JpaRepository<DocumentLock, Long> {
    // 단순 조회 (비관적 락 제거)
    @Query("SELECT dl FROM DocumentLock dl WHERE dl.document.id = :documentId")
    Optional<DocumentLock> findByDocumentId(@Param("documentId") Long documentId);
    
    // 락 상태 조회용 (LAZY 로딩 문제 해결)
    @Query("SELECT dl FROM DocumentLock dl " +
           "LEFT JOIN FETCH dl.lockedBy " +
           "LEFT JOIN FETCH dl.document " +
           "WHERE dl.document.id = :documentId")
    Optional<DocumentLock> findByDocumentIdWithUser(@Param("documentId") Long documentId);
    
    @Modifying
    @Query("DELETE FROM DocumentLock dl WHERE dl.document.id = :documentId")
    void deleteByDocumentId(@Param("documentId") Long documentId);
    
    boolean existsByDocumentId(Long documentId);
}

