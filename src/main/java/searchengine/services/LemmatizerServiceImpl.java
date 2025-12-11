package searchengine.services;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LemmatizerServiceImpl implements LemmatizerService {

    // Регекс для слов (поддержка кириллицы и латиницы, апострофов)
    private static final Pattern WORD = Pattern.compile("[\\p{L}']{2,}");

    /**
     * Возвращает список лемм (тут - нормализованных слов).
     * Для учебного проекта используется простая нормализация:
     * - удаление диакритики,
     * - приведение к нижнему регистру,
     * - фильтрация коротких токенов.
     */
    @Override
    public List<String> getLemmas(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();

        String normalized = normalizeText(text);
        Matcher m = WORD.matcher(normalized);

        List<String> lemmas = new ArrayList<>();
        while (m.find()) {
            String w = m.group().toLowerCase(Locale.ROOT);
            w = stripTrailingApostrophe(w);
            if (w.length() < 2) continue;
            lemmas.add(w);
        }
        return lemmas;
    }

    /**
     * Собирает частоту встречаемости лемм в тексте.
     */
    @Override
    public Map<String, Integer> collectLemmaFrequencies(String text) {
        List<String> lemmas = getLemmas(text);
        Map<String, Integer> freq = new HashMap<>();
        for (String l : lemmas) freq.merge(l, 1, Integer::sum);
        return freq;
    }

    private String normalizeText(String s) {
        // убираем управляющие символы, нормализуем Unicode
        String t = Normalizer.normalize(s, Normalizer.Form.NFKC);
        // заменим не буквенные символы на пробелы (кроме апострофа)
        return t.replaceAll("[^\\p{L}' ]+", " ");
    }

    private String stripTrailingApostrophe(String w) {
        if (w.endsWith("'") || w.endsWith("’")) return w.substring(0, w.length()-1);
        return w;
    }
}
