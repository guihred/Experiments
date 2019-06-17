package japstudy.db;

import java.util.function.Consumer;
import java.util.function.Function;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HasLogging;

public class BaseDAO implements HasLogging {

    protected <T> T execute(Function<Session, T> run) {
		try (Session session = HibernateUtil.getSessionFactory().getCurrentSession()) {
			Transaction transaction = getTransaction(session);
			T apply = run.apply(session);
			session.flush();
			transaction.commit();
			session.close();
			return apply;
		} catch (HibernateException e) {
			getLogger().error("", e);
			return null;
		}
	}

    protected void executeRun(Consumer<Session> run) {
		try (Session session = HibernateUtil.getSessionFactory().getCurrentSession()) {
			Transaction transaction = getTransaction(session);
			run.accept(session);
			session.flush();
			transaction.commit();
		} catch (HibernateException e) {
			getLogger().error("", e);
		}
	}

	private static Transaction getTransaction(Session session) {
		Transaction beginTransaction = session.getTransaction();
		if (!beginTransaction.isActive()) {
			return session.beginTransaction();
		}
		return beginTransaction;
	}

}
