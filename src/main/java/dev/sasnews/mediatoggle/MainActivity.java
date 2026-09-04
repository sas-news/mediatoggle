package dev.sasnews.mediatoggle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {
    // Theme.NoDisplay activities MUST finish() inside onCreate (before onResume),
    // so all delayed work runs on app-scoped static state only. The activity
    // instance is never referenced from the pending Runnable.
    private static Context sApp;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable pending;
    private static final ArrayList<Long> tapTimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (sApp == null) {
                sApp = getApplicationContext();
            }
            final SharedPreferences prefs =
                    sApp.getSharedPreferences(PrefsConfig.PREFS_NAME, MODE_PRIVATE);
            final int windowMs = PrefsConfig.getTapWindowMs(prefs);
            tapTimes.add(SystemClock.elapsedRealtime());
            // TODO-DEBUG: remove after device diagnosis
            Log.d("MediaToggle", "tap n=" + tapTimes.size());
            // Promotion: each in-window tap pushes the deadline out by windowMs.
            if (pending != null) { handler.removeCallbacks(pending); pending = null; }
            pending = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pending = null;
                            long[] times = new long[tapTimes.size()];
                            for (int i = 0; i < times.length; i++) {
                                times[i] = tapTimes.get(i);
                            }
                            int count = TapSequence.classifyTapCount(times, windowMs);
                            tapTimes.clear();

                            SharedPreferences firePrefs = sApp.getSharedPreferences(
                                    PrefsConfig.PREFS_NAME, MODE_PRIVATE);
                            PrefsConfig.Action action;
                            if (count >= 3) {
                                action = PrefsConfig.getTripleTap(firePrefs);
                            } else if (count == 2) {
                                action = PrefsConfig.getDoubleTap(firePrefs);
                            } else {
                                action = PrefsConfig.getSingleTap(firePrefs);
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

                            // TODO-DEBUG: remove after device diagnosis
                            Log.d("MediaToggle", "fire taps=" + times.length + " count=" + count + " action=" + action);
                            // TODO-DEBUG: remove after device diagnosis
                            try {
                                Toast.makeText(sApp, "MediaToggle: tap " + count + " -> " + action, Toast.LENGTH_SHORT).show();
                            } catch (Throwable ignored) {
                            }
                            AudioManager am = (AudioManager) sApp.getSystemService(Context.AUDIO_SERVICE);
                            if (am != null) {
                                am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                                am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                            }
                        } catch (Throwable t) {
                            // never crash: headless dispatch must stay silent
                        } finally {
                            pending = null;
                        }
                    }
            };
            handler.postDelayed(pending, windowMs);
        } catch (Throwable t) {
            // fall through to finish()
        } finally {
            // REQUIRED by Theme.NoDisplay: finish synchronously in onCreate,
            // before onResume(). Delayed dispatch continues on static state.
            try {
                finish();
            } catch (Throwable ignored) {
            }
        }
    }
}
