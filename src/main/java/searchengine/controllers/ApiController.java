package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final IndexingService indexingService;
    private final SearchService searchService;
    private final StatisticsService statisticsService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing() {
        boolean started = indexingService.startIndexing();
        if (!started) {
            return ResponseEntity.ok(
                    java.util.Map.of("result", false, "error", "Индексация уже запущена")
            );
        }
        return ResponseEntity.ok(java.util.Map.of("result", true));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing() {
        boolean stopped = indexingService.stopIndexing();
        return ResponseEntity.ok(java.util.Map.of("result", stopped));
    }

    @PostMapping("/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam String url) {
        boolean ok = indexingService.indexPage(url);
        if (!ok) {
            return ResponseEntity.ok(java.util.Map.of("result", false, "error", "Страница не найдена"));
        }
        return ResponseEntity.ok(java.util.Map.of("result", true));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        SearchResponse response = searchService.search(query, site, offset, limit);
        return ResponseEntity.ok(response);
    }
}
