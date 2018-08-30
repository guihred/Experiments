package crypt;

import election.experiment.ElectionCrawlerApp;
import japstudy.JapaneseLessonApplication;
import japstudy.JapaneseLessonAudioSplitDisplay;
import japstudy.JapaneseLessonDisplay;
import japstudy.db.HibernateUtil;
import org.junit.Test;
import simplebuilder.HasLogging;

public final class FXHibernateTest implements HasLogging {
    @Test
    public void test() throws Throwable {
        HibernateUtil.getSessionFactory();
        FXTesting.testApps(ElectionCrawlerApp.class, JapaneseLessonApplication.class,
                JapaneseLessonAudioSplitDisplay.class, JapaneseLessonDisplay.class);
        HibernateUtil.shutdown();

    }
}
