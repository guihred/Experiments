package ethical.hacker;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class SitePage {
    private String title;
    private final String url;
    private List<String> links;

    private File print;

    public SitePage(String url) {
        this.url = url;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SitePage other = (SitePage) obj;
        return Objects.equals(other.url, url);
    }

    public List<String> getLinks() {
        return links;
    }

    public File getPrint() {
        return print;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public void setPrint(File print) {
        this.print = print;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return Stream.of(getTitle(), getUrl()).filter(StringUtils::isNotBlank).collect(Collectors.joining(" "));
    }
}