package net.illusor.swipeplayer.services;

import net.illusor.swipeplayer.domain.AudioFile;

import java.util.List;

class AudioPlayerSequentialPlaylist implements AudioPlayerPlaylist
{
    private final List<AudioFile> playlist;
    private final AudioPlayer audioPlayer;
    private final SoundService soundService;

    public AudioPlayerSequentialPlaylist(List<AudioFile> playList, AudioPlayer audioPlayer, SoundService soundService)
    {
        this.playlist = playList;
        this.audioPlayer = audioPlayer;
        this.soundService = soundService;
    }

    @Override
    public void onPlaybackComplete()
    {
        AudioFile file = this.getNext();
        if (file != null)
            this.soundService.play(file);
        else
            this.soundService.stop();
    }

    @Override
    public void onError(AudioFile audioFile)
    {
        this.onPlaybackComplete();
    }

    @Override
    public AudioFile getNext()
    {
        if (this.playlist == null || this.playlist.size() == 0)
            return null;

        int playlistSize = this.playlist.size();
        int nextIndex = this.playlist.indexOf(this.audioPlayer.getAudioFile()) + 1;
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

        if (newFile.exists() && !newFile.equals(this.audioPlayer.getAudioFile()))
            return newFile;
        else
            return null;
    }

    @Override
    public AudioFile getPrevious()
    {
        if (this.playlist == null || this.playlist.size() == 0)
            return null;

        int playlistSize = this.playlist.size();
        int nextIndex = this.playlist.indexOf(this.audioPlayer.getAudioFile()) - 1;
        if (nextIndex < 0 || nextIndex >= playlistSize)
            nextIndex = playlistSize - 1;

        int count = 0;
        AudioFile newFile;
        do
        {
            newFile = this.playlist.get(nextIndex);
            count++;
            nextIndex--;
            if (nextIndex < 0)
                nextIndex = playlistSize - 1;
        } while (!newFile.exists() && count < playlistSize);

        if (newFile.exists() && !newFile.equals(this.audioPlayer.getAudioFile()))
            return newFile;
        else
            return null;
    }
}
