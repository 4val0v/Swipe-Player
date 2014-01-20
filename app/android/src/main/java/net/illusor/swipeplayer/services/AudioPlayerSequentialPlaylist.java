package net.illusor.swipeplayer.services;

import net.illusor.swipeplayer.domain.AudioFile;

import java.util.List;

/**
 * Playlist, providing sequential playback of its contents
 */
class AudioPlayerSequentialPlaylist implements AudioPlayerPlaylist
{
    private final List<AudioFile> playlist;
    private final SoundService soundService;
    private AudioFile currentAudioFile;

    /**
     * Creates instance of {@link AudioPlayerSequentialPlaylist}
     * @param playList Contents of the playlist
     * @param soundService Sound Service, controlled by the playlist
     */
    public AudioPlayerSequentialPlaylist(List<AudioFile> playList, SoundService soundService)
    {
        this.playlist = playList;
        this.soundService = soundService;
    }

    @Override
    public void onPlaybackComplete(AudioFile audioFile)
    {
        this.currentAudioFile = audioFile;

        AudioFile file = this.getNext();
        if (file != null)
            this.soundService.play(file);
        else
            this.soundService.stop();
    }

    @Override
    public void onError(AudioFile audioFile)
    {
        audioFile.setValid(false);
        onPlaybackComplete(audioFile);
    }

    @Override
    public AudioFile getNext()
    {
        if (this.playlist == null || this.playlist.size() == 0)
            return null;

        int playlistSize = this.playlist.size();
        int nextIndex = this.playlist.indexOf(this.currentAudioFile) + 1;
        if (nextIndex < 0 || nextIndex >= playlistSize)
            nextIndex = 0;

        //look for the next suitable file to play
        int count = 0;
        AudioFile newFile;
        do
        {
            newFile = this.playlist.get(nextIndex);
            count++;
            nextIndex++;
            if (nextIndex >= playlistSize)
                nextIndex = 0;
        } while ((!newFile.exists() || !newFile.isValid()) && count < playlistSize);

        if (newFile.exists() && newFile.isValid() && !newFile.equals(this.currentAudioFile))
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
        int nextIndex = this.playlist.indexOf(this.currentAudioFile) - 1;
        if (nextIndex < 0 || nextIndex >= playlistSize)
            nextIndex = playlistSize - 1;

        //look for the next suitable file to play
        int count = 0;
        AudioFile newFile;
        do
        {
            newFile = this.playlist.get(nextIndex);
            count++;
            nextIndex--;
            if (nextIndex < 0)
                nextIndex = playlistSize - 1;
        } while ((!newFile.exists() || !newFile.isValid()) && count < playlistSize);

        if (newFile.exists() && newFile.isValid() && !newFile.equals(this.currentAudioFile))
            return newFile;
        else
            return null;
    }
}
