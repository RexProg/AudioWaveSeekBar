package com.rexprog.audiowaveseekbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AudioWaveSeekBar extends View {

    private static Paint paintInner;
    private static Paint paintOuter;
    private int width;
    private int height;
    private float startX;
    private int thumbX = 0;
    private View parentView;
    private Context context;
    private int thumbDX = 0;
    private byte[] waveformBytes;
    private boolean pressed = false;
    private boolean startDragging = false;
    private SeekBarChangeListener seekBarChangeListener;
    private int innerColor = 0xffafbcd7;
    private int outerColor = 0xff3b5998;
    private int pressedColor = 0xffa7b8dc;
    private int duration;

    public AudioWaveSeekBar(Context context) {
        this(context, null);
    }

    public AudioWaveSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioWaveSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        if (paintInner == null) {
            paintInner = new Paint();
            paintOuter = new Paint();
        }
    }

    public static float dip2px(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float px2cm(Context context, float cm) {
        return (cm / 2.54f) * context.getResources().getDisplayMetrics().density;
    }

    public void setOnSeekBarChangeListener(SeekBarChangeListener seekBarDelegate) {
        seekBarChangeListener = seekBarDelegate;
    }

    public void setColors(int inner, int outer, int selected) {
        innerColor = inner;
        outerColor = outer;
        pressedColor = selected;
    }

    public void setWaveform(byte[] waveform) {
        waveformBytes = waveform;
        invalidate();
    }

    public void setProgress(float progress) {
        if (duration != 0)
            progress = progress / duration;
        else
            progress = 0;
        thumbX = (int) Math.ceil(width * progress);
        if (thumbX < 0) {
            thumbX = 0;
        } else if (thumbX > width) {
            thumbX = width;
        }
        invalidate();
    }

    public void setParentView(View view) {
        parentView = view;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = onTouch(event.getAction(), event.getX(), event.getY());
        if (result)
            invalidate();
        return result || super.onTouchEvent(event);
    }

    public boolean onTouch(int action, float x, float y) {
        if (action == MotionEvent.ACTION_DOWN) {
            if (0 <= x && x <= width && y >= 0 && y <= height) {
                startX = x;
                pressed = true;
                thumbDX = (int) (x - thumbX);
                startDragging = false;
                return true;
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (pressed) {
                if (action == MotionEvent.ACTION_UP && seekBarChangeListener != null) {
                    seekBarChangeListener.OnSeekBarChangeListener((int) (((float) thumbX / (float) width) * duration));
                }
                pressed = false;
                return true;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (pressed) {
                if (startDragging) {
                    thumbX = (int) (x - thumbDX);
                    if (thumbX < 0) {
                        thumbX = 0;
                    } else if (thumbX > width) {
                        thumbX = width;
                    }
                }
                if (startX != -1 && Math.abs(x - startX) > px2cm(context, 0.2f)) {
                    if (parentView != null && parentView.getParent() != null) {
                        parentView.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    startDragging = true;
                    startX = -1;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setSize(right - left, bottom - top);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (waveformBytes == null || width == 0) {
            return;
        }
        float totalBarsCount = width / dip2px(context, 3);
        if (totalBarsCount <= 0.1f)
            return;

        int max = waveformBytes[0];
        int min = waveformBytes[0];
        for (int i = 1; i < waveformBytes.length; i++) {
            if (waveformBytes[i] > max) max = waveformBytes[i];
            if (waveformBytes[i] < min) min = waveformBytes[i];
        }
        int samplesCount = waveformBytes.length;
        float samplesPerBar = samplesCount / totalBarsCount;
        float barCounter = 0;
        int nextBarNum = 0;

        paintInner.setColor(pressed ? pressedColor : innerColor);
        paintOuter.setColor(outerColor);

        int barNum = 0;
        int lastBarNum;
        int drawBarCount;

        for (int a = 0; a < samplesCount; a++) {
            if (a != nextBarNum) {
                continue;
            }
            drawBarCount = 0;
            lastBarNum = nextBarNum;
            while (lastBarNum == nextBarNum) {
                barCounter += samplesPerBar;
                nextBarNum = (int) barCounter;
                drawBarCount++;
            }

            int value = (waveformBytes[a] - min) % (max + 1);

            for (int b = 0; b < drawBarCount; b++) {
                float x = barNum * dip2px(context, 4);
                if (x < thumbX && x + dip2px(context, 3) < thumbX) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        canvas.drawRoundRect(x, height - Math.max(dip2px(context, 3), height * value / max), x + dip2px(context, 3), height, 50, 50, paintOuter);
                    else
                        canvas.drawRect(x, height - Math.max(dip2px(context, 3), height * value / max), x + dip2px(context, 3), height, paintOuter);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        canvas.drawRoundRect(x, height - Math.max(dip2px(context, 3), height * value / max), x + dip2px(context, 3), height, 50, 50, paintInner);
                    else
                        canvas.drawRect(x, height - Math.max(dip2px(context, 3), height * value / max), x + dip2px(context, 3), height, paintInner);
                    if (x < thumbX) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            canvas.drawRoundRect(x, height - Math.max(dip2px(context, 3), height * value / max), thumbX, height, 50, 50, paintOuter);
                        else
                            canvas.drawRect(x, height - Math.max(dip2px(context, 3), height * value / max), thumbX, height, paintOuter);
                    }
                }
                barNum++;
            }
        }
    }

    public interface SeekBarChangeListener {
        void OnSeekBarChangeListener(int progress);
    }
}
