package dev.sasnews.mediatoggle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final Handler handler = new Handler();
    private static Runnable pending;
    private static final ArrayList<Long> tapTimes = new ArrayList<>();

    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences(PrefsConfig.PREFS_NAME, MODE_PRIVATE);
        final int windowMs = PrefsConfig.getTapWindowMs(prefs);
        final SharedPreferences finalPrefs = prefs;

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        registerTap(finalPrefs, windowMs);
    }

    private void registerTap(final SharedPreferences prefs, final int windowMs) {
        tapTimes.add(SystemClock.elapsedRealtime());
        if (pending == null) {
            pending = new Runnable() {
                @Override
                public void run() {
                    pending = null;
                    long[] times = new long[tapTimes.size()];
                    for (int i = 0; i < times.length; i++) {
                        times[i] = tapTimes.get(i);
                    }
                    int count = TapSequence.classifyTapCount(times, windowMs);
                    tapTimes.clear();

                    PrefsConfig.Action action;
                    if (count >= 3) {
                        action = PrefsConfig.getTripleTap(prefs);
                    } else if (count == 2) {
                        action = PrefsConfig.getDoubleTap(prefs);
                    } else {
                        action = PrefsConfig.getSingleTap(prefs);
                    }

                    int keyCode;
                    switch (action) {
                        case NEXT:
                            keyCode = KeyEvent.KEYCODE_MEDIA_NEXT;
                            break;
                        case PREVIOUS:
                            keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
                            break;
                        default:
                            keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
                            break;
                    }

                    if (audioManager != null) {
                        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                    }
                    finish();
                }
            };
            handler.postDelayed(pending, windowMs);
        }
    }
}