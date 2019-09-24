package contest;

import static java.util.stream.Collectors.toList;

import contest.db.Contest;
import contest.db.ContestQuestion;
import contest.db.ContestText;
import java.util.List;
import utils.BaseDAO;
import utils.BaseEntity;

public class ContestQuestionDAO extends BaseDAO {

    public List<ContestQuestion> list(Contest c) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT l ");
            hql.append("FROM ContestQuestion l ");
            hql.append("WHERE l.contest=:c ");
            hql.append("ORDER BY number");
            return session.createQuery(hql.toString(), ContestQuestion.class).setParameter("c", c).list().stream()
                .map(BaseDAO::initialize).collect(toList());
        });
    }

    public List<Contest> listContests() {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT l ");
            hql.append("FROM Contest l ");
            hql.append("ORDER BY key");
            return session.createQuery(hql.toString(), Contest.class).list();
        });
    }

    public List<ContestText> listTexts() {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT c ");
            hql.append("FROM ContestText c ");
            return session.createQuery(hql.toString(), ContestText.class).list().stream()
                .map(BaseDAO::initialize).collect(toList());
        });
    }

    public void saveOrUpdate(BaseEntity jap) {
        executeRun(session -> session.saveOrUpdate(jap));
    }

    public void saveOrUpdate(List<? extends BaseEntity> jap) {
        executeRun(session -> jap.forEach(session::saveOrUpdate));
    }

}
