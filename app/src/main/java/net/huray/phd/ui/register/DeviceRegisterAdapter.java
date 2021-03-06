package net.huray.phd.ui.register;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.huray.phd.R;
import net.huray.phd.bluetooth.model.entity.DiscoveredDevice;
import net.huray.phd.enumerate.DeviceType;
import net.huray.phd.model.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceRegisterAdapter extends BaseAdapter {
    private final Context context;
    private final DeviceType deviceType;

    private final List<Device> devices = new ArrayList<>();

    public DeviceRegisterAdapter(Context context, DeviceType deviceType) {
        this.context = context;
        this.deviceType = deviceType;
    }

    public void updateOmronDevices(List<DiscoveredDevice> datum) {
        devices.clear();
        for (DiscoveredDevice device : datum) {
            devices.add(new Device(context.getString(deviceType.getName()), device.getAddress()));
        }
        notifyDataSetChanged();
    }

    public String getDeviceAddress(int position) {
        return devices.get(position).getAddress();
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
            view = inflater.inflate(R.layout.item_scanned_device, parent, false);
            final ViewHolder holder = new ViewHolder();

            holder.tvName = view.findViewById(R.id.tv_scanned_device_name);
            holder.tvAddress = view.findViewById(R.id.tv_scanned_device_address);
            holder.tvName.setText(devices.get(position).getName());
            holder.tvAddress.setText(devices.get(position).getAddress());

            view.setTag(holder);
        }

        return view;
    }

    private class ViewHolder {
        TextView tvName;
        TextView tvAddress;
    }
}
