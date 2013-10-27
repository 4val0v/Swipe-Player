package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.helpers.FontHelper;

public class FormattedTextView extends TextView
{
    public FormattedTextView(Context context)
    {
        this(context, null);
    }

    public FormattedTextView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public FormattedTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FormattedTextView, defStyle, 0);
        int fontName = a.getInt(R.styleable.FormattedTextView_Font, -1);
        Typeface font = FontHelper.font(fontName);
        this.setTypeface(font);
        a.recycle();
    }
}
