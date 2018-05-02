package com.paydayme.spatialguide.ui.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lid.lib.LabelImageView;
import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.storage.InternalStorage;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BTDeviceAdapter extends RecyclerView.Adapter<BTDeviceAdapter.ViewHolder> {

    private static final String TAG = "BTDeviceAdapter";

    public interface OnItemClickListener {
        void onItemClick(BluetoothDevice item);
    }

    private Context context;
    private final Set<BluetoothDevice> bluetoothDevices;
    private final OnItemClickListener listener;

    public BTDeviceAdapter(Context context, Set<BluetoothDevice> items, OnItemClickListener listener) {
        this.context = context;
        this.bluetoothDevices = items;
        this.listener = listener;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_row, parent, false);
        return new ViewHolder(v, context);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind((BluetoothDevice) bluetoothDevices.toArray()[position], listener);
    }

    @Override public int getItemCount() {
        return bluetoothDevices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.bluetoothName) TextView bluetoothDeviceName;

        private Context context;

        ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            ButterKnife.bind(this, itemView);
        }

        void bind(final BluetoothDevice item, final OnItemClickListener listener) {
            bluetoothDeviceName.setText(item.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}