package dev.sasnews.mediatoggle;

import android.content.SharedPreferences;

/**
 * Single source of truth for the app's preferences.
 *
 * <p>Centralizes every preference key and its default value in one place so
 * callers never hardcode defaults or string keys. Reads are done through the
 * static accessors below, which fall back to the shared defaults whenever a
 * preference is unset (or holds an unknown value).</p>
 */
public final class PrefsConfig {

    /** The package-scoped preference file name. */
    public static final String PREFS_NAME = "mediatoggle_prefs";

    /** Key for the single-tap action assignment. */
    public static final String KEY_SINGLE = "single_tap_action";

    /** Key for the double-tap action assignment. */
    public static final String KEY_DOUBLE = "double_tap_action";

    /** Key for the triple-tap action assignment. */
    public static final String KEY_TRIPLE = "triple_tap_action";

    /** Key for the tap-window duration in milliseconds. */
    public static final String KEY_TAP_WINDOW_MS = "tap_window_ms";

    /** Default action for a single tap. */
    public static final Action DEFAULT_SINGLE = Action.PLAY_PAUSE;

    /** Default action for a double tap. */
    public static final Action DEFAULT_DOUBLE = Action.NEXT;

    /** Default action for a triple tap. */
    public static final Action DEFAULT_TRIPLE = Action.PREVIOUS;

    /** Default tap-window duration in milliseconds. */
    public static final int DEFAULT_TAP_WINDOW_MS = 300;

    private PrefsConfig() {
        // Non-instantiable utility holder.
    }

    /**
     * The actions a tap sequence can be assigned to.
     */
    public enum Action {
        PLAY_PAUSE("PLAY_PAUSE"),
        NEXT("NEXT"),
        PREVIOUS("PREVIOUS");

        private final String serialized;

        Action(String serialized) {
            this.serialized = serialized;
        }

        /**
         * Parses a stored string into an {@link Action}, falling back to
         * {@code fallback} when the value is null or not a known action name.
         */
        public static Action fromString(String value, Action fallback) {
            for (Action action : values()) {
                if (action.serialized.equals(value)) {
                    return action;
                }
            }
            return fallback;
        }

        @Override
        public String toString() {
            return serialized;
        }
    }

    /**
     * Reads a tap action assignment from {@code prefs}, returning
     * {@code def} when the key is unset or holds an unknown value.
     */
    public static Action getAction(SharedPreferences prefs, String key, Action def) {
        if (prefs == null || !prefs.contains(key)) {
            return def;
        }
        return Action.fromString(prefs.getString(key, null), def);
    }

    /**
     * Reads the single-tap action, defaulting to {@link #DEFAULT_SINGLE}.
     */
    public static Action getSingleTap(SharedPreferences prefs) {
        return getAction(prefs, KEY_SINGLE, DEFAULT_SINGLE);
    }

    /**
     * Reads the double-tap action, defaulting to {@link #DEFAULT_DOUBLE}.
     */
    public static Action getDoubleTap(SharedPreferences prefs) {
        return getAction(prefs, KEY_DOUBLE, DEFAULT_DOUBLE);
    }

    /**
     * Reads the triple-tap action, defaulting to {@link #DEFAULT_TRIPLE}.
     */
    public static Action getTripleTap(SharedPreferences prefs) {
        return getAction(prefs, KEY_TRIPLE, DEFAULT_TRIPLE);
    }

    /**
     * Reads the tap-window duration, defaulting to
     * {@link #DEFAULT_TAP_WINDOW_MS}.
     */
    public static int getTapWindowMs(SharedPreferences prefs) {
        return getTapWindowMs(prefs, DEFAULT_TAP_WINDOW_MS);
    }

    /**
     * Reads the tap-window duration, falling back to {@code def} when the
     * preference is unset or holds an invalid value.
     */
    public static int getTapWindowMs(SharedPreferences prefs, int def) {
        if (prefs == null || !prefs.contains(KEY_TAP_WINDOW_MS)) {
            return def;
        }
        int value = prefs.getInt(KEY_TAP_WINDOW_MS, def);
        return value > 0 ? value : def;
    }

    /** Stores a tap action assignment for later {@code commit()} / {@code apply()}. */
    public static void saveAction(SharedPreferences.Editor editor, String key, Action action) {
        if (editor == null) {
            return;
        }
        editor.putString(key, action != null ? action.toString() : null);
    }

    /** Stores the tap-window duration for later {@code commit()} / {@code apply()}. */
    public static void saveTapWindowMs(SharedPreferences.Editor editor, int ms) {
        if (editor == null) {
            return;
        }
        editor.putInt(KEY_TAP_WINDOW_MS, ms);
    }
}