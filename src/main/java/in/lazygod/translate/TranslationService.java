package in.lazygod.translate;

import java.util.List;

public interface TranslationService {
    List<String> translateBatch(List<String> segments, String src, String tgt);
}
