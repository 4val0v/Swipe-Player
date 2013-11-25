package net.illusor.swipeplayer.services;

import net.illusor.swipeplayer.domain.AudioFile;

import java.util.List;

public class AudioPlayerNextTrackBehavior implements AudioPlayerOnCompleteBehavior
{
    private final List<AudioFile> playlist;
    private final SoundService soundService;

    public AudioPlayerNextTrackBehavior(List<AudioFile> playList, SoundService soundService)
    {
        this.playlist = playList;
        this.soundService = soundService;
    }

    @Override
    public void onPlaybackComplete(AudioPlayer player)
    {
        if (this.playlist == null || this.playlist.size() == 0)
        {
            this.soundService.stop();
            return;
        }

        int playlistSize = this.playlist.size();
        int nextIndex = this.playlist.indexOf(player.getAudioFile()) + 1;
        if (nextIndex < 0 || nextIndex >= playlistSize)
            nextIndex = 0;

        int count = 0;
        AudioFile newFile;
        do
        {
            newFile = this.playlist.get(nextIndex);
            count++;
            nextIndex++;
            if (nextIndex >= playlistSize)
                nextIndex = 0;
        } while (!newFile.exists() && count < playlistSize);

        if (newFile.exists())
            this.soundService.play(newFile);
        else
            this.soundService.stop();
    }
}
