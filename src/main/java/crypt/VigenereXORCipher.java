
package crypt;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.exception.RuntimeIOException;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;

public class VigenereXORCipher {

    private static final Logger LOGGER = HasLogging.log();
    private static final Map<Integer,
            Double> MAPA_FREQUENCIA = ImmutableMap.<Integer, Double>builder().put('e' + 0, 12.702).put('t' + 0, 9.056)
                    .put('a' + 0, 8.167).put('o' + 0, 7.507).put('i' + 0, 6.966).put('n' + 0, 6.749).put('s' + 0, 6.327)
                    .put('h' + 0, 6.094).put('r' + 0, 5.987).put('d' + 0, 4.253).put('l' + 0, 4.025).put('c' + 0, 2.782)
                    .put('u' + 0, 2.758).put('m' + 0, 2.406).put('w' + 0, 2.361).put('f' + 0, 2.228).put('g' + 0, 2.015)
                    .put('y' + 0, 1.974).put('p' + 0, 1.929).put('b' + 0, 1.492).put('v' + 0, 0.978).put('k' + 0, 0.772)
                    .put('j' + 0, 0.153).put('x' + 0, 0.150).put('q' + 0, 0.095).put('z' + 0, 0.074).build();
    private static final int MAX_BYTE = 256;

    private int current;

    // 0, 0, 140, 181, 87, 0, 53
    private int[] keys = new int[] { 0, 0, 0, 0, 0, 0, 0 };

    private List<Integer>[] keysList = Stream.generate(ArrayList::new).limit(keys.length).toArray(List[]::new);

    public String decrypt(String k, String s) {
        current = 0;
        int length = k.length();
        return s.chars().map(i -> (i ^ k.charAt(current++ % length)) % MAX_BYTE)
                .mapToObj(i -> Character.valueOf((char) i)).map(Object::toString).collect(Collectors.joining());
    }

    public String encrypt(String k, String s) {
        return encrypt(k, s.chars().boxed().collect(Collectors.toList()));
    }

    public void findKey(long keySize) throws IOException {
        String line = Files.readAllLines(ResourceFXUtils.toPath("ctext.txt")).get(0);
        String[] split = line.split("(?<=\\G..)");
        final List<Integer> numbers = Stream.of(split).map(s -> Integer.valueOf(s, 16)).collect(Collectors.toList());
        for (int i = 0; i < keySize; i++) {
            current = 0;
            int j = i;
            List<Integer> stream =
                    numbers.stream().sequential().filter(bite -> current++ % keySize == j).collect(Collectors.toList());
            double maxSum = 0.0;
            for (int b = 0; b < MAX_BYTE; b++) {
                int n = b;
                List<Integer> map =
                        stream.stream().map(l -> (char) (l ^ n) & MAX_BYTE - 1).collect(Collectors.toList());
                if (map.parallelStream().allMatch(m -> within(m) && !Character.isDigit(m))) {
                    int size = map.size();
                    Map<Integer, Long> charHistogram =
                            map.parallelStream().map(c -> Character.valueOf((char) c.intValue())).map(c -> (int) c)
                                    .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
                    double sum = 0.0;
                    for (int k = 0; k < MAX_BYTE; k++) {
                        sum += MAPA_FREQUENCIA.getOrDefault(k, 1.0) * charHistogram.getOrDefault(k, 0L).doubleValue()
                                / Double.valueOf(size);
                    }
                    if (maxSum < sum) {
                        maxSum = sum;
                        LOGGER.trace("{},{},{}", i, maxSum, n);
                        keys[i] = n;
                        keysList[i].add(n);
                    }

                }
            }
        }
        bruteForce(0, numbers);

        String keysString = Arrays.toString(keys);
        LOGGER.info(keysString);
        String encrypt = encrypt(keys, numbers);
        LOGGER.info(encrypt);
        current = 0;
        LOGGER.info("{}", Stream.of(keysList).map(l -> "" + current++ + l + "\n").collect(Collectors.toList()));

    }

    public long findKeySize() {
        Path path = ResourceFXUtils.toPath("ctext.txt");
        try (Stream<String> lines = Files.lines(path)) {
            String line = lines.findFirst().orElse("");
            String[] split = line.split("(?<=\\G..)");
            final List<Integer> keySizeList =
                    Stream.of(split).map(s -> Integer.valueOf(s, 16)).collect(Collectors.toList());
            long max = 0;
            long bestKeySize = 0;
            final int maxKeySize = 14;
            for (int keySize = 2; keySize < maxKeySize; keySize++) {
                int i = keySize;
                current = 0;
                Map<Integer, Long> mapBigFreq = keySizeList.stream().filter(bite -> current++ % i == 0)
                        .collect(Collectors.groupingBy(b -> b, Collectors.counting()));
                List<Entry<Integer, Long>> sorted = mapBigFreq.entrySet().stream()
                        .sorted(Comparator.comparingLong(Entry<Integer, Long>::getValue).reversed())
                        .collect(Collectors.toList());
                if (max < sorted.get(0).getValue()) {
                    max = sorted.get(0).getValue();
                    bestKeySize = keySize;
                }

                LOGGER.trace("keysize={}{}", keySize, sorted);

            }

            LOGGER.info("{}", keySizeList);
            return bestKeySize;
        } catch (Exception e) {
            throw new RuntimeIOException("ERROR IN ACCESS FILE", e);
        }
    }

    private void bruteForce(int i, List<Integer> numbers) {
        List<Integer> d = keysList[i];
        for (int j = 0; j < d.size(); j++) {
            keys[i] = d.get(j);
            if (i < keys.length - 1) {
                bruteForce(i + 1, numbers);
                continue;
            }
            String keysString = Arrays.toString(keys);
            LOGGER.trace(keysString);
            String encrypt = encrypt(keys, numbers);
            LOGGER.trace(encrypt);
        }
    }

    private String encrypt(int[] k, List<Integer> s) {
        current = 0;
        int length = k.length;
        return s.stream().map(i -> i ^ k[current++ % length]).map(i -> Character.valueOf((char) i.intValue()))
                .map(Object::toString).collect(Collectors.joining());
    }

    private String encrypt(String k, List<Integer> s) {
        current = 0;
        int length = k.length();
        return s.stream().map(i -> i ^ k.charAt(current++ % length)).map(i -> Character.valueOf((char) i.intValue()))
                .map(Object::toString).collect(Collectors.joining());
    }

    public static void main(String[] args) {
        try {
            VigenereXORCipher vigenereCypher = new VigenereXORCipher();
            vigenereCypher.findKeySize();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private static boolean within(Integer m) {
        final int minWritableCharacter = 32;
        final int maxWritableCharacter = 128;
        return m > minWritableCharacter && m < maxWritableCharacter;
    }

}
