package com.habitrpg.android.habitica.ui.helpers;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class UsernameSpan extends MetricAffectingSpan implements ParcelableSpan {

    private final int color = Color.parseColor("#6133b4");

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    private void writeToParcelInternal(@NonNull Parcel dest, int flags) {
        dest.writeInt(color);
    }

    public void updateDrawState(@NonNull TextPaint textPaint) {
        textPaint.setColor(color);
        apply(textPaint);
    }

    @Override
    public int getSpanTypeId() {
        return 0;
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint textPaint) {
        apply(textPaint);
    }

    private static void apply(Paint paint) {
        int oldStyle;

        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

        int want = oldStyle | Typeface.BOLD;

        Typeface tf;
        if (old == null) {
            tf = Typeface.defaultFromStyle(want);
        } else {
            tf = Typeface.create(old, want);
        }

        int fake = want & ~tf.getStyle();

        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }
}
