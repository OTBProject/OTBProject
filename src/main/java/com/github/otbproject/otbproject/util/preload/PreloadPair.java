package com.github.otbproject.otbproject.util.preload;

public class PreloadPair<T> {
    public final T tNew;
    public final T tOld;

    public PreloadPair(T tNew, T tOld) {
        this.tNew = tNew;
        this.tOld = tOld;
    }
}
