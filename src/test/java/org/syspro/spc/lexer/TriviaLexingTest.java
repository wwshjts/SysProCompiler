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
        List<Logger.Log> expected = List.of(
                new Logger.Log(0, 0, 0,0, "<INDENT>"),
                new Logger.Log(input.length(), input.length(), 0,0, "<DEDENT>")

        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Simple Indent")
    public void simpleIndent() {
        String input = "\n  .";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        List<Logger.Log> expected = Arrays.asList(
                new Logger.Log(0, 0, 0, 0, "<INDENT>"),
                new Logger.Log(0, 3, 3, 0, "."),
                new Logger.Log(input.length(), input.length(), 0, 0, "<DEDENT>")
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Simple Indent with CRLF")
    public void simpleIndentCRLF() {
        String input = "\r\n  .";
        SpcLexer lex = new SpcLexer();
        List<String> result = (lex.lex(input)).stream().map(Token::toString).toList();

        List<String> expected = Arrays.asList(
                "<INDENT>", ".", "<DEDENT>"
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Simple Indent Dedent")
    public void simpleIndentDedent() {
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
    public void simpleIndentDedentCRLF() {
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
    @DisplayName("Trailing trivia")
    public void trailingTrivia() {
        String input = "\r\n  .\n    \r\n";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        List<Logger.Log> expected = Arrays.asList(
                new Logger.Log(1, 1, 0, 0, "<INDENT>"),
                new Logger.Log(0, input.codePointCount(0, input.length()) - 1, 4, 7, "."),
                new Logger.Log(input.length() - 1, input.length() - 1, 0, 0, "<DEDENT>")
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Simple trailing trivia")
    public void simpleTrailingTrivia() {
        String input = "\r\n  .    ";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        List<Logger.Log> expected = Arrays.asList(
                new Logger.Log(1, 1, 0, 0, "<INDENT>"),
                new Logger.Log(0, input.length() - 1, 4, 4, "."),
                new Logger.Log(input.length(), input.length(), 0, 0, "<DEDENT>")
        );

        Assertions.assertEquals(expected, result);
    }


    @Test
    @DisplayName("Tabs and indentation")
    public void tabsAndIndent() {
        String input = "<<\n  .\n\t\t.";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        List<Logger.Log> expected = Arrays.asList(
                new Logger.Log(0, 1, 0, 0, "<<"),
                new Logger.Log(2, 2, 0, 0, "<INDENT>"),
                new Logger.Log(2, 5, 3, 0, "."),
                new Logger.Log(6, 6, 0, 0, "<INDENT>"),
                new Logger.Log(6, input.codePointCount(0, input.length()) - 1, 3, 0, "."),
                new Logger.Log(input.length(), input.length(), 0, 0, "<DEDENT>"),
                new Logger.Log(input.length(), input.length(), 0, 0, "<DEDENT>")

        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Drop indentation")
    public void drop() {
        String input = "<<\n  .\n\t\t.\n";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        List<Logger.Log> expected = Arrays.asList(
                new Logger.Log(0, 1, 0, 0, "<<"),
                new Logger.Log(2, 2, 0, 0, "<INDENT>"),
                new Logger.Log(2, 5, 3, 0, "."),
                new Logger.Log(6, 6, 0, 0, "<INDENT>"),
                new Logger.Log(6, input.codePointCount(0, input.length()) - 1, 3, 1, "."),
                new Logger.Log(input.length() - 1, input.length() - 1, 0, 0, "<DEDENT>"),
                new Logger.Log(input.length() - 1, input.length() - 1, 0, 0, "<DEDENT>")
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Incorrect level of indentation")
    public void incorrectLevelOfIndent() {
        // the trivia length after dot % 2 = 1, so the token indent doesn't yields
        String input = "\r\n  .\n\t .";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        List<Logger.Log> expected = Arrays.asList(
                new Logger.Log(1, 1, 0, 0, "<INDENT>"),
                new Logger.Log(0, 4, 4, 0, "."),
                new Logger.Log(5, 8, 3, 0, "."),
                new Logger.Log(input.length(), input.length(), 0, 0, "<DEDENT>")
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Correct level of indentation")
    public void correctLevelOfIndent() {
        String input = "\r\n  .\n\t\t.";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        List<Logger.Log> expected = Arrays.asList(
                new Logger.Log(1, 1, 0, 0, "<INDENT>"),
                new Logger.Log(0, 4, 4, 0, "."),
                new Logger.Log(5, 5, 0, 0, "<INDENT>"),
                new Logger.Log(5, 8, 3, 0, "."),
                new Logger.Log(input.length(), input.length(), 0, 0, "<DEDENT>"),
                new Logger.Log(input.length(), input.length(), 0, 0, "<DEDENT>")
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Multiple levels of indentation")
    public void multipleIndent() {
        String input = "\r\n  .\n\t\t\t\t.\n";
        SpcLexer lex = new SpcLexer();
        List<Logger.Log> result = (lex.spcLex(input)).logger.toList();

        List<Logger.Log> expected = Arrays.asList(
                new Logger.Log(1, 1, 0, 0, "<INDENT>"),
                new Logger.Log(0, 4, 4, 0, "."),
                new Logger.Log(5, 5, 0, 0, "<INDENT>"),
                new Logger.Log(5, 5, 0, 0, "<INDENT>"),
                new Logger.Log(5, 5, 0, 0, "<INDENT>"),
                new Logger.Log(5, 11, 5, 1, "."),
                new Logger.Log(input.length() - 1, input.length() - 1, 0, 0, "<DEDENT>"),
                new Logger.Log(input.length() - 1, input.length() - 1, 0, 0, "<DEDENT>"),
                new Logger.Log(input.length() - 1, input.length() - 1, 0, 0, "<DEDENT>"),
                new Logger.Log(input.length() - 1, input.length() - 1, 0, 0, "<DEDENT>")
        );

        Assertions.assertEquals(expected, result);
    }
    @Test
    @DisplayName("Alone and suffering comment")
    public void singleCom() {
        String input = """
                  # why we still here 
                """;
        SpcLexer lex = new SpcLexer();
        SpcLexer.ResultOfLexing resultOfLexing = lex.spcLex(input);
        List<Logger.Log> result = (resultOfLexing).logger.toList();
        List<Token> tokens = (resultOfLexing).lex_result;

        Assertions.assertEquals(0, tokens.size());
    }


    @Test
    @DisplayName("Tricky indentation and commentaries")
    public void trickyIndentation() {
        String input = """
                .
                \t.
                \t\t.
                \t\t\t
                .""";
        SpcLexer lex = new SpcLexer();
        SpcLexer.ResultOfLexing resultOfLexing = lex.spcLex(input);
        List<Logger.Log> result = (resultOfLexing).logger.toList();
        List<Token> tokens = (resultOfLexing).lex_result;

        List<Logger.Log> expected = List.of(
                new Logger.Log(0,0,0, 0,"."),
                new Logger.Log(1,1, 0,0, "<INDENT>"),
                new Logger.Log(1,3,2, 0,"."),
                new Logger.Log(4,4, 0,0, "<INDENT>"),
                new Logger.Log(4,7,3, 0,"."),
                new Logger.Log(12,12, 0,0, "<DEDENT>"),
                new Logger.Log(12,12, 0,0, "<DEDENT>"),
                new Logger.Log(8,13,5, 0,".")

        );

        Assertions.assertEquals(expected, result);

    }
}
