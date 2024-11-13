package utils;

import java.util.ArrayList;
import java.util.List;

/*
* Class provides log's for the compiler
* need for testing and debugging,
* cause provided hierarchy encapsulates some very useful info
 */
public class Logger {
    private final List<Log> logs = new ArrayList<>();
    private final boolean loggingEnabled = true;


    public void logTokenLexStage(int start, int end, int leadingTriviaLength, int trailingTriviaLength, String strRepresentation) {
        logs.add(new Log(start, end, leadingTriviaLength, trailingTriviaLength, strRepresentation, SpcComponent.LEXER));
    }

    public String toString() {
        return logs.stream().map(Log::toString).toList().toString();
    }

    public List<Log> toList() {
        return logs;
    }

    public static Log lexLogOf(int start, int end, int leadingTriviaLength, int trailingTriviaLength, String strRepresentation) {
        return new Log(start, end, leadingTriviaLength,  trailingTriviaLength, strRepresentation, SpcComponent.LEXER);
    }


    // represent lexer log
    public record Log(int start, int end, int leadingTriviaLength, int trailingTriviaLength, String strRepresentation, SpcComponent component)  {

        @Override
        public String toString() {
            return "Component: " + component + "\n" +
                    "Token start: " + start + "\n" +
                    "Token end: " + end + "\n" +
                    "Leading Trivia: " + leadingTriviaLength + "\n" +
                    "Trailing Trivia: " + trailingTriviaLength + "\n" +
                    "String repr: " + strRepresentation +"\n";
        }

        public Log withEnd(int end) {
            return new Log(start, end, leadingTriviaLength, trailingTriviaLength, strRepresentation, component);
        }

        public Log withTrailingTriviaLength(int trailingTriviaLength) {
            return new Log(start, end, leadingTriviaLength, trailingTriviaLength, strRepresentation, component);
        }
    }
}
