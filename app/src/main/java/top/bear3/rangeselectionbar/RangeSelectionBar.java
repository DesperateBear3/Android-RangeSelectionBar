package top.bear3.rangeselectionbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * author : TT
 * e-mail : tianruofengxing@163.com
 * time   : 2018/03/26
 * desc   :
 * version: 1.0
 */

public class RangeSelectionBar extends View {
    private static final String TAG = "RangeSelectionBar";
    private static final int COLOR_THUMB = 0xFF00A3DA;
    private static final int COLOR_LOW_HIGH_DEFAULT = 0xB2757575;

    private static final int MIN_HEIGHT = 20;       // dp

    private int max, low, high;         // 最大值，低游标和高游标
    private float width, height;          // 控件高和宽
    private float barWidth;               // 进度条宽

    private int lowAreaColor;
    private int middleAreaColor;
    private int highAreaColor;

    private float middleBarHeight;
    private float sideBarHeight;              // 中间条的高度

    private float thumbWidth;              // 滑块大小

    private float lowPosition, highPosition;// 两个滑块的坐标，X轴

    private Paint paint;

    private ThumbType currentThumb;         // 当前操作的滑块

    private ThumbDrawable lowThumbDrawable;
    private ThumbDrawable highThumbDrawable;

    private OnRangeSelectionBarChangeListener listener;

    public RangeSelectionBar(Context context) {
        this(context, null);
    }

    public RangeSelectionBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeSelectionBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        max = 1000;
        low = 0;
        high = 1000;

        thumbWidth = (int) dp2px(MIN_HEIGHT);

        lowAreaColor = COLOR_LOW_HIGH_DEFAULT;
        middleAreaColor = COLOR_THUMB;
        highAreaColor = COLOR_LOW_HIGH_DEFAULT;

        sideBarHeight = (int) dp2px(2);
        middleBarHeight = (int) dp2px(2);

        paint = new Paint();
        paint.setAntiAlias(true);

