package ethical.hacker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.ConsoleUtils;
import utils.HasLogging;

public class EthicalHackApp extends Application {

    private static final Logger LOG = HasLogging.log();
    private ObservableMap<String, Set<String>> count = FXCollections.observableHashMap();

    @Override
    public void start(final Stage primaryStage) throws Exception {
        VBox vBox = new VBox();
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
        FilteredList<Map<String, String>> filt = items.filtered(e -> true);
        TableView<Map<String, String>> commonTable = new SimpleTableViewBuilder<Map<String, String>>().items(filt)
            .build();

        Button detworkInformationScanner = CommonsFX.newButton("_Network Information", e -> {
            items.clear();
            List<Map<String, String>> nsInformation = NetworkInformationScanner.getNetworkInformation();
            items.addAll(nsInformation);
            Set<String> keySet = nsInformation.stream().flatMap(m -> m.keySet().stream()).collect(Collectors.toSet());
            addColumns(commonTable, keySet);
        });
        Button processScanner = CommonsFX.newButton("Process _Scan", e -> {
            items.clear();
            List<Map<String, String>> currentTasks = ProcessScan.scanCurrentTasks();
            items.addAll(currentTasks);
            Set<String> keySet = items.stream().flatMap(m -> m.keySet().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
            addColumns(commonTable, keySet);
        });
        vBox.getChildren().addAll(detworkInformationScanner, processScanner);

        TextField dns = new TextField("google.com");

        Button nameServerButton = CommonsFX.newButton("DNS _Lookup", e -> {
            items.clear();
            Map<String, String> nsInformation = NameServerLookup.getNSInformation(dns.getText());
            items.add(nsInformation);
            Set<String> keySet = nsInformation.keySet();
            addColumns(commonTable, keySet);
        });
        vBox.getChildren().addAll(new Text("DNS"), dns, nameServerButton);

        TextField address = new TextField(TracerouteScanner.IP_TO_SCAN);
        Button pingTrace = CommonsFX.newButton("Ping _Trace", e -> {
            items.clear();
            Map<String, String> nsInformation = PingTraceRoute.getInformation(address.getText());
            items.add(nsInformation);
            Set<String> keySet = nsInformation.keySet();
            addColumns(commonTable, keySet);
        });
        vBox.getChildren().addAll(new Text("Ping Adress"), address, pingTrace);

        TextField networkAddress = new TextField(TracerouteScanner.NETWORK_ADDRESS);
        ProgressIndicator progressIndicator = new ProgressIndicator(0);
        progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());
        progressIndicator.setVisible(false);
        Button portScanner = CommonsFX.newButton("_Port Scan", e -> {
            items.clear();
            addColumns(commonTable, Arrays.asList("Host", "Route", "OS", "Ports"));
            new Thread(() -> {
                ObservableMap<String, List<String>> scanNetworkOpenPorts = PortScanner
                    .scanNetworkOpenPorts(networkAddress.getText());
                scanNetworkOpenPorts.addListener(updateItemOnChange(items, "Host", "Ports"));
                ObservableMap<String, List<String>> oses = PortScanner.scanPossibleOSes(networkAddress.getText());
                oses.addListener(updateItemOnChange(items, "Host", "OS"));
                ObservableMap<String, List<String>> networkRoutes = TracerouteScanner
                    .scanNetworkRoutes(networkAddress.getText());
                networkRoutes.addListener(updateItemOnChange(items, "Host", "Route"));
                DoubleProperty defineProgress = ConsoleUtils.defineProgress(3);
                progressIndicator.setVisible(true);
                progressIndicator.progressProperty().bind(defineProgress);
                ConsoleUtils.waitAllProcesses();
            }).start();

        });
        vBox.getChildren().addAll(new Text("Network Adress"), networkAddress, portScanner);
        vBox.getChildren().addAll(progressIndicator);

        vBox.getChildren().addAll(new Text("Filter Results"), configurarFiltroRapido(filt));
        HBox hBox = new HBox(vBox, commonTable);
        final int columnWidth = 120;
        commonTable.prefWidthProperty().bind(hBox.widthProperty().add(-columnWidth));
        primaryStage.setTitle("Ethical Hack App");
        primaryStage.setScene(new Scene(hBox, 500, 500));
        primaryStage.show();

    }

    private void addColumns(final TableView<Map<String, String>> simpleTableViewBuilder,
        final Collection<String> keySet) {
        simpleTableViewBuilder.getColumns().clear();
        keySet.forEach(key -> {
            final TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(
                param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key), "-")));
            column.prefWidthProperty().bind(simpleTableViewBuilder.widthProperty().divide(keySet.size()).add(-5));
            simpleTableViewBuilder.getColumns().add(column);
        });
    }

    private TextField configurarFiltroRapido(FilteredList<Map<String, String>> filteredData) {
        TextField filterField = new TextField();
        filterField.textProperty()
            .addListener((o, old, value) -> filteredData.setPredicate(row -> {
                if (value == null || value.isEmpty()) {
                    return true;
                }
                return row.toString().toLowerCase().contains(value.toLowerCase());
            }));
        return filterField;
    }

    private void updateItem(final ObservableList<Map<String, String>> items, final String primaryKey,
        final String targetKey, Change<? extends String, ? extends List<String>> change) {
        synchronized (count) {
            if (!change.wasAdded()) {
                return;
            }
            String key = change.getKey();
            List<String> valueAdded = change.getValueAdded();
            Map<String, String> e2 = items.stream().filter(c -> key.equals(c.get(primaryKey))).findAny()
                .orElseGet(ConcurrentHashMap::new);
            e2.put(primaryKey, key);
            e2.put(targetKey, valueAdded.stream().collect(Collectors.joining("\n")));
            if (!items.contains(e2)) {
                items.add(e2);
            }
            items.add(0, items.remove(0));
            Set<String> orDefault = count.getOrDefault(primaryKey, new HashSet<>());
            orDefault.add(key);
            count.put(targetKey, orDefault);
            LOG.debug("{} of {} = {}", targetKey, key, valueAdded);
        }
    }

    private MapChangeListener<String, List<String>> updateItemOnChange(final ObservableList<Map<String, String>> items,
        final String primaryKey, final String targetKey) {
        count.clear();
        return change -> updateItem(items, primaryKey, targetKey, change);
    }

    public static void main(final String[] args) {
        launch(args);
    }

}
