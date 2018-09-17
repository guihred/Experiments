package election.experiment;

import japstudy.db.BaseDAO;
import java.util.List;

public class CidadeDAO extends BaseDAO {

    public synchronized void saveOrUpdate(Object cidade) {
        executeRun(session -> session.saveOrUpdate(cidade));
	}

	public List<Cidade> list() {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT l ");
            hql.append("FROM Cidade l");
            return session.createQuery(hql.toString(), Cidade.class).list();
        });
	}

}
