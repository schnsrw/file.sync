package in.lazygod.controller;

import in.lazygod.translate.docx.DocxTranslator;
import in.lazygod.translate.txt.TxtTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class TranslationController {

    private final TxtTranslator txtTranslator;
    private final DocxTranslator docxTranslator;

    @PostMapping(value = "/translate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> translate(@RequestParam("file") MultipartFile file,
                                            @RequestParam String targetLang,
                                            @RequestParam(required = false) String sourceLang) throws IOException {
        String name = file.getOriginalFilename();
        String ext = (name != null && name.contains(".")) ? name.substring(name.lastIndexOf('.') + 1) : "";
        Locale locale = Locale.forLanguageTag(sourceLang == null ? "en" : sourceLang);
        byte[] out;
        MediaType mediaType;
        switch (ext.toLowerCase()) {
            case "txt" -> {
                out = txtTranslator.translate(file.getBytes(), sourceLang, targetLang, locale);
                mediaType = MediaType.TEXT_PLAIN;
            }
            case "docx" -> {
                out = docxTranslator.translate(file.getBytes(), sourceLang, targetLang, locale);
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type");
        }
        String outName = "translated_" + (name == null ? "out" : name);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + outName)
                .contentType(mediaType)
                .body(out);
    }
}
