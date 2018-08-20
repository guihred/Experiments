package contest.db;

import japstudy.db.HibernateUtil;
import java.util.List;
import org.hibernate.Session;

public class ContestQuestionDAO {

	public void saveOrUpdate(ContestQuestion jap) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.saveOrUpdate(jap);
		session.flush();
		session.getTransaction().commit();
	}

	public List<ContestQuestion> list() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT l ");
		hql.append("FROM ContestQuestion l ");
		hql.append("ORDER BY number");
		List<ContestQuestion> list = session.createQuery(hql.toString(), ContestQuestion.class).list();
		session.getTransaction().commit();
		return list;
	}

}
