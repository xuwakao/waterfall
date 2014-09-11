package android.support.v4.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;
import cn.wacao.waterfall.framework.ui.pager.PagerSelectedAdapter;

/**
 * Created by wacao on 2014/9/11.
 */
public abstract class FixedPageFragmentAdapter extends PagerSelectedAdapter {

        public FixedPageFragmentAdapter(FragmentManager fm) {
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
