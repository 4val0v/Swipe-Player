package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.SeekBar;

public class TrackBar extends SeekBar
{
    private static final float paddingThreshold = 0.15f;
    private float lastTouchX;

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

        final int x = (int) event.getX();
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
                scale = (float)(x - this.getPaddingLeft()) / (float)available;
            }
        }

        this.lastTouchX = event.getX();
        this.setProgress((int)(scale * this.getMax()));
    }

    private void trackTouch(MotionEvent event)
    {
        /*final int width = getWidth();
        final int available = width - this.getPaddingLeft() - this.getPaddingRight();

        final float dxAbs = event.getX() - this.lastTouchX;
        final float dxRel = dxAbs / (float)available;

        int offset = Math.round(dxRel * this.getMax());
        if (offset != 0)
        {
            this.lastTouchX = event.getX();
            this.setProgress(this.getProgress() + offset);
            Log.d("SWIPE", String.format("%s / %s / %s", dxRel * this.getMax(), dxRel, this.getProgress()));
        }*/
    }

    private void attemptClaimDrag()
    {
        final ViewParent parent = this.getParent();
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(true);
    }
}
