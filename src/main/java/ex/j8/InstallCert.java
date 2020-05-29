package ex.j8;

/*
 */

import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class InstallCert {


    private static final Logger LOG = HasLogging.log();

    public static void installCertificate(String url) throws Exception {
        String string = url;
        String s = "^https*://(.*)/*.*";
        if (url.matches(s)) {
            URL url2 = new URL(url);
            string = url2.getHost();
            if (url2.getPort() != -1) {
                string +=":"+url2.getPort();
            }
        }

        String[] c = string.split(":");
        String host = c[0];
        int port = c.length == 1 ? 443 : Integer.parseInt(c[1]);

        installCertificate(host, port, "changeit".toCharArray());
    }

    public static void main(String[] args) throws Exception {

        String string = "correiov3.dataprev.gov.br";
        installCertificate(string);
    }

    private static void installCertificate(String host, int port, char[] passphrase) throws Exception {
        File file = ResourceFXUtils.toFile("cacerts");
        LOG.info("Loading KeyStore {}...", file);
        KeyStore ks;
        try (InputStream in = new FileInputStream(file)) {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(in, passphrase);
        }
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[] { tm }, null);
        SSLSocketFactory factory = context.getSocketFactory();

        LOG.info("Opening connection to {}:{}...", host, port);
        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
            socket.setSoTimeout(10000);
            LOG.info("Starting SSL handshake...");
            socket.startHandshake();
            LOG.info("");
            LOG.info("No errors, certificate is already trusted");
            return;
        } catch (SSLException e) {
            LOG.info("ERROR STARTING HANDSHAKE");
            LOG.trace("ERROR STARTING HANDSHAKE", e);
        }

        X509Certificate[] chain = tm.chain;
        if (chain == null || chain.length == 0) {
            LOG.info("Could not obtain server certificate chain");
            return;
        }


        LOG.info("Server sent {} certificate(s):", chain.length);
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            LOG.info(" {} Subject {}", i + 1, cert.getSubjectDN());
            LOG.info("   Issuer  " + cert.getIssuerDN());
            sha1.update(cert.getEncoded());
            sha1.digest();
            md5.update(cert.getEncoded());
            md5.digest();
            String alias = host + "-" + (i + 1);
            ks.setCertificateEntry(alias, cert);
            LOG.info("Added certificate to keystore \"cacerts\" using alias \"{}\"", alias);
        }

        try (OutputStream out = new FileOutputStream(file)) {
            ks.store(out, passphrase);
        }
    }

    private static class SavingTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain1, String authType) throws CertificateException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain1, String authType) throws CertificateException {
            chain = chain1;
            tm.checkServerTrusted(chain1, authType);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }
    }

}
