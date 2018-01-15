package com.ilham1012.ecgbpi.helper;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ilham1012.ecgbpi.R;
import com.ilham1012.ecgbpi.POJO.EcgRecord;

import java.util.List;

/**
 * Created by Lab Desain 2 on 12/02/2016.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CustomViewHolder> {
    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;

        CustomViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.list_item_title);
            subtitle = (TextView)itemView.findViewById(R.id.list_item_subtitle);
        }
    }

    List<EcgRecord> records;

    public RVAdapter(List<EcgRecord> records){
        this.records = records;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
        CustomViewHolder pvh = new CustomViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int i) {
        holder.title.setText(records.get(i).getRecordingName());
        holder.subtitle.setText(records.get(i).getRecordingTime());

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", this.toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }



}
