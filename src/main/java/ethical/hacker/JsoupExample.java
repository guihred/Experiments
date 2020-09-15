package ethical.hacker;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.assertj.core.api.exception.RuntimeIOException;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.ConsumerEx;
import utils.ex.HasLogging;

@SuppressWarnings({ "unused", "static-method" })
public class JsoupExample {
    private static final String MOZILLA = "Mozilla";
    private static final String ABOUT_HTML = "About.html";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/51.0.2704.103 Safari/537.36";

    private static final Logger log = HasLogging.log();

    public void filterMailToLinks() throws IOException {
        Document doc = Jsoup.connect("http://stackoverflow.com/questions/15893655/").userAgent(MOZILLA).get();

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

        log.info("{}", emails);
        log.info("{}", links);

    }

    // Display all elements as block

    private void displayAllElements() throws IOException {
        String html = Files.toString(ResourceFXUtils.toFile(ABOUT_HTML), StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);
        String html2 = doc.html();
        log.info("{}", html2);
    }

    // To display the output with each element treated as a block element, the
    // outline option has to be
    // enabled on the document's OutputSettings.
    private void displayAllElementsWithOutline() throws IOException {
        String html = Files.toString(ResourceFXUtils.toFile(ABOUT_HTML), StandardCharsets.UTF_8);

        Document doc = Jsoup.parse(html);

        doc.outputSettings().outline(true);

        String html2 = doc.html();
        log.info("{}", html2);
    }

    // A simple authentication POST request with Jsoup
    // Extract the data from HTML document file
    private void extractDataFromHTML() throws IOException {

        File inputFile = ResourceFXUtils.toFile(ABOUT_HTML);
        // load file
        // parse file as HTML document
        Document doc = Jsoup.parse(inputFile, "UTF-8");
        // select element by <a>
        Elements elements = doc.select("a");
        log.info("{}", elements);
    }

    // Extracting JavaScript data with Jsoup
    // create HTML with JavaScript data
    private void extractingJavaScriptDataWithJsoup() {

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
        log.info("{}", doc);
    }

    // Extracting all the URLs from a website using JSoup (recursion)
    // Extract Twitter Markup
    private void extractTwitterMarkup() throws IOException {

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

        for (String twitterTag : twitterTags) {

            // find a matching meta tag
            Element meta = doc.select("meta[name=" + twitterTag + "]").first();

            // if found, get the value of the content attribute
            String content = meta != null ? meta.attr("content") : "";

            // display results
            log.info("{} = {}%n", twitterTag, content);
        }
    }
    // Extracting email adresses & links to other pages

    // Extract the URLs and titles of links
    private void extractURL() throws IOException {

        Document doc = Jsoup.connect("http://stackoverflow.com").userAgent(MOZILLA).get();
        for (Element e : doc.select("a.question-hyperlink")) {
            String attr = e.attr("abs:href");
            log.info("{}", attr);
            String text = e.text();
            log.info("{}", text);
        }
    }

    // Extract full URL from partial HTML
    private void extractURLPartialHTML() {

        String bodyFragment = "<div><a href=\"/documentation\">Stack Overflow Documentation</a></div>";

        Document doc = Jsoup.parseBodyFragment(bodyFragment);
        String link = doc.select("div > a").first().attr("href");

        log.info("{}", link);
        doc = Jsoup.parseBodyFragment(bodyFragment, "http://stackoverflow.com");

        link = doc.select("div > a").first().absUrl("href");

        log.info("{}", link);
    }

    // Below is an example request that will log you into the GitHub website
    // # Constants used in this example
    private void loginGitHub() throws IOException {
        final String LOGIN_FORM_URL = "https://github.com/login";
        final String LOGIN_ACTION_URL = "https://github.com/session";
        final String USERNAME = "yourUsername";
        final String password = "yourPassword";

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
        formData.put("login", USERNAME);
        formData.put("password", password);
        formData.put("authenticity_token", authToken);

        // # Now send the form for login
        Response homePage = Jsoup.connect(LOGIN_ACTION_URL).cookies(cookies).data(formData)
                .method(Connection.Method.POST).userAgent(USER_AGENT).execute();

        String html = homePage.parse().html();
        log.info("{}", html);
    }

    // Parsing JavaScript Generated Page with Jsoup and HtmUnit About.html - source
    // code
    private void normalParse() throws IOException {

        Document doc = Jsoup.parse(new File(ABOUT_HTML), "UTF-8");

        // iterate over row and col
        for (Element row : doc.select("table#data > tbody > tr")) {
            for (Element col : row.select("td")) {
                // print results
                String ownText = col.ownText();
                log.info("{}", ownText);
            }
        }
    }

    // How to parse my page as rendered in the browser?
    private void renderedInTheBrowser() throws IOException {

        // load page using HTML Unit and fire scripts
        WebClient webClient = new WebClient();
        HtmlPage myPage = webClient.getPage(new File(ABOUT_HTML).toURI().toURL());

        // convert page to generated HTML and convert to document
        Document doc = Jsoup.parse(myPage.asXml());

        // iterate row and col
        for (Element row : doc.select("table#data > tbody > tr")) {
            for (Element col : row.select("td")) {
                // print results
                String ownText = col.ownText();
                log.info("{}", ownText);
            }
        }

        // clean up resources
        webClient.closeAllWindows();

    }

    // Selecting elements using CSS selectors
    private void selectingElements() {

        String html = "<!DOCTYPE html>" + "<html>" + "<head>" + "<title>Hello world!</title>" + "</head>" + "<body>"
                + "<h1>Hello there!</h1>" + "<p>First paragraph</p>" + "<p class=\"not-first\">Second paragraph</p>"
                + "<p class=\"not-first third\">Third <a href=\"About.html\">paragraph</a></p>" + "</body>" + "</html>";

        // Parse the document
        Document doc = Jsoup.parse(html);

        // Get document title
        String title = doc.select("head > title").first().text();
        log.info("{}", title); // Hello world!

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
        log.info("link {}", link);
        // or the first <h1> element in the document body
        Element headline = doc.select("body").first().select("h1").first();
        log.info("headline {}", headline);
    }

    private void simpleAuthentication() throws IOException {

        Connection.Response loginResponse = Jsoup.connect("yourWebsite.com/loginUrl").userAgent(USER_AGENT)
                .data("username", "yourUsername").data("password", "yourPassword").method(Method.POST).execute();
        log.info("{}", loginResponse);
    }

    public static void checkElement(String name, Element elem) {
        if (elem == null) {
            throw new RuntimeIOException("Unable to find " + name);
        }
    }

    public static class ReadAllLinks {

        private Set<String> uniqueURL = new HashSet<>();
        private String my_site;

        public void getLinks(URL url) throws IOException {
            my_site = url.getHost();
            getLinks(url.toString());
        }

        private void getLinks(String url) throws IOException {
            Document doc = Jsoup.connect(url).userAgent(MOZILLA).get();
            Elements links = doc.select("a");

            if (links.isEmpty()) {
                return;
            }

            links.stream().map(link -> link.attr("abs:href")).forEachOrdered(ConsumerEx.makeConsumer(thisUrl -> {
                boolean add = uniqueURL.add(thisUrl);
                if (add && thisUrl.contains(my_site)) {
                    log.info("{}", thisUrl);
                    getLinks(thisUrl);
                }
            }));

        }
    }

}
