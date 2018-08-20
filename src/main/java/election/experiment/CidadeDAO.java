package election.experiment;

import japstudy.db.HibernateUtil;
import java.util.List;
import org.hibernate.Session;

public class CidadeDAO {

	public void saveOrUpdate(Object cidade) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.saveOrUpdate(cidade);
		session.getTransaction().commit();
	}

	public List<Cidade> list() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT l ");
        hql.append("FROM Cidade l");
        List<Cidade> list = session.createQuery(hql.toString(), Cidade.class).list();
		session.getTransaction().commit();
		return list;
	}

}
