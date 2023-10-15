package ru.practicum.compilation.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.compilation.model.Compilation;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @EntityGraph(value = "compilation-with-events")
    List<Compilation> findAllByPinned(Boolean pinned, PageRequest page);

    @EntityGraph(value = "compilation-with-events")
    Optional<Compilation> findById(Long compId);
}