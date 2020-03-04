package utils;

import static utils.ClassReflectionUtils.getters;
import static utils.ClassReflectionUtils.invoke;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.hibernate.Session;
import org.hibernate.Transaction;

@SuppressWarnings("static-method")
public class BaseDAO implements HasLogging {

    public void delete(BaseEntity jap) {
        executeRun(session -> session.delete(jap));
    }

    public void delete(List<? extends BaseEntity> jap) {
        executeRun(session -> jap.forEach(session::delete));
    }
    public void saveOrUpdate(BaseEntity jap) {
        executeRun(session -> session.saveOrUpdate(jap));
    }

    public void saveOrUpdate(List<? extends BaseEntity> jap) {
        executeRun(session -> jap.forEach(session::saveOrUpdate));
    }

    protected <T> T execute(Function<Session, T> run) {
        return SupplierEx.get(() -> {
            try (Session session = HibernateUtil.getSessionFactory().getCurrentSession()) {
                Transaction transaction = getTransaction(session);
                T apply = run.apply(session);
                session.flush();
                transaction.commit();
                session.close();
                return apply;
            }
        });
    }

    protected void executeRun(Consumer<Session> run) {
        RunnableEx.run(() -> {
            try (Session session = HibernateUtil.getSessionFactory().getCurrentSession()) {
                Transaction transaction = getTransaction(session);
                run.accept(session);
                session.flush();
                transaction.commit();
            }
        });
    }

    protected static <T> T initialize(T e) {
        getters(e.getClass()).forEach(m -> invoke(e, m));
        return e;
    }

    private static Transaction getTransaction(Session session) {
        Transaction beginTransaction = session.getTransaction();
        if (!beginTransaction.isActive()) {
            return session.beginTransaction();
        }
        return beginTransaction;
    }

}
