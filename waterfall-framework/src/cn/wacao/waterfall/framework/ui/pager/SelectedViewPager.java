package cn.wacao.waterfall.framework.ui.pager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import cn.wacao.waterfall.framework.utils.Function;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by wacao on 14-7-15.
 */
public class SelectedViewPager extends ViewPager {
    private PageChangeListenerWrapper mWrapper;

    public SelectedViewPager(Context context) {
        super(context);
    }

    public SelectedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(null);
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mWrapper = new PageChangeListenerWrapper(this, listener);
        super.setOnPageChangeListener(mWrapper.getWrapperPageListener());
        if(getAdapter() != null && getAdapter() instanceof PagerSelectedAdapter){
            ((PagerSelectedAdapter) getAdapter()).setSelectedInitialize(true);
        }else{
            mWrapper.getWrapperPageListener().onPageSelected(getCurrentItem());
            mWrapper.getWrapperPageListener().onPageScrollStateChanged(ViewPager.SCROLL_STATE_IDLE);
        }
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        if (mWrapper != null) {
            mWrapper.getWrapperPageListener().onPageSelected(0);
            mWrapper.getWrapperPageListener().onPageScrollStateChanged(ViewPager.SCROLL_STATE_IDLE);
        }else if(getAdapter() != null && getAdapter() instanceof PagerSelectedAdapter){
            ((PagerSelectedAdapter) getAdapter()).setSelectedInitialize(true);
        }
    }

    private static class PageChangeListenerWrapper {
        private final OnPageChangeListener mPageListener;
        private final WeakReference<SelectedViewPager> mPager;

        public PageChangeListenerWrapper(SelectedViewPager viewPager, OnPageChangeListener listener) {
            this.mPager = new WeakReference<SelectedViewPager>(viewPager);
            this.mPageListener = listener;
        }

        public OnPageChangeListener getWrapperPageListener() {
            return this.mWrapperPageListener;
        }

        private OnPageChangeListener mWrapperPageListener = new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mPageListener != null) {
                    mPageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (mPageListener != null) {
                    mPageListener.onPageSelected(position);
                }

                final SelectedViewPager pager = mPager.get();
                if (pager != null && pager.getAdapter() != null && pager.getAdapter() instanceof PagerSelectedAdapter) {
                    final PagerSelectedAdapter adapter = (PagerSelectedAdapter) pager.getAdapter();
                    PagerFragment fragment = adapter.getPosFragment(position);
                    if (fragment != null) {
                        fragment.onSelected(position);
                    }
                    List<PagerFragment> fragmentList = adapter.excludePosFragment(position);
                    if (!Function.empty(fragmentList)) {
                        for (PagerFragment item : fragmentList) {
                            if (item != null)
                                item.onUnSelected(adapter.indexOfFragment(item));
                        }
                    }
                } else {
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    final SelectedViewPager pager = mPager.get();
                    if (pager != null && pager.getAdapter() != null && pager.getAdapter() instanceof PagerSelectedAdapter) {
                        final PagerSelectedAdapter adapter = (PagerSelectedAdapter) pager.getAdapter();
                        final int position = pager.getCurrentItem();
                        PagerFragment fragment = adapter.getPosFragment(position);
                        if (fragment != null)
                            fragment.onPageScrollComplete(position);
                    } else {
                    }
                }
                if (mPageListener != null) {
                    mPageListener.onPageScrollStateChanged(state);
                }
            }
        };
    }
}
