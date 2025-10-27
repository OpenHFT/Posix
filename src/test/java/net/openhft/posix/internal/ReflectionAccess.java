package net.openhft.posix.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflection helper that supports both Java 8 and post-Java 9 runtimes when
 * checking accessibility. Java 8 lacks {@code AccessibleObject.canAccess},
 * so we fall back to the legacy {@code isAccessible()} API.
 */
public final class ReflectionAccess {
    private static final Method CAN_ACCESS_METHOD = locateCanAccess();

    private ReflectionAccess() {
    }

    public static void ensureAccessible(Field field, Object target) {
        if (!canAccess(field, target)) {
            field.setAccessible(true);
        }
    }

    private static boolean canAccess(Field field, Object target) {
        Method method = CAN_ACCESS_METHOD;
        if (method != null) {
            try {
                return (Boolean) method.invoke(field, target);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to invoke AccessibleObject.canAccess", e);
            }
        }
        return legacyIsAccessible(field);
    }

    @SuppressWarnings("deprecation")
    private static boolean legacyIsAccessible(AccessibleObject object) {
        return object.isAccessible();
    }

    private static Method locateCanAccess() {
        try {
            return AccessibleObject.class.getMethod("canAccess", Object.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}

