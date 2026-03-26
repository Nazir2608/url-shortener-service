package com.nazir.urlshortener.repository;

import com.nazir.urlshortener.domain.UrlGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UrlGroupRepository extends JpaRepository<UrlGroup, UUID> {

    List<UrlGroup> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
