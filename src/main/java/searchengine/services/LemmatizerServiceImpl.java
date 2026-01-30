package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LemmatizerServiceImpl implements LemmatizerService {

    private final LuceneMorphology morphology;

    public LemmatizerServiceImpl() throws Exception {
        this.morphology = new RussianLuceneMorphology();
    }

    @Override
    public Map<String, Integer> collectLemmaFrequencies(String text) {

        Map<String, Integer> result = new HashMap<>();

        if (text == null || text.isBlank()) {
            return result;
        }

        String[] words = text
                .toLowerCase()
                .replaceAll("[^а-яa-z ]", " ")
                .split("\\s+");

        for (String word : words) {

            if (word.length() < 2) continue;

            List<String> forms = morphology.getNormalForms(word);

            if (forms.isEmpty()) continue;

            String lemma = forms.get(0);

            result.put(lemma, result.getOrDefault(lemma, 0) + 1);
        }

        return result;
    }
}
