package election;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerCities2018Task extends CommonCrawlerTask<String> {

    private static final String ELEICOES_2018_URL = "https://www.todapolitica.com";

    private CandidatoDAO candidatoDAO = new CandidatoDAO();

    private List<String> estados = Arrays.asList("acre", "alagoas", "amazonas", "amapa", "bahia", "ceara",
        "distrito-federal", "espirito-santo", "goias", "maranhao", "minas-gerais", "mato-grosso-sul", "mato-grosso",
        "para", "paraiba", "pernambuco", "piaui", "parana", "rio-janeiro", "rio-grande-norte", "rondonia", "roraima",
        "rio-grande-sul", "santa-catarina", "sergipe", "sao-paulo", "tocantins");
    private Map<String, String> estadosMap = ImmutableMap.<String, String>builder().put("acre", "AC")
        .put("alagoas", "AL").put("amazonas", "AM").put("amapa", "AP").put("bahia", "BA").put("ceara", "CE")
        .put("distrito-federal", "DF").put("espirito-santo", "ES").put("goias", "GO").put("maranhao", "MA")
        .put("minas-gerais", "MG").put("mato-grosso-sul", "MS").put("mato-grosso", "MT").put("para", "PA")
        .put("paraiba", "PB").put("pernambuco", "PE").put("piaui", "PI").put("parana", "PR").put("rio-janeiro", "RJ")
        .put("rio-grande-norte", "RN").put("rondonia", "RO").put("roraima", "RR").put("rio-grande-sul", "RS")
        .put("santa-catarina", "SC").put("sergipe", "SE").put("sao-paulo", "SP").put("tocantins", "TO").build();
    private List<String> cargos = Arrays.asList("senador", "governador", "deputado-federal", "deputado-estadual");

    @Override
    protected List<String> getList() {

        List<String> urls = new ArrayList<>();

        urls.add("candidatos-presidencia");
        for (String estado : estados) {
            for (String cargo : cargos) {
                if ("deputado-estadual".equals(cargo) && "distrito-federal".equals(estado)) {
                    cargo = "deputado-distrital";
                }
                urls.add("candidatos-" + cargo + "-" + estado);
            }
        }

        return urls;
    }

    @Override
    protected void performTask(String estado) {

        String es = estados.stream().filter(estado::contains).findFirst().orElse("brasil");
        String cargo = estado.contains("presidencia") ? "presidente"
            : cargos.stream().filter(estado::contains).findFirst().orElse("deputado-distrital");
        int i = 1;
        while (true) {
            try {
                Document parse = getDocument(getUrl(estado, i));

                Elements select = parse.select(".card-candidate-results");
                boolean umEleito = false;

                for (int j = select.size() - 1; j >= 0; j--) {
                    Element element = select.get(j);
                    Candidato candidato = new Candidato();
                    candidato.setEstado(estadosMap.getOrDefault(es, "BR"));
                    candidato.setCargo(cargo);
                    candidato.setFotoUrl(element.select("img").attr("src"));
                    String href = element.attr("href");
                    candidato.setHref(href);
                    candidato.setNome(element.select(".candidate-name").text());
                    String text = element.select(".candidate-name").text();
                    candidato.setNumero(convertNumerico(text));
                    candidato.setPartido(element.select(".candidate-party").text().split(" - ")[0]);
                    candidato.setVotos(convertNumerico(element.select(".number-votes").first().text()));
                    String text2 = element.select(".elect-state").text().trim();
                    boolean equals = "Eleito".equals(text2);
                    if (equals) {
                        umEleito = true;
                    }
                    candidato.setEleito(equals);
                    Document detailsDocument = getDocument(ELEICOES_2018_URL + href);
                    Elements select2 = detailsDocument.select(".info-candidato");
                    Elements children = select2.first().children();
                    String nomeCompleto = children.get(0).child(1).text();
                    candidato.setFotoUrl(detailsDocument.select(".candidate-photo img").attr("src"));
                    candidato.setNomeCompleto(nomeCompleto);
                    candidato.setNascimento(extractDate(children.get(2).child(1).text()));
                    candidato.setNaturalidade(children.get(3).child(1).text());
                    candidato.setOcupacao(children.get(5).child(1).text());
                    candidato.setGrauInstrucao(children.get(6).child(1).text());
                    candidatoDAO.saveOrUpdate(candidato);
                }
                if (select.isEmpty()) {
                    if (i == 1) {
                        getLogger().error("NOT FOUND {}", estado);
                    }
                    break;
                }
                if (!umEleito) {
                    getLogger().error("ERRO in {}", estado);
                }
                i++;
            } catch (Exception e) {
                getLogger().error("ERRO cidade {}", estado);
                getLogger().trace("ERRO cidade " + estado, e);
                return;
            }
        }
    }

    private static String getUrl(String estado, int i) {
        if (i == 1) {
            return ELEICOES_2018_URL + "/eleicoes-2018/" + estado + "/";
        }
        return ELEICOES_2018_URL + "/eleicoes-2018/" + estado + "/" + i + "/";
    }

}