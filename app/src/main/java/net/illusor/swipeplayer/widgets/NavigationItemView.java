package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import net.illusor.swipeplayer.R;

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
        LayoutInflater.from(context).inflate(R.layout.list_item_nav_history, this);
        this.setOrientation(LinearLayout.HORIZONTAL);

        this.icon = this.findViewById(R.id.id_nav_history_icon);
        this.text = (FormattedTextView)this.findViewById(R.id.id_nav_history_title);

        int paddingH = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
        int paddingV = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, context.getResources().getDisplayMetrics());
        this.setPadding(paddingH, paddingV, paddingH, paddingV);
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
        int height = this.getPaddingTop() + this.getPaddingBottom() + this.text.getVerticalSize();
        this.setMeasuredDimension(ViewGroup.LayoutParams.MATCH_PARENT, height);
    }
}
