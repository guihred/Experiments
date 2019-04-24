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
    private static final int WAIT_INTERVAL_MILLIS = 5_000; // 5 SECONDS
    private static final int PROCESS_MAX_TIME_LIMIT = 120_000; // 2 MINUTES
    private static final String ACTIVE_FLAG = "active";
    private static final Logger LOGGER = HasLogging.log();
    private static final String EXECUTING = "Executing \"{}\"";
    private static final Map<String, Boolean> PROCESSES = new ConcurrentHashMap<>();

    private ConsoleUtils() {
    }

    public static DoubleProperty defineProgress(final double n) {
        SimpleDoubleProperty simpleDoubleProperty = new SimpleDoubleProperty(0);
        new Thread(() -> {
            while (PROCESSES.values().stream().anyMatch(e -> !e)) {
                try {
                    long count = PROCESSES.values().stream().filter(e -> !e).count();
                    double newValue = (n - count) / n;
                    simpleDoubleProperty.set(newValue);
                    Thread.sleep(500);
                } catch (Exception e1) {
                    LOGGER.trace("", e1);
                }
            }
            simpleDoubleProperty.set(1);
        }).start();
        return simpleDoubleProperty;
    }

    public static DoubleProperty defineProgress(final String totalRegex, final String progressRegex,
        final Map<String, ObservableList<String>> executeInConsoleAsync, final ToDoubleFunction<String> function) {
        return defineProgress(totalRegex, progressRegex, executeInConsoleAsync, function, function);
    }

    public static DoubleProperty defineProgress(final String totalRegex, final String progressRegex,
        final Map<String, ObservableList<String>> executeInConsoleAsync, final ToDoubleFunction<String> function,
        final ToDoubleFunction<String> function2) {
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
                double applyAsDouble = function2.applyAsDouble(text);
                double doubleValue = total.doubleValue();
                progress.set(applyAsDouble / doubleValue);
            }
        });
        executeInConsoleAsync.get(ACTIVE_FLAG).addListener((final Change<? extends String> e) -> progress.set(1));
        return progress;
    }

    public static Map<String, String> executeInConsole(final String cmd, final Map<String, String> responses) {
        Map<String, String> result = new HashMap<>();
        LOGGER.info(EXECUTING, cmd);
        Process exec = newProcess(cmd);
        try (BufferedReader in = new BufferedReader(
            new InputStreamReader(exec.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                LOGGER.trace(line);
                String line1 = line;
                result.putAll(responses.entrySet().stream().filter(r -> line1.matches(r.getKey())).collect(
                    Collectors.toMap(Entry<String, String>::getKey, e -> line1.replaceAll(e.getKey(), e.getValue()))));

            }
            exec.waitFor();
            PROCESSES.put(cmd, true);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return result;
    }

    public static Map<String, ObservableList<String>> executeInConsoleAsync(final String cmd,
        final Map<String, String> responses) {
        Map<String, ObservableList<String>> result = new HashMap<>();
        result.put(ACTIVE_FLAG, FXCollections.observableArrayList());
        responses.forEach((reg, li) -> result.put(reg, FXCollections.observableArrayList()));
        new Thread(RunnableEx.makeRunnable(() -> updateRegexMapValues(cmd, responses, result))).start();
        return result;
    }

    public static List<String> executeInConsoleInfo(final String cmd) {
        List<String> execution = new ArrayList<>();
        LOGGER.info(EXECUTING, cmd);

        Process p = newProcess(cmd);

        try (BufferedReader in2 = new BufferedReader(
            new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in2.readLine()) != null) {
                LOGGER.trace("{}", line);
                execution.add(line);
            }
            p.waitFor();
            PROCESSES.put(cmd, true);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return execution;
    }

    public static ObservableList<String> executeInConsoleInfoAsync(final String cmd, final Runnable... onFinish) {
        ObservableList<String> execution = FXCollections.observableArrayList();
        LOGGER.info(EXECUTING, cmd);
        PROCESSES.put(cmd, false);
        new Thread(() -> {
            Process p = newProcess(cmd);
            try (BufferedReader in2 = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in2.readLine()) != null) {
                    LOGGER.trace("{}", line);
                    execution.add(line);
                }
                p.waitFor();
                for (int i = 0; i < onFinish.length; i++) {
                    onFinish[i].run();
                }
                PROCESSES.put(cmd, true);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }).start();
        return execution;
    }

    public static void waitAllProcesses() {
        long currentTimeMillis = System.currentTimeMillis();
        while (PROCESSES.values().stream().anyMatch(e -> !e)) {
            try {
                List<String> processes = PROCESSES.entrySet().stream().filter(e -> !e.getValue())
                    .map(Entry<String, Boolean>::getKey).collect(Collectors.toList());
                String formated = processes.stream().collect(Collectors.joining("\n", "\n", ""));
                LOGGER.info("Running {} processes {}", processes.size(), formated);
                Thread.sleep(WAIT_INTERVAL_MILLIS);
                if (System.currentTimeMillis() - currentTimeMillis > PROCESS_MAX_TIME_LIMIT) {
                    PROCESSES.keySet().stream().collect(Collectors.toList()).forEach(k -> PROCESSES.put(k, true));
                    LOGGER.error("Processes \"{}\" taking too long", formated);
                    break;
                }
            } catch (Exception e1) {
                LOGGER.trace("", e1);
            }
        }
    }

    private static Process newProcess(final String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            PROCESSES.put(cmd, false);
            return p;
        } catch (Exception e) {
            throw new RuntimeIOException("ERROR CREATING PROCESS", e);
        }
    }

    private static void updateRegexMapValues(final String cmd, final Map<String, String> responses,
        final Map<String, ObservableList<String>> result) {
        LOGGER.info(EXECUTING, cmd);
        Process exec = newProcess(cmd);
        try (BufferedReader in = new BufferedReader(
            new InputStreamReader(exec.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                LOGGER.trace(line);
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
    }

}
