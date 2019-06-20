package japstudy.db;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {

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
			return new Configuration()
					.configure()
					.buildSessionFactory();

		} catch (Exception ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

}