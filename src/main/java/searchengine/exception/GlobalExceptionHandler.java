package searchengine.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public IndexingResponse handleIllegalArgument(IllegalArgumentException ex) {
        return new IndexingResponse(false, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public SearchResponse handleAnyException(Exception ex) {
        return new SearchResponse(false, 0, null, ex.getMessage());
    }
}
