package org.sypro.spc.lexer;

import syspro.tm.lexer.*;
import utils.Logger;

import java.util.*;

/*
    some conventions, that code in SpcLexer uses:
    1) Every method, that begins with "scan" should modify Context in that way:
        Begin state: Context index points to first codePoint in lexeme
        End state: Context index points to the last codePoint in lexeme
 */

public class SpcLexer implements Lexer {

    public ResultOfLexing spcLex(String s) {
        LinkedList<Token> tokens = new LinkedList<>();
        Context ctx = new Context(s);

        while (ctx.has()) {
            tokens.addAll(scanToken(ctx));
            ctx.next();
        }

        List<Logger.Log> logs = ctx.logger.toList();

        assert logs.size() == tokens.size();

        // if there is trailing trivia on the last token
        if (!logs.isEmpty() && (logs.getLast().strRepresentation().equals("<DEDENT>") ||
                (logs.getLast()).end() < ctx.length)) {
            int last_tkn_index = logs.getLast().strRepresentation().equals("<DEDENT>") ? logs.size() - 2 : logs.size() - 1;

            Logger.Log  last_log = logs.get(last_tkn_index);
            Token last_tkn = tokens.get(last_tkn_index);

            tokens.set(last_tkn_index, last_tkn.withEnd(ctx.length - 1 ).withTrailingTriviaLength(
                    ctx.length - last_log.end() - 1));

            logs.set(last_tkn_index, last_log.withEnd(ctx.length - 1).withTrailingTriviaLength(
                    ctx.length - last_log.end() - 1));

        }

        return new ResultOfLexing(tokens, ctx.logger);
    }

    @Override
    public List<Token> lex(String s) {
        return spcLex(s).lex_result;
    }


