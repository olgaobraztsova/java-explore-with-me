package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats.model.EndPointHit;
import ru.practicum.stats.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndPointHit, Long> {

    @Query("select new ru.practicum.stats.model.ViewStats (eph.app, eph.uri, count(eph.ip))" +
            "from EndPointHit eph " +
            "where (eph.timestamp between :start and :end) " +
            "group by eph.app, eph.uri " +
            "order by count(eph.ip) desc")
    List<ViewStats> findEndPointHitsByStartAndEnd(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.stats.model.ViewStats (app, uri, count(distinct ip) as hits) " +
            "from EndPointHit " +
            "where (timestamp between ?1 and ?2) " +
            "group by app, uri " +
            "order by hits desc")
    List<ViewStats> findHitsByStartAndEndUnique(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.stats.model.ViewStats (app, uri, count(distinct ip) as hits) " +
            "from EndPointHit " +
            "where (timestamp between ?1 and ?2) " +
            "and uri IN ?3 " +
            "group by app, uri " +
            "order by hits desc")
    List<ViewStats> findHitsByStartAndEndAndByUrisUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.stats.model.ViewStats (eph.app, eph.uri, count(eph.ip))" +
            "from EndPointHit eph " +
            "where (eph.timestamp between :start and :end) " +
            "and eph.uri IN (:uris) " +
            "group by eph.app, eph.uri " +
            "order by count(eph.ip) desc")
    List<ViewStats> findHitsByStartAndEndAndByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

}
