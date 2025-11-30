package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final LemmatizerService lemmatizerService;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final PageRepository pageRepository;

    @Override
    public SearchResponse search(String query, String site, int offset, int limit) {

        SearchResponse response = new SearchResponse();
        response.setResult(true);

        if (query == null || query.isBlank()) {
            response.setResult(false);
            response.setError("Пустой поисковый запрос");
            return response;
        }

        Map<String, Integer> queryLemmas =
                lemmatizerService.collectLemmaFrequencies(query);

        List<Lemma> lemmas =
                lemmaRepository.findByLemmaIn(queryLemmas.keySet());

        if (lemmas.isEmpty()) {
            response.setCount(0);
            response.setData(List.of());
            return response;
        }

        Map<Page, Float> relevance = new HashMap<>();

        for (Lemma lemma : lemmas) {
            List<Index> indices = indexRepository.findByLemma(lemma);
            for (Index index : indices) {
                Page page = index.getPage();
                relevance.merge(page, index.getRank(), Float::sum);
            }
        }

        List<Map.Entry<Page, Float>> sorted =
                new ArrayList<>(relevance.entrySet());

        sorted.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

        List<SearchResult> results = new ArrayList<>();

        for (Map.Entry<Page, Float> entry : sorted) {
            Page page = entry.getKey();

            SearchResult item = new SearchResult(
                    page.getSite().getUrl(),
                    page.getSite().getName(),
                    page.getPath(),
                    page.getContent(),
                    makeSnippet(page.getContent(), queryLemmas.keySet()),
                    entry.getValue()
            );

            results.add(item);
        }

        int total = results.size();

        List<SearchResult> finalList = results.subList(
                Math.min(offset, total),
                Math.min(offset + limit, total)
        );

        response.setCount(total);
        response.setData(finalList);

        return response;
    }

    private String makeSnippet(String content, Set<String> queryWords) {
        if (content == null) return "";
        String text = content.replaceAll("<[^>]+>", " ");

        for (String word : queryWords) {
            text = text.replace(word, "<b>" + word + "</b>");
        }

        return text.length() > 300
                ? text.substring(0, 300) + "..."
                : text;
    }
}

