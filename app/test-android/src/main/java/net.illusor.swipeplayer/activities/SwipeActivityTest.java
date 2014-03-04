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

package net.illusor.swipeplayer.activities;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.ToggleButton;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.fragments.AudioControlFragment;
import net.illusor.swipeplayer.fragments.PlaylistOptionsFragment;
import net.illusor.swipeplayer.widgets.DropDown;
import net.illusor.swipeplayer.widgets.DurationDisplayView;

public class SwipeActivityTest extends ActivityInstrumentationTestCase2<SwipeActivity>
{
    private SwipeActivity activity;
    private SlidingMenu slidingMenu;
    private AudioControlFragment audioControlFragment;
    private PlaylistOptionsFragment playlistOptionsFragment;
    private ViewPager viewPager;
    private DurationDisplayView durationDisplayView;
    private ToggleButton btnShuffle, btnRepeat;

    public SwipeActivityTest()
    {
        super(SwipeActivity.class);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.setActivityInitialTouchMode(true);

        this.activity = this.getActivity();
        FragmentManager fragmentManager = this.activity.getSupportFragmentManager();
        this.slidingMenu = (SlidingMenu) this.activity.findViewById(R.id.id_playlist_menu);
        this.audioControlFragment = (AudioControlFragment) fragmentManager.findFragmentById(R.id.id_audio_control);
        this.playlistOptionsFragment = (PlaylistOptionsFragment) fragmentManager.findFragmentById(R.id.id_fragment_options);
        this.viewPager = (ViewPager) this.activity.findViewById(R.id.id_swipe_view_pager);
        this.durationDisplayView = (DurationDisplayView) this.activity.findViewById(R.id.id_audio_durations);
        this.btnShuffle = (ToggleButton) this.activity.findViewById(R.id.id_playlist_shuffle);
        this.btnRepeat = (ToggleButton) this.activity.findViewById(R.id.id_playlist_repeat);
    }

    @SmallTest
    public void testPreconditions() throws Exception
    {
        assertNotNull("SwipeActivity is null", this.activity);
        assertNotNull("SlidingMenu is null", this.slidingMenu);
        assertNotNull("AudioControlFragment is null", this.audioControlFragment);
        assertNotNull("PlaylistOptionsFragment is null", this.playlistOptionsFragment);
        assertNotNull("ViewPager is null", this.viewPager);
        assertNotNull("DurationDisplayView is null", this.durationDisplayView);
        assertNotNull("BtnShuffle is null", this.btnShuffle);
        assertNotNull("BtnRepeat is null", this.btnRepeat);
    }

    @SmallTest
    public void testInitialActivityState() throws Exception
    {
        assertEquals("Sliding menu should be initially hidden", false, this.slidingMenu.isMenuShowing());
        assertEquals("AudioControlFragment should be initially hidden", View.GONE, this.audioControlFragment.getView().getVisibility());

        PagerAdapter adapter = this.viewPager.getAdapter();
        assertNotNull("ViewPager adapter is null", adapter);

        assertEquals("ViewPager should initially contain 2 pages", 2, adapter.getCount());
        assertEquals("ViewPager should initially focus the 1st page", 0, this.viewPager.getCurrentItem());

        assertEquals("Folder browser history should initially contain 1 item", 1, this.activity.getBrowserHistory().size());
        assertNull("Playlist directory should initially be null", this.activity.getCurrentMediaDirectory());
    }

    @SmallTest
    public void testInitialPagerAdapterState() throws Exception
    {
        PagerAdapter adapter = this.viewPager.getAdapter();
        assertTrue("PagerAdapter has incorrect type", adapter instanceof SwipePagerAdapter);
    }

    @SmallTest
    public void testInitialBrowserState() throws Exception
    {
        View folderBrowserView = this.viewPager.getChildAt(1);

        DropDown dropDown = (DropDown) folderBrowserView.findViewById(R.id.id_fb_nav_history);
        assertNotNull("FolderBrowser dropdown is null", dropDown);

        SpinnerAdapter spinnerAdapter = dropDown.getAdapter();
        assertNotNull("FolderBrowser dropdown adapter is null", spinnerAdapter);
        assertEquals("FolderBrowser dropdown adapter should initially contain 1 item", 1, spinnerAdapter.getCount());

        ListView listView = (ListView) folderBrowserView.findViewById(R.id.id_fb_audio_files);
        assertNotNull("FolderBrowser list is null", listView);

        ListAdapter listAdapter = listView.getAdapter();
        assertNotNull("FolderBrowser list adapter is null", listAdapter);
        assertEquals("FolderBrowser list adapter should initially contain 1 item", 1, listAdapter.getCount());
    }
}
