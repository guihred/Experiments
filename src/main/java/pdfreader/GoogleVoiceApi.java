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
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.FunctionEx;

public class GoogleVoiceApi {
    private static final String API_KEY = "AIzaSyBEMO6bUszqedhYmKm-bAu91gVLqKEBRZ4";
    /*
     * POST https://texttospeech.googleapis.com/v1/text:synthesize?key=
     * AIzaSyBEMO6bUszqedhYmKm-bAu91gVLqKEBRZ4 { "input": { "text": "", }, "voice":
     * { "languageCode": "en-US", "name": "en-US-Standard-G", "ssmlGender": "FEMALE"
     * }, "audioConfig": { "audioEncoding": "MP3" } }
     */

    public static void main(String[] args) throws IOException {
        BufferedReader newBufferedReader = Files.newBufferedReader(
                new File("C:\\Users\\guigu\\Downloads\\Padrão.txt").toPath(), StandardCharsets.UTF_8);
        List<String> lines = newBufferedReader.lines().map(FunctionEx.makeFunction(s -> {
            String[] split = s.split("\t");
            File audio = synthesize(split[0], "en-US");
            return Stream.of(split[0] + "[sound:" + audio.getName() + "]", split[1], "")
                    .collect(Collectors.joining("\t"));
        })).collect(Collectors.toList());
        Files.write(ResourceFXUtils.getOutFile("mp3/BaralhoGoogle.txt").toPath(), lines);

    }

    public static File synthesize(String phrase, String language) throws IOException {
        String content = String.format(
                "{"
                + "\"input\": {\"text\": \"%s\"},"
                + "\"voice\": {\"languageCode\": \"%s\","
                        + "\"name\": \"\"},"
                + "\"audioConfig\": {\"audioEncoding\": \"MP3\"}"
                + "}",
                phrase, language);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        String md5Hash = HashVerifier.getMD5Hash(phrase);
        File outFile = ResourceFXUtils.getOutFile("mp3/" + md5Hash + ".mp3");
        PhantomJSUtils.postJson("https://texttospeech.googleapis.com/v1/text:synthesize?key=" + API_KEY, content,
                headers, outFile);
        Map<String, Object> object = JsonExtractor.toObject(outFile, "audioContent", "error");
        if (object.containsKey("audioContent")) {
            String access = JsonExtractor.access(object, String.class, "audioContent");
            Files.write(outFile.toPath(), StringSigaUtils.decode64Bytes(access));
        }
        return outFile;
    }

}
