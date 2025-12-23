package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;

@Service
public class IndexingServiceImpl implements IndexingService {

    @Override
    public IndexingResponse start() {
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stop() {
        return new IndexingResponse(true, null);
    }
}
