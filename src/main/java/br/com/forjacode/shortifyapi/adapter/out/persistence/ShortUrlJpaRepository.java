package br.com.forjacode.shortifyapi.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShortUrlJpaRepository extends JpaRepository<ShortUrlJpaEntity, Long> {

    Optional<ShortUrlJpaEntity> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Query("select s.accessCount from ShortUrlJpaEntity s where s.shortCode = :shortCode")
    Optional<Long> findAccessCountByShortCode(@Param("shortCode") String shortCode);

    @Modifying(clearAutomatically = true)
    @Query("update ShortUrlJpaEntity s set s.accessCount = s.accessCount + 1 where s.id = :id")
    int incrementAccessCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("delete from ShortUrlJpaEntity s where s.shortCode = :shortCode")
    int deleteByShortCode(@Param("shortCode") String shortCode);
}
