package japstudy.db;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import utils.ConsoleUtils;
import utils.HasLogging;

public final class HibernateUtil {

    private static final Logger LOG = HasLogging.log();
    private static SessionFactory sessionFactory = buildSessionFactory();
    private static boolean shutdownEnabled = true;

    private HibernateUtil() {
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    public static void main(String[] args) {
        shutdown();
    }

    public static void setShutdownEnabled(boolean shutdownEnabled) {
        HibernateUtil.shutdownEnabled = shutdownEnabled;
    }

    public static void shutdown() {
        // Close caches and connection pools
        if (shutdownEnabled && !getSessionFactory().isClosed()) {
            getSessionFactory().close();
        }
    }

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Exception ex) {
            LOG.trace("ERROR CONNECTING TO DATABASE", ex);
            LOG.info("ERROR CONNECTING TO DATABASE");
            Process newUnmappedProcess = ConsoleUtils.newUnmappedProcess(".\\runHibernate.bat");
            try {
                final int millis = 5000;
                Thread.sleep(millis);
                return new Configuration().configure().buildSessionFactory();
            } catch (Exception ex2) {
                newUnmappedProcess.destroy();
                throw new ExceptionInInitializerError(ex2);
            }
        }
    }

}