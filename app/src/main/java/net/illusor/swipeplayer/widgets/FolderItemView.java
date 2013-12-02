package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;

public class FolderItemView extends LinearLayout implements View.OnClickListener
{
    private AudioFile audioFile;
    private FormattedTextView title;
    private View iconPlayAll;
    private OnPlayClickListener onPlayClickListener;

    public FolderItemView(Context context)
    {
        this(context, null);
    }

    public FolderItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        this.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.item_folder_bg));

        LayoutInflater.from(context).inflate(R.layout.list_item_folder, this);
        this.title = (FormattedTextView)this.findViewById(R.id.id_file_title);
        this.iconPlayAll = this.findViewById(R.id.id_file_play_all);
        this.iconPlayAll.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if (this.onPlayClickListener != null)
            this.onPlayClickListener.onPlayClick(this.audioFile);
    }

    public void setAudioFile(AudioFile audioFile)
    {
        this.audioFile = audioFile;
        this.title.setText(audioFile.getTitle());
        this.iconPlayAll.setVisibility(audioFile.hasSubDirectories() ? View.VISIBLE : View.GONE);
    }

    public void setOnPlayClickListener(OnPlayClickListener listener)
    {
        this.onPlayClickListener = listener;
    }

    public interface OnPlayClickListener
    {
        void onPlayClick(AudioFile audioFile);
    }
}
