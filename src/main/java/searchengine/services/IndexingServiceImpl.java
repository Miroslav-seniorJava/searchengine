package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private boolean indexingInProgress = false;

    @Override
    public IndexingResponse start() {
        if (indexingInProgress) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }
        indexingInProgress = true;
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stop() {
        if (!indexingInProgress) {
            return new IndexingResponse(false, "Индексация не запущена");
        }
        indexingInProgress = false;
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse indexPage(String url) {
        if (url == null || url.isBlank()) {
            return new IndexingResponse(false, "URL не задан");
        }
        return new IndexingResponse(true, null);
    }
}
