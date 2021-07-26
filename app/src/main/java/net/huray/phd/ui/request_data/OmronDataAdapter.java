package net.huray.phd.ui.request_data;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import net.huray.phd.enumerate.DeviceType;

public class OmronDataAdapter extends BaseAdapter {
    private Context context;
    private DeviceType deviceType;

    public OmronDataAdapter(Context context, DeviceType deviceType) {
        this.context = context;
        this.deviceType = deviceType;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
