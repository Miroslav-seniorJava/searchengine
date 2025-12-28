package searchengine.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IndexingException.class)
    public IndexingResponse handleIndexingException(IndexingException ex) {
        return new IndexingResponse(false, ex.getMessage());
    }

    @ExceptionHandler(SearchException.class)
    public SearchResponse handleSearchException(SearchException ex) {
        return new SearchResponse(false, 0, null, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public IndexingResponse handleCommonException(Exception ex) {
        return new IndexingResponse(false, "Внутренняя ошибка сервера");
    }
}
