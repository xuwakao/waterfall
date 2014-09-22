package cn.wacao.waterfall.framework.ui.banner;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujiexing on 14-6-10.
 */
public abstract class AdAdapter<T> extends BaseAdapter {
    protected List<T> mData = new ArrayList<T>();
    protected Context mContext;

    public AdAdapter(Context context){
        mContext = context;
    }

    private void updateData(List<T> data, boolean clear) {
        if (data == null || data.size() == 0)
            return;
        if (clear)
            mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * set adpter data
     * @param data
     */
    public void setData(List<T> data) {
        updateData(data, true);
    }

    /**
     * add data to adpter
     * @param data
     */
    public void addData(List<T> data) {
        updateData(data, false);
    }

    public List<T> getData(){
        return mData;
    }

    @Override
    public int getCount() {
        if (mData.size() <= 1)
            return mData.size();
        return Integer.MAX_VALUE;
    }

    @Override
    public T getItem(int position) {
        if (mData.size() == 0)
            return null;
        return mData.get(position % mData.size());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);
}
