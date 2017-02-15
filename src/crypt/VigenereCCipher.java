package crypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VigenereCCipher {
	private static final Logger LOGGER = LoggerFactory.getLogger(VigenereCCipher.class);
	private static final String ENCODED = "MOMUD EKAPV TQEFM OEVHP AJMII CDCTI FGYAG JSPXY ALUYM NSMYH"
			+ "VUXJE LEPXJ FXGCM JHKDZ RYICU HYPUS PGIGM OIYHF WHTCQ KMLRD" + "ITLXZ LJFVQ GHOLW CUHLO MDSOE KTALU VYLNZ RFGBX PHVGA LWQIS"
			+ "FGRPH JOOFW GUBYI LAPLA LCAFA AMKLG CETDW VOELJ IKGJB XPHVG" + "ALWQC SNWBU BYHCU HKOCE XJEYK BQKVY KIIEH GRLGH XEOLW AWFOJ"
			+ "ILOVV RHPKD WIHKN ATUHN VRYAQ DIVHX FHRZV QWMWV LGSHN NLVZS" + "JLAKI FHXUF XJLXM TBLQV RXXHR FZXGV LRAJI EXPRV OSMNP KEPDT"
			+ "LPRWM JAZPK LQUZA ALGZX GVLKL GJTUI ITDSU REZXJ ERXZS HMPST" + "MTEOE PAPJH SMFNB YVQUZ AALGA YDNMP AQOWT UHDBV TSMUE UIMVH"
			+ "QGVRW AEFSP EMPVE PKXZY WLKJA GWALT VYYOB YIXOK IHPDS EVLEV" + "RVSGB JOGYW FHKBL GLXYA MVKIS KIEHY IMAPX UOISK PVAGN MZHPW"
			+ "TTZPV XFCCD TUHJH WLAPF YULTB UXJLN SIJVV YOVDJ SOLXG TGRVO" + "SFRII CTMKO JFCQF KTINQ BWVHG TENLH HOGCS PSFPV GJOKM SIFPR"
			+ "ZPAAS ATPTZ FTPPD PORRF TAXZP KALQA WMIUD BWNCT LEFKO ZQDLX" + "BUXJL ASIMR PNMBF ZCYLV WAPVF QRHZV ZGZEF KBYIO OFXYE VOWGB"
			+ "BXVCB XBAWG LQKCM ICRRX MACUO IKHQU AJEGL OIJHH XPVZW JEWBA" + "FWAML ZZRXJ EKAHV FASMU LVVUT TGK";

	private static final double[] FREQ = { 0.08167, 0.01492, 0.02782, 0.04253, 0.12702, 0.02228, 0.02015, 0.06094,
			0.06966,
			0.00153, 0.00772, 0.04025, 0.02406,
			0.06749, 0.07507, 0.01929, 0.00095, 0.05987, 0.06327, 0.09056, 0.02758, 0.00978, 0.02360, 0.00150, 0.01974, 0.00074 };

	int bestMatch(final double[] a, final double[] b) {
		double sum = 0, fit, d, bestFit = 1e100;
		int i, rotate, bestRotate = 0;
		for (i = 0; i < 26; i++) {
			sum += a[i];
		}
		for (rotate = 0; rotate < 26; rotate++) {
			fit = 0;
			for (i = 0; i < 26; i++) {
				d = a[(i + rotate) % 26] / sum - b[i];
				fit += d * d / b[i];
			}

			if (fit < bestFit) {
				bestFit = fit;
				bestRotate = rotate;
			}
		}

		return bestRotate;
	}

	double freqEveryNth(final int[] msg, int len, int interval, char[] key) {
		double sum, d, ret;
		double[] out = new double[26], accu = new double[26];
		int i, j, rot;

		for (j = 0; j < interval; j++) {
			for (i = 0; i < 26; i++) {
				out[i] = 0;
			}
			for (i = j; i < len; i += interval) {
				out[msg[i]]++;
			}
			rot = bestMatch(out, FREQ);
			key[j] = (char) rot;
			key[j] += 'A';
			for (i = 0; i < 26; i++) {
				accu[i] += out[(i + rot) % 26];
			}
		}

		for (i = 0, sum = 0; i < 26; i++) {
			sum += accu[i];
		}

		for (i = 0, ret = 0; i < 26; i++) {
			d = accu[i] / sum - FREQ[i];
			ret += d * d / FREQ[i];
		}

		key[interval] = '\0';
		return ret;
	}

	int inicio() {
		int[] txt = new int[ENCODED.length()];
		int len = 0, j;
		char[] key = new char[100];
		double fit, bestFit = 1e100;

		for (j = 0; j < ENCODED.length(); j++) {
			if (Character.isUpperCase(ENCODED.charAt(j))) {
				txt[len++] = ENCODED.charAt(j) - 'A';
			}
		}

		for (j = 1; j < 30; j++) {
			fit = freqEveryNth(txt, len, j, key);
			System.out.printf("%f, key length: %2d, %s", fit, j, key);
			if (fit < bestFit) {
				bestFit = fit;
				LOGGER.info(" <--- best so far");
			}
			LOGGER.info("\n");
		}

		return 0;
	}

	public static void main(String[] args) {
		new VigenereCCipher().inicio();
	}

}
