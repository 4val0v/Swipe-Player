package net.illusor.swipeplayer.activities;

import android.test.ActivityInstrumentationTestCase2;

public class SwipeActivityTest extends ActivityInstrumentationTestCase2<SwipeActivity>
{
    public SwipeActivityTest()
    {
        super(SwipeActivity.class);
    }

    public void testActivityNotNull()
    {
        assertNotNull(this.getActivity());
    }
}
