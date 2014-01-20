package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.helpers.DimensionHelper;
import net.illusor.swipeplayer.helpers.TimeFormatter;

public class DurationDisplayView extends LinearLayout
{
    private final FormattedTextView textPassed, textLeft;
    private boolean cancelHide;

    public DurationDisplayView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.durations_display, this);
        this.setBackgroundResource(R.drawable.durations_display_bg);
        this.setOrientation(LinearLayout.VERTICAL);
        this.setGravity(Gravity.CENTER);

        this.textPassed = (FormattedTextView)this.findViewById(R.id.id_duration_passed);
        this.textLeft = (FormattedTextView)this.findViewById(R.id.id_duration_left);
        this.textPassed.setText("2:15");
        this.textLeft.setText("-1:05");
    }

    @Override
    protected int getSuggestedMinimumHeight()
    {
        int size = (int) DimensionHelper.dipToPx(218);
        return size;
    }

    @Override
    protected int getSuggestedMinimumWidth()
    {
        int size = (int) DimensionHelper.dipToPx(218);
        return size;
    }

    @Override
    public void setVisibility(int visibility)
    {
        if (visibility == View.VISIBLE)
        {
            this.show();
        }
        else
        {
            this.cancelHide = false;
            this.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    hide();
                }
            }, 750);
        }
    }

    private void show()
    {
        this.cancelHide = true;
        super.setVisibility(View.VISIBLE);
    }

    private void hide()
    {
        if (!this.cancelHide)
        super.setVisibility(View.GONE);
    }

    public void setDuration(int passed, int total)
    {
        final String timePassed = TimeFormatter.hhmmss(passed);
        final String timeLeft = TimeFormatter.hhmmss(total - passed);
        this.textPassed.setText(timePassed);
        this.textLeft.setText("-" + timeLeft);
    }
}
