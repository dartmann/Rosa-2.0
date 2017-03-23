package de.davidartmann.android.rosa2.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

/**
 * Transformation class for the Picasso library, to round the imageview.
 * Created by david on 23.08.16.
 * -------------------------------
 * Credit:
 * https://www.dropbox.com/s/lp3d43hra3gbhul/RoundedTransformation.java
 *
 * enables hardware accelerated rounded corners
 * original idea here : http://www.curious-creature.org/2012/12/11/android-recipe-1-image-with-rounded-corners/
 */
public class RoundedTransformation implements Transformation {

    private static final String ROUNDED = "rounded";
    private int mMargin;
    private int mRadius;

    public RoundedTransformation(int margin, int radius) {
        mMargin = margin;
        mRadius = radius;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawRoundRect(new RectF(mMargin, mMargin, source.getWidth() - mMargin,
                source.getHeight() - mMargin), mRadius, mRadius, paint);
        if (source != output) {
            source.recycle();
        }
        return output;
    }

    @Override
    public String key() {
        return ROUNDED;
    }
}
