package cn.wacao.waterfall.framework.ui.pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by wacao on 14-7-15.
 */
public class PagerFragment extends Fragment implements IPagerPosition{
    private int mPos = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setPosition(int position) {
        mPos = position;
    }

    @Override
    public void onSelected(int position) {
    }

    @Override
    public void onUnSelected(int position) {

    }

    @Override
    public void onPageScrollComplete(int position) {

    }
}
