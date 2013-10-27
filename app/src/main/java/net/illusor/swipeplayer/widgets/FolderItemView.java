package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.illusor.swipeplayer.R;

public class FolderItemView extends LinearLayout
{
    private TextView title;
    private View iconIsFolder, iconHasPlaylistFiles;

    public FolderItemView(Context context)
    {
        this(context, null);
    }

    public FolderItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.list_item_folder, this);
        this.title = (TextView)this.findViewById(R.id.id_file_title);
        this.iconIsFolder = this.findViewById(R.id.id_file_is_folder);
        this.iconHasPlaylistFiles = this.findViewById(R.id.id_folder_has_playlist_files);
    }

    public void setTitle(CharSequence title)
    {
        this.title.setText(title);
    }

    public void setIsFolder(boolean isFolder)
    {
        this.iconIsFolder.setVisibility(isFolder ? VISIBLE : GONE);
    }

    public void setHasPlaylistFiles(boolean hasPlaylistFiles)
    {
        this.iconHasPlaylistFiles.setVisibility(hasPlaylistFiles ? VISIBLE : GONE);
    }
}
