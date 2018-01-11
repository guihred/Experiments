package election.experiment;

import java.util.List;

import org.hibernate.Session;

import japstudy.db.HibernateUtil;

public class CidadeDAO {

	public void saveOrUpdate(Object cidade) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.saveOrUpdate(cidade);
		session.flush();
		session.getTransaction().commit();
	}

	public List<Cidade> list() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT l ");
		hql.append("FROM Cidade l ");
		List<Cidade> list = session.createQuery(hql.toString(), Cidade.class).setMaxResults(10).list();
		session.getTransaction().commit();
		return list;
	}

}
