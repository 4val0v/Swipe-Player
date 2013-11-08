package net.illusor.swipeplayer.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import net.illusor.swipeplayer.R;

import java.lang.reflect.Field;

public class DropDown extends Spinner
{
    private AlertDialog popup;

    public DropDown(Context context)
    {
        super(context);
    }

    public DropDown(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public DropDown(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        if (this.popup != null && this.popup.isShowing())
        {
            this.popup.dismiss();
            this.popup = null;
        }
    }

    @Override
    public boolean performClick()
    {
        Context context = getContext();
        final DropDownAdapter adapter = new DropDownAdapter(getAdapter());
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        CharSequence prompt = this.getPrompt();
        if (prompt != null)
            builder.setTitle(prompt);

        this.popup = builder.setSingleChoiceItems(adapter, this.getSelectedItemPosition(), this).create();
        this.popup.getWindow().setGravity(Gravity.TOP);
        this.popup.show();

        /*ViewParent parent = this.popup.getListView();
        while (parent != null && parent instanceof View)
        {
            LayoutParams lp = ((View) parent).getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams)
            {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)lp;
                params.bottomMargin = 0;params.topMargin = 0;
                params.leftMargin = 0;params.rightMargin = 0;
                ((View) parent).setLayoutParams(params);
            }

            try
            {
                Field gravity = lp.getClass().getField("gravity");
                gravity.set(lp, Gravity.TOP);
            }
            catch (Exception e)
            {

            }

            ((View) parent).setPadding(0, 0, 0, 0);
            parent = parent.getParent();
        }

        WindowManager.LayoutParams WMLP = this.popup.getWindow().getAttributes();
        WMLP.x = 0; WMLP.y = 0;
        WMLP.horizontalMargin = 20;
        WMLP.gravity = Gravity.TOP | Gravity.LEFT;
        this.popup.getWindow().setAttributes(WMLP);*/

        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        super.onClick(dialog, which);

        dialog.dismiss();
        this.popup = null;
    }

    /**
     * <p>Wrapper class for an Adapter. Transforms the embedded Adapter instance
     * into a ListAdapter.</p>
     */
    private static class DropDownAdapter implements ListAdapter, SpinnerAdapter
    {
        private SpinnerAdapter mAdapter;
        private ListAdapter mListAdapter;

        /**
         * <p>Creates a new ListAdapter wrapper for the specified adapter.</p>
         *
         * @param adapter the Adapter to transform into a ListAdapter
         */
        public DropDownAdapter(SpinnerAdapter adapter)
        {
            this.mAdapter = adapter;
            if (adapter instanceof ListAdapter)
            {
                this.mListAdapter = (ListAdapter) adapter;
            }
        }

        public int getCount()
        {
            return mAdapter == null ? 0 : mAdapter.getCount();
        }

        public Object getItem(int position)
        {
            return mAdapter == null ? null : mAdapter.getItem(position);
        }

        public long getItemId(int position)
        {
            return mAdapter == null ? -1 : mAdapter.getItemId(position);
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            return getDropDownView(position, convertView, parent);
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            return mAdapter == null ? null :
                    mAdapter.getDropDownView(position, convertView, parent);
        }

        public boolean hasStableIds()
        {
            return mAdapter != null && mAdapter.hasStableIds();
        }

        public void registerDataSetObserver(DataSetObserver observer)
        {
            if (mAdapter != null)
            {
                mAdapter.registerDataSetObserver(observer);
            }
        }

        public void unregisterDataSetObserver(DataSetObserver observer)
        {
            if (mAdapter != null)
            {
                mAdapter.unregisterDataSetObserver(observer);
            }
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
         * Otherwise, return true.
         */
        public boolean areAllItemsEnabled()
        {
            final ListAdapter adapter = mListAdapter;
            return adapter == null || adapter.areAllItemsEnabled();
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
         * Otherwise, return true.
         */
        public boolean isEnabled(int position)
        {
            final ListAdapter adapter = mListAdapter;
            return adapter == null || adapter.isEnabled(position);
        }

        public int getItemViewType(int position)
        {
            return 0;
        }

        public int getViewTypeCount()
        {
            return 1;
        }

        public boolean isEmpty()
        {
            return getCount() == 0;
        }
    }
}
