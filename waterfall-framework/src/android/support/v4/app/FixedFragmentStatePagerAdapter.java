package android.support.v4.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

/**
 * fix fragmentstatepageradapter resotre state  bug
 *
 * Created by wacao on 2014/9/11.
 */
public abstract class FixedFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

        public FixedFragmentStatePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment f = (Fragment)super.instantiateItem(container, position);
            Bundle savedFragmentState = f.mSavedFragmentState;
            if (savedFragmentState != null) {
                savedFragmentState.setClassLoader(((Object)f).getClass().getClassLoader());
            }
            return f;
        }

}
