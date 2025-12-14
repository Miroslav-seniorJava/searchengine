package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    @Override
    public SearchResponse search(String query, String site, int offset, int limit) {

        if (query == null || query.isBlank()) {
            return new SearchResponse(
                    false,
                    0,
                    Collections.emptyList(),
                    "Задан пустой поисковый запрос"
            );
        }

        // TODO: здесь будет реальная логика поиска
        List<SearchResult> results = Collections.emptyList();

        return new SearchResponse(
                true,
                results.size(),
                results,
                null
        );
    }
}
