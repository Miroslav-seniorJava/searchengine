package searchengine.services;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LemmatizerServiceImpl implements LemmatizerService {

    @Override
    public Map<String, Integer> collectLemmaFrequencies(String text) {

        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        String[] words = text.toLowerCase()
                .replaceAll("[^а-яa-z ]", " ")
                .split("\\s+");

        Map<String, Integer> freq = new HashMap<>();

        for (String word : words) {

            if (word.length() < 2) continue;

            freq.put(word, freq.getOrDefault(word, 0) + 1);
        }

        return freq;
    }
}
