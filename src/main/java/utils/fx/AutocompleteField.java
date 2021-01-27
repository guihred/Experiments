package utils.fx;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.commons.lang3.StringUtils;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public class AutocompleteField extends TextField {
    protected final SortedSet<String> entries = new TreeSet<>();
    protected ObservableList<String> filteredEntries = FXCollections.observableArrayList();
    private ContextMenu entriesPopup = new ContextMenu();
    private boolean caseSensitive;
    private boolean popupHidden;
    private String textOccurenceStyle = "-fx-font-weight: bold; -fx-fill: red;";
    protected int maxEntries = 10;
    protected FunctionEx<String, String> textSelected = s -> s;

    private String wordSeparator = " ";

    public AutocompleteField() {
        filteredEntries.addAll(entries);
        textProperty().addListener((obs, ds, sb2) -> onTextChange());
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

    public String getWordSeparator() {
        return wordSeparator;
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

    public void setEntries(Collection<? extends String> c) {
        entries.clear();
        entries.addAll(c);
    }

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public void setOnTextSelected(FunctionEx<String, String> wordSeparator) {
        textSelected = wordSeparator;
    }

    public void setPopupHidden(boolean popupHidden) {
        this.popupHidden = popupHidden;
    }

    public void setTextOccurenceStyle(String textOccurenceStyle) {
        this.textOccurenceStyle = textOccurenceStyle;
    }
    public void setWordSeparator(String wordSeparator) {
        this.wordSeparator = wordSeparator;
    }

    protected void onTextSelected(String result) {
        setText(FunctionEx.apply(textSelected, result));
    }


    protected Collection<String> searchResult(String s) {
        return entries.stream().filter(m -> StringUtils.contains(m, s)).limit(maxEntries + 1L)
                .collect(Collectors.toList());
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

    private void addSearchResults(LinkedList<String> searchResult, String text) {
        filteredEntries.clear();
        filteredEntries.addAll(searchResult);
        // Only show popup if not in filter mode
        if (!isPopupHidden()) {
            populatePopup(searchResult, text);
            if (!entriesPopup.isShowing()) {
                RunnableEx.ignore(() -> entriesPopup.show(AutocompleteField.this, Side.BOTTOM, 0, 0));
            }
        }
    }

    private Pattern getPattern(String text) {
        if (isCaseSensitive()) {
            return Pattern.compile(".*" + text + ".*");
        }
        return Pattern.compile(".*" + text + ".*", Pattern.CASE_INSENSITIVE);
    }

    private void onTextChange() {
        if (getText().isEmpty()) {
            filteredEntries.clear();
            filteredEntries.addAll(entries);
            entriesPopup.hide();
            return;
        }
        LinkedList<String> searchResult = new LinkedList<>();
        // Check if the entered Text is part of some entry
        String text = getText();
        addSearches(searchResult, text);
        if (!searchResult.isEmpty()) {
            addSearchResults(searchResult, text);
            return;
        }
        entriesPopup.hide();
        if (text.contains(wordSeparator)) {
            String[] words = text.split(wordSeparator);
            if (words.length > 0) {
                String s = words[words.length - 1];
                String matches = Stream.of(words).limit(words.length - 1L).collect(Collectors.joining(wordSeparator));

                Collection<String> wordsNearestSum = searchResult(s);
                searchResult.addAll(wordsNearestSum.stream().filter(e -> !s.equals(e))
                        .map(e -> matches + wordSeparator + e).collect(Collectors.toList()));
                addSearchResults(searchResult, s);
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
                // Part before occurence (might be empty)
                Text pre = new Text(result.substring(0, occurence));
                // Part of (first) occurence
                Text in = new Text(result.substring(occurence, occurence + text.length()));
                in.setStyle(getTextOccurenceStyle());
                // Part after occurence
                Text post = new Text(result.substring(occurence + text.length(), result.length()));
                entryFlow = new TextFlow(pre, in, post);
            } else {
                entryFlow = new TextFlow(new Text(result));
            }

            CustomMenuItem item = new CustomMenuItem(entryFlow, true);
            item.setOnAction(actionEvent -> {
                onTextSelected(result);
                entriesPopup.hide();
            });
            menuItems.add(item);
        }
        entriesPopup.getItems().clear();
        entriesPopup.getItems().addAll(menuItems);
    }

}