package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ExtendedListView extends ListView
{
    public ExtendedListView(Context context)
    {
        this(context, null);
    }

    public ExtendedListView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ExtendedListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


}
