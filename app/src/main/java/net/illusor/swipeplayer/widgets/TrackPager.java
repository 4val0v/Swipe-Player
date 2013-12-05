package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.fragments.TrackPagerAdapter;

public class TrackPager extends ViewPager
{
    private final TouchHandler touchHandler;
    private PageChangeHandler listener;

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
        this.listener = new PageChangeHandler(listener, this);
        super.setOnPageChangeListener(this.listener);
    }

    @Override
    public int getCurrentItem()
    {
        return super.getCurrentItem() - 1;
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
        private static final int TOUCH_THRESHOLD_DP = 40;

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

    private class PageChangeHandler implements OnPageChangeListener
    {
        private final OnPageChangeListener wrappedListener;
        private final ViewPager control;

        private PageChangeHandler(OnPageChangeListener wrappedListener, ViewPager control)
        {
            this.wrappedListener = wrappedListener;
            this.control = control;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
            this.wrappedListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position)
        {
            final int count = this.control.getAdapter().getCount();

            if (position == 0)
            {
                this.control.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        control.setCurrentItem(count - 2);
                    }
                }, 350);
            }
            else if (position == count - 1)
            {
                this.control.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        control.setCurrentItem(1);
                    }
                }, 350);
            }
            else
            {
                this.wrappedListener.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {
            this.wrappedListener.onPageScrollStateChanged(state);
        }
    }
}
