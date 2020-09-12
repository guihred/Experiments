package pdfreader;

import java.util.Locale;
import javax.speech.Central;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import utils.ex.RunnableEx;

public enum Speaker {
    SPEAKER;
    private Synthesizer synthesizer;

    Speaker() {
        loadSynthetizer();
    }

    public void dealocate() {
        RunnableEx.run(synthesizer::deallocate);
    }

    public void speak(String string) {
        // speaks the given text until queue is empty.
        RunnableEx.run(() -> {
            synthesizer.speakPlainText(string, null);
            synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        });
    }

    private void loadSynthetizer() throws EngineStateError {
        RunnableEx.run(() -> {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            // Register Engine
            Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
            SynthesizerModeDesc desc = new SynthesizerModeDesc(null, "general", Locale.US, false, null);
            synthesizer = Central.createSynthesizer(desc);
            RunnableEx.runIf(synthesizer, s -> {
                synthesizer.allocate();
                synthesizer.resume();
            });
        });
    }
}
