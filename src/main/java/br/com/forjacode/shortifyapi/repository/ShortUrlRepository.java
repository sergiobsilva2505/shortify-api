package br.com.forjacode.shortifyapi.repository;

import br.com.forjacode.shortifyapi.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Modifying(clearAutomatically = true)
    @Query("update ShortUrl s set s.accessCount = s.accessCount + 1 where s.id = :id")
    void incrementAccessCount(@Param("id") Long id);
}
