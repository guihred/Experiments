package ethical.hacker;

import extract.ExcelService;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.StageHelper;
import utils.*;

public class EthicalHackController extends EthicalHackApp {

    public void copyContent(KeyEvent ev) {
        SimpleTableViewBuilder.copyContent(commonTable, ev);
    }

    public void initialize() {
        final int columnWidth = 120;
        commonTable.prefWidthProperty().bind(parent.widthProperty().add(-columnWidth));
        ports.textProperty().bind(
                Bindings.createStringBinding(() -> String.format("Port Services %s", portsSelected), portsSelected));
        commonTable.setItems(CommonsFX.newFastFilter(resultsFilter, items.filtered(e -> true)));
        commonTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        commonTable.setOnKeyReleased(this::copyContent);
        Map<Integer, String> tcpServices = PortServices.getTcpServices();
        ObservableList<Entry<Integer, String>> tcpItems =
                FXCollections.synchronizedObservableList(FXCollections.observableArrayList(tcpServices.entrySet()
                        .stream().map(AbstractMap.SimpleEntry::new).collect(Collectors.toList())));

        servicesTable.setItems(CommonsFX.newFastFilter(filterField, tcpItems.filtered(e -> true)));
        address.setText(TracerouteScanner.IP_TO_SCAN);
        networkAddress.setText(TracerouteScanner.NETWORK_ADDRESS);
        Map<Integer, CheckBox> portChecks = new HashMap<>();
        portColumn.setCellFactory(SimpleTableViewBuilder.newCellFactory((item, cell) -> {
            cell.setGraphic(getCheckBox(portsSelected, portChecks, item));
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

        RunnableEx.runNewThread(() -> {
            String text = address.getText();
            for (String ip : text.split("[\\s,;]+")) {
                Map<String, String> nsInformation = PingTraceRoute.getInformation(ip);
                RunnableEx.runInPlatform(() -> {
                    if (items.isEmpty()) {
                        Set<String> keySet = nsInformation.keySet();
                        EthicalHackApp.addColumns(commonTable, keySet);
                    }
                    items.add(nsInformation);
                });
            }

        });
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

    public void onActionWhoIs() {
        items.clear();
        ObservableList<Map<String, String>> scanIps = new WhoIsScanner().scanIps(address.getText());
        items.addAll(scanIps);
        List<String> asList =
                Arrays.asList("as", "assize", "ascountry", "asname", "asabusecontact", "network", "number");
        scanIps.addListener((Change<? extends Map<String, String>> m) -> {
            while (m.next()) {
                List<? extends Map<String, String>> addedSubList = m.getAddedSubList();
                List<String> keySet = addedSubList.stream().flatMap(e -> e.keySet().stream()).distinct()
                        .sorted(Comparator.comparing(e -> -asList.indexOf(e))).collect(Collectors.toList());
                if (items.isEmpty()) {
                    RunnableEx.runInPlatform(() -> EthicalHackApp.addColumns(commonTable, keySet));
                }
                items.addAll(addedSubList);
            }
        });
    }

    public void onExportExcel() {
        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new LinkedHashMap<>();
        ObservableList<TableColumn<Map<String, String>, ?>> columns = commonTable.getColumns();
        for (TableColumn<Map<String, String>, ?> tableColumn : columns) {
            String text = tableColumn.getText();
            mapa.put(text, t -> t.getOrDefault(text, ""));
        }
        File outFile = ResourceFXUtils.getOutFile("xlsx/hackResult.xlsx");
        ExcelService.getExcel(items, mapa, outFile);
        ImageFXUtils.openInDesktop(outFile);
    }

    @Override
    public void start(final Stage primaryStage) {
        CommonsFX.loadFXML("Ethical Hack App", "EthicalHackApp.fxml", this, primaryStage, WIDTH, WIDTH);
    }

    public static CheckBox getCheckBox(List<Integer> checkedPorts, Map<Integer, CheckBox> checkbox,
            Entry<Integer, String> e) {
        return checkbox.computeIfAbsent(e.getKey(), i -> EthicalHackController.newCheck(checkedPorts, e));
    }

    public static void main(final String[] args) {
        launch(args);
    }

    protected static CheckBox newCheck(List<Integer> arrayList, Entry<Integer, String> e) {
        CheckBox check = new CheckBox();
        check.selectedProperty().addListener((ob, o, val) -> EthicalHackApp.addIfChecked(arrayList, e, val));
        return check;
    }

}