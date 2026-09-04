package dev.sasnews.mediatoggle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

/**
 * Material settings screen for tap-action assignments and tap window.
 */
public class SettingsActivity extends Activity {

    private RadioGroup singleGroup;
    private RadioGroup doubleGroup;
    private RadioGroup tripleGroup;
    private Slider windowSlider;
    private TextView windowLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final float density = getResources().getDisplayMetrics().density;
        final int padding = (int) (16 * density + 0.5f);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padding, padding, padding, padding);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        singleGroup = addActionRow(root, "\u30b7\u30f3\u30b0\u30eb\u30bf\u30c3\u30d7");
        doubleGroup = addActionRow(root, "\u30c0\u30d6\u30eb\u30bf\u30c3\u30d7");
        tripleGroup = addActionRow(root, "\u30c8\u30ea\u30d7\u30eb\u30bf\u30c3\u30d7");

        TextView windowTitle = new TextView(this);
        windowTitle.setText("タップ間隔 (ms)");
        root.addView(windowTitle);

        windowLabel = new TextView(this);
        root.addView(windowLabel);

        windowSlider = new Slider(this);
        windowSlider.setValueFrom(200f);
        windowSlider.setValueTo(500f);
        windowSlider.setStepSize(10f);
        windowSlider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        windowSlider.addOnChangeListener((slider, value, fromUser) ->
                windowLabel.setText(Math.round(value) + " ms"));
        root.addView(windowSlider);

        Button save = new Button(this);
        save.setText("\u4fdd\u5b58");
        save.setOnClickListener(v -> {
            SharedPreferences prefs =
                    getSharedPreferences(PrefsConfig.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            PrefsConfig.saveAction(editor, PrefsConfig.KEY_SINGLE, selected(singleGroup));
            PrefsConfig.saveAction(editor, PrefsConfig.KEY_DOUBLE, selected(doubleGroup));
            PrefsConfig.saveAction(editor, PrefsConfig.KEY_TRIPLE, selected(tripleGroup));
            PrefsConfig.saveTapWindowMs(editor, Math.round(windowSlider.getValue()));
            editor.apply();
            finish();
        });
        root.addView(save);

        setContentView(root);

        SharedPreferences prefs =
                getSharedPreferences(PrefsConfig.PREFS_NAME, MODE_PRIVATE);
        load(prefs);
    }

    private RadioGroup addActionRow(LinearLayout root, String label) {
        TextView title = new TextView(this);
        title.setText(label);
        root.addView(title);

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        for (PrefsConfig.Action action : PrefsConfig.Action.values()) {
            RadioButton button = new RadioButton(this);
            button.setText(action.toString());
            button.setTag(action);
            group.addView(button);
        }
        root.addView(group);
        return group;
    }

    private void load(SharedPreferences prefs) {
        checkAction(singleGroup, PrefsConfig.getSingleTap(prefs));
        checkAction(doubleGroup, PrefsConfig.getDoubleTap(prefs));
        checkAction(tripleGroup, PrefsConfig.getTripleTap(prefs));
        int ms = Math.min(500, Math.max(200, PrefsConfig.getTapWindowMs(prefs)));
        windowSlider.setValue((float) ms);
        windowLabel.setText(ms + " ms");
    }

    private static void checkAction(RadioGroup group, PrefsConfig.Action action) {
        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton button = (RadioButton) group.getChildAt(i);
            if (button.getTag() == action) {
                button.setChecked(true);
                return;
            }
        }
        // Fallback: check the first button when nothing matched.
        if (group.getChildCount() > 0) {
            ((RadioButton) group.getChildAt(0)).setChecked(true);
        }
    }

    private static PrefsConfig.Action selected(RadioGroup group) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId != -1) {
            RadioButton button = group.findViewById(checkedId);
            if (button != null && button.getTag() instanceof PrefsConfig.Action) {
                return (PrefsConfig.Action) button.getTag();
            }
        }
        return PrefsConfig.Action.PLAY_PAUSE;
    }
}
