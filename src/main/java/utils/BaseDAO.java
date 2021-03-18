package utils;

import static utils.ClassReflectionUtils.getters;
import static utils.ClassReflectionUtils.invoke;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

@SuppressWarnings("static-method")
public class BaseDAO implements HasLogging {

    public final void delete(BaseEntity jap) {
        executeRun(session -> session.delete(jap));
    }

    public final void delete(List<? extends BaseEntity> jap) {
        executeRun(session -> jap.forEach(session::delete));
    }

    public final void saveOrUpdate(BaseEntity jap) {
        executeRun(session -> session.saveOrUpdate(jap));
    }

    public final void saveOrUpdate(List<? extends BaseEntity> jap) {
        executeRun(session -> jap.forEach(session::saveOrUpdate));
    }

    protected final <T> T execute(Function<Session, T> run) {
        return SupplierEx.get(() -> {
            try (Session session = HibernateUtil.getSessionFactory().getCurrentSession()) {
                Transaction transaction = getTransaction(session);
                T apply = run.apply(session);
                session.flush();
                transaction.commit();
                return apply;
            }
        });
    }

    protected final void executeRun(Consumer<Session> run) {
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

    @SuppressWarnings({ "deprecation", "unchecked" })
    protected static <T> Map<String, T> toMap(Query<?> createQuery) {
        createQuery.setResultTransformer(new ResultTransformer() {
            @Override
            public List transformList(List collection) {
                return collection;
            }

            @Override
            public Map<String, T> transformTuple(Object[] tuple, String[] aliases) {
                return IntStream.range(0, tuple.length).boxed()
                        .collect(Collectors.toMap(i -> aliases[i], i -> (T) tuple[i]));
            }
        });
        return (Map<String, T>) createQuery.uniqueResult();
    }
    private static Transaction getTransaction(Session session) {
        Transaction beginTransaction = session.getTransaction();
        if (!beginTransaction.isActive()) {
            return session.beginTransaction();
        }
        return beginTransaction;
    }

}
