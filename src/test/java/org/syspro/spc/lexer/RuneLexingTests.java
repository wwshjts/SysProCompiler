package org.syspro.spc.lexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sypro.spc.lexer.SpcLexer;
import syspro.tm.lexer.BadToken;
import syspro.tm.lexer.Token;
import utils.Logger;

import java.util.List;

public class RuneLexingTests {
    @Test
    @DisplayName("Short escape rune")
    public void shortEscape() {
        String input = "'\t'";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();
        List<Logger.Log> expected = List.of(
                new Logger.Log(0, 2, 0, 0, "'\\U+0009'")
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Unicode escape rune")
    public void unicodeEscape() {
        String input = "'\\U+1F916'";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        // TODO: report bug with string representation of rune token
        List<Logger.Log> expected = List.of(
                new Logger.Log(0, input.length() - 1, 0, 0, "'\\U+129302'")
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Unicode rune")
    public void unicodeRune() {
        String input = "'\uD83E\uDD16'";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        // TODO: report bug with string representation of rune token
        List<Logger.Log> expected = List.of(
                new Logger.Log(0, 2, 0, 0, "'\\U+129302'")
        );

        Assertions.assertEquals(expected, result);
    }
}
