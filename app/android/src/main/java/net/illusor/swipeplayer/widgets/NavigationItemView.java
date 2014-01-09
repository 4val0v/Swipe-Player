package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.helpers.DimensionHelper;

public class NavigationItemView extends LinearLayout
{
    private View icon;
    private FormattedTextView text;

    public NavigationItemView(Context context)
    {
        this(context, null);
    }

    public NavigationItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        int paddingH = (int) DimensionHelper.dipToPx(16);
        int paddingV = (int) DimensionHelper.dipToPx(14);
        this.setPadding(paddingH, paddingV, paddingH, paddingV);
        this.setOrientation(LinearLayout.HORIZONTAL);

        LayoutInflater.from(context).inflate(R.layout.list_item_nav_history, this);

        this.icon = this.findViewById(R.id.id_nav_history_icon);
        this.text = (FormattedTextView) this.findViewById(R.id.id_nav_history_title);
    }

    public void setIconVisible(boolean visible)
    {
        this.icon.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setText(CharSequence text)
    {
        this.text.setText(text);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST)
        {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            this.setMeasuredDimension(widthSize, this.getMeasuredHeight());
        }
    }
}
