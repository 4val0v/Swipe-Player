package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.fragments.TrackListAdapter;

public class TrackPager extends ViewPager
{
    private boolean isPressed;
    private OnPageChangeListener listener;

    public TrackPager(Context context)
    {
        super(context);
    }

    public TrackPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        switch (ev.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
            {
                this.isPressed = true;
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                if (this.isPressed)
                {
                    this.isPressed = false;
                    this.performClick();
                }
                break;
            }
            default:
                this.isPressed = false;
        }

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

    public TrackListAdapter getTrackAdapter()
    {
        return (TrackListAdapter)this.getAdapter();
    }
}
