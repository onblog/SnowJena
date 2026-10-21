package io.github.onblog.limiter;

import java.time.Duration;

/**
 * Argument validation shared by the limiter implementations.
 *
 * <p>Package-private on purpose: validation is an implementation detail, not public API.
 */
final class Checks {

    private Checks() {
    }

    static String name(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }
        return name.trim();
    }

    static int positive(int value, String what) {
        if (value <= 0) {
            throw new IllegalArgumentException(what + " must be > 0 but was " + value);
        }
        return value;
    }

    static long nanos(Duration window, String what) {
        if (window == null) {
            throw new IllegalArgumentException(what + " must not be null");
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException(what + " must be > 0 but was " + window);
        }
        try {
            return window.toNanos();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(what + " is too large: " + window, e);
        }
    }

    static long multiply(long a, long b, String what) {
        try {
            return Math.multiplyExact(a, b);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(what + " is too large", e);
        }
    }
}
