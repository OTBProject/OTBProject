package com.github.otbproject.otbproject.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;

public final class CustomCollectors {
    private CustomCollectors() {}

    public static <T> Collector<T, ?, ConcurrentHashMap.KeySetView<T, Boolean>> toConcurrentSet() {
        return Collector.of(ConcurrentHashMap::newKeySet, ConcurrentHashMap.KeySetView::add, (c1, c2) -> {
            c1.addAll(c2);
            return c1;
        });
    }
}
