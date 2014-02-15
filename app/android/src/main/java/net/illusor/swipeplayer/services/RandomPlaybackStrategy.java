package net.illusor.swipeplayer.services;

import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.domain.AudioPlaylist;

public class RandomPlaybackStrategy implements PlaybackStrategy
{
    @Override
    public AudioFile getNext(AudioFile current)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AudioFile getPrevious(AudioFile current)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setPlaylist(AudioPlaylist playlist)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
