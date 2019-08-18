package fxtests;

import static exercism.Bob.hey;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

@SuppressWarnings("static-method")
public class BobTest {

    @Test
    public void askingANumericQuestion() {
        assertEquals("", "Sure.", hey("You are, what, like 15?"));
    }

    @Test
    public void askingAQuestion() {
        assertEquals("", "Sure.", hey("Does this cryogenic chamber make me look fat?"));
    }

    @Test
    public void calmlySpeakingWithUmlauts() {
        assertEquals("", "Whatever.", hey("\u00dcml\u00e4\u00dcts"));
    }

    @Test
    public void forcefulQuestions() {
        assertEquals("", "Whoa, chill out!", hey("WHAT THE HELL WERE YOU THINKING?"));
    }

    @Test
    public void onlyNumbers() {
        assertEquals("", "Whatever.", hey("1, 2, 3"));
    }

    @Test
    public void prattlingOn() {
        assertEquals("", "Sure.", hey("Wait! Hang on. Are you going to be OK?"));
    }

    @Test
    public void prolongedSilence() {
        assertEquals("", "Fine. Be that way!", hey("    "));
    }

    @Test
    public void questionWithOnlyNumbers() {
        assertEquals("", "Sure.", hey("4?"));
    }

    @Test
    public void saySomething() {
        assertEquals("", "Whatever.", hey("Tom-ay-to, tom-aaaah-to."));
    }

    @Test
    public void shouting() {
        assertEquals("", "Whoa, chill out!", hey("WATCH OUT!"));
    }

    @Test
    public void shoutingNumbers() {
        assertEquals("", "Whoa, chill out!", hey("1, 2, 3 GO!"));
    }

    @Test
    public void shoutingWithNoExclamationMark() {
        assertEquals("", "Whoa, chill out!", hey("I HATE YOU"));
    }

    @Test
    public void shoutingWithSpecialCharacters() {
        assertEquals("", "Whoa, chill out!", hey("ZOMG THE %^*@#$(*^ ZOMBIES ARE COMING!!11!!1!"));
    }

    @Test
    public void shoutingWithUmlauts() {
        assertEquals("", "Whoa, chill out!", hey("\u00dcML\u00c4\u00dcTS!"));
    }

    @Test
    public void silence() {
        assertEquals("", "Fine. Be that way!", hey(""));
    }

    @Test
    public void statementContainingQuestionMark() {
        assertEquals("", "Whatever.", hey("Ending with ? means a question."));
    }

    @Test
    public void talkingForcefully() {
        assertEquals("", "Whatever.", hey("Let's go make out behind the gym!"));
    }

    @Test
    public void usingAcronymsInRegularSpeech() {
        assertEquals("", "Whatever.", hey("It's OK if you don't want to go to the DMV."));
    }
}