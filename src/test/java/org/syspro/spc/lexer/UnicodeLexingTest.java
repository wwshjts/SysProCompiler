package org.syspro.spc.lexer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnicodeLexingTest {

    @Test
    @DisplayName("One-code point characters in UTF-16")
    public void oneCodePoint() {
        String test_input_eng = "Hello, world";
        String test_input_ru = "Счастье, для всех, даром и пусть никто не уйдет обиженным";
    }

    @Test
    @DisplayName("Non spacing mark")
    public void nonSpacingMark() {
        String test_input = "йоу";
    }

    @Test
    @DisplayName("Surrogate pair")
    public void surrogatePair() {
        String test_input = "Strength_is_not_physics_strength_is_\uD83C\uDFCB";

        System.out.println(test_input);
        System.out.println(test_input.length() + "|" + test_input.codePointCount(0, test_input.length()));
        System.out.println(test_input.charAt(test_input.length() - 1));
    }

}
