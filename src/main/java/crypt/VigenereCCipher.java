package crypt;

import org.slf4j.Logger;
import utils.HasLogging;

public class VigenereCCipher implements HasLogging {
	private static final Logger LOGGER = HasLogging.log();
	private static final int NUMBER_OF_LETTERS = 26;
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
			0.06966, 0.00153, 0.00772, 0.04025, 0.02406, 0.06749, 0.07507, 0.01929, 0.00095, 0.05987, 0.06327, 0.09056,
			0.02758, 0.00978, 0.02360, 0.00150, 0.01974, 0.00074 };

	public int bestMatch(final double[] a, final double[] b) {
		double sum = 0;
		for (int i = 0; i < NUMBER_OF_LETTERS; i++) {
			sum += a[i];
		}
        if (sum == 0) {
            return 0;
        }
		int bestRotate = 0;
        double bestFit = 1e100;
		for (int rotate = 0; rotate < NUMBER_OF_LETTERS; rotate++) {
			double fit = 0;
			for (int i = 0; i < NUMBER_OF_LETTERS; i++) {
				double d = a[(i + rotate) % NUMBER_OF_LETTERS] / sum - b[i];
				fit += d * d / b[i];
			}

			if (fit < bestFit) {
				bestFit = fit;
				bestRotate = rotate;
			}
		}

		return bestRotate;
	}

	public double freqEveryNth(final int[] msg, int len, int interval, char[] key) {
		double[] out = new double[NUMBER_OF_LETTERS];
		double[] accu = new double[NUMBER_OF_LETTERS];
		for (int j = 0; j < interval; j++) {
			for (int i = 0; i < NUMBER_OF_LETTERS; i++) {
				out[i] = 0;
			}
			for (int i = j; i < len; i += interval) {
				out[msg[i]]++;
			}
			int rot = bestMatch(out, FREQ);
			key[j] = (char) rot;
			key[j] += 'A';
			for (int i = 0; i < NUMBER_OF_LETTERS; i++) {
				accu[i] += out[(i + rot) % NUMBER_OF_LETTERS];
			}
		}
        double sum = 0;
		for (int i = 0; i < NUMBER_OF_LETTERS; i++) {
			sum += accu[i];
		}
        if (sum == 0) {
            return 0;
        }
        double ret = 0;
        for (int i = 0; i < NUMBER_OF_LETTERS; i++) {
			double d = accu[i] / sum - FREQ[i];
			ret += d * d / FREQ[i];
		}

		key[interval] = '\0';
		return ret;
	}

	public int inicio() {
		int len = 0;
		char[] key = new char[100];
		double bestFit = 1e100;

		int[] txt = new int[ENCODED.length()];
		for (int j = 0; j < ENCODED.length(); j++) {
			if (Character.isUpperCase(ENCODED.charAt(j))) {
				txt[len++] = ENCODED.charAt(j) - 'A';
			}
		}

		for (int j = 1; j < 30; j++) {
			double fit = freqEveryNth(txt, len, j, key);
            getLogger().trace("{}, key length: {}, {}", fit, j, key);
			if (fit < bestFit) {
				bestFit = fit;
                LOGGER.info("{} <--- best so far", bestFit);
			}
		}

		return 0;
	}


}
