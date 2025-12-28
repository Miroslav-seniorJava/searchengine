package searchengine.services;

import java.util.Map;

public interface LemmatizerService {

    Map<String, Integer> collectLemmaFrequencies(String text);
}
