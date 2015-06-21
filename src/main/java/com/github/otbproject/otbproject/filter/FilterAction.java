package com.github.otbproject.otbproject.filter;

public enum FilterAction implements Comparable<FilterAction> {
    BAN(10), TIMEOUT(6), STRIKE(4), PURGE(2), WARN(0);

    int severity;

    FilterAction(int severity) {
        this.severity = severity;
    }
}
