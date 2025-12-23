package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final IndexingService indexingService;
    private final SearchService searchService;

    @PostMapping("/startIndexing")
    public IndexingResponse startIndexing() {
        return indexingService.start();
    }

    @PostMapping("/stopIndexing")
    public IndexingResponse stopIndexing() {
        return indexingService.stop();
    }

    @GetMapping("/search")
    public SearchResponse search(
            @RequestParam String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return searchService.search(query, site, offset, limit);
    }
}
