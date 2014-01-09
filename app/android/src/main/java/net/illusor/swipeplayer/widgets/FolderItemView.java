package net.illusor.swipeplayer.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.helpers.DimensionHelper;

@TargetApi(16)
public class FolderItemView extends LinearLayout
{
    private FormattedTextView text;
    private View icon;

    public FolderItemView(Context context)
    {
        this(context, null);
    }

    public FolderItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        int paddingH = (int) DimensionHelper.dipToPx(16);
        int paddingV = (int) DimensionHelper.dipToPx(10);
        this.setPadding(paddingH, paddingV, paddingH, paddingV);
        this.setOrientation(LinearLayout.HORIZONTAL);

        Drawable bg = context.getResources().getDrawable(R.drawable.item_folder_bg);
        if (Build.VERSION.SDK_INT < 16)
            this.setBackgroundDrawable(bg);
        else
            this.setBackground(bg);

        LayoutInflater.from(context).inflate(R.layout.list_item_folder, this);

        this.text = (FormattedTextView)this.findViewById(R.id.id_folder_title);
        this.icon = this.findViewById(R.id.id_folder_selected);
    }

    public void setText(CharSequence text)
    {
        this.text.setText(text);
    }

    public void setSelected(boolean isSelected)
    {
        this.icon.setVisibility(isSelected ? View.VISIBLE : View.GONE);
    }
}
