package org.sypro.spc.exception;

// Inner unchecked spc exceptions
public class SpcCriticalExceptions extends RuntimeException {
    private final SpcComponent component;
    private final String detail;
    public SpcCriticalExceptions(SpcComponent component, String detail) {
        this.component = component;
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "SpcCriticalException: " + detail + "\n" + "From: " + component + "\n";
    }
}
