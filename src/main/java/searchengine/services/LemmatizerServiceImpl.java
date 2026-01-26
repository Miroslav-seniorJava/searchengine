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
    public List<String> getLemmas(String text) {

        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] words = text.toLowerCase()
                .replaceAll("[^а-яa-z ]", " ")
                .split("\\s+");

        List<String> lemmas = new ArrayList<>();

        for (String word : words) {

            if (word.length() < 2) continue;

            List<String> normalForms = morphology.getNormalForms(word);

            if (!normalForms.isEmpty()) {
                lemmas.add(normalForms.get(0));
            }
        }

        return lemmas;
    }

    @Override
    public Map<String, Integer> collectLemmaFrequencies(String text) {

        List<String> lemmas = getLemmas(text);
        Map<String, Integer> freq = new HashMap<>();

        for (String lemma : lemmas) {
            freq.put(lemma, freq.getOrDefault(lemma, 0) + 1);
        }

        return freq;
    }
}

