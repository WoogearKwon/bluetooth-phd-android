package net.huray.phd.ui.device_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.huray.phd.R;
import net.huray.phd.enumerate.DeviceType;
import net.huray.phd.utils.PrefUtils;

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
    public View getView(int position, View view, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);

        if (view == null) {
            view = inflater.inflate(R.layout.item_device_list, parent, false);
            final ViewHolder holder = new ViewHolder();

            holder.tvDeviceName = view.findViewById(R.id.tv_device_item);
            holder.tvDeviceName.setText(context.getString(devices.get(position).getName()));

            holder.ivIndicator = view.findViewById(R.id.iv_connection_indicator);
            setIndicator(holder.ivIndicator, devices.get(position));

            view.setTag(holder);
        }

        return view;
    }

    private void setIndicator(ImageView view, DeviceType type) {
        if (type == DeviceType.OMRON_WEIGHT) {
            boolean isOmronWeightConnected = PrefUtils.getOmronBleWeightDeviceAddress() != null;
            setIndicatorOn(view, isOmronWeightConnected);
            return;
        }

        if (type == DeviceType.OMRON_BP) {
            boolean isOmronBpConnected = PrefUtils.getOmronBleBpDeviceAddress() != null;
            setIndicatorOn(view, isOmronBpConnected);
        }

        if (type == DeviceType.I_SENS_BS) {
            boolean isIsensBsConnected = PrefUtils.getIsensBleDeviceAddress() != null;
            setIndicatorOn(view, isIsensBsConnected);
        }
    }

    private void setIndicatorOn(ImageView view, boolean isConnected) {
        if (isConnected) view.setImageResource(R.drawable.round_blue);
    }

    private class ViewHolder {
        private TextView tvDeviceName;
        private ImageView ivIndicator;
    }
}
