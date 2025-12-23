package searchengine.dto.indexing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexingResponse {

    private boolean result;
    private String error;
}
