package com.tni.mobile.project1.Adapter;

import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tni.mobile.project1.Dao.MemoryDao;
import com.tni.mobile.project1.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.MyViewHolder> {

    private ArrayList<MemoryDao> memoryList = new ArrayList<MemoryDao>();
    PackageManager pm;
    DecimalFormat mFormat = new DecimalFormat("##,###,##0");

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView packageName, appName, memoryUsed;
        public ImageView appIcon;

        public MyViewHolder(View view){
            super(view);
            packageName = (TextView) view.findViewById(R.id.packageName);
            memoryUsed = (TextView) view.findViewById(R.id.memUsed);
            appIcon = (ImageView) view.findViewById(R.id.appIcon);
            appName = (TextView) view.findViewById(R.id.appName);
        }
    }

    public MemoryAdapter(ArrayList<MemoryDao> memoryList, PackageManager pm) {
        this.memoryList = memoryList;
        this.pm = pm;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.memory_list_content, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MemoryDao topic = memoryList.get(position);
        holder.packageName.setText(topic.getPName());
        holder.appName.setText(topic.getAppName());
        String mem = mFormat.format(topic.getMemory()) + " kB";
        holder.memoryUsed.setText(mem);
        try {
            holder.appIcon.setImageDrawable(pm.getApplicationIcon(topic.getPName()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return memoryList.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        memoryList.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(ArrayList<MemoryDao> list) {
        memoryList.addAll(list);
        notifyDataSetChanged();
    }
}
