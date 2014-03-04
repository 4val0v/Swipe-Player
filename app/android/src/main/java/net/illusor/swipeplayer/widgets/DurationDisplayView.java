/*Copyright 2014 Nikita Kobzev

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
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.helpers.DimensionHelper;
import net.illusor.swipeplayer.helpers.TimeFormatter;

/**
 * Displays timings when rewinding an audio track
 */
public class DurationDisplayView extends LinearLayout
{
    private final FormattedTextView textPassed, textLeft;//text "time passed" and "time left"
    private boolean cancelHide;

    public DurationDisplayView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.durations_display, this);
        this.setOrientation(LinearLayout.VERTICAL);
        this.setGravity(Gravity.CENTER);

        this.textPassed = (FormattedTextView)this.findViewById(R.id.id_duration_passed);
        this.textLeft = (FormattedTextView)this.findViewById(R.id.id_duration_left);
        this.textPassed.setText("2:15");
        this.textLeft.setText("-1:05");

        this.setColorsInverse(false);
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

    /**
     * Inverts the default layout colors
     * @param inverse if colors should be inverted
     */
    public void setColorsInverse(boolean inverse)
    {
        Resources r = this.getContext().getResources();
        this.textPassed.setColor(r.getColor(inverse ? R.color.color_durations_passed_inverse : R.color.color_durations_passed));
        this.textLeft.setColor(r.getColor(inverse ? R.color.color_durations_left_inverse : R.color.color_durations_left));
        this.setBackgroundResource(inverse ? R.drawable.durations_display_bg_inverse : R.drawable.durations_display_bg);
    }

    /**
     * Sets the durations to display onto the view
     * @param passed milliseconds passed since an audio track has began
     * @param total total audio track length in milliseconds
     */
    public void setDuration(int passed, int total)
    {
        final String timePassed = TimeFormatter.hhmmss(passed);
        final String timeLeft = TimeFormatter.hhmmss(total - passed);
        this.textPassed.setText(timePassed);
        this.textLeft.setText("-" + timeLeft);
    }

    /**
     * Shows the view
     */
    private void show()
    {
        this.cancelHide = true;
        super.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the view
     */
    private void hide()
    {
        if (!this.cancelHide)
        super.setVisibility(View.GONE);
    }
}
