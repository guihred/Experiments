package japstudy.db;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

	private static final SessionFactory sessionFactory = buildSessionFactory();
    private static boolean shutdownEnabled;

	private static SessionFactory buildSessionFactory() {
		try {
			return new Configuration()
					.configure()
					.buildSessionFactory();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

    public static void setShutdownEnabled(boolean shutdownEnabled) {
        HibernateUtil.shutdownEnabled = shutdownEnabled;
    }

	public static void shutdown() {
        if (shutdownEnabled) {
		// Close caches and connection pools
            getSessionFactory().close();
        }
	}

}