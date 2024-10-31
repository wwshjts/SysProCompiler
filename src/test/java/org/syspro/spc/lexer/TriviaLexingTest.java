package org.syspro.spc.lexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sypro.spc.lexer.SpcLexer;
import syspro.tm.lexer.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriviaLexingTest {

    @Test
    @DisplayName("Indentation and commentaries")
    public void indentationAndCommentaries() {
        String input = "\n  #*comment*";
        SpcLexer lex = new SpcLexer();
        List<Token> result = (lex.lex(input));
        List<Token> expected = new ArrayList<>();
        Assertions.assertEquals( expected, result);
    }

    @Test
    @DisplayName("Simple Indent")
    public void SimpleIndent() {
        String input = "\n  .";
        SpcLexer lex = new SpcLexer();
        List<String> result = (lex.lex(input)).stream().map(Token::toString).toList();

        List<String> expected = Arrays.asList(
                "<INDENT>", "."
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
        List<String> result = (lex.lex(input)).stream().map(Token::toString).toList();

        List<String> expected = Arrays.asList(
                "<INDENT>", ".", "<DEDENT>"
        );

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Simple Indent Dedent with CRLF")
    public void SimpleIndentDedentCRLF() {
        String input = "\r\n  .\r\n";
        SpcLexer lex = new SpcLexer();
        List<String> result = (lex.lex(input)).stream().map(Token::toString).toList();

        List<String> expected = Arrays.asList(
                "<INDENT>", ".", "<DEDENT>"
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
