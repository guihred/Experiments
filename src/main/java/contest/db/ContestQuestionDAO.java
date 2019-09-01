package contest.db;

import static java.util.stream.Collectors.toList;
import static utils.ClassReflectionUtils.getters;
import static utils.ClassReflectionUtils.invoke;

import japstudy.db.BaseDAO;
import japstudy.db.BaseEntity;
import java.util.List;

public class ContestQuestionDAO extends BaseDAO {

    public List<ContestQuestion> list() {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT l ");
            hql.append("FROM ContestQuestion l ");
            hql.append("ORDER BY number");
            return session.createQuery(hql.toString(), ContestQuestion.class).list().stream().map(e -> {
                getters(ContestQuestion.class).forEach(m -> invoke(e, m));
                return e;
            }).collect(toList());
        });
    }

    public void saveOrUpdate(BaseEntity jap) {
        executeRun(session -> session.saveOrUpdate(jap));
    }

    public void saveOrUpdate(List<? extends BaseEntity> jap) {
        executeRun(session -> jap.forEach(session::saveOrUpdate));
    }

}
