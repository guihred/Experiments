package extract.web;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.net.ssl.SSLHandshakeException;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.ProjectProperties;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class JsoupUtils {
    public static final String USER_AGENT = ProjectProperties.getField();
    private static final Logger LOG = HasLogging.log();

    private JsoupUtils() {
    }

    public static Document asDocument(String html) {
        return Jsoup.parse(html);
    }

    public static String displayAllElements(File file) throws IOException {
        String html = Files.toString(file, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);
        return doc.html();
    }

    public static String displayAllElementsWithOutline(File file) throws IOException {
        String html = Files.toString(file, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);
        doc.outputSettings().outline(true);
        return doc.html();
    }

    public static Response executeRequest(String url, Map<String, String> cookies) {
        Connection connect = HttpConnection.connect(url);
        if (ExtractUtils.isProxySet()) {
            // addProxyAuthorization(connect);
        }
        connect.timeout(ExtractUtils.HUNDRED_SECONDS);
        connect.cookies(cookies);
        connect.ignoreContentType(true);
        connect.userAgent(USER_AGENT);

        return SupplierEx.makeSupplier(connect::execute, e -> {
            if (e instanceof SSLHandshakeException) {
                InstallCert.installCertificate(url);
            }
        }).get();

    }

    // A simple authentication POST request with Jsoup
    // Extract the data from HTML document file
    public static Elements extractDataFromHTML(File inputFile) throws IOException {
        Document doc = Jsoup.parse(inputFile, "UTF-8");
        // select element by <a>
        return doc.select("a");
    }

    // Extracting JavaScript data with Jsoup
    // create HTML with JavaScript data
    public static Document extractingJavaScriptDataWithJsoup() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html> <html> <head> <title>Hello Jsoup!</title>");
        html.append("<script>");
        html.append("StackExchange.docs.comments.init({");
        html.append("highlightColor: '#F4A83D',");
        html.append("backgroundColor:'#FFF',");
        html.append("});");
        html.append("</script>");
        html.append("<script>");
        html.append("document.write(<style type='text/css'>div,iframe { top: 0; position:absolute; }</style>');");
        html.append("</script>\n");
        html.append("</head><body></body> </html>");
        // parse as HTML document
        Document doc = Jsoup.parse(html.toString());
        String defaultBackground = "backgroundColor:'#FFF'";
        // get <script>
        for (Element scripts : doc.getElementsByTag("script")) {
            // get data from <script>
            for (DataNode dataNode : scripts.dataNodes()) {
                // find data which contains backgroundColor:'#FFF'
                if (dataNode.getWholeData().contains(defaultBackground)) {
                    // replace '#FFF' -> '#ddd'
                    String newData = dataNode.getWholeData().replaceAll(defaultBackground, "backgroundColor:'#ddd'");
                    // set new data contents
                    dataNode.setWholeData(newData);
                }
            }
        }
        return doc;
    }

    // Extracting all the URLs from a website using JSoup (recursion)
    // Extract Twitter Markup
    public static List<String> extractTwitterMarkup() throws IOException {
        // Twitter markup documentation:
        // https://dev.twitter.com/cards/markup
        String[] twitterTags = { "twitter:site", "twitter:site:id", "twitter:creator", "twitter:creator:id",
                "twitter:description", "twitter:title", "twitter:image", "twitter:image:alt", "twitter:player",
                "twitter:player:width", "twitter:player:height", "twitter:player:stream", "twitter:app:name:iphone",
                "twitter:app:id:iphone", "twitter:app:url:iphone", "twitter:app:name:ipad", "twitter:app:id:ipad",
                "twitter:app:url:ipadt", "twitter:app:name:googleplay", "twitter:app:id:googleplay",
                "twitter:app:url:googleplay" };
        // Connect to URL and extract source code
        Document doc = Jsoup.connect("http://stackoverflow.com/").get();
        List<String> twitterEntries = new ArrayList<>();
        for (String twitterTag : twitterTags) {
            // find a matching meta tag
            Element meta = doc.select("meta[name=" + twitterTag + "]").first();
            // if found, get the value of the content attribute
            String content = meta != null ? meta.attr("content") : "";
            // display results
            twitterEntries.add(twitterTag + "=" + content);
        }
        return twitterEntries;
    }

    // Extracting email adresses & links to other pages
    // Extract the URLs and titles of links
    public static List<Entry<String, String>> extractURL(String url) throws IOException {
        Document doc = Jsoup.connect(url).userAgent(USER_AGENT).get();
        return doc.select("a.question-hyperlink").stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.attr("abs:href"), e.text())).collect(Collectors.toList());
    }

    // Extract full URL from partial HTML
    public static void extractURLPartialHTML() {
        String bodyFragment = "<div><a href=\"/documentation\">Stack Overflow Documentation</a></div>";
        Document doc = Jsoup.parseBodyFragment(bodyFragment);
        String link = doc.select("div > a").first().attr("href");
        LOG.info("{}", link);
        doc = Jsoup.parseBodyFragment(bodyFragment, "http://stackoverflow.com");
        link = doc.select("div > a").first().absUrl("href");
        LOG.info("{}", link);
    }

    public static void filterMailToLinks() throws IOException {
        Document doc = Jsoup.connect("http://stackoverflow.com/questions/15893655/").userAgent(USER_AGENT).get();
        Pattern p = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        Matcher matcher = p.matcher(doc.text());
        Set<String> emails = new HashSet<>();
        while (matcher.find()) {
            emails.add(matcher.group());
        }
        Set<String> links = new HashSet<>();
        Elements elements = doc.select("a[href]");
        for (Element e : elements) {
            links.add(e.attr("href"));
        }
        LOG.info("{}", emails);
        LOG.info("{}", links);
    }

    public static Document getDocument(final String url) throws IOException {
        Connection connect = Jsoup.connect(url);
        if (!ExtractUtils.isNotProxied()) {
            addProxyAuthorization(connect);
        }
        return connect.userAgent(USER_AGENT).get();
    }

    public static Document getDocument(String url, Map<String, String> cookies) throws IOException {
        Response execute = executeRequest(url, cookies);
        Map<String, String> cookies2 = execute.cookies();
        cookies.putAll(cookies2);
        return execute.parse();
    }

    public static List<String> getTables(Element renderPage) {
        Elements select = renderPage.select("table");
        List<String> tablesFound = new ArrayList<>();
        for (Element table : select) {
            String tableElements = table.children().stream().flatMap(tbody -> tbody.children().stream())
                    .map(tr -> tr.children().stream().map(Element::text).collect(Collectors.joining("\t")))
                    .collect(Collectors.joining("\n"));
            tablesFound.add(tableElements);
        }
        return tablesFound;
    }

    // Parsing JavaScript Generated Page with Jsoup and HtmUnit About.html - source
    // code
    public static Document normalParse(File in) throws IOException {
        return Jsoup.parse(in, "UTF-8");
    }

    // How to parse my page as rendered in the browser?
    public static void renderedInTheBrowser(URL url) throws IOException {
        // load page using HTML Unit and fire scripts
        WebClient webClient = new WebClient();
        HtmlPage myPage = webClient.getPage(url);
        // convert page to generated HTML and convert to document
        Document doc = Jsoup.parse(myPage.asXml());
        // iterate row and col
        for (Element row : doc.select("table#data > tbody > tr")) {
            for (Element col : row.select("td")) {
                // print results
                String ownText = col.ownText();
                LOG.info("{}", ownText);
            }
        }
        // clean up resources
        webClient.closeAllWindows();
    }

    // Selecting elements using CSS selectors
    public static void selectingElements() {
        String html = "<!DOCTYPE html>" + "<html>" + "<head>" + "<title>Hello world!</title>" + "</head>" + "<body>"
                + "<h1>Hello there!</h1>" + "<p>First paragraph</p>" + "<p class=\"not-first\">Second paragraph</p>"
                + "<p class=\"not-first third\">Third <a href=\"About.html\">paragraph</a></p>" + "</body>" + "</html>";
        // Parse the document
        Document doc = Jsoup.parse(html);
        // Get document title
        String title = doc.select("head > title").first().text();
        LOG.info("{}", title); // Hello world!
        // Get all paragraphs except from the first
        // Same as
        Elements otherParagraphs = doc.select("p");
        otherParagraphs.remove(0);
        // Get the third paragraph (second in the list otherParagraphs which
        // excludes the first paragraph)
        // Alternative:
        Element thirdParagraph = doc.select("p.third").first();
        // You can also select within elements, e.g. anchors with a href attribute
        // within the third paragraph.
        Element link = thirdParagraph.select("a[href]").first();
        LOG.info("link {}", link);
        // or the first <h1> element in the document body
        Element headline = doc.select("body").first().select("h1").first();
        LOG.info("headline {}", headline);
    }

    private static void addProxyAuthorization(Connection connect) {
        connect.header("Proxy-Authorization", "Basic " + ExtractUtils.getEncodedAuthorization());
    }
}
