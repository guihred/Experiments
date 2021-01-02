package extract;

/*
 */

import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class InstallCert {
    private static final int HTTPS_PORT = 443;
    private static final Logger LOG = HasLogging.log();

    private InstallCert() {
    }

    public static void installCertificate(String url) throws GeneralSecurityException {
        String urlFullInformation = getFullURL(url);
        String[] c = urlFullInformation.split(":");
        String host = c[0];
        int port = c.length == 1 ? HTTPS_PORT : Integer.parseInt(c[1]);

        installCertificate(host, port, "changeit".toCharArray());
    }

    private static String getFullURL(String url) {
        return SupplierEx.remap(() -> {
            if (!url.matches("^https*://(.*)/*.*")) {
                return url;
            }
            URL url2 = new URL(url);
            String host = url2.getHost();
            if (url2.getPort() == -1) {
                return host;
            }
            return host + ":" + url2.getPort();
        }, "ERROR CREATING URL");

    }

    private static void installCertificate(String host, int port, char[] passphrase) throws GeneralSecurityException {
        File file = ResourceFXUtils.toFile("cacerts");
        LOG.info("Loading KeyStore {}...", file);
        KeyStore ks = loadKeyStore(passphrase, file);
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[] { tm }, null);
        SSLSocketFactory factory = context.getSocketFactory();

        LOG.info("Opening connection to {}:{}...", host, port);
        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
            final int timeout = 10000;
            socket.setSoTimeout(timeout);
            LOG.info("Starting SSL handshake...");
            socket.startHandshake();
            LOG.info("");
            LOG.info("No errors, certificate is already trusted");
            return;
        } catch (Exception e) {
            LOG.info("ERROR STARTING HANDSHAKE");
            LOG.trace("ERROR STARTING HANDSHAKE", e);
        }

        X509Certificate[] chain = tm.getAcceptedIssuers();
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
            LOG.info("   Issuer  {}", cert.getIssuerDN());
            sha1.update(cert.getEncoded());
            sha1.digest();
            md5.update(cert.getEncoded());
            md5.digest();
            String alias = host + "-" + (i + 1);
            ks.setCertificateEntry(alias, cert);
            LOG.info("Added certificate to keystore \"cacerts\" using alias \"{}\"", alias);
        }

        saveKeyStore(passphrase, file, ks);
    }

    private static KeyStore loadKeyStore(char[] passphrase, File file) {
        return SupplierEx.remap(() -> {
            KeyStore ks;
            try (InputStream in = new FileInputStream(file)) {
                ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(in, passphrase);
            }
            return ks;

        }, "ERROR LOADING " + file);

    }

    private static void saveKeyStore(char[] passphrase, File file, KeyStore ks) {
        RunnableEx.remap(() -> {
            try (OutputStream out = new FileOutputStream(file)) {
                ks.store(out, passphrase);
            }
        }, "ERROR SAVING KeyStore" + file);
    }

}
