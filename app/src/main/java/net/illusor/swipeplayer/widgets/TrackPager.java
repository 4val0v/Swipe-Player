package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.fragments.TrackPagerAdapter;

public class TrackPager extends ViewPager
{
    private final TouchHandler touchHandler;
    private OnPageChangeListener listener;

    public TrackPager(Context context)
    {
        this(context, null);
    }

    public TrackPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.touchHandler = new TouchHandler(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        this.touchHandler.handleTouch(ev);
        return super.onTouchEvent(ev);
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener)
    {
        super.setOnPageChangeListener(listener);
        this.listener = listener;
    }

    public boolean swipeToItem(AudioFile audioFile)
    {
        if (this.getAdapter() == null)
            return false;

        int index = this.getTrackAdapter().getData().indexOf(audioFile);
        if (index < 0) return false;

        if (index == this.getCurrentItem())
            return true;

        super.setOnPageChangeListener(null);
        super.setCurrentItem(index, true);
        super.setOnPageChangeListener(this.listener);

        return true;
    }

    public TrackPagerAdapter getTrackAdapter()
    {
        return (TrackPagerAdapter)this.getAdapter();
    }

    private class TouchHandler
    {
        private static final int TOUCH_THRESHOLD_DP = 10;

        private boolean isPressed;
        private int touchThresholdPx;
        private float touchX, touchY;

        private TouchHandler(Context context)
        {
            this.touchThresholdPx = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TOUCH_THRESHOLD_DP, context.getResources().getDisplayMetrics());
        }

        public void handleTouch(MotionEvent ev)
        {
            switch (ev.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                {
                    this.isPressed = true;
                    this.touchX = ev.getX();
                    this.touchY = ev.getY();
                    break;
                }
                case MotionEvent.ACTION_UP:
                {
                    if (this.isPressed)
                    {
                        this.isPressed = false;
                        performClick();
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE:
                {
                    if (this.isPressed)
                    {
                        boolean xMove = Math.abs(this.touchX - ev.getX()) < this.touchThresholdPx;
                        boolean yMove = Math.abs(this.touchY - ev.getY()) < this.touchThresholdPx;

                        if (xMove || yMove)
                            this.isPressed = false;
                    }
                }
                default:
                    this.isPressed = false;
            }
        }
    }
}
