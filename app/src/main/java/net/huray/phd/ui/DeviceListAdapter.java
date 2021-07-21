package net.huray.phd.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.huray.phd.R;
import net.huray.phd.enumerate.DeviceType;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends BaseAdapter {
    private final Context context;
    private final List<DeviceType> devices = new ArrayList<>();

    public DeviceListAdapter(Context context) {
        this.context = context;
        initDeviceList();
    }

    private void initDeviceList() {
        devices.add(DeviceType.OMRON_WEIGHT);
        devices.add(DeviceType.OMRON_BP);
        devices.add(DeviceType.I_SENS_BS);
        notifyDataSetChanged();
    }

    public int getDeviceTypeNumber(int position) {
        return devices.get(position).getNumber();
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View oldView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View contentView = oldView;

        if (contentView == null) {
            contentView = inflater.inflate(R.layout.item_device_list, parent, false);
            final DeviceViewHolder holder = new DeviceViewHolder();
            holder.tvDeviceName = contentView.findViewById(R.id.tv_device_item);

            setDeviceNames(holder.tvDeviceName, devices.get(position));
            contentView.setTag(holder);
        }

        return contentView;
    }

    private void setDeviceNames(TextView tv, DeviceType type) {
        tv.setText(type.getName());
    }

    private class DeviceViewHolder {
        private TextView tvDeviceName;
    }
}
