package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class WebCrawler {

    private final PageRepository pageRepository;

    private final Set<String> visited = new HashSet<>();

    public void crawl(Site site, String url) {

        if (visited.contains(url)) return;
        visited.add(url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Page page = Page.builder()
                    .site(site)
                    .path(url.replace(site.getUrl(), ""))
                    .code(200)
                    .content(doc.html())
                    .build();

            pageRepository.save(page);

            Elements links = doc.select("a[href]");

            for (Element link : links) {

                String nextUrl = link.absUrl("href");

                if (nextUrl.startsWith(site.getUrl()) && !nextUrl.contains("#")) {
                    crawl(site, nextUrl);
                }
            }

        } catch (IOException ignored) {
        }
    }
}
