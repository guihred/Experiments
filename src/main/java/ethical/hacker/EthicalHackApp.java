package ethical.hacker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox vBox = new VBox();
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
        TableView<Map<String, String>> commonTable = new SimpleTableViewBuilder<Map<String, String>>().items(items)
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
        Button portScanner = CommonsFX.newButton("_Port Scan", e -> {
            items.clear();
            addColumns(commonTable, Arrays.asList("Host", "Route", "OS", "Ports"));
            new Thread(() -> {
                ObservableMap<String, List<String>> scanNetworkOpenPorts = PortScanner
                        .scanNetworkOpenPorts(networkAddress.getText());
                scanNetworkOpenPorts
                        .addListener(updateItemOnChange(items, "Host", "Ports"));
                ObservableMap<String, List<String>> oses = PortScanner.scanPossibleOSes(networkAddress.getText());
                oses.addListener(updateItemOnChange(items, "Host", "OS"));
                ObservableMap<String, List<String>> networkRoutes = TracerouteScanner
                        .scanNetworkRoutes(networkAddress.getText());
                networkRoutes.addListener(updateItemOnChange(items, "Host", "Route"));
                ConsoleUtils.waitAllProcesses();

            }).start();

        });
        vBox.getChildren().addAll(new Text("Network Adress"), networkAddress, portScanner);

        HBox hBox = new HBox(vBox, commonTable);
        commonTable.prefWidthProperty().bind(hBox.widthProperty().add(-120));
        primaryStage.setTitle("Ethical Hack App");
        primaryStage.setScene(new Scene(hBox, 500, 500));
        primaryStage.show();

    }

    private void addColumns(TableView<Map<String, String>> simpleTableViewBuilder, Collection<String> keySet) {
        simpleTableViewBuilder.getColumns().clear();
        keySet.forEach(key -> {
            final TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(
                    param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key), "-")));
            column.prefWidthProperty().bind(simpleTableViewBuilder.widthProperty().divide(keySet.size()).add(-5));
            simpleTableViewBuilder.getColumns().add(column);
        });
    }

    private MapChangeListener<String, List<String>> updateItemOnChange(ObservableList<Map<String, String>> items,
            String primaryKey, String targetKey) {
        return change -> {
            if (!change.wasAdded()) {
                return;
            }
            String key = change.getKey();
            List<String> valueAdded = change.getValueAdded();
            Map<String, String> e2 = items.stream().filter(c -> key.equals(c.get(primaryKey))).findAny()
                    .orElseGet(ConcurrentHashMap<String, String>::new);
            e2.put(primaryKey, key);
            e2.put(targetKey, valueAdded.stream().collect(Collectors.joining("\n")));
            if (!items.contains(e2)) {
                items.add(e2);
            }
            Map<String, String> map = items.remove(0);
            items.add(0, map);
            LOG.info("{} of {} = {}", targetKey, key, valueAdded);
        };
    }

    public static void main(String[] args) {
        launch(args);
    }

}
