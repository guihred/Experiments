package election;

import japstudy.db.BaseDAO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CandidatoDAO extends BaseDAO {

    public List<String> distinct(String field) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT  ");
            hql.append(" DISTINCT CAST(");
            hql.append(field);
            hql.append(" AS String) ");
            hql.append(" FROM Candidato ");
            return session.createQuery(hql.toString(), String.class).list();
        });
    }

    public Long distinctNumber(String field) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT  ");
            hql.append(" COUNT(DISTINCT ");
            hql.append(field);
            hql.append(")");
            hql.append(" FROM Candidato ");
            return session.createQuery(hql.toString(), Long.class).uniqueResult();
        });
    }

    public Map<String, Long> histogram(String field) {

        List<Object[]> execute = execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT  ");
            hql.append(field);
            hql.append(", Count(HREF) AS c ");
            hql.append(" FROM Candidato ");
            hql.append(" WHERE ");
            hql.append(field);
            hql.append(" IS NOT NULL AND eleito IS TRUE ");
            hql.append(" GROUP BY ");
            hql.append(field);
            hql.append(" ORDER BY c DESC");
            return session.createQuery(hql.toString(), Object[].class).list();
        });
        Map<String, Long> linkedHashMap = new LinkedHashMap<>();
        for (Object[] objects : execute) {
            Object o = objects[0];
            Object o1 = objects[1];
            linkedHashMap.put(Objects.toString(o), ((Number) o1).longValue());

        }
        return linkedHashMap;
    }

    public List<Candidato> list(int startPosition, int maxResult) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append(" SELECT l ");
            hql.append(" FROM Candidato l");
            hql.append(" WHERE l.nomeCompleto IS NULL");
            return session.createQuery(hql.toString(), Candidato.class).setFirstResult(startPosition)
                .setMaxResults(maxResult).list();
        });
    }

    public Candidato retrieve(String href) {
        return execute(session -> session.get(Candidato.class, href));
    }

    public void saveOrUpdate(Candidato candidato) {
        executeRun(session -> session.merge(candidato));
    }

    public Long size() {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT COUNT(l) ");
            hql.append(" FROM Candidato l");
            hql.append(" WHERE l.nomeCompleto IS NULL");
            return session.createQuery(hql.toString(), Long.class).uniqueResult();
        });
    }

}
