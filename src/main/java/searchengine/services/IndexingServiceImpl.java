package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.*;
import searchengine.repository.*;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmatizerService lemmatizerService;

    @Override
    public IndexingResponse start() {

        Iterable<Site> sites = siteRepository.findAll();

        for (Site site : sites) {

            if (site.getPages() == null) continue;

            for (Page page : site.getPages()) {

                Map<String, Integer> lemmas =
                        lemmatizerService.collectLemmaFrequencies(page.getContent());

                for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {

                    String lemmaText = entry.getKey();
                    int count = entry.getValue();

                    Lemma lemma = lemmaRepository
                            .findByLemmaAndSiteId(lemmaText, site.getId().intValue())
                            .orElseGet(() -> {

                                Lemma l = Lemma.builder()
                                        .lemma(lemmaText)
                                        .site(site)
                                        .frequency(0)
                                        .build();

                                return lemmaRepository.save(l);
                            });

                    lemma.setFrequency(lemma.getFrequency() + count);
                    lemmaRepository.save(lemma);

                    Index index = Index.builder()
                            .page(page)
                            .lemma(lemma)
                            .rank(count)
                            .build();

                    indexRepository.save(index);
                }
            }
        }

        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stop() {
        return new IndexingResponse(true, null);
    }
}
