package election;

import japstudy.db.BaseDAO;
import java.util.*;
import java.util.stream.Collectors;

public class CandidatoDAO extends BaseDAO {

    public List<String> distinct(String field) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT  ");
            hql.append(" DISTINCT ");
            hql.append(field);
            hql.append(" AS c FROM Candidato ORDER BY c");
            return session.createQuery(hql.toString(), Object.class).list().stream().map(Objects::toString)
                .collect(Collectors.toList());
        });
    }

    public long distinctNumber(String field) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT  ");
            hql.append(" COUNT(DISTINCT ");
            hql.append(field);
            hql.append(")");
            hql.append(" FROM Candidato ");
            return session.createQuery(hql.toString(), Long.class).uniqueResult();
        }).longValue();
    }

    public Map<String, Long> histogram(String field, Map<String, Set<String>> fieldMap) {

        List<Object[]> execute = execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT  ");
            hql.append(field);
            hql.append(", Count(HREF) AS c ");
            hql.append(" FROM Candidato ");
            hql.append(" WHERE ");
            hql.append(field);
            hql.append(" IS NOT NULL ");
            hql.append(getConditions(fieldMap));
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
        return list(startPosition, maxResult, new HashMap<>());
    }
    public List<Candidato> list(int startPosition, int maxResult, Map<String, Set<String>> fieldMap) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append(" SELECT l ");
            hql.append(" FROM Candidato l");
            hql.append(" WHERE 1=1 ");
            hql.append(getConditions(fieldMap));
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

    private String getConditions(Map<String, Set<String>> fieldMap) {
        return fieldMap.entrySet().stream().filter(e -> !e.getValue().isEmpty())
            .map(e -> " AND " + e.getKey() + " IN "
                + e.getValue().stream().collect(Collectors.joining("','", "('", "')")))
            .collect(Collectors.joining(""));
    }

}
