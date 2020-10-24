package fxtests;

import ethical.hacker.WebBrowserApplication;
import extract.WordService;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.scene.image.Image;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import utils.ExtractUtils;
import utils.FileTreeWalker;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FXKibanaReportTest extends AbstractTestExecution {
    @Test
    public void testWordReport() {

        List<String> asList = Arrays.asList(
                "https://n321p000124.fast.prevnet/app/kibana#/visualize/edit/191e1a10-9e56-11e9-bffd-37ab21b02682?_g="
                        + "(refreshInterval:(pause:!t,value:0),time:(from:now-24h,mode:quick,"
                        + "to:now))&_a=(filters:!(('$state':(store:appState),meta:(alias:!n,disabled:!f,"
                        + "index:'3008cad0-f1b4-11e8-a078-93662b30045b',key:clientip.keyword,negate:!f,"
                        + "params:!('191.96.73.211'),type:phrases,value:'191.96.73.211'),"
                        + "query:(bool:(minimum_should_match:1,"
                        + "should:!((match_phrase:(clientip.keyword:'191.96.73.211'))))))),linked:!f,"
                        + "query:(language:lucene,query:''),uiState:(vis:(params:(sort:(columnIndex:!n,"
                        + "direction:!n)))),vis:(aggs:!((enabled:!t,id:'1',params:(),schema:metric,"
                        + "type:count),(enabled:!t,id:'2',params:(field:dtpsistema.keyword,"
                        + "missingBucket:!f,missingBucketLabel:Missing,order:desc,orderBy:'1',"
                        + "otherBucket:!f,otherBucketLabel:Other,size:200),schema:bucket,"
                        + "type:terms)),params:(perPage:10,showMetricsAtAllLevels:!f,showPartialRows:!f,"
                        + "showTotal:!f,sort:(columnIndex:!n,direction:!n),totalFunc:sum),"
                        + "title:'%5BAcesso%20Web%5D%20Quantidade%20de%20Acessos%20por%20Sistema',type:table))");

        String finalIP = "191.96.73.227";
        List<Object> images = getImages(asList, finalIP);
        Map<String, Object> mapaSubstituicao = new HashMap<>();
        File file = ResourceFXUtils.getOutFile("docx/resultado.docx");
        mapaSubstituicao.put("Figuras da Evidência / Contexto", images);
        mapaSubstituicao.put("Informar data de início/fim", "24/10/2020");
        mapaSubstituicao.put("Informar os indicadores de ameaça",
                Arrays.asList("- Volumetria de acessos acima da média dos demais usuários",
                        "- Volumetria de dados alta", "- Padrão de acesso automatizado",
                        "- Diversos CPFs acessados, considerando que o Meu INSS é para uso", "individual do cidadão."));
        mapaSubstituicao.put(
                "Acessos volumétricos originados do IP 191.235.64.85 direcionados ao vip-pmeuinssprxr.inss.gov.br",
                "Acessos volumétricos originados do IP " + finalIP + " direcionados ao vip-pmeuinssprxr.inss.gov.br ");
        WordService.getWord(mapaSubstituicao, "ModeloGeralReporte.docx", file);
    }

    private List<Object> getImages(List<String> asList, String finalIP) {
        if (ExtractUtils.isNotProxied()) {
            File outFile = ResourceFXUtils.getOutFile("png");
            return FileTreeWalker
                    .getFirstFileMatch(outFile, p -> p.getFileName().toString().matches("snapshot\\d+.png")).stream()
                    .map(FunctionEx.makeFunction(p -> new Image(p.toUri().toURL().toExternalForm())))
                    .collect(Collectors.toList());

        }

        List<Object> images = new ArrayList<>();
        WebBrowserApplication show = show(WebBrowserApplication.class);
        for (String kibanaURL : asList) {
            String replaceAll = kibanaURL.replaceAll("191.96.73.211", finalIP);
            interactNoWait(() -> show.loadSite(replaceAll));
            measureTime("WordService.getWord", () -> {
                AtomicBoolean atomicBoolean = new AtomicBoolean(true);
                while (atomicBoolean.get()) {
                    interactNoWait(() -> {
                        boolean loading = show.isLoading();
                        atomicBoolean.set(loading);
                        return loading;
                    });
                    sleep(2000);
                }

                interactNoWait(RunnableEx.make(() -> {
                    File saveHtmlImage = show.saveHtmlImage();
                    Image value = new Image(ResourceFXUtils.convertToURL(saveHtmlImage).toExternalForm());
                    images.add(value);
                }));
            });
        }
        return images;
    }

}