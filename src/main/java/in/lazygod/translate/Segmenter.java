package in.lazygod.translate;

import com.ibm.icu.text.BreakIterator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class Segmenter {
    public List<String> sentences(String text, Locale locale) {
        BreakIterator bi = BreakIterator.getSentenceInstance(locale);
        bi.setText(text);
        List<String> out = new ArrayList<>();
        int start = bi.first();
        for (int end = bi.next(); end != BreakIterator.DONE; start = end, end = bi.next()) {
            out.add(text.substring(start, end));
        }
        return out;
    }
}
