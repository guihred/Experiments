package neuro;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simplebuilder.ResourceFXUtils;

public class BrazilianVerbsConjugator {
	enum Mode {
		PRESENT, PRETERITE, IMPERFECT, PLUPERFECT, FUTURE, CONDITIONAL;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(BrazilianVerbsConjugator.class);

	private static final boolean DEBUG = false;

	private static Map<Mode, String[]> first = new EnumMap<>(Mode.class);

	private static Map<Mode, String[]> fourth = new EnumMap<>(Mode.class);

	private static final Map<String, List<String>> IRREGULAR = ImmutableMap.<String, List<String>>builder()
			.put("ir", Arrays.asList(""))
			.put("dar", Arrays.asList(""))
			.put("ser", Arrays.asList(""))
			.put("crer", Arrays.asList(""))
			.put("caber", Arrays.asList(""))
			.put("estar", Arrays.asList(""))
			.put("poder", Arrays.asList(""))
			.put("ouvir", Arrays.asList(""))
			.put("saber", Arrays.asList(""))
			.put("medir", Arrays.asList(""))
			.put("subir", Arrays.asList(""))
			.put("fugir", Arrays.asList(""))
			.put("servir", Arrays.asList(""))
			.put("perder", Arrays.asList(""))
			.put("trazer", Arrays.asList(""))
			.put("dormir", Arrays.asList(""))
			.put("entupir", Arrays.asList(""))
			.put("ler", Arrays.asList("", "re"))
			.put("rir", Arrays.asList("", "sor"))
			.put("acudir", Arrays.asList("", "s"))
			.put("aver", Arrays.asList("h", "re"))
			.put("sumir", Arrays.asList("", "con"))
			.put("valer", Arrays.asList("", "equi"))
			.put("vertir", Arrays.asList("di", "ad"))
			.put("cobrir", Arrays.asList("", "des","en"))
			.put("ulir", Arrays.asList("b", "reb", "escap"))
			.put("ver", Arrays.asList("", "ante", "pre", "re"))
			.put("querer", Arrays.asList("", "des", "mal", "re"))
			.put("seguir", Arrays.asList("","con", "per", "pros"))
			.put("gredir", Arrays.asList("a", "re", "trans", "pro"))
			.put("pedir", Arrays.asList("", "desim", "des", "ex", "im"))
			.put("sentir", Arrays.asList("","as", "con", "pres", "res" ))
			.put("iar", Arrays.asList("od", "remed", "med", "intermed", "incend", "ans"))
			.put("ter", Arrays.asList("", "abs", "con", "de", "entre", "man", "ob", "re", "sus", "ver"))
			.put("dizer", Arrays.asList("", "ante", "ben", "con", "contra", "des", "inter", "mal", "pre"))
			.put("fazer", Arrays.asList("", "carni", "contra", "lique", "mal", "per", "putre", "rare", "satis"))
			.put("vir", Arrays.asList("", "ad", "a", "contra", "con", "desa", "de", "inter", "pro", "re", "sobre", "ante"))
			.put("uzir",Arrays.asList("abd", "ad", "cond", "contraprod", "ded", "ind", "introd", "l", "prod", "red", "rel","reprod", "retrad", "sed", "sobrel", "trad", "tremel"))
			.put("erir", Arrays.asList("f", "transf", "ref", "prof", "pref", "interf", "inf", "indef", "dif", "disf", "def", "desf", "conf", "af", "auf", "ins", "ad", "dig", "g", "ing", "pret", "sug"))
			.build();

	private static Map<Mode, String[]> second = new EnumMap<>(Mode.class);

