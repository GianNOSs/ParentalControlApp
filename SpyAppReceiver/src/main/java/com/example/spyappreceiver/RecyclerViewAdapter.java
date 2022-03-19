package com.example.spyappreceiver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements Filterable {
    List<Activity> activityList;
    List<Activity> SortActivityList;
    Context context;

    public RecyclerViewAdapter(List<Activity> activityList, Context context) {
        this.activityList = activityList;
        SortActivityList = new ArrayList<>(activityList);
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_line_activity, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tv_datetime.setText(activityList.get(position).getDatetime());
        holder.tv_type.setText(activityList.get(position).getType());
        holder.tv_activity.setText(activityList.get(position).getActivity());
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            List<Activity> filteredList = new ArrayList<>();

            if(charSequence.toString().isEmpty()) {
                filteredList.addAll(SortActivityList);
            } else {
                for (Activity activity: SortActivityList) {
                    if (activity.getDatetime().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        filteredList.add(activity);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            activityList.clear();
            activityList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };
    public class MyViewHolder extends  RecyclerView.ViewHolder{
        TextView tv_datetime;
        TextView tv_type;
        TextView tv_activity;
        ConstraintLayout parentLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_datetime = itemView.findViewById(R.id.tv_datetime);
            tv_type = itemView.findViewById(R.id.tv_type);
            tv_activity = itemView.findViewById(R.id.tv_activity);
            parentLayout = itemView.findViewById(R.id.oneLineActivityLayout);
        }
    }
}
