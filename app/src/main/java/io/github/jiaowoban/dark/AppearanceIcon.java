package io.github.jiaowoban.dark;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/** Native-sized two-tone moon icon, with a real intrinsic size for ImageView. */
final class AppearanceIcon extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean dark;

    AppearanceIcon(boolean dark) { this.dark = dark; }

    @Override public void draw(Canvas canvas) {
        int save = canvas.save();
        canvas.translate(getBounds().left, getBounds().top);
        canvas.scale(getBounds().width() / 32f, getBounds().height() / 32f);
        Path crescent = new Path();
        crescent.moveTo(20, 5);
        crescent.cubicTo(6, 2, 0, 20, 13, 27);
        crescent.cubicTo(20, 31, 28, 26, 29, 20);
        crescent.cubicTo(16, 24, 12, 13, 20, 5);
        crescent.close();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.1f);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(dark ? 0xffb8b8b8 : 0xff303238);
        canvas.drawPath(crescent, paint);
        paint.setColor(0xff016aed);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(26, 4, 26, 12, paint);
        canvas.drawLine(22, 8, 30, 8, paint);
        canvas.restoreToCount(save);
    }

    @Override public int getIntrinsicWidth() { return 32; }
    @Override public int getIntrinsicHeight() { return 32; }
    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
