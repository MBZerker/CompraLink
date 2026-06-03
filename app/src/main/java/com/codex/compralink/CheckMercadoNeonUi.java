package com.codex.compralink;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

final class CheckMercadoNeonUi {
    static final int BG = Color.rgb(5, 8, 23);
    static final int CARD = Color.rgb(11, 18, 32);
    static final int CARD_2 = Color.rgb(19, 35, 58);
    static final int BLUE = Color.rgb(45, 140, 255);
    static final int GREEN = Color.rgb(57, 229, 108);
    static final int CYAN = Color.rgb(54, 224, 224);
    static final int DANGER = Color.rgb(255, 77, 109);
    static final int TEXT = Color.rgb(244, 248, 255);
    static final int MUTED = Color.rgb(152, 167, 195);

    private CheckMercadoNeonUi() {
    }

    static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static Drawable panel(Context context) {
        return neonBox(context, BLUE, 24, false, false);
    }

    static Drawable card(Context context, int color) {
        return neonBox(context, color, 18, false, false);
    }

    static Drawable input(Context context, boolean active) {
        return neonBox(context, active ? BLUE : GREEN, 18, active, false);
    }

    static Drawable chip(Context context, int color) {
        return neonBox(context, color, 14, true, false);
    }

    static Drawable button(Context context, int color) {
        return statefulBox(context, color, 18);
    }

    static Drawable dangerButton(Context context) {
        return statefulBox(context, DANGER, 18);
    }

    static Drawable iconButton(Context context, int color) {
        return statefulBox(context, color, 16);
    }

    private static StateListDrawable statefulBox(Context context, int color, int radiusDp) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{-android.R.attr.state_enabled}, neonBox(context, color, radiusDp, false, true));
        states.addState(new int[]{android.R.attr.state_pressed}, neonBox(context, CYAN, radiusDp, true, false));
        states.addState(new int[]{android.R.attr.state_focused}, neonBox(context, color, radiusDp, true, false));
        states.addState(new int[]{}, neonBox(context, color, radiusDp, false, false));
        return states;
    }

    private static Drawable neonBox(Context context, int color, int radiusDp, boolean active, boolean disabled) {
        return new NeonBoxDrawable(
                color,
                dp(context, radiusDp),
                Math.max(1f, dp(context, active ? 2 : 1)),
                dp(context, active ? 5 : 4),
                disabled
        );
    }

    private static int alpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int blend(int color, int base, float amount) {
        int r = Math.round(Color.red(color) * amount + Color.red(base) * (1f - amount));
        int g = Math.round(Color.green(color) * amount + Color.green(base) * (1f - amount));
        int b = Math.round(Color.blue(color) * amount + Color.blue(base) * (1f - amount));
        return Color.rgb(r, g, b);
    }

    private static final class NeonBoxDrawable extends Drawable {
        private final int color;
        private final float radius;
        private final float stroke;
        private final float glow;
        private final boolean disabled;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        NeonBoxDrawable(int color, float radius, float stroke, float glow, boolean disabled) {
            this.color = color;
            this.radius = radius;
            this.stroke = stroke;
            this.glow = glow;
            this.disabled = disabled;
        }

        @Override
        public void draw(Canvas canvas) {
            rect.set(getBounds());
            rect.inset(glow, glow);

            int top = disabled ? Color.rgb(9, 14, 26) : blend(color, CARD_2, 0.12f);
            int bottom = disabled ? Color.rgb(7, 10, 20) : CARD;
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(
                    rect.left, rect.top, rect.right, rect.bottom,
                    top, bottom, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, radius, radius, paint);
            paint.setShader(null);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setColor(alpha(color, disabled ? 72 : 150));
            canvas.drawRoundRect(rect, radius, radius, paint);

            drawTopLeftGlow(canvas, disabled ? alpha(color, 72) : color, disabled);
        }

        private void drawTopLeftGlow(Canvas canvas, int glowColor, boolean dimmed) {
            float strongStroke = stroke + glow * 0.42f;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(strongStroke);
            drawTopLeftSegments(canvas, glowColor, dimmed ? 36 : 54);

            paint.setStrokeWidth(Math.max(stroke + 1f, strongStroke * 0.52f));
            drawTopLeftSegments(canvas, glowColor, dimmed ? 82 : 210);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setShader(null);
        }

        private void drawTopLeftSegments(Canvas canvas, int glowColor, int startAlpha) {
            float left = rect.left;
            float top = rect.top;
            float right = rect.right;
            float bottom = rect.bottom;
            float topEnd = left + (right - left) * 0.46f;
            float leftEnd = top + (bottom - top) * 0.46f;
            float arcSize = radius * 2f;
            RectF arc = new RectF(left, top, left + arcSize, top + arcSize);
            paint.setShader(null);
            paint.setColor(alpha(glowColor, Math.max(28, startAlpha - 35)));
            canvas.drawArc(arc, 180f, 90f, false, paint);

            paint.setShader(new LinearGradient(
                    left + radius, top, topEnd, top,
                    alpha(glowColor, startAlpha), alpha(glowColor, 0), Shader.TileMode.CLAMP));
            canvas.drawLine(left + radius, top, topEnd, top, paint);

            paint.setShader(new LinearGradient(
                    left, top + radius, left, leftEnd,
                    alpha(glowColor, startAlpha), alpha(glowColor, 0), Shader.TileMode.CLAMP));
            canvas.drawLine(left, top + radius, left, leftEnd, paint);
            paint.setShader(null);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(android.graphics.ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return android.graphics.PixelFormat.TRANSLUCENT;
        }
    }
}
