package in.lazygod.translate.docx;

import in.lazygod.translate.Masker;
import in.lazygod.translate.Segmenter;
import in.lazygod.translate.TranslationService;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DocxTranslator {
    private final Segmenter segmenter;
    private final Masker masker;
    private final TranslationService ts;

    public DocxTranslator(Segmenter segmenter, Masker masker, TranslationService ts) {
        this.segmenter = segmenter;
        this.masker = masker;
        this.ts = ts;
    }

    public byte[] translate(byte[] docx, String src, String tgt, Locale locale) throws IOException {
        try (var is = new ByteArrayInputStream(docx);
             var doc = new XWPFDocument(is);
             var os = new ByteArrayOutputStream()) {

            for (XWPFParagraph p : doc.getParagraphs()) {
                String logical = p.getText();
                if (logical == null || logical.isBlank()) continue;

                var mr = masker.mask(logical);
                List<String> segs = segmenter.sentences(mr.text(), locale);
                List<String> outSegs = ts.translateBatch(segs, src, tgt);
                String translated = masker.unmask(String.join("", outSegs), mr.dict());

                clearRuns(p);
                XWPFRun r = p.createRun();
                r.setText(translated, 0);
            }
            doc.write(os);
            return os.toByteArray();
        }
    }

    private void clearRuns(XWPFParagraph p) {
        for (int i = p.getRuns().size() - 1; i >= 0; i--) {
            p.removeRun(i);
        }
    }
}
