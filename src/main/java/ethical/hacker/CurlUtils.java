package ethical.hacker;

import static utils.ExtractUtils.*;

import extract.web.JsoupUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import utils.ConsoleUtils;
import utils.ResourceFXUtils;

public class CurlUtils {

    private String url;
    private List<String> params = new ArrayList<>();
    private List<String> result = new ArrayList<>();

    public Document asDocument() {
        return JsoupUtils.asDocument(result.stream().collect(Collectors.joining("\n")));
    }

    public CurlUtils cookies(File cookieFile) {
        params.add(String.format("-c \"%s\"", cookieFile));
        return this;
    }

    public CurlUtils defaultProxy() {
        if (isNotProxied()) {
            return this;
        }

        params.add(String.format("-u %s:%s", getHTTPUsername(), getHTTPPassword()));
        params.add(String.format("-x %s:%s", PROXY_ADDRESS, PROXY_PORT));

        return this;
    }

    public CurlUtils referer(String url1) {
        params.add("-e " + url1);
        return this;
    }

    public List<String> result() {
        return result;
    }

    public CurlUtils run() {
        String par = params.stream().collect(Collectors.joining(" ", "", " "));
        result = ConsoleUtils.executeInConsoleInfo(String.format("curl %s%s", par, url));
        return this;
    }

    public CurlUtils saveToFile(File f) {
        params.add(String.format("-o \"%s\"", f));
        return this;
    }

    public CurlUtils url(String url1) {
        url = url1;
        return this;
    }

    public CurlUtils userAgent(String url1) {
        params.add(String.format("-A \"%s\"", url1));
        return this;
    }

    public static void main(String[] args) {
        System.out.println(new CurlUtils()
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0")
                .cookies(ResourceFXUtils.getOutFile("html/cookies.txt"))
                .saveToFile(ResourceFXUtils.getOutFile("html/test.html")).url("https://www.google.com/").run()
                .asDocument());
    }

}
