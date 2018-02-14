package com.tni.mobile.project1.Adapter;

import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tni.mobile.project1.Dao.NetworkDao;
import com.tni.mobile.project1.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class NetworkAdapter extends RecyclerView.Adapter<NetworkAdapter.NetworkViewHolder> {
    private ArrayList<NetworkDao> networkList = new ArrayList<NetworkDao>();
    private PackageManager pm;
    DecimalFormat mFormat = new DecimalFormat("##,###,##0");

    public class NetworkViewHolder extends RecyclerView.ViewHolder {
        public TextView packageName, appName, tx, rx;
        public ImageView appIcon;

        public NetworkViewHolder(View view){
            super(view);
            packageName = (TextView) view.findViewById(R.id.packageName);
            appName = (TextView) view.findViewById(R.id.appName);
            tx = (TextView) view.findViewById(R.id.tx);
            rx = (TextView) view.findViewById(R.id.rx);
            appIcon = (ImageView) view.findViewById(R.id.appIcon);
        }
    }

    public NetworkAdapter(ArrayList<NetworkDao> networkList, PackageManager pm) {
        this.networkList = networkList;
        this.pm = pm;
    }

    @Override
    public NetworkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.network_list_content, parent, false);

        return new NetworkViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NetworkViewHolder holder, int position) {
        NetworkDao topic = networkList.get(position);
        holder.packageName.setText(topic.getPName());
        holder.appName.setText(topic.getAppName());

        String transmit = mFormat.format(topic.getTx()) + "";
        holder.tx.setText(transmit);

        String receive = mFormat.format(topic.getRx()) + "";
        holder.rx.setText(receive);

        try {
            holder.appIcon.setImageDrawable(pm.getApplicationIcon(topic.getPName()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return networkList.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        networkList.clear();
        notifyDataSetChanged();
    }

}
