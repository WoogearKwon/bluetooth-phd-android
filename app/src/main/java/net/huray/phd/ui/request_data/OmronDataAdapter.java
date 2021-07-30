package net.huray.phd.ui.request_data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.huray.phd.R;
import net.huray.phd.enumerate.DeviceType;
import net.huray.phd.model.BpData;
import net.huray.phd.model.WeightData;

import java.util.ArrayList;
import java.util.List;

public class OmronDataAdapter extends BaseAdapter {
    private final Context context;
    private final DeviceType deviceType;

    private final List<WeightData> weightDataList = new ArrayList<>();
    private final List<BpData> bpDataList = new ArrayList<>();

    public OmronDataAdapter(Context context, DeviceType deviceType) {
        this.context = context;
        this.deviceType = deviceType;
    }

    public void addWeightData(List<WeightData> data) {
        weightDataList.addAll(data);
        notifyDataSetChanged();
    }

    public void addBpData(List<BpData> data) {
        bpDataList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (deviceType.isBpDevice()) {
            return bpDataList.size();
        }

        return weightDataList.size();
    }

    @Override
    public Object getItem(int i) {
        if (deviceType.isBpDevice()) {
            return bpDataList.get(i);
        }

        return weightDataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);

        if (view == null) {
            switch (deviceType) {
                case OMRON_WEIGHT:
                    view = inflater.inflate(R.layout.item_omron_weight_data, parent, false);
                    setWeightDataView(view, i);
                    break;

                case OMRON_BP:
//                    setBpDatView(view);
                    break;
            }
        }

        return view;
    }

    private void setWeightDataView(View view, int i) {
        WeightViewHolder holder = new WeightViewHolder();
        holder.tvTimeStamp = view.findViewById(R.id.tv_omron_weight_time);
        holder.tvWeight = view.findViewById(R.id.tv_omron_weight_value);
        holder.tvBodyFat = view.findViewById(R.id.tv_omron_weight_body_fat);

        holder.tvTimeStamp.setText(weightDataList.get(i).getTimeStamp());
        holder.tvWeight.setText(String.valueOf(weightDataList.get(i).getWeight()));
        holder.tvBodyFat.setText(String.valueOf(weightDataList.get(i).getBodyFat()));

        view.setTag(holder);
    }

    private void setBpDatView(View view) {
        BpViewHolder holder = new BpViewHolder();
        holder.tvTimeStamp = view.findViewById(R.id.tv_omron_bp_time);
        holder.tvLowPressure = view.findViewById(R.id.tv_omron_low_bp);
        holder.tvHighPressure = view.findViewById(R.id.tv_omron_high_bp);

        view.setTag(holder);
    }

    private class WeightViewHolder {
        TextView tvTimeStamp;
        TextView tvWeight;
        TextView tvBodyFat;
    }

    private class BpViewHolder {
        TextView tvTimeStamp;
        TextView tvLowPressure;
        TextView tvHighPressure;
    }
}