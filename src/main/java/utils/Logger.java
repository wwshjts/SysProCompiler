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

    public void logToken(int start, int end, int leadingTriviaLength, int trailingTriviaLength, String strRepresentation) {
        logs.add(new Log(start, end, leadingTriviaLength, trailingTriviaLength, strRepresentation));
    }

    public String toString() {
        return logs.stream().map(Log::toString).toList().toString();
    }

    public List<Log> toList() {
        return logs;
    }


    // represent lexer log
    public record Log(int start, int end, int leadingTriviaLength, int trailingTriviaLength, String strRepresentation) {

        @Override
        public String toString() {
            return "Token start: " + start + "\n" +
                    "Token end: " + end + "\n" +
                    "Leading Trivia: " + leadingTriviaLength + "\n" +
                    "Trailing Trivia: " + trailingTriviaLength + "\n" +
                    "String repr: " + strRepresentation +"\n";
        }

        public Log withEnd(int end) {
            return new Log(start, end, leadingTriviaLength, trailingTriviaLength, strRepresentation);
        }

        public Log withTrailingTriviaLength(int trailingTriviaLength) {
            return new Log(start, end, leadingTriviaLength, trailingTriviaLength, strRepresentation);
        }
    }
}
