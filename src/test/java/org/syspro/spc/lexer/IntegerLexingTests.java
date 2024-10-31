package org.syspro.spc.lexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sypro.spc.lexer.SpcLexer;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class IntegerLexingTests {
    @Test
    @DisplayName("Integer without suffix")
    public void integer() {
        String input = "42";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();
        List<Logger.Log> expected = List.of(
                new Logger.Log(0, 1, 0, 0, "42")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("32 bit integer literal")
    public void i32() {
        String input = "42i32";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();
        List<Logger.Log> expected = List.of(
                new Logger.Log(0, 4, 0, 0, "42i32")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Unsigned 32 bit integer literal")
    public void u32() {
        String input = "42u32";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();
        List<Logger.Log> expected = List.of(
                new Logger.Log(0, 4, 0, 0, "42u32")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("64 bit integer literal")
    public void i64() {
        String input = "42i64";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();
        List<Logger.Log> expected = List.of(
                new Logger.Log(0, 4, 0, 0, "42i64")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Unsigned 64 bit integer literal")
    public void u64() {
        String input = "42u64";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();
        List<Logger.Log> expected = List.of(
                new Logger.Log(0, 4, 0, 0, "42u64")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Integers and trivia")
    public void triviaTest() {
        String input = "#calc 42\n\n\n21u64    +   21i64\n";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();
        List<Logger.Log> expected = List.of(
                new Logger.Log(0, 15,11, 0, "21u64"),
                new Logger.Log(16, 20,4, 0, "+"),
                new Logger.Log(21, input.length() - 1, 3, 1, "21i64")
        );
        Assertions.assertEquals(expected, result);
    }

    // TODO: make
    @Test
    @DisplayName("Bad suffix in the end of file")
    public void badSuffixAtTheEOFTest() {
        String input = "42u6";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();
        List<Logger.Log> expected = List.of(
                new Logger.Log(0, 4, 0, 0, "42u64")
        );
    }




}
