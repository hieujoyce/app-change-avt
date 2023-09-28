package com.hieujoyce.firstdemo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hieujoyce.firstdemo.R;
import com.hieujoyce.firstdemo.databinding.ActivityMainBinding;
import com.hieujoyce.firstdemo.databinding.ItemAvtBinding;
import com.hieujoyce.firstdemo.models.ImageAvt;

import java.util.List;

public class AvtListAdapter extends RecyclerView.Adapter<AvtListAdapter.AvtViewHolder> {
    public List<ImageAvt> data;
    private Context context;
    private ActivityMainBinding mActivityMainBinding;
    public  AvtListAdapter(Context context, List<ImageAvt> data, ActivityMainBinding mActivityMainBinding) {
        this.context = context;
        this.data = data;
        this.mActivityMainBinding = mActivityMainBinding;
        hideBtn();
    }
    public int selectedPosition = -1;
    public void updateList(List<ImageAvt> data) {
        this.data = data;
        notifyDataSetChanged();
    }
    public void removeAt(int position) {
        this.data.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.data.size());
    }
    public void setPo() {
        this.selectedPosition = -1;
        hideBtn();
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public AvtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAvtBinding mItemAvtBinding = ItemAvtBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AvtViewHolder(mItemAvtBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AvtViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ImageAvt item = data.get(position);
        if(item == null) return;
        Glide.with(context).load(item.url).into(holder.mItemAvtBinding.imgAvt);
        if(selectedPosition==position)
            holder.mItemAvtBinding.imgAvt.setBackground(ContextCompat.getDrawable(context, R.drawable.border_select));
        else
            holder.mItemAvtBinding.imgAvt.setBackground(ContextCompat.getDrawable(context, R.drawable.border_select_none));
        holder.mItemAvtBinding.imgAvt.setOnClickListener(view -> {
            selectedPosition=position;
            showBtn();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public class AvtViewHolder extends RecyclerView.ViewHolder {
        private ItemAvtBinding mItemAvtBinding;
        public AvtViewHolder(@NonNull ItemAvtBinding mItemAvtBinding) {
            super(mItemAvtBinding.getRoot());
            this.mItemAvtBinding = mItemAvtBinding;
        }
    }
    public void hideBtn() {
        mActivityMainBinding.imgSave.setVisibility(View.GONE);
        mActivityMainBinding.imgDelete.setVisibility(View.GONE);
    }
    public void showBtn() {
        mActivityMainBinding.imgSave.setVisibility(View.VISIBLE);
        mActivityMainBinding.imgDelete.setVisibility(View.VISIBLE);
    }
}