        lowThumbDrawable = new ThumbDrawable(COLOR_THUMB, 0xFFD3D3D3, new float[]{dp2px(6f), dp2px(10f), dp2px(15f)});
        lowThumbDrawable.setCallback(this);
        highThumbDrawable = new ThumbDrawable(COLOR_THUMB, 0xFFD3D3D3, new float[]{dp2px(6f), dp2px(10f), dp2px(15f)});
        highThumbDrawable.setCallback(this);
    }

    public void setOnRangeSelectionBarChangeListener(OnRangeSelectionBarChangeListener listener) {
        this.listener = listener;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        if (max < high) {
            throw new RuntimeException("Max must bigger than high");
        }

        this.max = max;
        invalidate();
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        if (low < 0) {
            low = 0;
        } else if (low >= high) {
            throw new RuntimeException("low must smaller than high");
        }

        this.low = low;
        invalidate();
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        if (high > max) {
            high = max;
        } else if (high <= low) {
            throw new RuntimeException("high must bigger than low");
        }

        this.high = high;
        invalidate();
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

        barWidth = width - thumbWidth * 2;

        lowPosition = low / (float) max * barWidth + thumbWidth;
        highPosition = high / (float) max * barWidth + thumbWidth;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        Log.d(TAG, "onTouchEvent: " + event.getAction());

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            TouchArea flag = getTouchArea(event);

            switch (flag) {
                case LOW_AREA:
                    updateLowThumb(event.getX());
                    currentThumb = ThumbType.LOW;
                    lowThumbDrawable.setPressed();
                    break;

                case HIGH_AREA:
                    updateHighThumb(event.getX());
                    currentThumb = ThumbType.HIGH;
                    highThumbDrawable.setPressed();
                    break;

                default:
                    return false;
            }

            notifyStartTrackingTouch();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (currentThumb == ThumbType.LOW) {
                if (event.getX() < highPosition) {
                    updateLowThumb(event.getX());
                }
            } else if (currentThumb == ThumbType.HIGH) {
                if (event.getX() > lowPosition) {
                    updateHighThumb(event.getX());
                }
            }

            notifyProgressChanged();

        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            currentThumb = null;
            lowThumbDrawable.cancel();
            highThumbDrawable.cancel();

            notifyStopTrackingTouch();
        }

        invalidate();

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw: ");

        drawBar(canvas);
        drawThumb(canvas);
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        lowThumbDrawable.cancel();
        highThumbDrawable.cancel();
    }

    private void updateLowThumb(float newX) {
        if (newX < thumbWidth) {
            newX = thumbWidth;
        }

        lowPosition = newX;
        low = getThumbProgress(lowPosition);
    }

    private void updateHighThumb(float newX) {
        if (newX > barWidth + thumbWidth) {
            newX = barWidth + thumbWidth;
        }

        highPosition = newX;
        high = getThumbProgress(highPosition);
    }

    private int getThumbProgress(float position) {
        return (int) ((position - thumbWidth) / barWidth * max);
    }

    private TouchArea getTouchArea(MotionEvent event) {
        float x = event.getX();

        // 按下判断区域，此时只需判断在哪个区域即可
        if (x <= lowPosition) {
            return TouchArea.LOW_AREA;
        } else if (x >= highPosition) {
            return TouchArea.HIGH_AREA;
        } else {
            if (x - lowPosition <= highPosition - x) {
                return TouchArea.LOW_AREA;
            } else {
                return TouchArea.HIGH_AREA;
            }
        }
    }

    private void drawBar(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);

        float sideBarTop = (height - sideBarHeight) / 2;
        float sideBarBottom = sideBarTop + sideBarHeight;

        paint.setColor(lowAreaColor);
        canvas.drawRect(thumbWidth, sideBarTop, lowPosition, sideBarBottom, paint);

        paint.setColor(middleAreaColor);
        canvas.drawRect(lowPosition, (height - middleBarHeight) / 2, highPosition,
                (height - middleBarHeight) / 2 + middleBarHeight, paint);

        paint.setColor(highAreaColor);
        canvas.drawRect(highPosition, sideBarTop, thumbWidth + barWidth, sideBarBottom, paint);
    }

    private void drawThumb(Canvas canvas) {
//        paint.setColor(COLOR_THUMB);

        lowThumbDrawable.setBounds((int) (lowPosition - dp2px(10)), 0, (int) (lowPosition + dp2px(10)), (int) height);
        highThumbDrawable.setBounds((int) (highPosition - dp2px(10)), 0, (int) (highPosition + dp2px(10)), (int) height);


        if (currentThumb == ThumbType.LOW) {
            lowThumbDrawable.setPressed();
        } else if (currentThumb == ThumbType.HIGH) {
            highThumbDrawable.setPressed();
        }

        lowThumbDrawable.draw(canvas);
        highThumbDrawable.draw(canvas);
    }

    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void notifyStartTrackingTouch() {
        if (listener != null) {
            listener.onStartTrackingTouch(this);
        }
    }

    private void notifyProgressChanged() {
        if (listener != null) {
            listener.onProgressChanged(this, low, high);
        }
    }

    private void notifyStopTrackingTouch() {
        if (listener != null) {
            listener.onStopTrackingTouch(this);
        }
    }

    private enum TouchArea {
        LOW_AREA,               // 低游标区域
        HIGH_AREA              // 高游标区域
    }

    private enum ThumbType {
        LOW,
        HIGH
    }

    public interface OnRangeSelectionBarChangeListener {
        void onStartTrackingTouch(RangeSelectionBar rangeSelectionBar);

        void onProgressChanged(RangeSelectionBar rangeSelectionBar, int lowProgress, int highProgress);

        void onStopTrackingTouch(RangeSelectionBar rangeSelectionBar);
    }

    private static class ThumbDrawable extends Drawable {
        private Paint paint;

        private int thumbColor;
        private int ripplesColor;

        private ValueAnimator animator;

        private ThumbState state;

        private float[] thumbRadiusList;
        private float thumbRadius;

        private boolean isInAnimator;

        public ThumbDrawable(final int thumbColor, int ripplesColor, float[] thumbRadiusList) {
            this.thumbColor = thumbColor;
            this.ripplesColor = ripplesColor;
            this.thumbRadiusList = thumbRadiusList;

            thumbRadius = thumbRadiusList[0];

            paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            animator = ValueAnimator.ofFloat(thumbRadiusList).setDuration(500);
            animator.setInterpolator(new ThumbInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    thumbRadius = (float) animation.getAnimatedValue();
                    Log.d(TAG,"thumb radius : " + thumbRadius);
                    invalidateSelf();
                }
            });
        }

        public void setThumbColor(int thumbColor) {
            this.thumbColor = thumbColor;
        }

        public void setRipplesColor(int ripplesColor) {
            this.ripplesColor = ripplesColor;
        }

        public void cancel() {
            thumbRadius = thumbRadiusList[0];
            if (animator != null) {
                animator.cancel();
            }
            isInAnimator = false;
            state = ThumbState.NORMAL;
        }

        public void setPressed() {
            state = ThumbState.PRESSED;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            startAnimator();

            Log.d(TAG, "draw drawable");

            final Rect rect = getBounds();
            float cx = rect.exactCenterX();
            float cy = rect.exactCenterY();

            // 画中间的圆
            paint.setColor(thumbColor);
            if (thumbRadius <= thumbRadiusList[1]) {
                canvas.drawCircle(cx, cy, thumbRadius, paint);
            } else {
                canvas.drawCircle(cx, cy, thumbRadiusList[1], paint);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
            invalidateSelf();
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        private void startAnimator() {
            if (state != ThumbState.PRESSED) {
                return;
            }

            if (isInAnimator) {
                return;
            }

            if (animator.isRunning() || animator.isStarted()) {
                return;
            }

            isInAnimator = true;
            animator.start();
        }

        private enum ThumbState {
            NORMAL,
            PRESSED
        }

        private class ThumbInterpolator implements Interpolator {
            @Override
            public float getInterpolation(float input) {
                return (float) Math.sin(input * Math.PI / 2);
            }
        }
    }
}
