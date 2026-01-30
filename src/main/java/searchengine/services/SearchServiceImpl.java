package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.*;
import searchengine.model.*;
import searchengine.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final LemmatizerService lemmatizer;
    private final LemmaRepository lemmaRepo;
    private final IndexRepository indexRepo;

    @Override
    public SearchResponse search(String query, String site, int offset, int limit) {

        Map<String, Integer> lemmas = lemmatizer.collectLemmaFrequencies(query);

        if (lemmas.isEmpty()) {
            return new SearchResponse(false, 0, new ArrayList<>(), "Пустой запрос");
        }

        List<Lemma> lemmaList = lemmaRepo.findByLemmaIn(lemmas.keySet());

        Map<Page, Float> relevance = new HashMap<>();

        for (Lemma lemma : lemmaList) {
            for (Index idx : indexRepo.findByLemma(lemma)) {
                relevance.merge(
                        idx.getPage(),
                        idx.getRank(),
                        Float::sum
                );
            }
        }

        List<SearchResult> results = relevance.entrySet()
                .stream()
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .skip(offset)
                .limit(limit)
                .map(e -> new SearchResult(
                        e.getKey().getSite().getUrl(),
                        e.getKey().getSite().getName(),
                        e.getKey().getPath(),
                        "",
                        "",
                        e.getValue()
                ))
                .collect(Collectors.toList());

        return new SearchResponse(true, relevance.size(), results, null);
    }
}
