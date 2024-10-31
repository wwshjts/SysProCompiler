package org.syspro.spc.lexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sypro.spc.lexer.SpcLexer;
import syspro.tm.lexer.BooleanLiteralToken;
import syspro.tm.lexer.IdentifierToken;
import syspro.tm.lexer.Keyword;
import syspro.tm.lexer.Token;
import utils.Logger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdentifierLexingTests {

    @Test
    @DisplayName("Boolean literals")
    public void booleanLiteral() {
        String input = "true false";
        SpcLexer lexer = new SpcLexer();
        SpcLexer.ResultOfLexing resultOfLexing = lexer.spcLex(input);
        List<Logger.Log> result = (resultOfLexing).logger.toList();
        List<Token> tokens = (resultOfLexing).lex_result;

        List<Logger.Log> expected = List.of(
                new Logger.Log(0, 3, 0, 0, "true"),
                new Logger.Log(4, input.length() - 1, 1, 0, "false")
        );

        Assertions.assertEquals(expected, result);

        for (Token tkn : tokens) {
            Assertions.assertInstanceOf(BooleanLiteralToken.class, tkn);
        }
    }

    @Test
    @DisplayName("Contextual keywords, that can be resolved in lexing stage")
    public void contextualKeyWord() {
        String input = "class\nobject\ninterface";
        SpcLexer lexer = new SpcLexer();
        SpcLexer.ResultOfLexing resultOfLexing = lexer.spcLex(input);
        List<Token> tokens = (resultOfLexing).lex_result;

        for (Token keyword : tokens) {
            Assertions.assertInstanceOf(IdentifierToken.class, keyword);
            IdentifierToken contextual_keyword = (IdentifierToken) keyword;
            Assertions.assertNotNull(contextual_keyword.contextualKeyword);
        }
    }

    @Test
    @DisplayName("Contextual identifiers")
    public void contextualIdentifier() {
        String input = "\n  class";
        SpcLexer lexer = new SpcLexer();
        SpcLexer.ResultOfLexing resultOfLexing = lexer.spcLex(input);
        List<Token> tokens = (resultOfLexing).lex_result;

        Token tkn = tokens.getLast();
        Assertions.assertInstanceOf(IdentifierToken.class, tkn);
        Keyword keyword = ((IdentifierToken) tkn).contextualKeyword;
        Assertions.assertNull(keyword);
    }

    @Test
    @DisplayName("Non spacing mark")
    public void nonSpacingMark() {
        String input = "йоу";

        SpcLexer lexer = new SpcLexer();
        SpcLexer.ResultOfLexing resultOfLexing = lexer.spcLex(input);
        List<Token> tokens = (resultOfLexing).lex_result;

        Token tkn = tokens.getLast();
        Assertions.assertEquals(input, tkn.toString());
    }

}
