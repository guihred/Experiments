package contest;

import static java.util.stream.Collectors.toList;

import contest.db.Contest;
import contest.db.ContestQuestion;
import contest.db.ContestText;
import java.util.List;
import java.util.stream.Collectors;
import org.hibernate.query.Query;
import utils.BaseDAO;

public class ContestQuestionDAO extends BaseDAO {

    public void deleteContest(Contest hasEqual) {
        this.delete(this.listTexts(hasEqual));
        List<ContestQuestion> listQuestions2 = list(hasEqual);
        this.delete(listQuestions2.stream().filter(e -> e.getOptions() != null).flatMap(e -> e.getOptions().stream())
            .collect(Collectors.toList()));
        this.delete(listQuestions2);
        this.delete(hasEqual);
    }

    public List<Contest> hasEqual(Contest c) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT c ");
            hql.append("FROM Contest c ");
            hql.append("WHERE c.name=:name ");
            hql.append("AND c.job=:job ");
            if (c.getKey() != null) {
                hql.append("AND c.key<:key ");
            }
            Query<Contest> query = session.createQuery(hql.toString(), Contest.class);
            query.setParameter("name", c.getName());
            query.setParameter("job", c.getJob());
            if (c.getKey() != null) {
                query.setParameter("key", c.getKey());
            }
            return query.list().stream().map(BaseDAO::initialize).collect(toList());
        });
    }

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
            return session.createQuery(hql.toString(), ContestText.class).list().stream().map(BaseDAO::initialize)
                .collect(toList());
        });
    }

    public List<ContestText> listTexts(Contest c) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT l ");
            hql.append("FROM ContestText l ");
            hql.append("WHERE l.contest=:c ");
            return session.createQuery(hql.toString(), ContestText.class).setParameter("c", c).list().stream()
                .map(BaseDAO::initialize).collect(toList());
        });
    }

}
