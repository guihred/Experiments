package others;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class HuffmanTree {

    private HuffmanTree zerod;
    private HuffmanTree oned;

    private Entry<Integer, Long> entry;
    private String code;

    public HuffmanTree(Entry<Integer, Long> entry) {
        this.entry = entry;
    }

    public HuffmanTree(HuffmanTree zerod, HuffmanTree oned) {
        this.zerod = zerod;
        this.oned = oned;
    }

    public String decode(String text) {
        List<String> codesAsString =
                text.codePoints().mapToObj(e -> String.valueOf(Character.toChars(e))).collect(Collectors.toList());
        HuffmanTree cur = this;
        StringBuilder decoded = new StringBuilder();
        StringBuilder curString = new StringBuilder();
        for (int i = 0; i < codesAsString.size(); i++) {
            curString.append(codesAsString.get(i));
            String str = curString.toString();
            if (cur.entry == null) {
                cur = cur.oned.code.startsWith(str) ? cur.oned : cur.zerod;
            }
            if (cur.entry != null) {
                decoded.append(String.valueOf(Character.toChars(cur.entry.getKey())));
                curString.delete(0, curString.length());
                cur = this;
            }
        }
        return decoded.toString();
    }

    public String encode(String text) {
        return text.codePoints().boxed().map(this::get).map(e -> e.code).collect(Collectors.joining());
    }

    @Override
    public String toString() {
        if (entry != null) {
            return String.valueOf(Character.toChars(entry.getKey())) + "=" + code + "\n";
        }
        return zerod.toString() + oned.toString();
    }

    private HuffmanTree get(Integer codePoint) {
        if (entry != null && entry.getKey().equals(codePoint)) {
            return this;
        }
        if (oned.search(codePoint)) {
            return oned.get(codePoint);
        }
        return zerod.get(codePoint);
    }

    private Long getValue() {
        if (entry != null) {
            return entry.getValue();
        }
        return zerod.getValue() + oned.getValue();
    }

    private boolean search(Integer key) {
        if (entry != null) {
            return entry.getKey().equals(key);
        }
        return oned.search(key) || zerod.search(key);
    }

    private void setCode(String code) {
        this.code = code;
        if (entry == null) {
            zerod.setCode(code + "0");
            oned.setCode(code + "1");
        }
    }

    public static HuffmanTree buildTree(String text) {

        List<HuffmanTree> huffmanCollection = getHistogram(text).entrySet().stream().map(HuffmanTree::new)
                .sorted(Comparator.comparing(HuffmanTree::getValue)).collect(Collectors.toList());
        while (huffmanCollection.size() > 1) {
            HuffmanTree entry = huffmanCollection.remove(0);
            HuffmanTree entry2 = huffmanCollection.remove(0);
            HuffmanTree huffmanTree = new HuffmanTree(entry2, entry);
            huffmanCollection.add(huffmanTree);
            huffmanCollection.sort(Comparator.comparing(HuffmanTree::getValue));
        }
        HuffmanTree huffmanTree = huffmanCollection.get(0);
        huffmanTree.setCode("");
        return huffmanTree;
    }

    public static double entropy(String text) {
        return getHistogram(text).values().stream().mapToDouble(e -> e.doubleValue() / text.length())
                .map(e -> -e * Math.log10(e) / Math.log(2)).sum();
    }

    private static Map<Integer, Long> getHistogram(String text) {
        return text.codePoints().boxed().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
    }

}
