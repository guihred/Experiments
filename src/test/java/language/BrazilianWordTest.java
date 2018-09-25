package language;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import neuro.BrazilianWordSyllableSplitter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BrazilianWordTest {

	private String input;

	public BrazilianWordTest(String input) {
		this.input = input;
	}


	@Parameterized.Parameters
	public static Collection<String> data() {

		return Arrays.asList("au-tô-no-mo", "tui-ui-ú", "ou-to-no", "di-nhei-ro", "sal-dar", "dês-mai-a-do",
				"U-ru-guai", "i-guais", "quais-quer", "u-ru-guai-a-na", "prai-a", "tei-a", "joi-a", "sa-bo-rei-e",
				"es-tei-o", "ar-roi-o", "con-lui-o", "a-mên-do", "ca-a-tin-ga", "sa-ú-de", "flu-ir", "chu-va",
				"mo-lha", "es-ta-nho", "guel-ra", "a-que-la", "to-cha", "fi-lha", "ni-nho", "que-rer", "guei-xa",
				"bar-ro", "as-sun-to", "des-cer", "nas-ço", "es-xu-dar", "ex-ce-to", "car-ro", "nas-cer", "dês-ço",
				"ex-ces-so", "ab-do-me", "sub-ma-ri-no", "ap-ti-dão", "dig-no", "con-vic-ção", "as-tu-to", "ap-to",
				"cír-cu-lo", "ad-mi-tir", "ob-tu-rar", "a-pli-ca-ção", "a-pre-sen-tar", "a-brir", "re-tra-to",
				"de-ca-tlo", "gnós-ti-co", "pneu-má-ti-co", "mne-mô-ni-co", "Sa-a-ra", "com-pre-en-do", "xi-i-ta",
				"vo-o", "pa-ra-cu-u-ba", "oc-ci-pi-tal", "in-te-lec-ção", "de-sa-ten-to", "di-sen-te-ri-a",
				"tran-sa-tlân-ti-co", "su-ben-ten-di-do", "su-ben-ten-der", "dis-fun-ção", "di-sen-te-ri-a",
				"su-per-mer-ca-do", "su-pe-ra-mi-go", "ex-ce-ção", "des-cer", "ter-ra", "pás-sa-ro", "guer-ra",
				"ni-nho", "chu-va", "quei-jo", "ca-de-a-do", "ju-í-za", "La-ís", "Pa-ra-guai", "a-ve-ri-guei", "cai-xa",
				"fei-xe", "flau-ta", "pra-to", "ap-to", "ab-dô-men", "cír-cu-lo", "pneu-mo-ni-a", "pneu-má-ti-co",
				"psi-có-lo-go");
	}

	@Test
	public void test() {
        assertEquals("The syllables should match", input,
                BrazilianWordSyllableSplitter.splitSyllables(input.replaceAll("-", "")));
	}
}

