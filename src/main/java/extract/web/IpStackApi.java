package extract.web;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import utils.ProjectProperties;
import utils.ResourceFXUtils;

public final class IpStackApi {
    private static final String IP_STACK_APIKEY = ProjectProperties.getField();

    private IpStackApi() {
    }

    public static Map<String, String> getIPGeoInformation(String ip) throws IOException {
        File outFile = newJsonFile(ip + "geoip");
        if (!outFile.exists()) {
            getFromURL("http://api.ipstack.com/" + ip, outFile);
        }
        Map<String, Object> geoInfo =
                JsonExtractor.toObject(outFile, "city", "region_code", "country_code");
        return JsonExtractor.accessMap(geoInfo);
    }

    public static void main(String[] args) throws IOException {
        getIPGeoInformation("152.250.210.24");
    }

    private static void getFromURL(String url, File outFile) throws IOException {
        PhantomJSUtils.makeGet(url + "?access_key=" + IP_STACK_APIKEY, new HashMap<>(), outFile);
    }

    private static File newJsonFile(String string) {
        String replaceAll = string.replaceAll("[:/\\?]+", "_");
        return ResourceFXUtils.getOutFile("json/" + replaceAll + ".json");
    }
}
