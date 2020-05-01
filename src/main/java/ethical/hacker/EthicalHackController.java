package ethical.hacker;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableHashMap;
import static javafx.collections.FXCollections.synchronizedObservableList;
import static simplebuilder.SimpleTableViewBuilder.newCellFactory;

import extract.ExcelService;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.StageHelper;
import utils.*;

public class EthicalHackController extends Application {
    private static final int WIDTH = 500;
    @FXML
    private TextField resultsFilter;
    @FXML
    private TextField dns;

    @FXML
    private TableView<Entry<Integer, String>> servicesTable;
    @FXML
    private TextField address;
    @FXML
    private TextField networkAddress;
    @FXML
    private TextField filterField;
    @FXML
    private TableColumn<Entry<Integer, String>, Object> portColumn;
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Text ports;
    @FXML
    private TableView<Map<String, String>> commonTable;
    private ObservableList<Integer> portsSelected = observableArrayList();
    private ObservableList<Map<String, String>> items = synchronizedObservableList(observableArrayList());

    private ObservableMap<String, Set<String>> count = observableHashMap();
    public void initialize() {
        final int columnWidth = 120;
        HBox parent = (HBox) commonTable.getParent();
        commonTable.prefWidthProperty().bind(parent.widthProperty().add(-columnWidth));
        ports.textProperty().bind(
                Bindings.createStringBinding(() -> String.format("Port Services %s", portsSelected), portsSelected));
        commonTable.setItems(CommonsFX.newFastFilter(resultsFilter, items.filtered(e -> true)));

        Map<Integer, String> tcpServices = PortServices.getTcpServices();
        ObservableList<Entry<Integer, String>> tcpItems = synchronizedObservableList(observableArrayList(
                tcpServices.entrySet().stream().map(AbstractMap.SimpleEntry::new).collect(Collectors.toList())));

        servicesTable.setItems(CommonsFX.newFastFilter(filterField, tcpItems.filtered(e -> true)));
        address.setText(TracerouteScanner.IP_TO_SCAN);
        networkAddress.setText(TracerouteScanner.NETWORK_ADDRESS);
        Map<Integer, CheckBox> portChecks = new HashMap<>();
        portColumn.setCellFactory(newCellFactory((item, cell) -> {
            cell.setGraphic(EthicalHackApp.getCheckBox(portsSelected, portChecks, item));
            cell.setText(Objects.toString(item.getKey()));
        }));
    }
    public void onActionCurrentTasks() {
        items.clear();
        List<Map<String, String>> currentTasks = ProcessScan.scanCurrentTasks();
        items.addAll(currentTasks);
        Set<String> keySet =
                items.stream().flatMap(m -> m.keySet().stream()).collect(Collectors.toCollection(LinkedHashSet::new));
        EthicalHackApp.addColumns(commonTable, keySet);
    }

    public void onActionDNSLookup() {
        items.clear();
        Map<String, String> nsInformation = NameServerLookup.getNSInformation(dns.getText());
        items.add(nsInformation);
        Set<String> keySet = nsInformation.keySet();
        EthicalHackApp.addColumns(commonTable, keySet);
    }

    public void onActionIps(ActionEvent event) {
        StageHelper.fileAction("Select IP File", file -> networkAddress.setText(String.format("-iL \"%s\"", file)),
                "Any", "*.*").handle(event);
    }

    public void onActionNetstats() {
        items.clear();
        List<Map<String, String>> currentTasks = ProcessScan.scanNetstats();
        items.addAll(currentTasks);
        Set<String> keySet =
                items.stream().flatMap(m -> m.keySet().stream()).collect(Collectors.toCollection(LinkedHashSet::new));
        EthicalHackApp.addColumns(commonTable, keySet);
    }

    public void onActionNetworkInformation() {
        items.clear();
        List<Map<String, String>> nsInformation = NetworkInformationScanner.getIpConfigInformation();
        items.addAll(nsInformation);
        Set<String> keySet = nsInformation.stream().flatMap(m -> m.keySet().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        EthicalHackApp.addColumns(commonTable, keySet);
    }

    public void onActionPingTrace() {
        items.clear();
        Map<String, String> nsInformation = PingTraceRoute.getInformation(address.getText());
        items.add(nsInformation);
        Set<String> keySet = nsInformation.keySet();
        EthicalHackApp.addColumns(commonTable, keySet);
    }

    public void onActionPortScan() {
        items.clear();
        EthicalHackApp.addColumns(commonTable, Arrays.asList("Host", "Ports", "Route", "OS"));
        RunnableEx.runNewThread(() -> {
            progressIndicator.setVisible(true);
            ObservableMap<String, List<String>> scanNetworkOpenPorts =
                    PortScanner.scanNetworkOpenPorts(networkAddress.getText(), portsSelected);
            scanNetworkOpenPorts.addListener(EthicalHackApp.updateItemOnChange("Host", "Ports", count, items));
            ObservableMap<String, List<String>> oses = PortScanner.scanPossibleOSes(networkAddress.getText());
            oses.addListener(EthicalHackApp.updateItemOnChange("Host", "OS", count, items));
            ObservableMap<String, List<String>> networkRoutes =
                    TracerouteScanner.scanNetworkRoutes(networkAddress.getText());
            networkRoutes.addListener(EthicalHackApp.updateItemOnChange("Host", "Route", count, items));
            DoubleProperty defineProgress = ConsoleUtils.defineProgress(3);
            progressIndicator.progressProperty().unbind();
            progressIndicator.progressProperty().bind(defineProgress);
            ConsoleUtils.waitAllProcesses();
        });
    }

    public void onActionWebsiteScan() {
        items.clear();
        String text = dns.getText();
        if (!text.contains("http")) {
            text = "https://" + text;
        }
        new WebsiteScanner(100, 20).getLinkNetwork(text,
                ip -> items.add(ClassReflectionUtils.getDescriptionMap(new URL(ip), new HashMap<>())));
        List<String> fields = Arrays.asList("Protocol", "Host", "Path", "Query", "File");
        EthicalHackApp.addColumns(commonTable, fields);
    }

    public void onExportExcel() {
        
        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new LinkedHashMap<>();
        ObservableList<TableColumn<Map<String, String>, ?>> columns = commonTable.getColumns();
        for (TableColumn<Map<String, String>, ?> tableColumn : columns) {
            String text = tableColumn.getText();
            mapa.put(text, t -> t.getOrDefault(text, ""));
        }
        File outFile = ResourceFXUtils.getOutFile("hackResult.xlsx");
        ExcelService.getExcel(items, mapa, outFile);
        ImageFXUtils.openInDesktop(outFile);

    }

    @Override
    public void start(final Stage primaryStage) {
        CommonsFX.loadFXML("Ethical Hack App", "EthicalHackApp.fxml", this,primaryStage, WIDTH, WIDTH);
    }

    public static void main(final String[] args) {
        launch(args);
    }

}