package in.lazygod.translate.txt;

import in.lazygod.translate.Masker;
import in.lazygod.translate.Segmenter;
import in.lazygod.translate.TranslationService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
public class TxtTranslator {
    private final Segmenter segmenter;
    private final Masker masker;
    private final TranslationService ts;

    public TxtTranslator(Segmenter segmenter, Masker masker, TranslationService ts) {
        this.segmenter = segmenter;
        this.masker = masker;
        this.ts = ts;
    }

    public byte[] translate(byte[] bytes, String src, String tgt, Locale locale) throws IOException {
        String text = new String(bytes, StandardCharsets.UTF_8);
        var mr = masker.mask(text);
        List<String> segs = segmenter.sentences(mr.text(), locale);
        List<String> outSegs = ts.translateBatch(segs, src, tgt);
        String joined = String.join("", outSegs);
        String finalText = masker.unmask(joined, mr.dict());
        return finalText.getBytes(StandardCharsets.UTF_8);
    }
}
