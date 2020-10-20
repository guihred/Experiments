package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class ConsoleUtils {
    public static final int PROCESS_MAX_TIME_LIMIT = 120_000; // 2 MINUTES
    private static final String ACTIVE_FLAG = "active";
    private static final Logger LOGGER = HasLogging.log();
    private static final String EXECUTING = "Executing \"{}\"";
    private static final Map<String, Boolean> PROCESSES = new ConcurrentHashMap<>();

    private ConsoleUtils() {
    }

    public static DoubleProperty defineProgress(final double n) {
        SimpleDoubleProperty progress = new SimpleDoubleProperty(0);
        RunnableEx.runNewThread(() -> {
            while (PROCESSES.values().stream().anyMatch(e -> !e)) {
                long count = PROCESSES.values().stream().filter(e -> !e).count();
                CommonsFX.update(progress, (n - count) / n);
                RunnableEx.sleepSeconds(0.5);
            }
            CommonsFX.update(progress, 1);
        });
        return progress;
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
        executeInConsoleAsync.get(totalRegex).addListener((Change<? extends String> c) -> {
            while (c.next()) {
                String text = c.getAddedSubList().get(0);
                double applyAsDouble = function.applyAsDouble(text);
                total.set(applyAsDouble);
            }
        });
        executeInConsoleAsync.get(progressRegex).addListener((Change<? extends String> c) -> {
            while (c.next()) {
                String text = c.getAddedSubList().get(0);
                double applyAsDouble = function2.applyAsDouble(text);
                double doubleValue = total.doubleValue();
                progress.set(applyAsDouble / doubleValue);
            }
        });
        executeInConsoleAsync.get(ACTIVE_FLAG).addListener((Change<? extends String> e) -> progress.set(1));
        return progress;
    }

    public static Map<String, String> executeInConsole(final String cmd, final Map<String, String> responses) {
        Map<String, String> result = new HashMap<>();
        LOGGER.debug(EXECUTING, cmd);
        Process exec = newProcess(cmd);
        try (BufferedReader in = new BufferedReader(
            new InputStreamReader(exec.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                LOGGER.debug(line);
                String line1 = line;
                result.putAll(responses.entrySet().stream().filter(r -> line1.matches(r.getKey())).collect(
                    Collectors.toMap(Entry<String, String>::getKey, e -> line1.replaceAll(e.getKey(), e.getValue()))));

            }
            exec.waitFor();
            PROCESSES.put(cmd, true);
			logProcesses();

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
        RunnableEx.runNewThread(() -> updateRegexMapValues(cmd, responses, result));
        return result;
    }

    public static List<String> executeInConsoleInfo(final String cmd) {
        List<String> execution = new ArrayList<>();
        LOGGER.info(EXECUTING, cmd);

        Process p = newProcess(cmd);

        try (BufferedReader in2 = new BufferedReader(
            new InputStreamReader(p.getInputStream(), StandardCharsets.ISO_8859_1))) {
            String line;
            while ((line = in2.readLine()) != null) {
                String fixEncoding = StringSigaUtils.fixEncoding(line, StandardCharsets.ISO_8859_1,
                    Charset.forName("IBM00858"));
                LOGGER.debug("{}", fixEncoding);
                execution.add(fixEncoding);
            }

            p.waitFor();
            PROCESSES.put(cmd, true);
			logProcesses();

        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return execution;
    }

    public static ObservableList<String> executeInConsoleInfo(final String cmd,
            
            
            InputStream inputStream) {
        ObservableList<String> execution = FXCollections.observableArrayList();
        LOGGER.info(EXECUTING, cmd);
        PROCESSES.put(cmd, false);
        RunnableEx.run(() -> {
            Process p = newProcess(cmd);
            IOUtils.copy(inputStream, p.getOutputStream());
            LOGGER.info("INPUT WRITTEN");
            p.getOutputStream().close();
            p.waitFor();
            PROCESSES.put(cmd, true);
            logProcesses();
        });
        return execution;
    }

    public static ObservableList<String> executeInConsoleInfoAsync(final String cmd, final Runnable... onFinish) {
        ObservableList<String> execution = FXCollections.observableArrayList();
        LOGGER.info(EXECUTING, cmd);
        PROCESSES.put(cmd, false);
        RunnableEx.runNewThread(() -> {
            RunnableEx.sleepSeconds(0.1);
            Process p = newProcess(cmd);
            try (BufferedReader in2 = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in2.readLine()) != null) {
                    LOGGER.debug("{}", line);
                    execution.add(line);
                }
                p.waitFor();
                for (int i = 0; i < onFinish.length; i++) {
                    onFinish[i].run();
                }
                PROCESSES.put(cmd, true);
				logProcesses();

            } catch (Exception e) {
                LOGGER.error("", e);
            }
        });
        return execution;
    }

    public static Process startProcessAndWait(final String cmd, String regex) {
        return SupplierEx.remap(() -> makeProcessAndWait(cmd, regex), "ERROR CREATING PROCESS");
    }

    public static void waitAllProcesses() {
        long currentTimeMillis = System.currentTimeMillis();
        while (PROCESSES.values().stream().anyMatch(e -> !e)) {
            try {
                List<String> processes = PROCESSES.entrySet().stream().filter(e -> !e.getValue())
                    .map(Entry<String, Boolean>::getKey).collect(Collectors.toList());
                String formated = processes.stream().collect(Collectors.joining("\n", "\n", ""));
                LOGGER.debug("Running {} processes {}", processes.size(), formated);
                RunnableEx.sleepSeconds(5);
                if (System.currentTimeMillis() - currentTimeMillis > PROCESS_MAX_TIME_LIMIT) {
                    PROCESSES.keySet().stream().forEach(k -> PROCESSES.put(k, true));
                    LOGGER.error("Processes \"{}\" taking too long", formated);
                    break;
                }
            } catch (Exception e1) {
                LOGGER.debug("", e1);
            }
        }
    }

    private static void logProcesses() {
		long count = PROCESSES.values().stream().filter(e -> e).count();
		LOGGER.info("{}/{} Completed ", count, PROCESSES.size());
	}

    private static Process makeProcessAndWait(final String cmd, String regex) throws IOException {
        List<String> response = Collections.synchronizedList(new ArrayList<>(Arrays.asList(regex)));
        Process exec = Runtime.getRuntime().exec(cmd);
        RunnableEx.runNewThread(() -> {
            try (BufferedReader in = new BufferedReader(
                new InputStreamReader(exec.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    LOGGER.debug(line);
                    String line0 = line;
                    if (response.stream().anyMatch(line0::matches)) {
                        response.clear();
                        return;
                    }
                }
            }
        });
        long currentTimeMillis = System.currentTimeMillis();
        while (!response.isEmpty()) {
            if (System.currentTimeMillis() - currentTimeMillis > ConsoleUtils.PROCESS_MAX_TIME_LIMIT) {
                break;
            }
        }
        return exec;
    }

    private static Process newProcess(final String cmd) {
        return SupplierEx.remap(() -> {
            Process p = Runtime.getRuntime().exec(cmd);
            PROCESSES.put(cmd, false);
            return p;
        }, "ERROR CREATING PROCESS");
    }

    private static void updateRegexMapValues(final String cmd, final Map<String, String> responses,
        final Map<String, ObservableList<String>> result) {
        LOGGER.info(EXECUTING, cmd);
        Process exec = newProcess(cmd);
        try (BufferedReader in = new BufferedReader(
            new InputStreamReader(exec.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                LOGGER.debug(line);
                String line1 = line;
                Map<String, String> regMap = responses.entrySet().stream().filter(r -> line1.matches(r.getKey()))
                    .collect(Collectors.toMap(Entry<String, String>::getKey,
                        e -> line1.replaceAll(e.getKey(), e.getValue())));
                regMap.forEach((reg, li) -> result.get(reg).add(li));
            }
            exec.waitFor();
            result.get(ACTIVE_FLAG).add("");
            PROCESSES.put(cmd, true);
			logProcesses();

        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

}
