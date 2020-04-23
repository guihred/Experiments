package fxpro.ch06;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class ThreadInformationModel {

    private final ObservableList<String> stackTraces = FXCollections.observableArrayList();
    private final ObservableList<String> threadNames = FXCollections.observableArrayList();

    public ThreadInformationModel() {
        update();
    }

    public ObservableList<String> getStackTraces() {
        return stackTraces;
    }

    public ObservableList<String> getThreadNames() {
        return threadNames;
    }

    public final void update() {
        threadNames.clear();
        stackTraces.clear();
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        map.forEach((k, v) -> {
            threadNames.add("\"" + k.getName() + "\"");
            stackTraces.add(formatStackTrace(v));
        });
    }

    private static String formatStackTrace(StackTraceElement[] value) {
        return Stream.of(value).map(StackTraceElement::toString)
            .collect(Collectors.joining("\n at ", "StackTrace: ", ""));
    }
}