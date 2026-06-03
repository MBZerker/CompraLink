package com.codex.compralink;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TextView;

final class CheckMercadoUi {
    static final int BG = Color.rgb(3, 8, 23);
    static final int CARD = Color.rgb(16, 26, 44);
    static final int CARD_2 = Color.rgb(24, 36, 58);
    static final int GREEN = Color.rgb(61, 220, 101);
    static final int BLUE = Color.rgb(38, 120, 255);
    static final int TEXT = Color.rgb(245, 247, 255);
    static final int MUTED = Color.rgb(169, 179, 199);
    static final int DANGER = Color.rgb(255, 56, 105);

    private CheckMercadoUi() {
    }

    static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static TextView label(Context context, String text, int sp, int color, int style) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setIncludeFontPadding(true);
        if (style != 0) view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }
}
