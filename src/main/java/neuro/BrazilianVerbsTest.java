package neuro;

import static org.junit.Assert.assertEquals;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import neuro.BrazilianVerbsConjugator.Mode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BrazilianVerbsTest {
	private Entry<String, List<String>> input;
	public BrazilianVerbsTest(Entry<String, List<String>> input) {
		this.input = input;
	}
	@Parameterized.Parameters
	public static Collection<Entry<String, List<String>>> dataPresentMode() {
		return Arrays.asList(
				entry("ser", "sou", "és", "é", "somos", "sois", "são"),
				entry("ver", "vejo", "vês", "vê", "vemos", "vedes", "veem"),
				entry("vir", "venho", "vens", "vem", "vimos", "vindes", "vêm"),
				entry("ter", "tenho", "tens", "tem", "temos", "tendes", "têm"),
				entry("moer", "moo", "móis", "mói", "moemos", "moeis", "moem"),
				entry("amar", "amo", "amas", "ama", "amamos", "amais", "amam"),
				entry("crer", "creio", "crês", "crê", "cremos", "credes", "creem"),
				entry("pedir", "peço", "pedes", "pede", "pedimos", "pedis", "pedem"),
				entry("subir", "subo", "sobes", "sobe", "subimos", "subis", "sobem"),
				entry("sumir", "sumo", "somes", "some", "sumimos", "sumis", "somem"),
				entry("bater", "bato", "bates", "bate", "batemos", "bateis", "batem"),
				entry("odiar", "odeio", "odeias", "odeia", "odiamos", "odiais", "odeiam"),
				entry("dormir", "durmo", "dormes", "dorme", "dormimos", "dormis", "dormem"),
				entry("acudir", "acudo", "acodes", "acode", "acudimos", "acudis", "acodem"),
				entry("sentir", "sinto", "sentes", "sente", "sentimos", "sentis", "sentem"),
				entry("partir", "parto", "partes", "parte", "partimos", "partis", "partem"),
				entry("prever", "prevejo", "prevês", "prevê", "prevemos", "prevedes", "preveem"),
				entry("ansiar", "anseio", "anseias", "anseia", "ansiamos", "ansiais", "anseiam"),
				entry("abduzir", "abduzo", "abduzes", "abduz", "abduzimos", "abduzis", "abduzem"),
				entry("inserir", "insiro", "inseres", "insere", "inserimos", "inseris", "inserem"),
				entry("entupir", "entupo", "entopes", "entope", "entupimos", "entupis", "entopem"),
				entry("manter", "mantenho", "manténs", "mantém", "mantemos", "mantendes", "mantêm"),
				entry("atribuir", "atribuo", "atribuis", "atribui", "atribuímos", "atribuís", "atribuem"),
				entry("divertir", "divirto", "divertes", "diverte", "divertimos", "divertis", "divertem"),
				entry("descobrir", "descubro", "descobres", "descobre", "descobrimos", "descobris", "descobrem"),
				entry("intervir", "intervenho", "intervéns", "intervém", "intervimos", "intervindes", "intervêm"),
				entry("incendiar", "incendeio", "incendeias", "incendeia", "incendiamos", "incendiais", "incendeiam"),
				entry("satisfazer", "satisfaço", "satisfazes", "satisfaz", "satisfazemos", "satisfazeis", "satisfazem"),
				entry("transferir", "transfiro", "transferes", "transfere", "transferimos", "transferis", "transferem")
		);
	}
	private static Entry<String, List<String>> entry(String verb, String... conjugated) {
		return new SimpleEntry<>(verb, Arrays.asList(conjugated));
	}
	@Test
	public void testPresent() {
		assertEquals(input.getValue(), BrazilianVerbsConjugator.conjugate(input.getKey()).get(Mode.PRESENT));
	}
}

