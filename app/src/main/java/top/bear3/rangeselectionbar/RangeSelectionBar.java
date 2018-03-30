package top.bear3.rangeselectionbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * author : TT
 * e-mail : tianruofengxing@163.com
 * time   : 2018/03/26
 * desc   :
 * version: 1.0
 */

public class RangeSelectionBar extends View {
    private static final int COLOR_THUMB = 0xFF00A3DA;

    private int max, low, high;         // 最大值，低游标和高游标
    private int width, height;          // 控件高和宽
    private int barWidth;               // 进度条宽

    private int lowAreaColor;
    private int middleAreaColor;
    private int highAreaColor;

    private int barHeight;              // 中间条的高度

    private int lowThumbWidth;
    private int highThumbWidth;

    private int barTop, barBottom;      // 进度条上面的坐标和下面的坐标，Y轴
    private int lowPosition, highPosition;// 两个滑块的坐标，X轴

    private Paint paint;

    public RangeSelectionBar(Context context) {
        this(context, null);
    }

    public RangeSelectionBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeSelectionBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        max = 1000;
        low = 500;
        high = 700;

        lowThumbWidth = (int) dp2px(6);
        highThumbWidth = (int) dp2px(6);

        lowAreaColor = Color.GRAY;
        middleAreaColor = Color.RED;
        highAreaColor = Color.GRAY;

        barHeight = (int) dp2px(3);

        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

       width = getWidth();
       height = getHeight();

       barWidth = width - lowThumbWidth / 2 - highThumbWidth / 2;

       barTop = (height - barHeight) / 2;
       barBottom = barTop + barHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        lowPosition = (int) (low / (double) max * barWidth + lowThumbWidth / 2);
        highPosition = (int) (high / (double) max * barWidth + lowThumbWidth / 2);

        drawBar(canvas);
        drawThumb(canvas);
    }

    private void drawBar(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(lowAreaColor);
        canvas.drawRect(lowThumbWidth / 2, barTop, lowPosition, barBottom, paint);

        paint.setColor(middleAreaColor);
        canvas.drawRect(lowPosition, barTop, highPosition, barBottom, paint);

        paint.setColor(highAreaColor);
        canvas.drawRect(highPosition, barTop, lowThumbWidth / 2 + barWidth, barBottom, paint);
    }

    private void drawThumb(Canvas canvas) {
        paint.setColor(COLOR_THUMB);
        canvas.drawCircle(lowPosition, height / 2, dp2px(6), paint);
        canvas.drawCircle(highPosition, height / 2, dp2px(6), paint);
    }

    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private enum TouchArea {
        INVALID,                // 无效
        LOW_AREA,               // 低游标区域
        ON_LOW,                 // 低游标
        MIDDLE,                 // 中间区域
        ON_HIGH,                // 高游标
        HIGH_AREA               // 高游标区域
    }

}
