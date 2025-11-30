package searchengine.services;

import java.util.List;
import java.util.Map;

public interface LemmatizerService {
    List<String> getLemmas(String text);
    Map<String,Integer> collectLemmaFrequencies(String text);
}
