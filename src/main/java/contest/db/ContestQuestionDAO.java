package contest.db;

import japstudy.db.BaseDAO;
import japstudy.db.BaseEntity;
import java.util.List;
import java.util.stream.Collectors;
import utils.ClassReflectionUtils;

public class ContestQuestionDAO extends BaseDAO {

    public List<ContestQuestion> list() {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT l ");
            hql.append("FROM ContestQuestion l ");
            hql.append("ORDER BY number");
			return session.createQuery(hql.toString(), ContestQuestion.class).list().stream().map(e -> {
				ClassReflectionUtils.getGetterMethodsRecursive(ContestQuestion.class)
						.forEach(m -> ClassReflectionUtils.invoke(e, m));
				return e;
			}).collect(Collectors.toList())
			;
        });
	}

    public void saveOrUpdate(BaseEntity jap) {
        executeRun(session -> session.saveOrUpdate(jap));
	}

	public void saveOrUpdate(List<? extends BaseEntity> jap) {
        executeRun(session -> jap.forEach(session::saveOrUpdate));
    }

}
