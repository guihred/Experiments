package japstudy.db;

import java.util.List;

import org.hibernate.Session;

public class LessonDAO {

	public void saveOrUpdate(JapaneseLesson jap) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.saveOrUpdate(jap);
		session.flush();
		session.getTransaction().commit();
	}

	public List<JapaneseLesson> list() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT l ");
		hql.append("FROM JapaneseLesson l ");
		hql.append("ORDER BY lesson, exercise");
		List<JapaneseLesson> list = session
				.createQuery(hql.toString(), JapaneseLesson.class).list();
		session.getTransaction().commit();
		return list;
	}


}
