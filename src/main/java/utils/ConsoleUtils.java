package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import org.assertj.core.api.exception.RuntimeIOException;
import org.slf4j.Logger;

public final class ConsoleUtils {
    private static final String ACTIVE_FLAG = "active";
    private static final Logger LOGGER = HasLogging.log();
    private static final String EXECUTING = "Executing \"{}\"";
    private static final Map<String, Boolean> PROCESSES = new ConcurrentHashMap<>();

    private ConsoleUtils() {
    }

    public static void executeInConsole(String cmd) {

        LOGGER.info(EXECUTING, cmd);
        Process p = newProcess(cmd);
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                LOGGER.info("\"{}\"", line);
            }
            p.waitFor();
            PROCESSES.put(cmd, true);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private static Process newProcess(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            PROCESSES.put(cmd, false);
            return p;
        } catch (Exception e) {
            LOGGER.error("ERROR CREATING PROCESS", e);
            throw new RuntimeIOException("", e);
        }
    }

    public static void waitAllProcesses() {
        while (PROCESSES.values().stream().anyMatch(e -> !e)) {
            try {
                List<String> collect = PROCESSES.entrySet().stream().filter(e -> !e.getValue())
                        .map(Entry<String, Boolean>::getKey).collect(Collectors.toList());
                LOGGER.info("Runing processes {}", collect);
                Thread.sleep(5000);
            } catch (Exception e1) {
                LOGGER.trace("", e1);
            }
        }
    }

    public static List<String> executeInConsoleInfo(String cmd) {
        List<String> execution = new ArrayList<>();
        LOGGER.info(EXECUTING, cmd);

        Process p = newProcess(cmd);

        try (BufferedReader in2 = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in2.readLine()) != null) {
                LOGGER.info("{}", line);
                execution.add(line);
            }
            p.waitFor();
            PROCESSES.put(cmd, true);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return execution;
    }

    public static Map<String, String> executeInConsole(String cmd, Map<String, String> responses) {
        Map<String, String> result = new HashMap<>();
        LOGGER.info(EXECUTING, cmd);
        Process exec = newProcess(cmd);
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(exec.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                LOGGER.info(line);
                String line1 = line;
                result.putAll(responses.entrySet().stream().filter(r -> line1.matches(r.getKey())).collect(Collectors
                        .toMap(Entry<String, String>::getKey, e -> line1.replaceAll(e.getKey(), e.getValue()))));

            }
            exec.waitFor();
            PROCESSES.put(cmd, true);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return result;
    }

    public static DoubleProperty defineProgress(String totalRegex, String progressRegex,
            Map<String, ObservableList<String>> executeInConsoleAsync, ToDoubleFunction<String> function) {
        SimpleDoubleProperty progress = new SimpleDoubleProperty(0);
        SimpleDoubleProperty total = new SimpleDoubleProperty(100);
        executeInConsoleAsync.get(totalRegex).addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                String text = c.getAddedSubList().get(0);
                double applyAsDouble = function.applyAsDouble(text);
                total.set(applyAsDouble);
            }
        });
        executeInConsoleAsync.get(progressRegex).addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                String text = c.getAddedSubList().get(0);
                double applyAsDouble = function.applyAsDouble(text);
                double doubleValue = total.doubleValue();
                progress.set(applyAsDouble / doubleValue);
            }
        });
        executeInConsoleAsync.get(ACTIVE_FLAG).addListener((Change<? extends String> e) -> progress.set(1));
        return progress;
    }

    public static Map<String, ObservableList<String>> executeInConsoleAsync(String cmd, Map<String, String> responses) {
        Map<String, ObservableList<String>> result = new HashMap<>();
        result.put(ACTIVE_FLAG, FXCollections.observableArrayList());
        responses.forEach((reg, li) -> result.put(reg, FXCollections.observableArrayList()));
        new Thread(RunnableEx.makeRunnable(() -> {
            LOGGER.info(EXECUTING, cmd);
            Process exec = newProcess(cmd);
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(exec.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    LOGGER.info(line);
                    String line1 = line;
                    Map<String, String> regMap = responses.entrySet().stream().filter(r -> line1.matches(r.getKey()))
                            .collect(Collectors.toMap(Entry<String, String>::getKey,
                                    e -> line1.replaceAll(e.getKey(), e.getValue())));
                    regMap.forEach((reg, li) -> result.get(reg).add(li));
                }
                exec.waitFor();
                result.get(ACTIVE_FLAG).add("");
                PROCESSES.put(cmd, true);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        })).start();
        return result;
    }

}
