package net.illusor.swipeplayer.services;

import net.illusor.swipeplayer.domain.AudioFile;

/**
 * Provides playback management for a list of audio files
 */
interface AudioPlayerPlaylist
{
    /**
     * Notifies the playlist that playback of the audio file has been completed
     * @param audioFile Audio file completed
     */
    public void onPlaybackComplete(AudioFile audioFile);

    /**
     * Notifies the playlist that playback of the audio file caused an error
     * @param audioFile Audio file caused an error
     */
    public void onError(AudioFile audioFile);

    /**
     * Gets the next element into the playlist
     * @return Next audio file to play
     */
    public AudioFile getNext();

    /**
     * Gets the previous element into the playlist
     * @return Previous audio file to play
     */
    public AudioFile getPrevious();
}
