/*Copyright 2013 Nikita Kobzev

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
