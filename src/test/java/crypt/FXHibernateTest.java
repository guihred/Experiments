package crypt;

import election.experiment.ElectionCrawlerApp;
import japstudy.JapaneseLessonApplication;
import japstudy.JapaneseLessonAudioSplitDisplay;
import japstudy.JapaneseLessonDisplay;
import japstudy.JapaneseLessonEditingDisplay;
import japstudy.db.HibernateUtil;
import org.junit.Test;
import simplebuilder.HasLogging;

public final class FXHibernateTest implements HasLogging {
    @Test
    public void test() throws Throwable {
        HibernateUtil.getSessionFactory(); 
        HibernateUtil.setShutdownEnabled(false);
        FXTesting.testApps(ElectionCrawlerApp.class, 
                JapaneseLessonApplication.class,
                JapaneseLessonEditingDisplay.class,
                JapaneseLessonAudioSplitDisplay.class, 
                JapaneseLessonDisplay.class);
        HibernateUtil.setShutdownEnabled(true);
        HibernateUtil.shutdown();

    }
}
