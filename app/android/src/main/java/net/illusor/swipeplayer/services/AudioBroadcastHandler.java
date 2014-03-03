/*Copyright 2014 Nikita Kobzev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package net.illusor.swipeplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import net.illusor.swipeplayer.SwipeApplication;
import net.illusor.swipeplayer.domain.AudioFile;

/**
 * Wrapper for sending and receiving messages using android message system
 */
public class AudioBroadcastHandler extends BroadcastReceiver
{
    private static final String ACTION_PLAY_AUDIO = "net.illusor.swipeplayer.services.SoundService.Play";
    private static final String ACTION_PLAY_STOP = "net.illusor.swipeplayer.services.SoundService.Stop";
    private static final String ACTION_PLAY_PAUSE = "net.illusor.swipeplayer.services.SoundService.Pause";
    private static final String ACTION_PLAY_RESUME = "net.illusor.swipeplayer.services.SoundService.Resume";

    private Context context;

    public AudioBroadcastHandler()
    {
        this.context = SwipeApplication.getAppContext();
    }

    /**
     * Registers current instance of the class as local message receiver
     */
    public void register()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY_AUDIO);
        filter.addAction(ACTION_PLAY_STOP);
        filter.addAction(ACTION_PLAY_PAUSE);
        filter.addAction(ACTION_PLAY_RESUME);
        LocalBroadcastManager.getInstance(this.context).registerReceiver(this, filter);
    }

    /**
     * Unregisters current instance of the class as local message receiver
     */
    public void unregister()
    {
        LocalBroadcastManager.getInstance(this.context).unregisterReceiver(this);
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
        }
    }

    /**
     * Sends a message: Playback of the new AudioFile has started
     * @param audioFile Audio file which is played now
     */
    void sendPlayAudioFile(AudioFile audioFile)
    {
        Intent intent = new Intent(ACTION_PLAY_AUDIO);
        intent.putExtra(ACTION_PLAY_AUDIO, audioFile);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    /**
     * Sends a message: Playback stopped
     */
    void sendPlaybackStop()
    {
        Intent intent = new Intent(ACTION_PLAY_STOP);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    /**
     * Sends a message: Playback is paused
     */
    void sendPlaybackPause()
    {
        Intent intent = new Intent(ACTION_PLAY_PAUSE);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    /**
     * Sends a message: Playback is resumed
     */
    void sendPlaybackResume()
    {
        Intent intent = new Intent(ACTION_PLAY_RESUME);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    /**
     * Fires when playback of the new file has started
     * @param audioFile AudioFile playing now
     */
    protected void onPlayAudioFile(AudioFile audioFile)
    {

    }

    /**
     * Fires when playback bas been stopped
     */
    protected void onPlaybackStop()
    {

    }

    /**
     * Fires when playback bas been paused
     */
    protected void onPlaybackPause()
    {

    }

    /**
     * Fires when playback bas been resumed
     */
    protected void onPlaybackResume()
    {

    }
}
