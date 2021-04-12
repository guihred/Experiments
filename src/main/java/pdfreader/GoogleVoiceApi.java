package pdfreader;

import extract.web.HashVerifier;
import extract.web.JsonExtractor;
import extract.web.PhantomJSUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import utils.ProjectProperties;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public class GoogleVoiceApi {
    private static final String GOOGLEAPIS_SYNTHESIZE_URL = ProjectProperties.getField();
    private static final String API_KEY = ProjectProperties.getField();


    public static void main(String[] args) {
        RunnableEx.run(() -> synthesize("How are you today?", "en-US"));
    }

    public static void makeCards(File file) throws IOException {
        try (BufferedReader newBufferedReader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            List<String> lines = newBufferedReader.lines().map(FunctionEx.makeFunction(s -> {
                String[] split = s.split("\t");
                File audio = synthesize(split[0], "en-US");
                return Stream.of(split[0] + "[sound:" + audio.getName() + "]", split[1], "")
                        .collect(Collectors.joining("\t"));
            })).collect(Collectors.toList());
            Files.write(ResourceFXUtils.getOutFile("mp3/BaralhoGoogle.txt").toPath(), lines);
        }
    }

    public static File synthesize(String phrase, String language) throws IOException {
        String content =
                String.format("{\"input\": {\"text\": \"%s\"},\"voice\": {\"languageCode\": \"%s\",\"name\": \"\"},"
                        + "\"audioConfig\": {\"audioEncoding\": \"MP3\"}}", phrase, language);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        String md5Hash = HashVerifier.getMD5Hash(phrase);
        File outFile = ResourceFXUtils.getOutFile("mp3/" + md5Hash + ".mp3");
        PhantomJSUtils.postJson(GOOGLEAPIS_SYNTHESIZE_URL + API_KEY, content,
                headers, outFile);
        String audioContentKey = "audioContent";
        Map<String, Object> object = JsonExtractor.toObject(outFile, audioContentKey, "error");
        if (object.containsKey(audioContentKey)) {
            String access = JsonExtractor.access(object, String.class, audioContentKey);
            Files.write(outFile.toPath(), StringSigaUtils.decode64Bytes(access));
        }
        return outFile;
    }

}
