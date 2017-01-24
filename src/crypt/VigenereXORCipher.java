package crypt;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VigenereXORCipher {

	private int current = 0;
	private PrintStream out;
	private static final Map<Integer,Double> MAPA_FREQUENCIA = ImmutableMap.<Integer,Double>builder()
        .put('e' + 0, 12.702d)
        .put('t' + 0, 9.056d)
		.put('a'+0, 8.167d)
        .put('o' + 0, 7.507d)
        .put('i' + 0, 6.966d)
        .put('n' + 0, 6.749d)
        .put('s' + 0, 6.327d)
        .put('h' + 0, 6.094d)
		.put('r' + 0, 5.987d)
		.put('d' + 0, 4.253d)
		.put('l' + 0, 4.025d)
		.put('c'+0, 2.782d)
        .put('u' + 0, 2.758d)
        .put('m' + 0, 2.406d)
		.put('w' + 0, 2.361d)
		.put('f'+0, 2.228d)
		.put('g'+0, 2.015d)
        .put('y' + 0, 1.974d)
		.put('p'+0, 1.929d)
        .put('b' + 0, 1.492d)
		.put('v'+0, 0.978d)
        .put('k' + 0, 0.772d)
        .put('j' + 0, 0.153d)
		.put('x'+0, 0.150d)
        .put('q' + 0, 0.095d)
		.put('z'+0, 0.074d)
		.build();
	
	public VigenereXORCipher() {
		out = System.out;
		// out = new PrintStream(new File("log.txt"));
	}

	// 0, 0, 140, 181, 87, 0, 53
	private int[] keys = new int[] { 0, 0, 0, 0, 0, 0, 0 };
	@SuppressWarnings("unchecked")
	private List<Integer>[] keysList = Stream.generate(() -> new ArrayList<Integer>()).limit(7).toArray(List[]::new);

	// new List[] { new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new
	// ArrayList<>(), new ArrayList<>(),
	// new ArrayList<>(), new ArrayList<>() };

	public String encrypt(String k, List<Integer> s) {
		current = 0;
		int length = k.length();
		return s.stream().map(i -> {
			return i ^ k.charAt(current++ % length);
		}).map(i -> Character.valueOf((char) i.intValue())).map(c -> Character.toString(c)).collect(Collectors.joining());
	}

	public String encrypt(int[] k, List<Integer> s) {
		current = 0;
		int length = k.length;
		return s.stream().map(i -> (i ^ k[current++ % length])).map(i -> Character.valueOf((char) i.intValue()))
				.map(c -> Character.toString(c)).collect(Collectors.joining());
	}

	public String decrypt(String k, String s) {
		current = 0;
		int length = k.length();
		return s.chars().map(i -> (i ^ k.charAt(current++ % length)) % 256).mapToObj(i -> Character.valueOf((char) i)).map(Object::toString)
				.collect(Collectors.joining());
	}

	public static void main(String[] args) throws IOException {
		VigenereXORCipher vigenereCypher = new VigenereXORCipher();
		// long findKeySize = vigenereCypher.findKeySize();
		vigenereCypher.findKey(7L);
		// out.println(findKeySize);
	}
	public void findKey(long keySize) throws IOException {
		// keys = new char[Long.valueOf(keySize).intValue()];
		String line = Files.readAllLines(Paths.get("ctext.txt")).get(0);
		String[] split = line.split("(?<=\\G..)");
		List<Integer> collect = Stream.of(split).map(s -> Integer.valueOf(s, 16)).collect(Collectors.toList());
		for (int i = 0; i < keySize; i++) {
			current = 0;
			int j = i;
			List<Integer> stream = collect.stream().sequential().filter(bite -> current++ % keySize == j).collect(Collectors.toList());
			double maxSum = 0.0;
			for (int b = 0; b < 256; b++) {
				int B = b;
				List<Integer> map = stream.stream().map(l -> (char) (l ^ B) & 255).collect(Collectors.toList());
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
						out.println(i + "," + maxSum + "," + B);
						keys[i] = B;
						keysList[i].add(B);
					}

				}
			}
		}
		// for (int j = 0; j < 30; j++) {
		// for (int i = 0; i < keys.length; i++) {
		// keys[i] = keysList[i].get(0);
		// }
		// out.println(Arrays.toString(keys));
		// // current = 0;
		// // collect.forEach(a -> out.print(current++ % keySize));
		// // out.println();
		// out.println(encrypt(keys, collect));
		// }
		bruteForce(0, collect);

		out.println(Arrays.toString(keys));
		// current = 0;
		//
		// collect.forEach(a -> System.out.print(current++ % keySize));
		// out.println();
		out.println(encrypt(keys, collect));
		current = 0;
		out.println(Stream.of(keysList).map(l -> "" + current++ + l + "\n").collect(Collectors.toList()));

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
	public long findKeySize() throws IOException {
		String line = Files.readAllLines(Paths.get("ctext.txt")).get(0);
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

}
