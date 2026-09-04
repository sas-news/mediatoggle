package dev.sasnews.mediatoggle;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;

/**
 * Headless dispatcher for launcher shortcuts.
 *
 * <p>Reads the {@code action} string extra, maps it to a media keycode via
 * {@link PrefsConfig.Action}, fires ACTION_DOWN + ACTION_UP once through
 * {@link AudioManager#dispatchMediaKeyEvent}, then finishes. Never shows UI.</p>
 */
public class DispatchActivity extends Activity {

    /** Intent extra carrying PLAY_PAUSE | NEXT | PREVIOUS. */
    public static final String EXTRA_ACTION = "action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            String extra = getIntent() != null ? getIntent().getStringExtra(EXTRA_ACTION) : null;
            PrefsConfig.Action action =
                    PrefsConfig.Action.fromString(extra, PrefsConfig.Action.PLAY_PAUSE);
            int keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
            switch (action) {
                case NEXT:
                    keyCode = KeyEvent.KEYCODE_MEDIA_NEXT;
                    break;
                case PREVIOUS:
                    keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
                    break;
                case PLAY_PAUSE:
                default:
                    keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
                    break;
            }
            AudioManager audioManager =
                    (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                long now = android.os.SystemClock.uptimeMillis();
                audioManager.dispatchMediaKeyEvent(
                        new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0));
                audioManager.dispatchMediaKeyEvent(
                        new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0));
            }
        } catch (Exception ignored) {
            // Headless dispatch must never crash; fall through to finish().
        } finally {
            finish();
        }
    }
}
