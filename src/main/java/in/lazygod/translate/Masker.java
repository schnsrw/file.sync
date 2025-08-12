package in.lazygod.translate;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Masker {
    private static final Pattern URL = Pattern.compile("(https?://\\S+)");
    private static final Pattern VAR = Pattern.compile("(\\$\\{[^}]+}|%\\w|\\{[^}]+})");

    public MaskResult mask(String input) {
        Map<String, String> map = new LinkedHashMap<>();
        String out = input;
        out = replace(out, URL, map, "URL");
        out = replace(out, VAR, map, "VAR");
        return new MaskResult(out, map);
    }

    public String unmask(String translated, Map<String, String> map) {
        for (var e : map.entrySet()) {
            translated = translated.replace(e.getKey(), e.getValue());
        }
        return translated;
    }

    private String replace(String s, Pattern p, Map<String, String> map, String tag) {
        Matcher m = p.matcher(s);
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (m.find()) {
            String token = "⟦" + tag + i + "⟧";
            i++;
            map.put(token, m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(token));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public record MaskResult(String text, Map<String, String> dict) {}
}
