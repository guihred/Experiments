package extract;

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
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.ex.HasLogging;
public final class JsoupUtils {
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0";
    private static final Logger LOG = HasLogging.log();

    private JsoupUtils() {
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

    public static Response executeRequest(String url, Map<String, String> cookies) throws IOException {
        Connection connect = HttpConnection.connect(url);
        if (!ExtractUtils.isNotProxied()) {
            addProxyAuthorization(connect);
        }
        connect.timeout(ExtractUtils.HUNDRED_SECONDS);
        connect.cookies(cookies);
        connect.ignoreContentType(true);
        connect.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0");
        return connect.execute();
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
        List<String> arrayList = new ArrayList<>();
        for (String twitterTag : twitterTags) {
            // find a matching meta tag
            Element meta = doc.select("meta[name=" + twitterTag + "]").first();
            // if found, get the value of the content attribute
            String content = meta != null ? meta.attr("content") : "";
            // display results
            arrayList.add(twitterTag + "=" + content);
        }
        return arrayList;
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
        return connect
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101         Firefox/52.0")
                .get();
    }

    public static Document getDocument(String url, Map<String, String> cookies) throws IOException {
        Response execute = executeRequest(url, cookies);
        Map<String, String> cookies2 = execute.cookies();
        cookies.putAll(cookies2);
        return execute.parse();
    }

    public static List<String> getTables(Element renderPage) {
        Elements select = renderPage.select("table");
        List<String> arrayList = new ArrayList<>();
        for (Element table : select) {
            String collect = table.children().stream().flatMap(tbody -> tbody.children().stream())
                    .map(tr -> tr.children().stream().map(Element::text).collect(Collectors.joining("\t")))
                    .collect(Collectors.joining("\n"));
            arrayList.add(collect);
        }
        return arrayList;
    }

    // Below is an example request that will log you into the GitHub website
    // # Constants used in this example
    public static void loginGitHub(String username, String pass) throws IOException {
        final String LOGIN_FORM_URL = "https://github.com/login";
        final String LOGIN_ACTION_URL = "https://github.com/session";
        // # Go to login page and grab cookies sent by server
        Connection.Response loginForm =
                Jsoup.connect(LOGIN_FORM_URL).method(Connection.Method.GET).userAgent(USER_AGENT).execute();
        Document loginDoc = loginForm.parse(); // this is the document containing response html
        HashMap<String, String> cookies = new HashMap<>(loginForm.cookies()); // save the cookies to be passed on to
                                                                              // next request
        // # Prepare login credentials
        String authToken = loginDoc.select("#login > form > div:nth-child(1) > input[type=\"hidden\"]:nth-child(2)")
                .first().attr("value");
        HashMap<String, String> formData = new HashMap<>();
        formData.put("commit", "Sign in");
        formData.put("utf8", "e2 9c 93");
        formData.put("login", username);
        formData.put("pass" + "word", pass);
        formData.put("authenticity_token", authToken);
        // # Now send the form for login
        Response homePage = Jsoup.connect(LOGIN_ACTION_URL).cookies(cookies).data(formData)
                .method(Connection.Method.POST).userAgent(USER_AGENT).execute();
        String html = homePage.parse().html();
        LOG.info("{}", html);
    }

    // Parsing JavaScript Generated Page with Jsoup and HtmUnit About.html - source
    // code
    public static void normalParse(File in) throws IOException {
        Document doc = Jsoup.parse(in, "UTF-8");
        // iterate over row and col
        for (Element row : doc.select("table#data > tbody > tr")) {
            for (Element col : row.select("td")) {
                // print results
                String ownText = col.ownText();
                LOG.info("{}", ownText);
            }
        }
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

    public static Response simpleAuthentication(String url, String value, String value2) throws IOException {
        return Jsoup.connect(url).userAgent(USER_AGENT).data("username", value).data("pass" + "word", value2)
                .method(Method.POST).execute();
    }


    private static void addProxyAuthorization(Connection connect) {
        connect.header("Proxy-Authorization", "Basic " + ExtractUtils.getEncodedAuthorization());
    }
}
