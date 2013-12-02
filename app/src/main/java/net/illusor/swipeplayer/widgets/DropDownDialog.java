package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class DropDownDialog extends View
{
    public DropDownDialog(Context context)
    {
        this(context, null);
    }

    public DropDownDialog(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public DropDownDialog(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.setBackgroundColor(Color.GREEN);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        setMeasuredDimension(-1, -1);
    }
}
