package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    @Override
    public SearchResponse search(String query, String site, int offset, int limit) {

        if (query == null || query.trim().isEmpty()) {
            return new SearchResponse(false, 0, new ArrayList<>(), "Пустой поисковый запрос");
        }

        // ⚠️ Заглушка логики поиска (на следующем шаге сделаем настоящую)
        List<SearchResult> results = new ArrayList<>();

        return new SearchResponse(true, results.size(), results, null);
    }
}
