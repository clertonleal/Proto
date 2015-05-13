package com.clertonleal.proto;

public class Configuration {

    private boolean closeCursor;

    public boolean isClosingCursor() {
        return closeCursor;
    }

    public void closeCursor(boolean closeCursor) {
        this.closeCursor = closeCursor;
    }
}
