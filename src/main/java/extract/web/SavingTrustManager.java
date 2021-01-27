package extract.web;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.X509TrustManager;

class SavingTrustManager implements X509TrustManager {

    private final X509TrustManager tm;
    private List<X509Certificate> chain = new ArrayList<>();

    public SavingTrustManager(X509TrustManager tm) {
        this.tm = tm;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain1, String authType) throws CertificateException {
        chain.addAll(Arrays.asList(chain1));
        tm.checkServerTrusted(chain1, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain1, String authType) throws CertificateException {
        chain.addAll(new ArrayList<>(Arrays.asList(chain1)));
        tm.checkServerTrusted(chain1, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return chain.toArray(new X509Certificate[0]);
    }
}