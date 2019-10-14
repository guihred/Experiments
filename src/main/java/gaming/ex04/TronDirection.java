/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex04;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javafx.scene.input.KeyCode;

public enum TronDirection {

	UP(KeyCode.UP, KeyCode.W),
	LEFT(KeyCode.A, KeyCode.LEFT),
	DOWN(KeyCode.DOWN, KeyCode.S),
	RIGHT(KeyCode.RIGHT, KeyCode.D);
	private final List<KeyCode> codes;

	TronDirection(KeyCode... codes) {
		this.codes = Arrays.asList(codes);
	}

	public static TronDirection getByKeyCode(KeyCode code) {
		return Stream.of(values()).filter(e -> e.codes.contains(code)).findFirst().orElse(null);
	}
}
