package com.tni.mobile.project1.Adapter;

import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tni.mobile.project1.Dao.CPUDao;
import com.tni.mobile.project1.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CPUAdapter extends RecyclerView.Adapter<CPUAdapter.MyViewHolder> {

    private ArrayList<CPUDao> CPUList = new ArrayList<CPUDao>();
    PackageManager pm;
    DecimalFormat mFormat = new DecimalFormat("##0.0");

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

    public CPUAdapter(ArrayList<CPUDao> CPUList, PackageManager pm){
        this.CPUList = CPUList;
        this.pm = pm;
    }

    @Override
    public CPUAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.memory_list_content, parent, false);

        return new CPUAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CPUAdapter.MyViewHolder holder, int position) {
        CPUDao topic = CPUList.get(position);
        holder.packageName.setText(topic.getPName());
        holder.appName.setText(topic.getAppName());
        String usage = mFormat.format(topic.getCPU()) + " %";
        holder.memoryUsed.setText(usage);
        try {
            holder.appIcon.setImageDrawable(pm.getApplicationIcon(topic.getPName()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return CPUList.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        CPUList.clear();
        notifyDataSetChanged();
    }

}
