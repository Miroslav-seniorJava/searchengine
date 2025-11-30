package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repository.*;

import javax.transaction.Transactional;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sitesList;
    private final SiteRepository siteRepo;
    private final PageRepository pageRepo;
    private final LemmaRepository lemmaRepo;
    private final IndexRepository indexRepo;
    private final LemmatizerService lemmatizer;

    private final ExecutorService executor = Executors.newFixedThreadPool(6);
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public boolean startIndexing() {
        if (running.get()) return false;
        running.set(true);

        for (SitesList.SiteItem siteItem : sitesList.getSites()) {
            executor.submit(() -> indexSite(siteItem));
        }

        return true;
    }

    @Override
    public boolean stopIndexing() {
        running.set(false);
        return true;
    }

    @Override
    public boolean indexPage(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("SearchEngineBot/1.0")
                    .timeout(10000)
                    .get();

            String title = doc.title();
            String body = doc.body() != null ? doc.body().text() : "";
            String content = title + " " + body;

            URI uri = URI.create(url);
            String host = uri.getHost();

            Optional<Site> optionalSite = siteRepo.findAll().stream()
                    .filter(s -> host.endsWith(URI.create(s.getUrl()).getHost()))
                    .findFirst();

            Site site = optionalSite.orElseGet(() ->
                    siteRepo.save(
                            Site.builder()
                                    .url(uri.getScheme() + "://" + host)
                                    .name(host)
                                    .status(Status.INDEXED)
                                    .statusTime(LocalDateTime.now())
                                    .build()
                    )
            );

            Page page = pageRepo.save(
                    Page.builder()
                            .site(site)
                            .path(url)
                            .code(200)
                            .content(content)
                            .build()
            );

            indexPageContent(site, page, content);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private void indexSite(SitesList.SiteItem siteItem) {
        String root = siteItem.getUrl();

        try {
            Site site = siteRepo.findByUrl(root)
                    .orElseGet(() ->
                            siteRepo.save(
                                    Site.builder()
                                            .url(root)
                                            .name(siteItem.getName())
                                            .status(Status.INDEXING)
                                            .statusTime(LocalDateTime.now())
                                            .build()
                            )
                    );

            Queue<UrlDepth> queue = new ArrayDeque<>();
            Set<String> visited = ConcurrentHashMap.newKeySet();

            queue.add(new UrlDepth(root, 0));
            visited.add(root);

            while (!queue.isEmpty() && running.get()) {

                UrlDepth current = queue.poll();

                try {
                    Document doc = Jsoup.connect(current.url)
                            .userAgent("SearchEngineBot/1.0")
                            .timeout(10000)
                            .get();

                    String title = doc.title();
                    String body = doc.body() != null ? doc.body().text() : "";
                    String content = title + " " + body;

                    Page page = pageRepo.save(
                            Page.builder()
                                    .site(site)
                                    .path(current.url)
                                    .code(200)
                                    .content(content)
                                    .build()
                    );

                    indexPageContent(site, page, content);

                    if (current.depth < 2) { // Глубину можно вынести в настройки
                        Elements aTags = doc.select("a[href]");

                        for (var a : aTags) {
                            String href = a.absUrl("href");

                            if (href.isBlank() || visited.contains(href)) continue;

                            try {
                                URI check = URI.create(href);
                                if (check.getHost() == null) continue;
                                if (!check.getHost().endsWith(URI.create(root).getHost())) continue;
                            } catch (Exception ignore) {
                                continue;
                            }

                            visited.add(href);
                            queue.add(new UrlDepth(href, current.depth + 1));
                        }
                    }

                    Thread.sleep(150);

                } catch (Exception ignore) {}
            }

            site.setStatus(Status.INDEXED);
            site.setStatusTime(LocalDateTime.now());
            siteRepo.save(site);

        } catch (Exception e) {
            Site failed = siteRepo.findByUrl(root)
                    .orElseGet(() ->
                            Site.builder().url(root).name(siteItem.getName()).build()
                    );

            failed.setStatus(Status.FAILED);
            failed.setLastError(e.getMessage());
            failed.setStatusTime(LocalDateTime.now());
            siteRepo.save(failed);
        }
    }

    @Transactional
    protected void indexPageContent(Site site, Page page, String content) {

        Map<String, Integer> frequencies = lemmatizer.collectLemmaFrequencies(content);

        for (var entry : frequencies.entrySet()) {

            String lemmaStr = entry.getKey();
            int count = entry.getValue();

            Lemma lemma = lemmaRepo.findByLemmaAndSiteId(lemmaStr, site.getId())
                    .orElseGet(() ->
                            lemmaRepo.save(
                                    Lemma.builder()
                                            .lemma(lemmaStr)
                                            .site(site)
                                            .frequency(0)
                                            .build()
                            )
                    );

            lemma.setFrequency(lemma.getFrequency() + 1);
            lemmaRepo.save(lemma);

            Index index = Index.builder()
                    .page(page)
                    .lemma(lemma)
                    .rank(count) // важное исправление!
                    .build();

            indexRepo.save(index);
        }
    }

    private record UrlDepth(String url, int depth) {}
}
