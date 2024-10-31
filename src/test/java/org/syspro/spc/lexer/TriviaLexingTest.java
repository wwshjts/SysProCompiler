package org.syspro.spc.lexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sypro.spc.lexer.SpcLexer;
import syspro.tm.lexer.Token;
import utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriviaLexingTest {

    @Test
    @DisplayName("Indentation and commentaries")
    public void indentationAndCommentaries() {
        String input = "\n  #*comment*";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();
        List<Logger.Log> expected = new ArrayList<>();
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Simple Indent")
    public void SimpleIndent() {
        String input = "\n  .";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        List<Logger.Log> expected = Arrays.asList(
                new Logger.Log(0, 0, 0, 0, "<INDENT>"),
                new Logger.Log(0, 3, 3, 0, ".")
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Simple Indent with CRLF")
    public void SimpleIndentCRLF() {
        String input = "\r\n  .";
        SpcLexer lex = new SpcLexer();
        List<String> result = (lex.lex(input)).stream().map(Token::toString).toList();

        List<String> expected = Arrays.asList(
                "<INDENT>", "."
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Simple Indent Dedent")
    public void SimpleIndentDedent() {
        String input = "\n  .\n";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        List<Logger.Log> expected = Arrays.asList(
                new Logger.Log(0, 0, 0, 0, "<INDENT>"),
                new Logger.Log(0, 4, 3, 1, "."),
                new Logger.Log(4, 4, 0, 0, "<DEDENT>")
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Simple Indent Dedent with CRLF")
    public void SimpleIndentDedentCRLF() {
        String input = "\r\n  .\r\n";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        List<Logger.Log> expected = Arrays.asList(
                new Logger.Log(1, 1, 0, 0, "<INDENT>"),
                new Logger.Log(0, input.codePointCount(0, input.length()) - 1, 4, 2, "."),
                new Logger.Log(input.length() - 1, input.length() - 1, 0, 0, "<DEDENT>")
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Tricky indentation and commentaries")
    public void trickyIndentation() {
        /*
        \n__# *comment*\n___
         */
        String input1 = "Pixies\n  \n   # Where is my mind\n  Song";
        String input2 = "Low\n   \n # Down";
    }
}
