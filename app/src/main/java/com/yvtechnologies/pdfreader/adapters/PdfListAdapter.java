package com.yvtechnologies.pdfreader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yvtechnologies.pdfreader.databinding.ItemMainBinding;
import com.yvtechnologies.pdfreader.listeners.MainRecyclerClick;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PdfListAdapter extends RecyclerView.Adapter<PdfListAdapter.ViewHolder> {
    public List<File> fileList;
    private Context context;
    private MainRecyclerClick recyclerClick;

    public PdfListAdapter(List<File> fileList, Context context,MainRecyclerClick recyclerClick) {
        this.fileList = fileList;
        this.context = context;
        this.recyclerClick = recyclerClick;
    }

    @NonNull
    @Override
    public PdfListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemMainBinding itemMainBinding = ItemMainBinding.inflate(layoutInflater,parent,false);
        return new ViewHolder(itemMainBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfListAdapter.ViewHolder holder, int position) {
        File file =fileList.get(position);
        holder.itemMainBinding.setPdfModel(file);
        holder.itemMainBinding.setOnClickListener(recyclerClick);
        holder.itemMainBinding.setPosition(position);
        holder.itemMainBinding.executePendingBindings();
    }

    public void filteredList(ArrayList<File> filteredList){
        fileList = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return fileLi   st.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemMainBinding itemMainBinding;
        public ViewHolder(@NonNull ItemMainBinding itemMainBinding) {
            super(itemMainBinding.getRoot());
            this.itemMainBinding = itemMainBinding;
        }
    }
}
