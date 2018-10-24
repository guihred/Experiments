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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;

public class EthicalHackApp extends Application {

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
            new Thread(() -> {
                Map<String, List<String>> scanNetworkOpenPorts = PortScanner
                        .scanNetworkOpenPorts(networkAddress.getText());
                Map<String, List<String>> oses = PortScanner.scanPossibleOSes(networkAddress.getText());
                scanNetworkOpenPorts.forEach((key, valude) -> {
                    Map<String, String> hashMap = new LinkedHashMap<>();
                    hashMap.put("Host", key);
                    hashMap.put("Ports", valude.stream().collect(Collectors.joining("\n")));
                    hashMap.put("OS", oses.get(key).stream().collect(Collectors.joining("\n")));
                    items.add(hashMap);
                });
            }).start();

            addColumns(commonTable, Arrays.asList("Host", "Ports", "OS"));
        });
        Button traceScanner = CommonsFX.newButton("Tracer_oute", e -> {
            items.clear();
            ObservableMap<String, List<String>> scanNetworkOpenPorts = TracerouteScanner
                    .scanNetworkRoutes(networkAddress.getText());
            scanNetworkOpenPorts.addListener((MapChangeListener<String, List<String>>) change -> {
                String key = change.getKey();
                List<String> valueAdded = change.getValueAdded();
                HashMap<String, String> e2 = new HashMap<>();
                e2.put("Host", key);
                e2.put("Route", valueAdded.stream().collect(Collectors.joining("\n")));
                items.add(e2);
            });
            addColumns(commonTable, Arrays.asList("Host", "Route"));
        });
        vBox.getChildren().addAll(new Text("Network Adress"), networkAddress, portScanner, traceScanner);

        HBox hBox = new HBox(vBox, commonTable);
        commonTable.prefWidthProperty().bind(hBox.widthProperty().add(-120));

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
