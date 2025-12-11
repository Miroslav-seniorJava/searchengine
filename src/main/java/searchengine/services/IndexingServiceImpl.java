package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repository.*;

import jakarta.transaction.Transactional;
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

            URI u = URI.create(url);
            String host = u.getHost();

            Optional<Site> optionalSite = siteRepo.findAll().stream()
                    .filter(s -> {
                        try {
                            return host != null && URI.create(s.getUrl()).getHost() != null &&
                                    host.endsWith(URI.create(s.getUrl()).getHost());
                        } catch (Exception ex) {
                            return false;
                        }
                    })
                    .findFirst();

            Site site = optionalSite.orElseGet(() -> siteRepo.save(Site.builder()
                    .url(u.getScheme() + "://" + host)
                    .name(host)
                    .status(Status.INDEXED)
                    .statusTime(LocalDateTime.now())
                    .build()));

            Page page = pageRepo.save(Page.builder()
                    .site(site)
                    .path(url)
                    .code(200)
                    .content(content)
                    .build());

            indexPageContent(site, page, content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void indexSite(SitesList.SiteItem siteItem) {
        String root = siteItem.getUrl();
        try {
            Site site = siteRepo.findByUrl(root).orElseGet(() -> siteRepo.save(
                    Site.builder()
                            .url(root)
                            .name(siteItem.getName())
                            .status(Status.INDEXING)
                            .statusTime(LocalDateTime.now())
                            .build()
            ));

            Queue<UrlDepth> q = new ArrayDeque<>();
            Set<String> visited = ConcurrentHashMap.newKeySet();
            q.add(new UrlDepth(root, 0));
            visited.add(root);

            while (!q.isEmpty() && running.get()) {
                UrlDepth ud = q.poll();
                try {
                    Document doc = Jsoup.connect(ud.url)
                            .userAgent("SearchEngineBot/1.0")
                            .timeout(10000)
                            .get();

                    String title = doc.title();
                    String body = doc.body() != null ? doc.body().text() : "";
                    String content = title + " " + body;

                    Page page = Page.builder()
                            .site(site)
                            .path(ud.url)
                            .code(200)
                            .content(content)
                            .build();
                    page = pageRepo.save(page);

                    indexPageContent(site, page, content);

                    if (ud.depth < 2) { // configurable depth
                        Elements links = doc.select("a[href]");
                        for (var link : links) {
                            String href = link.absUrl("href");
                            if (href == null || href.isBlank()) continue;
                            if (visited.contains(href)) continue;
                            try {
                                URI u = URI.create(href);
                                if (u.getHost() == null) continue;
                                if (!u.getHost().endsWith(URI.create(root).getHost())) continue;
                            } catch (Exception ex) {
                                continue;
                            }
                            visited.add(href);
                            q.add(new UrlDepth(href, ud.depth + 1));
                        }
                    }

                    Thread.sleep(150);
                } catch (Exception ex) {
                    // ignore single page errors
                }
            }

            site.setStatus(Status.INDEXED);
            site.setStatusTime(LocalDateTime.now());
            siteRepo.save(site);

        } catch (Exception e) {
            Site failed = siteRepo.findByUrl(root).orElseGet(() -> Site.builder().url(root).name(siteItem.getName()).build());
            failed.setStatus(Status.FAILED);
            failed.setLastError(e.getMessage());
            failed.setStatusTime(LocalDateTime.now());
            siteRepo.save(failed);
        }
    }

    @Transactional
    protected void indexPageContent(Site site, Page page, String content) {
        Map<String, Integer> freq = lemmatizer.collectLemmaFrequencies(content);

        for (var e : freq.entrySet()) {
            String lemmaStr = e.getKey();
            int count = e.getValue();

            Lemma lemma = lemmaRepo.findByLemmaAndSiteId(lemmaStr, site.getId())
                    .orElseGet(() -> lemmaRepo.save(Lemma.builder().lemma(lemmaStr).site(site).frequency(0).build()));

            lemma.setFrequency(lemma.getFrequency() + count);
            lemmaRepo.save(lemma);

            Index idx = Index.builder()
                    .page(page)
                    .lemma(lemma)
                    .rank(count)
                    .build();

            indexRepo.save(idx);
        }
    }

    private static class UrlDepth { String url; int depth; UrlDepth(String u,int d){url=u;depth=d;} }
}
