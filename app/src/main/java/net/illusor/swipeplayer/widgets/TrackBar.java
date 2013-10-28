package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.SeekBar;

public class TrackBar extends SeekBar
{
    public TrackBar(Context context)
    {
        super(context);
    }

    public TrackBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public TrackBar(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!this.isEnabled())
            return false;

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                this.setPressed(true);
                this.trackTouchEvent(event);
                break;
            }
            case MotionEvent.ACTION_MOVE:
            {
                this.trackTouchEvent(event);
                this.attemptClaimDrag();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                this.trackTouchEvent(event);
                this.setPressed(false);
                this.invalidate();
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            {
                this.setPressed(false);
                this.invalidate();
                break;
            }
        }

        return true;
    }

    private void trackTouchEvent(MotionEvent event)
    {

    }

    private void attemptClaimDrag()
    {
        final ViewParent parent = this.getParent();
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(true);
    }
}
