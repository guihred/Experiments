package contest.db;

import japstudy.db.BaseDAO;
import java.util.List;

public class ContestQuestionDAO extends BaseDAO {

    public void saveOrUpdate(BaseEntity jap) {
        executeRun(session -> session.saveOrUpdate(jap));
	}

    public void saveOrUpdate(List<? extends BaseEntity> jap) {
        executeRun(session -> jap.forEach(session::saveOrUpdate));
    }

	public List<ContestQuestion> list() {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT l ");
            hql.append("FROM ContestQuestion l ");
            hql.append("ORDER BY number");
            return session.createQuery(hql.toString(), ContestQuestion.class).list();
        });
	}

}
