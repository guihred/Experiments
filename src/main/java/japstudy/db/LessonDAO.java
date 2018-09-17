package japstudy.db;

import java.time.LocalTime;
import java.util.List;

public class LessonDAO extends BaseDAO {

	public void saveOrUpdate(JapaneseLesson jap) {
        executeRun(session -> session.saveOrUpdate(jap));
	}

    public synchronized List<JapaneseLesson> list() {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT l ");
            hql.append("FROM JapaneseLesson l  ");
            //        hql.append("ORDER BY l.pk.lesson, l.pk.exercise")
            return session.createQuery(hql.toString(), JapaneseLesson.class).list();
        });
	}

	public Long getCountExerciseByLesson(Integer lesson) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT COUNT(DISTINCT l.id.exercise) ");
            hql.append("FROM JapaneseLesson l ");
            hql.append("WHERE l.id.lesson=:lesson");
            return session.createQuery(hql.toString(), Long.class).setParameter("lesson", lesson).uniqueResult();
        });
	}

	public LocalTime getMaxTimeLesson(Integer lesson, Integer exercise) {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT MAX(l.end) ");
            hql.append("FROM JapaneseLesson l ");
            hql.append("WHERE l.id.lesson=:lesson");
            hql.append(" AND l.id.exercise<:exercise");
            return session.createQuery(hql.toString(), LocalTime.class)
                    .setParameter("lesson", lesson)
                    .setParameter("exercise", exercise)
                    .uniqueResult();
        });
	}

}
