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
import utils.DateFormatUtils;
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
                        + "otherBucket:!f,otherBucketLabel:Other,size:200),schema:bucket,type:terms)),"
                        + "params:(perPage:10,showMetricsAtAllLevels:!f,showPartialRows:!f,"
                        + "showTotal:!f,sort:(columnIndex:!n,direction:!n),totalFunc:sum),"
                        + "title:'%5BAcesso%20Web%5D%20Quantidade%20de%20Acessos%20por%20Sistema',type:table))",
                "https://n321p000124.fast.prevnet/app/kibana#/visualize/edit/46ce7d10-fa2d-11e8-a078-93662b30045b?_g=(refreshInterval:(pause:!t,value:0),"
                        + "time:(from:now-24h,mode:quick,to:now))&_a=(filters:!(('$state':(store:appState),"
                        + "meta:(alias:!n,disabled:!f,index:'3008cad0-f1b4-11e8-a078-93662b30045b',key:clientip.keyword,negate:!t,params:(query:'127.0.0.1',type:phrase),"
                        + "type:phrase,value:'127.0.0.1'),"
                        + "query:(match:(clientip.keyword:(query:'127.0.0.1',type:phrase)))),"
                        + "('$state':(store:appState),"
                        + "meta:(alias:!n,disabled:!f,index:'3008cad0-f1b4-11e8-a078-93662b30045b',key:clientip.keyword,negate:!f,params:!('191.96.73.211'),"
                        + "type:phrases,value:'191.96.73.211'),"
                        + "query:(bool:(minimum_should_match:1,should:!((match_phrase:(clientip.keyword:'191.96.73.211'))))))),"
                        + "linked:!f,query:(language:lucene,query:''),"
                        + "uiState:(vis:(params:(sort:(columnIndex:!n,direction:!n)))),"
                        + "vis:(aggs:!((enabled:!t,id:'1',params:(),schema:metric,type:count),"
                        + "(enabled:!t,id:'2',params:(field:clientip.keyword,missingBucket:!f,missingBucketLabel:Missing,order:desc,orderBy:'1',otherBucket:!f,otherBucketLabel:Other,size:100),"
                        + "schema:bucket,type:terms)),"
                        + "params:(perPage:10,showMetricsAtAllLevels:!f,showPartialRows:!f,showTotal:!f,sort:(columnIndex:!n,direction:!n),"
                        + "totalFunc:sum),"
                        + "title:'%5BAcesso%20Web%5D%20Top%20Clientes%20por%20n%C3%BAmero%20de%20requisi%C3%A7%C3%B5es',type:table))",
                "https://n321p000124.fast.prevnet/app/kibana#/visualize/edit/feecd360-fa19-11e8-a078-93662b30045b?_g=(refreshInterval:(pause:!t,value:0),"
                        + "time:(from:now-24h,mode:quick,to:now))&_a=(filters:!(('$state':(store:appState),"
                        + "meta:(alias:!n,disabled:!f,index:'3008cad0-f1b4-11e8-a078-93662b30045b',key:dtpcategoria,negate:!f,params:(query:access,type:phrase),"
                        + "type:phrase,value:access),query:(match:(dtpcategoria:(query:access,type:phrase)))),"
                        + "('$state':(store:appState),"
                        + "meta:(alias:!n,disabled:!f,index:'3008cad0-f1b4-11e8-a078-93662b30045b',key:clientip.keyword,negate:!f,params:!('191.96.73.211'),"
                        + "type:phrases,value:'191.96.73.211'),"
                        + "query:(bool:(minimum_should_match:1,should:!((match_phrase:(clientip.keyword:'191.96.73.211'))))))),"
                        + "linked:!f,query:(language:lucene,query:''),"
                        + "uiState:(vis:(params:(sort:(columnIndex:!n,direction:!n)))),"
                        + "vis:(aggs:!((enabled:!t,id:'1',params:(),schema:metric,type:count),"
                        + "(enabled:!t,id:'2',params:(field:request.keyword,missingBucket:!f,missingBucketLabel:Missing,order:desc,orderBy:'1',otherBucket:!f,otherBucketLabel:Other,size:500),"
                        + "schema:bucket,type:terms),"
                        + "(enabled:!t,id:'3',params:(field:dtpsistema.keyword,missingBucket:!f,missingBucketLabel:Missing,order:desc,orderBy:'1',otherBucket:!f,otherBucketLabel:Other,size:50),"
                        + "schema:bucket,type:terms)),"
                        + "params:(perPage:10,showMetricsAtAllLevels:!f,showPartialRows:!f,showTotal:!f,sort:(columnIndex:!n,direction:!n),"
                        + "totalFunc:sum),title:'%5BAcesso%20WEB%5D%20TOP%20Request%20PATH',type:table))",
                "https://n321p000124.fast.prevnet/app/kibana#/visualize/edit/bb17efa0-fa17-11e8-a078-93662b30045b?_g=(refreshInterval:(pause:!t,value:0),"
                        + "time:(from:now-24h,mode:quick,to:now))&_a=(filters:!(('$state':(store:appState),"
                        + "meta:(alias:!n,disabled:!f,index:'3008cad0-f1b4-11e8-a078-93662b30045b',key:clientip.keyword,negate:!f,params:!('191.96.73.211'),"
                        + "type:phrases,value:'191.96.73.211'),"
                        + "query:(bool:(minimum_should_match:1,should:!((match_phrase:(clientip.keyword:'191.96.73.211'))))))),"
                        + "linked:!f,query:(language:kuery,query:''),uiState:(),vis:(aggs:!(),"
                        + "params:(expression:'.es(index%3D*apache-prod*,q%3D%22dtptype:nginx%20OR%20dtptype:apache%20OR%20dtptype:varnish%22,split%3D%20clientip.keyword:5).label(!'$1!',!'.*%3E.*:(.*)%3E.*!')',interval:auto),"
                        + "title:'%5BAcesso%20Web%5D%20%20Top%20Clientes%20por%20n%C3%BAmero%20de%20requisi%C3%A7%C3%B5es%20-%20Timeline',type:timelion))");

        String finalIP = "191.242.238.223";
        List<Object> images = getImages(asList, finalIP);
        Map<String, Object> mapaSubstituicao = new HashMap<>();
        File file = ResourceFXUtils.getOutFile("docx/resultado" + finalIP + ".docx");
        mapaSubstituicao.put("Figuras da Evidência / Contexto", images);
        mapaSubstituicao.put("Informar data de início/fim", DateFormatUtils.currentDate());
        mapaSubstituicao.put("Informar os indicadores de ameaça",
                Arrays.asList("- Volumetria de acessos acima da média dos demais usuários",
                        "- Volumetria de dados alta", "- Padrão de acesso automatizado",
                        "- Diversos CPFs acessados, considerando que o Meu INSS é para uso individual do cidadão."));
        mapaSubstituicao.put(
                "Acessos volumétricos originados do IP 191.235.64.85 direcionados ao vip-pmeuinssprxr.inss.gov.br",
                "Acessos volumétricos originados do IP " + finalIP + " direcionados ao vip-pmeuinssprxr.inss.gov.br ");
        WordService.getWord(mapaSubstituicao, "ModeloGeralReporte.docx", file);
    }

    private List<Object> getImages(List<String> urls, String finalIP) {
        if (ExtractUtils.isNotProxied()) {
            File outFile = ResourceFXUtils.getOutFile("png");
            return FileTreeWalker
                    .getFirstFileMatch(outFile, p -> p.getFileName().toString().matches("snapshot\\d+.png")).stream()
                    .sorted(Comparator.comparing(DateFormatUtils::getCreationDate).reversed())
                    .map(FunctionEx.makeFunction(p -> new Image(p.toUri().toURL().toExternalForm())))
                    .limit(urls.size())
                    .collect(Collectors.toList());

        }

        List<Object> images = new ArrayList<>();
        WebBrowserApplication show = show(WebBrowserApplication.class);
        for (String kibanaURL : urls) {
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
                    sleep(5000);
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