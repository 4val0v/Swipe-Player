package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.SeekBar;

public class TrackBar extends SeekBar
{
    private static final float paddingThreshold = 0.15f;
    private float initialTouchX;
    private int initialProgress;

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
                this.handleTouch(event);
                break;
            }
            case MotionEvent.ACTION_MOVE:
            {
                this.trackTouch(event);
                this.attemptClaimDrag();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                this.trackTouch(event);
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

    private void handleTouch(MotionEvent event)
    {
        final int width = getWidth();
        final int available = width - this.getPaddingLeft() - this.getPaddingRight();
        final int threshold = (int)(available * paddingThreshold);

        final float x = event.getX();
        float scale;
        if (x < this.getPaddingLeft() + threshold)
        {
            scale = 0.0f;
        }
        else
        {
            if (x > width - this.getPaddingRight() - threshold)
            {
                scale = 1.0f;
            }
            else
            {
                scale = (x - this.getPaddingLeft()) / (float)available;
            }
        }

        this.initialTouchX = event.getX();
        this.initialProgress = (int)(scale * this.getMax());
        this.setProgress(this.initialProgress);
    }

    private void trackTouch(MotionEvent event)
    {
        final int width = getWidth();
        final int available = width - this.getPaddingLeft() - this.getPaddingRight();

        final float dX = event.getX() - this.initialTouchX;
        final float dScale = dX / available;
        final int dProgress = (int)(dScale * this.getMax());
        this.setProgress(this.initialProgress + dProgress);
    }

    private void attemptClaimDrag()
    {
        final ViewParent parent = this.getParent();
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(true);
    }
}
