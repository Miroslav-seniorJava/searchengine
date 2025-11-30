package searchengine.services.impl;

import org.springframework.stereotype.Service;
import org.tartarus.snowball.ext.RussianStemmer;
import searchengine.services.LemmatizerService;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class LemmatizerServiceImpl implements LemmatizerService {

    private static final Pattern WORD = Pattern.compile("[\\p{IsAlphabetic}0-9]+");

    @Override
    public List<String> getLemmas(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isBlank()) return result;
        String[] tokens = text.toLowerCase(Locale.ROOT).split("\\s+");
        RussianStemmer stemmer = new RussianStemmer();
        for (String t : tokens) {
            String clean = t.replaceAll("[^\\p{IsAlphabetic}0-9]", "");
            if (clean.isEmpty()) continue;
            if (!WORD.matcher(clean).matches()) continue;
            stemmer.setCurrent(clean);
            stemmer.stem();
            String lemma = stemmer.getCurrent();
            if (lemma != null && !lemma.isEmpty()) result.add(lemma);
        }
        return result;
    }

    @Override
    public Map<String, Integer> collectLemmaFrequencies(String text) {
        Map<String,Integer> freq = new HashMap<>();
        for (String lemma : getLemmas(text)) freq.merge(lemma, 1, Integer::sum);
        return freq;
    }
}
