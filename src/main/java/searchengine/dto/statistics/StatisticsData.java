package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatisticsData {

    private TotalStatistics total;
    private java.util.List<DetailedStatisticsItem> detailed;
}
