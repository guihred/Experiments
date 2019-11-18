package utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;

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
			Process newUnmappedProcess = ConsoleUtils.startProcessAndWait(".\\runHibernate.bat",
					"Web Console server running at .+");
			try {
                LOG.info("LAUNCHING HIBERNATE COMMAND");
				return new Configuration().configure().buildSessionFactory();
			} catch (Exception ex2) {
				newUnmappedProcess.destroy();
				throw new ExceptionInInitializerError(ex2);
			}
		}
	}

}