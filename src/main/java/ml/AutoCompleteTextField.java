package ml;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.deeplearning4j.models.word2vec.Word2Vec;

public class AutoCompleteTextField extends TextField {
    private final SortedSet<String> entries;
    private ObservableList<String> filteredEntries = FXCollections.observableArrayList();
    private ContextMenu entriesPopup;
    private boolean caseSensitive;
    private boolean popupHidden;
    private String textOccurenceStyle = "-fx-font-weight: bold; -fx-fill: red;";
    private int maxEntries = 10;

    public AutoCompleteTextField(SortedSet<String> entrySet, Word2Vec word2Vec) {
        super();
        entries = entrySet == null ? new TreeSet<>() : entrySet;
        filteredEntries.addAll(entries);
        entriesPopup = new ContextMenu();
        textProperty().addListener((obs, ds, sb2) -> onTextChange(word2Vec));
        focusedProperty().addListener((obs, a, a2) -> entriesPopup.hide());
    }

    public SortedSet<String> getEntries() {
        return entries;
    }

    public ObservableList<String> getFilteredEntries() {
        return filteredEntries;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public String getTextOccurenceStyle() {
        return textOccurenceStyle;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public boolean isPopupHidden() {
        return popupHidden;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public void setPopupHidden(boolean popupHidden) {
        this.popupHidden = popupHidden;
    }

    public void setTextOccurenceStyle(String textOccurenceStyle) {
        this.textOccurenceStyle = textOccurenceStyle;
    }

    private void addSearches(LinkedList<String> searchResult, String text) {
        Pattern pattern = getPattern(text);
        for (String entry : entries) {
            Matcher matcher = pattern.matcher(entry);
            if (matcher.matches()) {
                searchResult.add(entry);
            }
        }
    }

    private Pattern getPattern(String text) {
        Pattern pattern;
        if (isCaseSensitive()) {
            pattern = Pattern.compile(".*" + text + ".*");
        } else {
            pattern = Pattern.compile(".*" + text + ".*", Pattern.CASE_INSENSITIVE);
        }
        return pattern;
    }

    private void onTextChange(Word2Vec word2Vec) {
        if (getText().isEmpty()) {
            filteredEntries.clear();
            filteredEntries.addAll(entries);
            entriesPopup.hide();
            return;
        }
        LinkedList<String> searchResult = new LinkedList<>();
        //Check if the entered Text is part of some entry
        String text = getText();
        addSearches(searchResult, text);
        if (!searchResult.isEmpty()) {
            filteredEntries.clear();
            filteredEntries.addAll(searchResult);
            //Only show popup if not in filter mode
            if (!isPopupHidden()) {
                populatePopup(searchResult, text);
                if (!entriesPopup.isShowing()) {
                    entriesPopup.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
                }
            }
            return;
        }
        entriesPopup.hide();
        if (text.contains(" ")) {
            String[] split = text.split(" ");
            if (split.length > 0) {
                String s = split[split.length - 1];
                if (word2Vec.hasWord(s)) {
                    Collection<String> wordsNearestSum = word2Vec.wordsNearestSum(s, maxEntries + 1);
                    searchResult.addAll(wordsNearestSum.stream().filter(e -> !s.equals(e)).map(e -> text + " " + e)
                            .collect(Collectors.toList()));
                }
                filteredEntries.clear();
                filteredEntries.addAll(searchResult);
                if (!isPopupHidden()) {
                    populatePopup(searchResult, s);
                    if (!entriesPopup.isShowing()) {
                        entriesPopup.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
                    }
                }
            }
        }
    }

    private void populatePopup(List<String> searchResult, String text) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        int count = Math.min(searchResult.size(), getMaxEntries());
        for (int i = 0; i < count; i++) {
            final String result = searchResult.get(i);
            int occurence;
            if (isCaseSensitive()) {
                occurence = result.indexOf(text);
            } else {
                occurence = result.toLowerCase().indexOf(text.toLowerCase());
            }
            TextFlow entryFlow;
            if (occurence >= 0) {

                //Part before occurence (might be empty)
                Text pre = new Text(result.substring(0, occurence));
                //Part of (first) occurence
                Text in = new Text(result.substring(occurence, occurence + text.length()));
                in.setStyle(getTextOccurenceStyle());
                //Part after occurence
                Text post = new Text(result.substring(occurence + text.length(), result.length()));
                entryFlow = new TextFlow(pre, in, post);
            } else {
                entryFlow = new TextFlow(new Text(result));

            }

            CustomMenuItem item = new CustomMenuItem(entryFlow, true);
            item.setOnAction(actionEvent -> {
                setText(result);
                entriesPopup.hide();
            });
            menuItems.add(item);
        }
        entriesPopup.getItems().clear();
        entriesPopup.getItems().addAll(menuItems);
    }

}