    // return's list because there is inputs, that not produce tokens, or produce two tokens at once
    private static LinkedList<Token> scanToken(Context ctx) {
        String ch = ctx.get();
        int codePoint = ctx.getCodePoint();
        //GeneralCategory category = GeneralCategory.getCategory(codePoint);

        LinkedList<Token> res = new LinkedList<>();

        int start_pos = ctx.getIndex();
        int end_of_trivia_pos = ctx.getIndex();

        // Scanning trivia produces IndentToken or don't produce any token
        if (UnicodeUtils.isNewLine(ch) || UnicodeUtils.isSpace(ch) || ch.equals("#")) {
            scanTrivia(ctx).ifPresent(res::addLast);
            ctx.next();
            end_of_trivia_pos = ctx.getIndex();
        }

        if (!ctx.has()) {
            return res;
        }

        // scan token after leading trivia
        ch = ctx.get();
        int leading_trivia_len = end_of_trivia_pos - start_pos;

        Token tkn = switch (ch) {
            case "." -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.DOT);
            case ":" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.COLON);
            case "," -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.COMMA);
            case "+" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.PLUS);
            case "-" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.MINUS);
            case "*" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.ASTERISK);
            case "/" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.SLASH);
            case "%" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.PERCENT);
            case "!" -> {
                Symbol s = scanNextSymbol(ctx, "=") ? Symbol.EXCLAMATION_EQUALS : Symbol.EXCLAMATION;
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case "~" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.TILDE);
            case "&" -> {
                Symbol s = scanNextSymbol(ctx, "&") ? Symbol.AMPERSAND_AMPERSAND : Symbol.AMPERSAND;
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case "|" -> {
                Symbol s = scanNextSymbol(ctx, "|") ? Symbol.BAR_BAR : Symbol.BAR;
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case "^" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.CARET);
            case "<" -> {
                Symbol s;
                if (scanNextSymbol(ctx, "=")) {
                    s = Symbol.LESS_THAN_EQUALS;
                } else if (scanNextSymbol(ctx, "<")) {
                   s = Symbol.LESS_THAN_LESS_THAN;
                }  else if (scanNextSymbol(ctx, ":")) {
                   s = Symbol.BOUND;
                } else {
                   s = Symbol.LESS_THAN;
                }
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case ">" -> {
                Symbol s;
                if (scanNextSymbol(ctx, "=")) {
                    s = Symbol.GREATER_THAN_EQUALS;
                } else if (scanNextSymbol(ctx, ">")) {
                    s = Symbol.GREATER_THAN_GREATER_THAN;
                } else {
                    s = Symbol.GREATER_THAN;
                }
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case "[" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.OPEN_BRACKET);
            case "]" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.CLOSE_BRACKET);
            case "(" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.OPEN_PAREN);
            case ")" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.CLOSE_PAREN);
            case "=" -> {
                Symbol s = scanNextSymbol(ctx, "&") ? Symbol.EQUALS_EQUALS : Symbol.EQUALS;
                yield new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, s);
            }
            case "?" -> new SymbolToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, Symbol.QUESTION);

            // scan integer
            case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> scanInteger(ctx);

            default -> new BadToken(0,0,0,0);
        };

        if (!(tkn instanceof BadToken)) {
            res.add(tkn);
            ctx.logger.logToken(start_pos, ctx.getIndex(), leading_trivia_len, 0, tkn.toString());
        }

        return res;
    }

    private static Optional<Token> scanTrivia(Context ctx) {
        String curr = ctx.get();                    // it is guaranteed that curr exists

        while (UnicodeUtils.isNewLine(curr) || UnicodeUtils.isSpace(curr) || curr.equals("#")) {

            if (curr.equals("#")) {
                scanComment(ctx);
            } else if (UnicodeUtils.isNewLine(curr)) {
                return scanIndent(ctx);
            }

            // TODO: ugly
            Optional<String> next = ctx.seek();
            if (next.isPresent() && (UnicodeUtils.isSpace(next.get()) || UnicodeUtils.isNewLine(next.get()))) {
                ctx.next();
                curr = ctx.get();
            } else {
                break;
            }

        }

        return Optional.empty();
    }

    private static Optional<Token> scanIndent(Context ctx) {
        String curr = ctx.get();
        Optional<String> next = ctx.seek();

        assert UnicodeUtils.isNewLine(curr);

        int last_new_line = ctx.index;
        int indentation_length = 0;

        while (next.isPresent() && (UnicodeUtils.isSpace(next.get()) || (UnicodeUtils.isNewLine(next.get())) || (next.get()).equals("#"))) {
            ctx.next();
            if (UnicodeUtils.isNewLine(ctx.get())) {
                last_new_line = ctx.index;
            } else if ((ctx.get()).equals("#")) {

                // handle this situation
                // \n____\n______\n\n\n\n\n____# comment\n____someCode

                scanComment(ctx);
            } else {
                // TODO: think about correctness
                indentation_length += UnicodeUtils.getNumberOfSpaces(ctx.get());
            }
            next = ctx.seek();
        }

        // situation like that \n____\n______\n\n\n\n\n
        if (next.isEmpty()) {
            // if there was indentation level 0, we shouldn't produce [<DEDENT>]
            if (ctx.getIndentationLevel() == 0) {
               return Optional.empty();
            }

            ctx.dropIndent();
            ctx.logger.logToken(ctx.index, ctx.index, 0, 0, "<DEDENT>");
            return Optional.of(new IndentationToken(ctx.index, ctx.index, 0, 0, -1));
        }

        // situation like that \n____\n______\n\n\n\n\n____some code


        // \n____\n______\n\n\n\n\n___some code
        // in other words there is incorrect level of indentation
        if (indentation_length % 2 != 0) {
            return Optional.empty();
        }

        /* situation like that
        * class Foo:
        * __field
         */
        if (ctx.getIndentationLevel() == 0) {
            ctx.increaseIndentationLevel();
            ctx.setIndentationLength(indentation_length);
            ctx.logger.logToken(last_new_line, last_new_line, 0, 0, "<INDENT>");
            return Optional.of(new IndentationToken(last_new_line, last_new_line, 0, 0, 1));
        }

        /* incorrect change if indentation
        class Foo:
        ____def foo():
        __smth_wrong
         */
        if (indentation_length % ctx.getIndentationLevel() != 0) {
            return Optional.empty();
        }

        if (indentation_length == 0) {
            ctx.dropIndent();
            if (ctx.getIndentationLevel() > 0) {
                ctx.logger.logToken(last_new_line, last_new_line, 0, 0, "<DEDENT>");
                return Optional.of(new IndentationToken(last_new_line, last_new_line, 0, 0, -1));
            } else {
                return Optional.empty();
            }
        }

        ctx.setIndentationLevel(ctx.getIndentationLength());
        ctx.setIndentationLength(indentation_length);
        ctx.logger.logToken(last_new_line, last_new_line, 0, 0, "<INDENT>");
        return Optional.of(new IndentationToken(last_new_line, last_new_line, 0, 0, 1));
    }

    // function that scan comment doesn't return token. It only modifies context.
    // it stops scanning when meets '\n' or '\r'
    private static void scanComment(Context ctx) {
        Optional<String> ch_opt = ctx.seek();

        // read all symbols in the comment string
        while (ch_opt.isPresent() && (!(ch_opt.get()).equals("\n")) && (!(ch_opt.get()).equals("\r"))) {
            ctx.next();
            ch_opt = ctx.seek();
        }
    }

    private static boolean scanNextSymbol(Context ctx, String symbol) {
        Optional<String> next = ctx.seek();
        if (next.isPresent() && next.get().equals(symbol)) {
            ctx.next();
            return true;
        }
        return false;
    }

    private static Token scanInteger(Context ctx) {
        return new BadToken(1,1,1,1);
    }


    /*
     *
     * Lexer should be thread-safe
     * so Context encapsulates state of SpcLexer
     *
     */
    private static class Context {
        // the string of code points of input
        private final String input;

        // number of read codePoints
        private int index = 0;

        // length of input string in code points
        private final int length;
        private final ArrayList<Token> tokens = new ArrayList<>();
        private int line = 1; // TODO: add line counting
        private int indentation_level = 0;
        private int indentation_length = -1;
        public final Logger logger = new Logger();

        Context (String input) {
            this.input = input;
            this.length = input.codePointCount(0, input.length());
        }

        String get() {
            assert has();
            int i = input.offsetByCodePoints(0,  index);
            return Character.toString(input.codePointAt(i));
        }

        int getCodePoint() {
            int i = input.offsetByCodePoints(0,  index);
            return input.codePointAt(i);
        }

        int seekCodePoint() {
            int i = input.offsetByCodePoints(0,  index + 1);
            return input.codePointAt(i);
        }

        // modifies scanning position
        void next() {
            index++;
        }

        Optional<String> seek() {
            if (hasNext()) {
                int i = input.offsetByCodePoints(0,  index + 1);
                return Optional.of(Character.toString(input.codePointAt(i)));
            } else {
                return Optional.empty();
            }
        }

        void dropIndent() {
            indentation_level = 0;
        }

        public boolean hasNext() {
            return index + 1 < length;
        }

        public boolean has() {
            return index < length;
        }

        public int getIndentationLevel() {
            return indentation_level;
        }

        public void increaseIndentationLevel() {
            this.indentation_level++;
        }

        public void setIndentationLevel(int n) {
            assert n > 0;
            indentation_level = n;
        }

        public int getIndentationLength() {
            return indentation_length;
        }

        public void setIndentationLength(int indentation_length) {
            assert indentation_length >= 0;
            this.indentation_length = indentation_length;
        }

        public int getIndex() {
            return index;
        }

    }

    public static class ResultOfLexing {
        public List<Token> lex_result;
        public Logger logger;

        ResultOfLexing(List<Token> lex_result, Logger logger) {
            this.lex_result = lex_result;
            this.logger = logger;
        }
    }
}