	private static final Map<String, Map<Mode, List<String>>> SIMPLE_IRREGULARITIES = ImmutableMap.<String, Map<Mode, List<String>>>builder()
			.put("vertir",ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT,Arrays.asList("virto", "vertes", "verte", "vertimos", "vertis", "vertem")).build())
			.put("servir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("sirvo", "serves", "serve", "servimos", "servis", "servem")).build())
			.put("iar", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("eio", "eias", "eia", "iamos", "iais", "eiam")).build())
			.put("subir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("subo", "sobes", "sobe", "subimos", "subis", "sobem")).build())
			.put("fugir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("fujo", "foges", "foge", "fugimos", "fugis", "fogem")).build())
			.put("valer", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("valho", "vales", "vale", "valemos", "valeis", "valem")).build())
			.put("dormir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("durmo", "dormes", "dorme", "dormimos", "dormis", "dormem")).build())
			.put("sumir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("sumo", "somes", "some", "sumimos", "sumis", "somem")).build())
			.put("acudir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("acudo", "acodes", "acode", "acudimos", "acudis", "acodem")).build())
			.put("entupir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("entupo", "entopes", "entope", "entupimos", "entupis", "entopem")).build())
			.put("ulir",ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("ulo", "oles", "ole", "ulimos", "ulis", "olem")).build())
			.put("uzir",ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("uzo", "uzes", "uz", "uzimos", "uzis", "uzem")).build())
			.put("ler", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("leio", "lês", "lê", "lemos", "leis", "leem")).build())
			.put("ouvir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("ouço", "ouves", "ouve", "ouvimos", "ouvis", "ouvem")).build())
			.put("perder", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("perco", "perdes", "perde", "perdemos", "perdeis", "perdem")).build())
			.put("crer", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("creio", "crês", "crê", "cremos", "credes", "creem")).build())
			.put("rir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("rio", "ris", "ri", "rimos", "rides", "riem")).build())
			.put("erir",ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("iro", "eres", "ere", "erimos", "eris", "erem")).build())
			.put("seguir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("sigo", "segues", "segue", "seguimos", "seguis", "seguem")).build())
			.put("sentir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("sinto", "sentes", "sente", "sentimos", "sentis", "sentem")).build())
			.put("cobrir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("cubro", "cobres", "cobre", "cobrimos", "cobris", "cobrem")).build())
			.put("gredir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("grido", "grides", "gride", "gredimos", "gredis", "gridem")).build())
			.put("medir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("meço", "medes", "mede", "medimos", "medis", "medem")).build())
			.put("pedir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("peço", "pedes", "pede", "pedimos", "pedis", "pedem")).build())
			.put("poder", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("posso", "podes", "pode", "podemos", "podeis", "podem"))
					.put(Mode.PRETERITE, Arrays.asList("pude", "pudeste", "pôde", "pudemos", "pudestes", "puderam"))
					.put(Mode.PLUPERFECT, Arrays.asList("pudera", "puderas", "pudera", "pudéramos", "pudéreis", "puderam")).build())
			.put("ver", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("vejo", "vês", "vê", "vemos", "vedes", "veem"))
					.put(Mode.PRETERITE, Arrays.asList("vi", "viste", "viu", "vimos", "vistes", "viram"))
					.put(Mode.PLUPERFECT, Arrays.asList("vira", "viras", "vira", "víramos", "víreis", "viram")).build())
			.put("dar", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("dou", "dás", "dá", "damos", "dais", "dão"))
					.put(Mode.PRETERITE, Arrays.asList("dei", "deste", "deu", "demos", "destes", "deram"))
					.put(Mode.PLUPERFECT, Arrays.asList("dera", "deras", "dera", "déramos", "déreis", "deram")).build())
			.put("vir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("venho", "véns", "vém", "vimos", "vindes", "vêm"))
					.put(Mode.PRETERITE, Arrays.asList("vim", "vieste", "veio", "viemos", "viestes", "vieram"))
					.put(Mode.IMPERFECT, Arrays.asList("vinha", "vinhas", "vinha", "vínhamos", "vínheis", "vinham"))
					.put(Mode.PLUPERFECT, Arrays.asList("viera", "vieras", "viera", "viéramos", "viéreis", "vieram")).build())
			.put("ter", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("tenho", "téns", "tém", "temos", "tendes", "têm"))
					.put(Mode.PRETERITE, Arrays.asList("tive", "tiveste", "teve", "tivemos", "tivestes", "tiveram"))
					.put(Mode.IMPERFECT, Arrays.asList("tinha", "tinhas", "tinha", "tínhamos", "tínheis", "tinham"))
					.put(Mode.PLUPERFECT, Arrays.asList("tivera", "tiveras", "tivera", "tivéramos", "tivéreis", "tiveram")).build())
			.put("aver", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("ei", "ás", "á", "avemos", "aveis", "ão"))
					.put(Mode.PRETERITE, Arrays.asList("ouve", "ouveste", "ouve", "ouvemos", "ouvestes", "ouveram"))
					.put(Mode.PLUPERFECT, Arrays.asList("ouvera", "ouveras", "ouvera", "ouvéramos", "ouvéreis", "ouveram"))
					.build())
			.put("querer", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("quero", "queres", "quer", "queremos", "quereis", "querem"))
					.put(Mode.PRETERITE, Arrays.asList("quis", "quiseste", "quis", "quisemos", "quisestes", "quiseram"))
					.put(Mode.PLUPERFECT, Arrays.asList("quisera", "quiseras", "quisera", "quiséramos", "quiséreis", "quiseram"))
					.build())
			.put("saber", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("sei", "sabes", "sabe", "sabemos", "sabeis", "sabem"))
					.put(Mode.PRETERITE, Arrays.asList("soube", "soubeste", "soube", "soubemos", "soubestes", "souberam"))
					.put(Mode.PLUPERFECT, Arrays.asList("soubera", "souberas", "soubera", "soubéramos", "soubéreis", "souberam"))
					.build())
			.put("caber", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("caibo", "cabes", "cabe", "cabemos", "cabeis", "cabem"))
					.put(Mode.PRETERITE, Arrays.asList("coube", "coubeste", "coube", "coubemos", "coubestes", "couberam"))
					.put(Mode.PLUPERFECT, Arrays.asList("coubera", "couberas", "coubera", "coubéramos", "coubéreis", "couberam"))
					.build())
			.put("estar", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("estou", "estás", "está", "estamos", "estais", "estão"))
					.put(Mode.PRETERITE, Arrays.asList("estive", "estiveste", "esteve", "estivemos", "estivestes", "estiveram"))
					.put(Mode.PLUPERFECT,Arrays.asList("estivera", "estiveras", "estivera", "estivéramos", "estivestes", "estiveram"))
					.build())
			.put("dizer", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("digo", "dizes", "diz", "dizemos", "dizeis", "dizem"))
					.put(Mode.PRETERITE, Arrays.asList("disse", "disseste", "disse", "dissemos", "dissestes", "disseram"))
					.put(Mode.PLUPERFECT, Arrays.asList("dissera", "disseras", "dissera", "disséramos", "disséreis", "disseram"))
					.put(Mode.FUTURE, Arrays.asList("direi", "dirás", "dirá", "diremos", "direis", "dirão"))
					.put(Mode.CONDITIONAL, Arrays.asList("diria", "dirias", "diria", "diríamos", "diríeis", "diriam"))
					.build())
			.put("trazer", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("trago", "trazes", "traz", "trazemos", "trazeis", "trazem"))
					.put(Mode.PRETERITE, Arrays.asList("trouxe", "trouxeste", "trouxe", "trouxemos", "trouxestes", "trouxeram"))
					.put(Mode.PLUPERFECT,Arrays.asList("trouxera", "trouxeras", "trouxera", "trouxéramos", "trouxéreis", "trouxeram"))
					.put(Mode.FUTURE, Arrays.asList("trarei", "trarás", "trará", "traremos", "trareis", "trarão"))
					.put(Mode.CONDITIONAL, Arrays.asList("traria", "trarias", "traria", "traríamos", "traríeis", "trariam"))
					.build())
			.put("ser", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("sou", "és", "é", "somos", "sois", "são"))
					.put(Mode.PRETERITE, Arrays.asList("fui", "foste", "foi", "fomos", "fostes", "foram"))
					.put(Mode.IMPERFECT, Arrays.asList("era", "eras", "era", "éramos", "éreis", "eram"))
					.put(Mode.PLUPERFECT,Arrays.asList("fora", "foras", "fora", "fôramos", "fôreis", "foram"))
					.build())
			.put("ir", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("vou", "vais", "vai", "vamos", "ides", "vão"))
					.put(Mode.PRETERITE, Arrays.asList("fui", "foste", "foi", "fomos", "fostes", "foram"))
					.put(Mode.PLUPERFECT,Arrays.asList("fora", "foras", "fora", "fôramos", "fôreis", "foram"))
					.build())
			.put("fazer", ImmutableMap.<Mode, List<String>>builder()
					.put(Mode.PRESENT, Arrays.asList("faço", "fazes", "faz", "fazemos", "fazeis", "fazem"))
					.put(Mode.PRETERITE, Arrays.asList("fiz", "fizeste", "fez", "fizemos", "fizestes", "fizeram"))
					.put(Mode.PLUPERFECT,Arrays.asList("fizera", "fizeste", "fizera", "fizéramos", "fizestes", "fizeram"))
					.put(Mode.FUTURE, Arrays.asList("farei", "farás", "fará", "faremos", "fareis", "farão"))
					.put(Mode.CONDITIONAL, Arrays.asList("faria", "farias", "faria", "faríamos", "faríeis", "fariam"))
					.build())
			.build();

	private static Map<Mode, String[]> third = new EnumMap<>(Mode.class);

	private static List<String> addDesinencia(String root, String[] tense) {
		return Stream.of(tense).map(a -> root + a).collect(Collectors.toList());

	}

	private static void addLetter(String add, String[]... tenses) {
		for (int i = 0; i < tenses.length; i++) {
			for (int j = 0; j < tenses[i].length; j++) {
				tenses[i][j] = add + tenses[i][j];
			}
		}
	}

	public static EnumMap<Mode, List<String>> conjugate(String verb) {
		if (isIrregular(verb)) {
			return
			irregularConjugation(verb);
		}
		if (verb.endsWith("ar")) {
			return
			firstConjugation(verb);
		}
		if (verb.endsWith("er")) {
			return
			secondConjugation(verb);
		}
		if (verb.endsWith("ir")) {
			return
			thirdConjugation(verb);
		}
		if (verb.endsWith("por") || verb.endsWith("pôr")) {
			return
			fourthConjugation(verb);
		}
		return null;
	}

	private static EnumMap<Mode, List<String>> firstConjugation(String verb) {
		String root = getRoot(verb);
		Map<Mode, String[]> firstConjugation = getFirstConjugation();
		String[] present = firstConjugation.get(Mode.PRESENT);
		String[] past = firstConjugation.get(Mode.PRETERITE);
		String[] incompletePast = firstConjugation.get(Mode.IMPERFECT);
		String[] pluPerfect = firstConjugation.get(Mode.PLUPERFECT);
		String[] future = firstConjugation.get(Mode.FUTURE);
		String[] futureImpefect = firstConjugation.get(Mode.CONDITIONAL);
		if (root.endsWith("e")) {
			addLetter("e", present, past, incompletePast, pluPerfect, future, futureImpefect);
			present[0] = "eio";
			present[1] = "eias";
			present[2] = "eia";
			present[5] = "eiam";
			root = root.substring(0, root.length() - 1);
		} else if (root.endsWith("i")) {
			addLetter("i", present, past, incompletePast, pluPerfect, future, futureImpefect);
			root = root.substring(0, root.length() - 1);
		} else if (root.endsWith("c")) {
			addLetter("c", present, past, incompletePast, pluPerfect, future, futureImpefect);
			past[0] = "quei";
			root = root.substring(0, root.length() - 1);
		} else if (root.endsWith("ç")) {
			addLetter("ç", present, past, incompletePast, pluPerfect, future, futureImpefect);
			past[0] = "cei";
			root = root.substring(0, root.length() - 1);

		} else if (root.endsWith("g")) {
			addLetter("g", present, past, incompletePast, pluPerfect, future, futureImpefect);
			past[0] = "guei";
			root = root.substring(0, root.length() - 1);
		}
		Map<Mode, String[]> hashMap = new EnumMap<>(Mode.class);
		hashMap.put(Mode.CONDITIONAL, futureImpefect);
		hashMap.put(Mode.FUTURE, future);
		hashMap.put(Mode.IMPERFECT, incompletePast);
		hashMap.put(Mode.PLUPERFECT, pluPerfect);
		hashMap.put(Mode.PRESENT, present);
		hashMap.put(Mode.PRETERITE, past);

		return printVerb(verb, root, hashMap);
	}

	private static EnumMap<Mode, List<String>> fourthConjugation(String verb) {
		String root = getRoot(verb);
		Map<Mode, String[]> fourthConjugation = getFourthConjugation();
		String[] present = fourthConjugation.get(Mode.PRESENT);
		String[] past = fourthConjugation.get(Mode.PRETERITE);
		String[] incompletePast = fourthConjugation.get(Mode.IMPERFECT);
		String[] pluPerfect = fourthConjugation.get(Mode.PLUPERFECT);
		String[] future = fourthConjugation.get(Mode.FUTURE);
		String[] futureImpefect = fourthConjugation.get(Mode.CONDITIONAL);
		Map<Mode, String[]> hashMap = new EnumMap<>(Mode.class);
		hashMap.put(Mode.CONDITIONAL, futureImpefect);
		hashMap.put(Mode.FUTURE, future);
		hashMap.put(Mode.IMPERFECT, incompletePast);
		hashMap.put(Mode.PLUPERFECT, pluPerfect);
		hashMap.put(Mode.PRESENT, present);
		hashMap.put(Mode.PRETERITE, past);

		return printVerb(verb, root, hashMap);

	}

	private static Map<Mode, String[]> getFirstConjugation() {
		String[] present = { "o", "as", "a", "amos", "ais", "am" };
		String[] future = { "arei", "arás", "ará", "aremos", "areis", "arão" };
		String[] pluPerfect = { "ara", "aras", "ara", "áramos", "áreis", "aram" };
		String[] incompletePast = { "ava", "avas", "ava", "ávamos", "áveis", "avam" };
		String[] past = { "ei", "aste", "ou", "amos", "astes", "aram" };
		String[] futureImpefect = { "aria", "arias", "aria", "aríamos", "aríeis", "ariam" };
		first.put(Mode.PRESENT, present);
		first.put(Mode.PRETERITE, past);
		first.put(Mode.IMPERFECT, incompletePast);
		first.put(Mode.PLUPERFECT, pluPerfect);
		first.put(Mode.FUTURE, future);
		first.put(Mode.CONDITIONAL, futureImpefect);
		return first;
	}
	private static Map<Mode, String[]> getFourthConjugation() {
		String[] past = { "us", "useste", "ôs", "usemos", "usestes", "useram" };
		String[] incompletePast = { "unha", "unhas", "unha", "únhamos", "únheis", "unham" };
		String[] future = { "orei", "orás", "orá", "oremos", "oreis", "orão" };
		String[] present = { "onho", "ões", "õe", "omos", "ondes", "õem" };
		String[] pluPerfect = { "usera", "useras", "usera", "uséramos", "uséreis", "useram" };
		String[] futureImpefect = { "oria", "orias", "oria", "oríamos", "oríeis", "oriam" };
		
		fourth.put(Mode.PRESENT, present);
		fourth.put(Mode.PRETERITE, past);
		fourth.put(Mode.IMPERFECT, incompletePast);
		fourth.put(Mode.PLUPERFECT, pluPerfect);
		fourth.put(Mode.FUTURE, future);
		fourth.put(Mode.CONDITIONAL, futureImpefect);

		return fourth;
		
	}

	private static String getRoot(String verb) {
		return verb.substring(0, verb.length() - 2);
	}
	private static Map<Mode, String[]> getSecondConjugation() {
		String[] incompletePast = { "ia", "ias", "ia", "íamos", "íeis", "iam" };
		String[] present = { "o", "es", "e", "emos", "eis", "em" };
		String[] past = { "i", "este", "eu", "emos", "estes", "eram" };
		String[] futureImpefect = { "eria", "erias", "eria", "eríamos", "eríeis", "eriam" };
		String[] pluPerfect = { "era", "eras", "era", "êramos", "êreis", "eram" };
		String[] future = { "erei", "erás", "erá", "eremos", "ereis", "erão" };
		second.put(Mode.PRESENT, present);
		second.put(Mode.PRETERITE, past);
		second.put(Mode.IMPERFECT, incompletePast);
		second.put(Mode.PLUPERFECT, pluPerfect);
		second.put(Mode.FUTURE, future);
		second.put(Mode.CONDITIONAL, futureImpefect);
		return second;
	}

	private static Map<Mode, String[]> getThirdConjugation() {
		String[] present = { "o", "es", "e", "imos", "is", "em" };
		String[] past = { "i", "iste", "iu", "imos", "istes", "iram" };
		String[] incompletePast = { "ia", "ias", "ia", "íamos", "íeis", "iam" };
		String[] pluPerfect = { "ira", "iras", "ira", "íramos", "íreis", "iram" };
		String[] future = { "irei", "irás", "irá", "iremos", "ireis", "irão" };
		String[] futureImpefect = { "iria", "irias", "iria", "iríamos", "iríeis", "iriam" };
		third.put(Mode.PRESENT, present);
		third.put(Mode.PRETERITE, past);
		third.put(Mode.IMPERFECT, incompletePast);
		third.put(Mode.PLUPERFECT, pluPerfect);
		third.put(Mode.FUTURE, future);
		third.put(Mode.CONDITIONAL, futureImpefect);
		
		return third;
		
	}
	private static Stream<String> getWords(URI txtFile) throws IOException {
		return Files.lines(Paths.get(txtFile), StandardCharsets.UTF_8).sequential().map(String::trim)
				.filter(s -> !s.isEmpty() && s.endsWith("siar"))
				.distinct();
	}
	

	private static EnumMap<Mode, List<String>> irregularConjugation(String verb) {
		Entry<String, List<String>> irregular = IRREGULAR.entrySet().stream()
				.filter(e -> e.getValue().stream().anyMatch(v -> (v + e.getKey()).equals(verb))).findFirst()
				.orElse(null);
		if (irregular != null) {
			String key = irregular.getKey();
			if(SIMPLE_IRREGULARITIES.containsKey(key)){
				Map<Mode, String[]> firstConjugation = null;
				if (key.endsWith("ar")) {
					firstConjugation = getFirstConjugation();
				} else if (key.endsWith("er")) {
					firstConjugation = getSecondConjugation();
				} else if (key.endsWith("ir")) {
					firstConjugation = getThirdConjugation();
				}
				return printIrregular(verb, key, firstConjugation);
			}
			System.out.println("CADÊ ->" + key);
		}

		return null;
	}

	private static boolean isIrregular(String verb) {
		return IRREGULAR.entrySet().stream()
				.anyMatch(e -> e.getValue().stream().anyMatch(v -> (v + e.getKey()).equals(verb)));
	}

	public static void main(String[] args) {

		try {
			Stream<String> words = getWords(ResourceFXUtils.toURI("verbs.dic"));
			words.forEach(BrazilianVerbsConjugator::conjugate);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}


	private static EnumMap<Mode, List<String>> printIrregular(String verb, String key,
			Map<Mode, String[]> conjugation) {
		Map<Mode, List<String>> map = SIMPLE_IRREGULARITIES.get(key);
		String root1 = verb.substring(0, verb.length() - key.length());

		List<Entry<Mode, String[]>> entrySet = conjugation.entrySet().stream()
				.collect(Collectors.toList());
		String root2 = getRoot(verb);
		for (int i = 0; i < entrySet.size(); i++) {
			Entry<Mode, String[]> entry = entrySet.get(i);
			Mode m = entry.getKey();
			if (map.containsKey(m)) {
				List<String> a = map.get(m);
				String[] array = a.toArray(new String[0]);
				if (m == Mode.PRESENT && (verb.equals("ter") || verb.equals("vir"))) {
					array[1] = array[1].replaceAll("é", "e");
					array[2] = array[2].replaceAll("é", "e");
				}
				addLetter(root1, array);
				conjugation.put(m, array);
			} else {
				addLetter(root2, entry.getValue());
			}
		}
		EnumMap<Mode, String[]> enumMap = new EnumMap<>(Mode.class);
		enumMap.putAll(conjugation);
		return printVerb(verb, "", enumMap);
	}

	private static EnumMap<Mode, List<String>> printVerb(String verb, String root, Map<Mode, String[]> tenses) {
		EnumMap<Mode, List<String>> enumMap = new EnumMap<>(Mode.class);
		List<List<String>> tens = new ArrayList<>();
		Set<Entry<Mode, String[]>> entrySet = tenses.entrySet();
		for (Entry<Mode, String[]> entry : entrySet) {
			List<String> addDesinencia = addDesinencia(root, entry.getValue());
			tens.add(addDesinencia);
			enumMap.put(entry.getKey(), addDesinencia);
		}
		System.out.println(verb);
		if (DEBUG) {
			System.out.println();
			int bigger = tens.stream().flatMap(List<String>::stream).mapToInt(String::length).max().getAsInt();
			for (int i = 0; i < tens.get(0).size(); i++) {
				for (int j = 0; j < tens.size(); j++) {
					String string = tens.get(j).get(i);
					String rightPad = StringUtils.rightPad(string, bigger, " ");
					System.out.print(rightPad + " ");
				}
				System.out.println();
			}
			System.out.println();
		}
		return enumMap;

	}
	
	

	private static EnumMap<Mode, List<String>> secondConjugation(String verb) {
		String root = getRoot(verb);
		Map<Mode, String[]> secondConjugation = getSecondConjugation();
		String[] present = secondConjugation.get(Mode.PRESENT);
		String[] past = secondConjugation.get(Mode.PRETERITE);
		String[] incompletePast = secondConjugation.get(Mode.IMPERFECT);
		String[] pluPerfect = secondConjugation.get(Mode.PLUPERFECT);
		String[] future = secondConjugation.get(Mode.FUTURE);
		String[] futureImpefect = secondConjugation.get(Mode.CONDITIONAL);

		if (root.endsWith("gu")) {
			addLetter("gu", present, past, incompletePast, pluPerfect, future, futureImpefect);
			present[0] = "go";
			root = root.substring(0, root.length() - 2);
		} else if (root.endsWith("o")) {
			addLetter("o", present, past, incompletePast, pluPerfect, future, futureImpefect);
			present[1] = "óis";
			present[2] = "ói";
			past[0] = "oí";
			incompletePast = new String[] { "oía", "oías", "oía", "oíamos", "oíeis", "oíam" };
			root = root.substring(0, root.length() - 1);
		} else if (root.endsWith("c")) {
			addLetter("c", present, past, incompletePast, pluPerfect, future, futureImpefect);
			present[0] = "ço";
			root = root.substring(0, root.length() - 1);
		} else if (root.endsWith("g")) {
			addLetter("g", present, past, incompletePast, pluPerfect, future, futureImpefect);
			present[0] = "jo";
			root = root.substring(0, root.length() - 1);
		}
		Map<Mode, String[]> hashMap = new EnumMap<>(Mode.class);
		hashMap.put(Mode.CONDITIONAL, futureImpefect);
		hashMap.put(Mode.FUTURE, future);
		hashMap.put(Mode.IMPERFECT, incompletePast);
		hashMap.put(Mode.PLUPERFECT, pluPerfect);
		hashMap.put(Mode.PRESENT, present);
		hashMap.put(Mode.PRETERITE, past);

		return printVerb(verb, root, hashMap);

	}

	private static EnumMap<Mode, List<String>> thirdConjugation(String verb) {
		String root = getRoot(verb);
		Map<Mode, String[]> thirdConjugation = getThirdConjugation();
		String[] present = thirdConjugation.get(Mode.PRESENT);
		String[] past = thirdConjugation.get(Mode.PRETERITE);
		String[] incompletePast = thirdConjugation.get(Mode.IMPERFECT);
		String[] pluPerfect = thirdConjugation.get(Mode.PLUPERFECT);
		String[] future = thirdConjugation.get(Mode.FUTURE);
		String[] futureImpefect = thirdConjugation.get(Mode.CONDITIONAL);
		if (root.endsWith("a")) {
			addLetter("a", present, past, incompletePast, pluPerfect, future, futureImpefect);
			present = new String[] { "aio", "ais", "ai", "aímos", "aís", "aem" };
			past = new String[] { "aí", "aíste", "aiu", "aímos", "aístes", "aíram" };
			incompletePast = new String[] { "aía", "aías", "aía", "aíamos", "aíeis", "aíam" };
			root = root.substring(0, root.length() - 1);
		} else if (root.endsWith("c")) {
			addLetter("c", present, past, incompletePast, pluPerfect, future, futureImpefect);
			present[0] = "ço";
			root = root.substring(0, root.length() - 1);
		} else if (root.endsWith("u")) {
			addLetter("u", present, past, incompletePast, pluPerfect, future, futureImpefect);
			present = new String[] { "uo", "uis", "ui", "uímos", "uís", "uem" };
			past = new String[] { "uí", "uíste", "uiu", "uímos", "uístes", "uíram" };
			incompletePast = new String[] { "uía", "uías", "uía", "uíamos", "uíeis", "uíam" };
			root = root.substring(0, root.length() - 1);
		} else if (root.endsWith("g")) {
			addLetter("g", present, past, incompletePast, pluPerfect, future, futureImpefect);
			present[0] = "jo";
			root = root.substring(0, root.length() - 1);
		}
		Map<Mode, String[]> hashMap = new EnumMap<>(Mode.class);
		hashMap.put(Mode.CONDITIONAL, futureImpefect);
		hashMap.put(Mode.FUTURE, future);
		hashMap.put(Mode.IMPERFECT, incompletePast);
		hashMap.put(Mode.PLUPERFECT, pluPerfect);
		hashMap.put(Mode.PRESENT, present);
		hashMap.put(Mode.PRETERITE, past);

		return printVerb(verb, root, hashMap);

	}

}