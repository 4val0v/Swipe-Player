package net.illusor.swipeplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import net.illusor.swipeplayer.domain.AudioFile;

import java.util.ArrayList;
import java.util.List;

public class AudioBroadcastHandler extends BroadcastReceiver
{
    private static final String ACTION_PLAY_AUDIO = "net.illusor.swipeplayer.services.SoundService.Play";
    private static final String ACTION_PLAY_STOP = "net.illusor.swipeplayer.services.SoundService.Stop";
    private static final String ACTION_PLAY_PAUSE = "net.illusor.swipeplayer.services.SoundService.Pause";
    private static final String ACTION_PLAY_RESUME = "net.illusor.swipeplayer.services.SoundService.Resume";
    private static final String ACTION_PLAYLIST_CHANGED = "net.illusor.swipeplayer.services.SoundService.Playlist";

    private Context context;

    public AudioBroadcastHandler()
    {
    }

    public AudioBroadcastHandler(Context context)
    {
        this.context = context;
    }

    public void register()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY_AUDIO);
        filter.addAction(ACTION_PLAY_STOP);
        filter.addAction(ACTION_PLAY_PAUSE);
        filter.addAction(ACTION_PLAY_RESUME);
        filter.addAction(ACTION_PLAYLIST_CHANGED);
        this.getClassContext().registerReceiver(this, filter);
    }

    public void unregister()
    {
        this.getClassContext().unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        switch (intent.getAction())
        {
            case ACTION_PLAY_AUDIO:
            {
                AudioFile audioFile = (AudioFile)intent.getSerializableExtra(ACTION_PLAY_AUDIO);
                this.onPlayAudioFile(audioFile);
                break;
            }
            case ACTION_PLAY_STOP:
            {
                this.onPlaybackStop();
                break;
            }
            case ACTION_PLAY_PAUSE:
            {
                this.onPlaybackPause();
                break;
            }
            case ACTION_PLAY_RESUME:
            {
                this.onPlaybackResume();
                break;
            }
            case ACTION_PLAYLIST_CHANGED:
            {
                PlaylistParcel parcel = intent.getParcelableExtra(ACTION_PLAYLIST_CHANGED);
                this.onPlaylistChanged(parcel.playlist);
                break;
            }
        }
    }

    void sendPlayAudioFile(AudioFile audioFile)
    {
        Intent intent = new Intent(ACTION_PLAY_AUDIO);
        intent.putExtra(ACTION_PLAY_AUDIO, audioFile);
        this.context.sendBroadcast(intent);
    }

    void sendPlaybackStop()
    {
        Intent intent = new Intent(ACTION_PLAY_STOP);
        this.context.sendBroadcast(intent);
    }

    void sendPlaybackPause()
    {
        Intent intent = new Intent(ACTION_PLAY_PAUSE);
        this.context.sendBroadcast(intent);
    }

    void sendPlaybackResume()
    {
        Intent intent = new Intent(ACTION_PLAY_RESUME);
        this.context.sendBroadcast(intent);
    }

    void sendPlaylistChanged(List<AudioFile> playlist)
    {
        PlaylistParcel parcel = new PlaylistParcel(playlist);
        Intent intent = new Intent(ACTION_PLAYLIST_CHANGED);
        intent.putExtra(ACTION_PLAYLIST_CHANGED, parcel);
        this.context.sendBroadcast(intent);
    }

    protected void onPlayAudioFile(AudioFile audioFile)
    {

    }

    protected void onPlaybackStop()
    {

    }

    protected void onPlaybackPause()
    {

    }

    protected void onPlaybackResume()
    {

    }

    protected void onPlaylistChanged(List<AudioFile> playlist)
    {

    }

    protected Context getClassContext()
    {
        return this.context;
    }

    private static class PlaylistParcel implements Parcelable
    {
        private List<AudioFile> playlist;

        private PlaylistParcel(List<AudioFile> playlist)
        {
            this.playlist = playlist;
        }

        private PlaylistParcel(Parcel parcel)
        {
            this.playlist = new ArrayList<>();
            parcel.readList(this.playlist, null);
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i)
        {
            parcel.writeList(this.playlist);
        }

        public static final Parcelable.Creator<PlaylistParcel> CREATOR = new Parcelable.Creator<PlaylistParcel>()
        {
            @Override
            public PlaylistParcel createFromParcel(Parcel parcel)
            {
                return new PlaylistParcel(parcel);
            }

            @Override
            public PlaylistParcel[] newArray(int i)
            {
                return new PlaylistParcel[i];
            }
        };
    }
}
