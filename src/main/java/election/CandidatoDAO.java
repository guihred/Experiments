package election;

import japstudy.db.BaseDAO;
import java.util.List;

public class CandidatoDAO extends BaseDAO {

    public Candidato retrieve(String href) {
        return execute(session -> session.get(Candidato.class, href));
    }

    public void saveOrUpdate(Candidato candidato) {
        executeRun(session -> session.merge(candidato));
    }

    public List<Candidato> list(int startPosition, int maxResult) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append(" SELECT l ");
            hql.append(" FROM Candidato l");
            hql.append(" WHERE l.nomeCompleto IS NULL");
            return session.createQuery(hql.toString(), Candidato.class)
                    .setFirstResult(startPosition)
                    .setMaxResults(maxResult).list();
        });
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
