package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.*;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.repository.LemmaRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepo;
    private final PageRepository pageRepo;
    private final LemmaRepository lemmaRepo;

    @Override
    public StatisticsResponse getStatistics() {

        List<Site> sites = siteRepo.findAll();

        int totalPages = (int) pageRepo.count();
        int totalLemmas = (int) lemmaRepo.count();

        TotalStatistics total = new TotalStatistics(
                sites.size(),
                totalPages,
                totalLemmas,
                false
        );

        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        for (Site site : sites) {
            DetailedStatisticsItem item = new DetailedStatisticsItem(
                    site.getUrl(),
                    site.getName(),
                    site.getPages() != null ? site.getPages().size() : 0,
                    site.getLemmas() != null ? site.getLemmas().size() : 0,
                    site.getStatus().name(),
                    site.getLastError() != null ? site.getLastError() : "",
                    site.getStatusTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
            detailed.add(item);
        }

        StatisticsData data = new StatisticsData(total, detailed);
        return new StatisticsResponse(true, data);
    }
}
