/*Copyright 2013 Nikita Kobzev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.fragments.TrackPagerAdapter;
import net.illusor.swipeplayer.helpers.DimensionHelper;

public class TrackPager extends ViewPager
{
    private final TouchHandler touchHandler;
    private PageChangeHandler listener;

    public TrackPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.touchHandler = new TouchHandler();
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

    public boolean swipeToItem(AudioFile audioFile)
    {
        if (this.getAdapter() == null)
            return false;

        int index = this.getTrackAdapter().getData().indexOf(audioFile);
        if (index < 0) return false;

        Log.d("SWIPE", "Current item is: " +  this.getCurrentItem());
        if (index == this.getCurrentItem())
            return true;

        super.setOnPageChangeListener(null);
        super.setCurrentItem(index, true);
        super.setOnPageChangeListener(this.listener);

        Log.d("SWIPE", "Current item set to: " +  this.getCurrentItem());

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

        private TouchHandler()
        {
            this.touchThresholdPx = (int) DimensionHelper.dipToPx(TOUCH_THRESHOLD_DP);
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
                default:
                {
                    if (this.isPressed)
                    {
                        boolean xMove = Math.abs(this.touchX - ev.getX()) > this.touchThresholdPx;
                        boolean yMove = Math.abs(this.touchY - ev.getY()) > this.touchThresholdPx;

                        if (xMove || yMove)
                            this.isPressed = false;
                    }
                }
            }
        }
    }

    /**
     * Wrapper class for cycling effect of the {@link TrackPager}
     */
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

            //if user has selected first or last items, which are "virtual",
            //we automatically switch the viewpager to opposite item from another end of adapter
            if (position == 0)
            {
                this.control.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        control.setCurrentItem(count - 2, false);
                    }
                }, 300);
            }
            else if (position == count - 1)
            {
                this.control.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        control.setCurrentItem(1, false);
                    }
                }, 300);
            }
            else
            {
                //otherwise we just pass call further
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
