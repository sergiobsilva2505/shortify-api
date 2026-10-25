package br.com.forjacode.shortifyapi.adapter.out.persistence;

import br.com.forjacode.shortifyapi.application.port.out.ShortUrlRepositoryPort;
import br.com.forjacode.shortifyapi.domain.exception.ShortCodeAlreadyExistsException;
import br.com.forjacode.shortifyapi.domain.exception.ShortUrlNotFoundException;
import br.com.forjacode.shortifyapi.domain.model.ShortUrl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ShortUrlPersistenceAdapter implements ShortUrlRepositoryPort {

    private final ShortUrlJpaRepository repository;
    private final ShortUrlPersistenceMapper mapper;

    public ShortUrlPersistenceAdapter(ShortUrlJpaRepository repository, ShortUrlPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ShortUrl save(ShortUrl shortUrl) {
        if (shortUrl.getId() != null) {
            return update(shortUrl);
        }
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(shortUrl)));
        } catch (DataIntegrityViolationException e) {
            throw new ShortCodeAlreadyExistsException(shortUrl.getShortCode());
        }
    }

    // Altera só a URL e o updatedAt na entidade gerenciada, para não sobrescrever um accessCount
    // incrementado concorrentemente com o valor (possivelmente defasado) do objeto de domínio.
    private ShortUrl update(ShortUrl shortUrl) {
        ShortUrlJpaEntity entity = repository.findById(shortUrl.getId())
                .orElseThrow(() -> new ShortUrlNotFoundException(shortUrl.getShortCode()));
        entity.updateOriginalUrl(shortUrl.getOriginalUrl(), shortUrl.getUpdatedAt());
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return repository.findByShortCode(shortCode).map(mapper::toDomain);
    }

    @Override
    public boolean existsByShortCode(String shortCode) {
        return repository.existsByShortCode(shortCode);
    }

    @Override
    public Optional<Long> findAccessCountByShortCode(String shortCode) {
        return repository.findAccessCountByShortCode(shortCode);
    }

    @Override
    public int incrementAccessCount(Long id) {
        return repository.incrementAccessCount(id);
    }

    @Override
    public int deleteByShortCode(String shortCode) {
        return repository.deleteByShortCode(shortCode);
    }
}
