package election.experiment;

import java.util.List;

import org.hibernate.Session;

import japstudy.db.HibernateUtil;

public class CandidatoDAO {

    public Candidato retrieve(String href) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Candidato candidato = session.get(Candidato.class, href);
        session.getTransaction().commit();
        return candidato;
    }

    public void saveOrUpdate(Candidato candidato) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
        session.merge(candidato);
		session.getTransaction().commit();
	}

    public List<Candidato> list(int startPosition, int maxResult) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		StringBuilder hql = new StringBuilder();
        hql.append(" SELECT l ");
        hql.append(" FROM Candidato l");
        hql.append(" WHERE l.nomeCompleto IS NULL");
        List<Candidato> list = session.createQuery(hql.toString(), Candidato.class).setFirstResult(startPosition)
                .setMaxResults(maxResult).list();
		session.getTransaction().commit();
		return list;
	}

    public Long size() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        StringBuilder hql = new StringBuilder();
        hql.append("SELECT COUNT(l) ");
        hql.append(" FROM Candidato l");
        // hql.append(" WHERE l.nomeCompleto IS NULL");
        Long list = session.createQuery(hql.toString(), Long.class).uniqueResult();
        session.getTransaction().commit();
        return list;
    }

}
