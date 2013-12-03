package net.illusor.swipeplayer.fragments;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.helpers.FontHelper;
import net.illusor.swipeplayer.widgets.FormattedTextView;
import net.illusor.swipeplayer.widgets.NavigationItemView;

import java.io.File;
import java.util.List;

class NavigationHistoryAdapter extends ArrayAdapter<File>
{
    private Context context;
    private List<File> navigationHistory;

    NavigationHistoryAdapter(Context context, List<File> navigationHistory)
    {
        super(context, 0, navigationHistory);
        this.context = context;
        this.navigationHistory = navigationHistory;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        TextView view = (TextView)convertView;
        if (convertView == null)
        {
            int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, this.context.getResources().getDisplayMetrics());

            view = new TextView(this.context);
            view.setTextColor(Color.WHITE);
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            view.setTypeface(FontHelper.PTSerifItalic);
            view.setEllipsize(TextUtils.TruncateAt.END);
            view.setPadding(padding, 0, padding, 0);
        }

        File item = this.getItem(position);
        view.setText(item.getName());

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        NavigationItemView view = (NavigationItemView)convertView;
        if (view == null)
            view = new NavigationItemView(this.context);

        File item = this.getItem(position);
        view.setText(item.getName());
        view.setIconVisible(position > 0);

        return view;
    }

    List<File> getData()
    {
        return navigationHistory;
    }
}
