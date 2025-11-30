package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetailedStatisticsItem {

    private String url;
    private String name;
    private int pages;
    private int lemmas;
    private String status;
    private String error;
    private long statusTime;
}
