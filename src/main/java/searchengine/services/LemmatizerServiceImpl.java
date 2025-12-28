package searchengine.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LemmatizerServiceImpl implements LemmatizerService {

    @Override
    public Map<String, Integer> collectLemmaFrequencies(String text) {
        Map<String, Integer> lemmas = new HashMap<>();

        if (text == null || text.trim().isEmpty()) {
            return lemmas;
        }

        String cleanedText = text
                .toLowerCase()
                .replaceAll("[^а-я\\s]", " ")
                .replaceAll("\\s+", " ");

        for (String word : cleanedText.split(" ")) {
            if (word.length() < 2) {
                continue;
            }
            lemmas.put(word, lemmas.getOrDefault(word, 0) + 1);
        }

        return lemmas;
    }
}
