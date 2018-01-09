
package crypt;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import simplebuilder.ResourceFXUtils;

public class VigenereXORCipher {

	private static final Logger LOGGER = LoggerFactory.getLogger(VigenereXORCipher.class);
	private static final Map<Integer,Double> MAPA_FREQUENCIA = ImmutableMap.<Integer,Double>builder()
			.put('e' + 0, 12.702D).put('t' + 0, 9.056D).put('a' + 0, 8.167D).put('o' + 0, 7.507D).put('i' + 0, 6.966D)
			.put('n' + 0, 6.749D).put('s' + 0, 6.327D).put('h' + 0, 6.094D).put('r' + 0, 5.987D).put('d' + 0, 4.253D)
			.put('l' + 0, 4.025D).put('c' + 0, 2.782D).put('u' + 0, 2.758D).put('m' + 0, 2.406D).put('w' + 0, 2.361D)
			.put('f' + 0, 2.228D).put('g' + 0, 2.015D).put('y' + 0, 1.974D).put('p' + 0, 1.929D).put('b' + 0, 1.492D)
			.put('v' + 0, 0.978D).put('k' + 0, 0.772D).put('j' + 0, 0.153D).put('x' + 0, 0.150D).put('q' + 0, 0.095D)
			.put('z' + 0, 0.074D)
		.build();
	private int current;
	
	// 0, 0, 140, 181, 87, 0, 53
	private int[] keys = new int[] { 0, 0, 0, 0, 0, 0, 0 };

    @SuppressWarnings({ "unchecked" })
    private List<Integer>[] keysList = Stream.generate(() -> new ArrayList<>()).limit(keys.length).toArray(List[]::new);
	private PrintStream out;

	public VigenereXORCipher() {
		out = System.out;
	}

	public void bruteForce(int i, List<Integer> collect) {
		List<Integer> d = keysList[i];
		for (int j = 0; j < d.size(); j++) {
			keys[i] = d.get(j);
			if (i < keys.length - 1) {
				bruteForce(i + 1, collect);
				continue;
			}
			out.println(Arrays.toString(keys));
			out.println(encrypt(keys, collect));
		}
	}

	public String decrypt(String k, String s) {
		current = 0;
		int length = k.length();
		return s.chars().map(i -> (i ^ k.charAt(current++ % length)) % 256).mapToObj(i -> Character.valueOf((char) i)).map(Object::toString)
				.collect(Collectors.joining());
	}

	public String encrypt(int[] k, List<Integer> s) {
		current = 0;
		int length = k.length;
		return s.stream().map(i -> i ^ k[current++ % length]).map(i -> Character.valueOf((char) i.intValue()))
				.map(Object::toString).collect(Collectors.joining());
	}
	public String encrypt(String k, List<Integer> s) {
		current = 0;
		int length = k.length();
		return s.stream().map(i -> i ^ k.charAt(current++ % length)).map(i -> Character.valueOf((char) i.intValue()))
				.map(Object::toString).collect(Collectors.joining());
	}
	public void findKey(long keySize) throws IOException {
		String line = Files.readAllLines(ResourceFXUtils.toPath("ctext.txt")).get(0);
		String[] split = line.split("(?<=\\G..)");
		List<Integer> collect = Stream.of(split).map(s -> Integer.valueOf(s, 16)).collect(Collectors.toList());
		for (int i = 0; i < keySize; i++) {
			current = 0;
			int j = i;
			List<Integer> stream = collect.stream().sequential().filter(bite -> current++ % keySize == j).collect(Collectors.toList());
			double maxSum = 0.0;
			for (int b = 0; b < 256; b++) {
				int n = b;
				List<Integer> map = stream.stream().map(l -> (char) (l ^ n) & 255).collect(Collectors.toList());
				if (map.parallelStream().allMatch(m -> m > 32 && m < 128 && !Character.isDigit(m))) {
					int size = map.size();
					Map<Integer, Long> collect2 = map.parallelStream().map(c -> Character.valueOf((char) c.intValue()))
							.map(c -> Integer.valueOf(c.charValue()))
							.collect(Collectors.groupingBy(c -> c, Collectors.counting()));
					double sum = 0.0;
					for (int k = 0; k < 256; k++) {
						sum += MAPA_FREQUENCIA.getOrDefault(k, 1.0) * collect2.getOrDefault(k, 0L).doubleValue() / Double.valueOf(size);

					}
					if (maxSum < sum) {
						maxSum = sum;
						out.println(i + "," + maxSum + "," + n);
						keys[i] = n;
						keysList[i].add(n);
					}

				}
			}
		}
		bruteForce(0, collect);

		out.println(Arrays.toString(keys));
		out.println(encrypt(keys, collect));
		current = 0;
		out.println(Stream.of(keysList).map(l -> "" + current++ + l + "\n").collect(Collectors.toList()));

	}

	public long findKeySize() throws IOException {
		String line = Files.readAllLines(ResourceFXUtils.toPath("ctext.txt")).get(0);
		String[] split = line.split("(?<=\\G..)");
		List<Integer> collect = Stream.of(split).map(s -> Integer.valueOf(s, 16)).collect(Collectors.toList());
		long max = 0;
		long bestKeySize = 0;
		for (int keySize = 2; keySize < 14; keySize++) {
			int i = keySize;
			current = 0;
			Map<Integer, Long> mapBigFreq = collect.stream().filter(bite -> current++ % i == 0)
					.collect(Collectors.groupingBy(b -> b, Collectors.counting()));
			List<Entry<Integer, Long>> sorted = mapBigFreq.entrySet().stream()
					.sorted(Comparator.comparing(Entry<Integer, Long>::getValue).reversed())
					.collect(Collectors.toList());
			if (max < sorted.get(0).getValue()) {
				max = sorted.get(0).getValue();
				bestKeySize = keySize;
			}

			out.println("keysize=" + keySize + sorted);

		}

		out.println(collect);
		return bestKeySize;
	}
	public static void main(String[] args) {
		try {
		VigenereXORCipher vigenereCypher = new VigenereXORCipher();
			vigenereCypher.findKey(7L);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

}
