package net.illusor.swipeplayer.services;

import net.illusor.swipeplayer.domain.AudioFile;

interface AudioPlayerPlaylist
{
    public void onPlaybackComplete();

    public void onError(AudioFile audioFile);

    public AudioFile getNext();

    public AudioFile getPrevious();
}
