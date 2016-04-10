package io.github.otbproject.otb.misc;

import scala.Function0;
import scala.Function1;
import scala.Function2;
import scala.Unit;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class SLambda {
    private SLambda() {}

    public static <T> UnaryOperator<T> toUnaryOperator(Function1<T, T> func) { return func::apply; }

    public static <T> Supplier<T> toSupplier(Function0<T> supplier) { return supplier::apply; }

    public static <T, R> Function<T, R> toFunction(Function1<T, R> func) { return func::apply; }

    public static Thread.UncaughtExceptionHandler toUncaughtExceptionHandler(Function2<Thread, Throwable, Unit> func) {
        return func::apply;
    }
}
