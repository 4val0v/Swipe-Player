package net.illusor.swipeplayer.widgets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import net.illusor.swipeplayer.R;

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
        final ListAdapter adapter = new DropDownAdapter(this.getAdapter());
        DropDownDialog dialog = new DropDownDialog(this.getContext());
        dialog.getWindow().setGravity(Gravity.TOP);

        dialog.setSingleChoiceItems(adapter, this);
        dialog.show();
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        dialog.dismiss();
        this.setSelection(which);
    }

    private static class DropDownDialog extends Dialog
    {
        private ListView listView;
        private OnClickListener listener;

        public DropDownDialog(Context context)
        {
            this(context, R.style.Theme_Dialog);
        }

        public DropDownDialog(Context context, int theme)
        {
            super(context, theme);
            this.initialize();
        }

        public DropDownDialog(Context context, boolean cancelable, OnCancelListener cancelListener)
        {
            super(context, cancelable, cancelListener);
            this.initialize();
        }

        private void initialize()
        {
            this.setCanceledOnTouchOutside(true);
            this.setContentView(R.layout.dropdown_open);
            this.listView = (ListView)this.findViewById(R.id.id_dropdown_list);
            this.listView.setOnItemClickListener(new ItemClickListener(this));

            Button button = (Button)this.findViewById(R.id.id_dropdown_close);
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    dismiss();
                }
            });
        }

        public void setSingleChoiceItems(ListAdapter adapter, OnClickListener listener)
        {
            this.listView.setAdapter(adapter);
            this.listener = listener;
        }

        private class ItemClickListener implements ListView.OnItemClickListener
        {
            private final DialogInterface dialog;

            private ItemClickListener(DialogInterface dialog)
            {
                this.dialog = dialog;
            }

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                listener.onClick(dialog, i);
            }
        }
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
                this.mListAdapter = (ListAdapter) adapter;
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
            return mAdapter == null ? null : mAdapter.getDropDownView(position, convertView, parent);
        }

        public boolean hasStableIds()
        {
            return mAdapter != null && mAdapter.hasStableIds();
        }

        public void registerDataSetObserver(DataSetObserver observer)
        {
            if (mAdapter != null)
                mAdapter.registerDataSetObserver(observer);
        }

        public void unregisterDataSetObserver(DataSetObserver observer)
        {
            if (mAdapter != null)
                mAdapter.unregisterDataSetObserver(observer);
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
         * Otherwise, return true.
         */
        public boolean areAllItemsEnabled()
        {
            final ListAdapter adapter = mListAdapter;
            if (adapter != null)
                return adapter.areAllItemsEnabled();
            else
                return true;
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
         * Otherwise, return true.
         */
        public boolean isEnabled(int position)
        {
            final ListAdapter adapter = mListAdapter;
            if (adapter != null)
                return adapter.isEnabled(position);
            else
                return true;
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
