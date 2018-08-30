package audio.mp3;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.Customsearch.Cse;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import simplebuilder.HasLogging;

public class GoogleImagesUtils {

    private static final Logger LOGGER = HasLogging.log(GoogleImagesUtils.class);

    public static List<String> getImagens(String artista) {

        try {
            HttpTransport transport = new ApacheHttpTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            Customsearch build = new Customsearch.Builder(transport, jsonFactory, null)
                    .setCustomsearchRequestInitializer(
                            new CustomsearchRequestInitializer("AIzaSyBAsSX8EPLHAZlother07UAMPF7vqBA2dWcisc"))
                    .setApplicationName("wdmsim").build();
            Cse cse = build.cse();

            Search execute = cse.list(artista).setCx("001081779786768539865:7f2uwv0iufy").setSearchType("image")
                    .execute();

            List<Result> items = execute.getItems();
            if (items != null) {
                return items.stream().map(Result::getLink).collect(Collectors.toList());
            }

        } catch (Exception e) {
            LOGGER.trace("", e);
        }
        return Collections.emptyList();
    }

}
