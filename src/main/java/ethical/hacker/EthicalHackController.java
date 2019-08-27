package ethical.hacker;

import static ethical.hacker.EthicalHackApp.addColumns;
import static ethical.hacker.EthicalHackApp.getCheckBox;
import static simplebuilder.SimpleTableViewBuilder.newCellFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import schema.sngpc.FXMLCreatorHelper;
import utils.ConsoleUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.StageHelper;

public class EthicalHackController {

    private static final Logger LOG = HasLogging.log();
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
    private ObservableList<Integer> portsSelected = FXCollections.observableArrayList();
    @FXML
    private TableColumn<Entry<Integer, String>, Object> portColumn;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Text ports;
    @FXML
    private TableView<Map<String, String>> commonTable;
    private ObservableList<Map<String, String>> items = FXCollections
        .synchronizedObservableList(FXCollections.observableArrayList());
    private ObservableMap<String, Set<String>> count = FXCollections.observableHashMap();

    public void initialize() {
        final int columnWidth = 120;
        HBox parent = (HBox) commonTable.getParent();
        commonTable.prefWidthProperty().bind(parent.widthProperty().add(-columnWidth));
        ports.textProperty()
            .bind(Bindings.createStringBinding(() -> String.format("Port Services %s", portsSelected), portsSelected));
        FilteredList<Map<String, String>> filt = items.filtered(e -> true);
        commonTable.setItems(filt);
        resultsFilter.textProperty().addListener((ob, old, value) -> filt
            .setPredicate(row -> StringUtils.isBlank(value) || StringUtils.containsIgnoreCase(row.toString(), value)));

        Map<Integer, String> tcpServices = PortServices.getTcpServices();
        ObservableList<Entry<Integer, String>> tcpItems = FXCollections
            .synchronizedObservableList(FXCollections.observableArrayList(
                tcpServices.entrySet().stream().map(AbstractMap.SimpleEntry::new).collect(Collectors.toSet())));

        FilteredList<Entry<Integer, String>> filtPorts = tcpItems.filtered(e -> true);
        filterField.textProperty().addListener((ob, old, value) -> filtPorts
            .setPredicate(row -> StringUtils.isBlank(value) || StringUtils.containsIgnoreCase(row.toString(), value)));
        servicesTable.setItems(filtPorts);

        Map<Integer, CheckBox> portChecks = new HashMap<>();
        portColumn.setCellFactory(newCellFactory((item, cell) -> {
            cell.setGraphic(getCheckBox(portsSelected, portChecks, item));
            cell.setText(Objects.toString(item.getKey()));
        }));
    }

    public void onActionDNSLookup() {
        items.clear();
        Map<String, String> nsInformation = NameServerLookup.getNSInformation(dns.getText());
        items.add(nsInformation);
        Set<String> keySet = nsInformation.keySet();
        addColumns(commonTable, keySet);
    }

    public void onActionIps() {
        StageHelper.fileAction("Select IP File", file -> networkAddress.setText(String.format("-iL \"%s\"", file)),
            "Any", "*.*");
    }

    public void onActionNetworkInformation() {
        items.clear();
        List<Map<String, String>> nsInformation = NetworkInformationScanner.getNetworkInformation();
        items.addAll(nsInformation);
        Set<String> keySet = nsInformation.stream().flatMap(m -> m.keySet().stream()).collect(Collectors.toSet());
        addColumns(commonTable, keySet);
    }

    public void onActionPingTrace() {
        items.clear();
        Map<String, String> nsInformation = PingTraceRoute.getInformation(address.getText());
        items.add(nsInformation);
        Set<String> keySet = nsInformation.keySet();
        addColumns(commonTable, keySet);
    }

    public void onActionPortScan() {
        items.clear();
        addColumns(commonTable, Arrays.asList("Host", "Ports", "Route", "OS"));
        new Thread(() -> {
            progressIndicator.setVisible(true);
            ObservableMap<String, List<String>> scanNetworkOpenPorts = PortScanner
                .scanNetworkOpenPorts(networkAddress.getText(), portsSelected);
            scanNetworkOpenPorts.addListener(updateItemOnChange("Host", "Ports"));
            ObservableMap<String, List<String>> oses = PortScanner.scanPossibleOSes(networkAddress.getText());
            oses.addListener(updateItemOnChange("Host", "OS"));
            ObservableMap<String, List<String>> networkRoutes = TracerouteScanner
                .scanNetworkRoutes(networkAddress.getText());
            networkRoutes.addListener(updateItemOnChange("Host", "Route"));
            DoubleProperty defineProgress = ConsoleUtils.defineProgress(3);
            progressIndicator.progressProperty().unbind();
            progressIndicator.progressProperty().bind(defineProgress);
            ConsoleUtils.waitAllProcesses();
        }).start();
    }

    public void onActionProcessScan() {
        items.clear();
        List<Map<String, String>> currentTasks = ProcessScan.scanCurrentTasks();
        items.addAll(currentTasks);
        Set<String> keySet = items.stream().flatMap(m -> m.keySet().stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        addColumns(commonTable, keySet);

    }

    private void updateItem(String primaryKey, String targetKey,
        Change<? extends String, ? extends List<String>> change) {
        synchronized (count) {
            if (!change.wasAdded()) {
                return;
            }
            HasLogging.log();
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
            LOG.trace("{} of {} = {}", targetKey, key, valueAdded);
        }
    }

    private MapChangeListener<String, List<String>> updateItemOnChange(String primaryKey, String targetKey) {
        count.clear();
        return change -> updateItem(primaryKey, targetKey, change);
    }

    public static void main(String[] args) {
        ResourceFXUtils.initializeFX();
        Platform.runLater(() -> FXMLCreatorHelper.duplicateStage(ResourceFXUtils.toFile("EthicalHackApp.fxml")));
    }

}