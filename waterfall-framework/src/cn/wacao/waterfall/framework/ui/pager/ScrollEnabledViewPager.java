package cn.wacao.waterfall.framework.ui.pager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by wacao on 2014/8/29.
 */
public class ScrollEnabledViewPager extends ViewPager {

    private boolean isPagingEnabled = true;

    public ScrollEnabledViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.isPagingEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isPagingEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.isPagingEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        if(isPagingEnabled == false && event.getAction() == MotionEvent.ACTION_UP){
            isPagingEnabled = true;
        }
        return false;
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }
}
