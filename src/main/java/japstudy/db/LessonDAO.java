package japstudy.db;

import java.time.LocalTime;
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
        hql.append("FROM JapaneseLesson l  ");
		hql.append("ORDER BY lesson, exercise");
		List<JapaneseLesson> list = session
				.createQuery(hql.toString(), JapaneseLesson.class).list();
		session.getTransaction().commit();
		return list;
	}

	public Long getCountExerciseByLesson(Integer lesson) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT COUNT(DISTINCT l.id.exercise) ");
		hql.append("FROM JapaneseLesson l ");
		hql.append("WHERE l.id.lesson=:lesson");
		Long list = session.createQuery(hql.toString(), Long.class).setParameter("lesson", lesson).uniqueResult();
		session.getTransaction().commit();
		return list;
	}

	public LocalTime getMaxTimeLesson(Integer lesson, Integer exercise) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT MAX(l.end) ");
		hql.append("FROM JapaneseLesson l ");
		hql.append("WHERE l.id.lesson=:lesson");
		hql.append(" AND l.id.exercise<:exercise");
		LocalTime list = session.createQuery(hql.toString(), LocalTime.class).setParameter("lesson", lesson)
				.setParameter("exercise", exercise)
				.uniqueResult();
		session.getTransaction().commit();
		return list;
	}

}
