package ethical.hacker;

import java.util.*;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;

public class EthicalHackApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String address = "google.com";
        ObservableList<Map<String, String>> value = FXCollections.observableArrayList();
        TableView<Map<String, String>> simpleTableViewBuilder = new SimpleTableViewBuilder<Map<String, String>>()
                .items(value).build();
        Button nameServerButton = CommonsFX.newButton("NS _Lookup", e -> {
            value.clear();
            Map<String, String> nsInformation = NameServerLookup.getNSInformation(address);
            value.add(nsInformation);
            Set<String> keySet = nsInformation.keySet();
            addColumns(simpleTableViewBuilder, keySet);
        });
        Button detworkInformationScanner = CommonsFX.newButton("_Network Information", e -> {
            value.clear();
            List<Map<String, String>> nsInformation = NetworkInformationScanner.getNetworkInformation();
            value.addAll(nsInformation);
            Set<String> keySet = nsInformation.stream().flatMap(m -> m.keySet().stream()).collect(Collectors.toSet());
            addColumns(simpleTableViewBuilder, keySet);
        });
        String address2 = "10.69.64.31";
        Button pingTrace = CommonsFX.newButton("Ping _Trace", e -> {
            value.clear();
            Map<String, String> nsInformation = PingTraceRoute.getInformation(address2);
            value.add(nsInformation);
            Set<String> keySet = nsInformation.keySet();
            addColumns(simpleTableViewBuilder, keySet);
        });
        String networkAddress = "10.69.64.31/28";
        Button portScanner = CommonsFX.newButton("_Port Scan", e -> {
            value.clear();
            Map<String, List<String>> scanNetworkOpenPorts = PortScanner.scanNetworkOpenPorts(networkAddress);
            Map<String, List<String>> oses = PortScanner.scanPossibleOSes(networkAddress);
            scanNetworkOpenPorts.forEach((key, valude) -> {
                Map<String, String> hashMap = new LinkedHashMap<>();
                hashMap.put("Host", key);
                hashMap.put("Ports", valude.stream().collect(Collectors.joining("\n")));
                hashMap.put("OS'es", oses.get(key).stream().collect(Collectors.joining("\n")));
                value.add(hashMap);
            });

            Set<String> keySet = value.stream().flatMap(m -> m.keySet().stream())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            addColumns(simpleTableViewBuilder, keySet);
        });
        Button processScanner = CommonsFX.newButton("Process _Scan", e -> {
            value.clear();
            List<Map<String, String>> currentTasks = ProcessScan.scanCurrentTasks();
            value.addAll(currentTasks);
            Set<String> keySet = value.stream().flatMap(m -> m.keySet().stream())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            addColumns(simpleTableViewBuilder, keySet);
        });
        Button traceScanner = CommonsFX.newButton("Tracer_oute", e -> {
            value.clear();
            ObservableMap<String, List<String>> scanNetworkOpenPorts = TracerouteScanner
                    .scanNetworkRoutes(networkAddress);
            scanNetworkOpenPorts.addListener((MapChangeListener<String, List<String>>) change -> {
                String key = change.getKey();
                List<String> valueAdded = change.getValueAdded();
                HashMap<String, String> e2 = new HashMap<>();
                e2.put("Host", key);
                e2.put("Route", valueAdded.stream().collect(Collectors.joining("\n")));
                value.add(e2);
            });
            addColumns(simpleTableViewBuilder, Arrays.asList("Host", "Route"));
        });
        VBox vBox = new VBox(nameServerButton, detworkInformationScanner, pingTrace, portScanner, processScanner,
                traceScanner);
        HBox hBox = new HBox(vBox,simpleTableViewBuilder);
        simpleTableViewBuilder.prefWidthProperty().bind(hBox.widthProperty().add(-120));

        primaryStage.setScene(new Scene(hBox, 500, 500));
        primaryStage.show();

    }

    private void addColumns(TableView<Map<String, String>> simpleTableViewBuilder, Collection<String> keySet) {
        simpleTableViewBuilder.getColumns().clear();
        keySet.forEach(key -> {
            final TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key))));
            column.prefWidthProperty().bind(simpleTableViewBuilder.widthProperty().divide(keySet.size()).add(-5));
            simpleTableViewBuilder.getColumns().add(column);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}
