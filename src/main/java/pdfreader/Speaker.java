package pdfreader;

import java.util.Locale;
import javax.speech.Central;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import org.slf4j.Logger;
import utils.HasLogging;

public enum Speaker {
    SPEAKER;
    private static final Logger LOG = HasLogging.log();
    private Synthesizer synthesizer;

    Speaker() {
        loadSynthetizer();
    }

    public void dealocate() {
        try {
            synthesizer.deallocate();
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public void speak(String string) {
        // speaks the given text until queue is empty.
        try {
            synthesizer.speakPlainText(string, null);
            synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    private void loadSynthetizer() throws EngineStateError {
        try {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");

            // Register Engine
            Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
            SynthesizerModeDesc desc = new SynthesizerModeDesc(null, "general", Locale.US, false, null);
            synthesizer = Central.createSynthesizer(desc);

            if (synthesizer == null) {
                LOG.error("Cannot create synthesizer");
                return;
            }
            synthesizer.allocate();
            synthesizer.resume();

        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public static void main(String[] args) {

        try {
            // set property as Kevin Dictionary
            Speaker.SPEAKER.speak("Hi");
            Speaker.SPEAKER.speak("How Are You");
            Speaker.SPEAKER.speak("Show me the money");
            Speaker.SPEAKER.dealocate();
        } catch (Exception e) {
            LOG.error("", e);
        }
    }
}
