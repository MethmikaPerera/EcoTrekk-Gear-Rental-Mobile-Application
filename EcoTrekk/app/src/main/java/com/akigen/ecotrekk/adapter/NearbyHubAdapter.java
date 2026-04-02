package com.akigen.ecotrekk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.model.HubLocation;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class NearbyHubAdapter extends RecyclerView.Adapter<NearbyHubAdapter.HubViewHolder> {

    public interface OnHubActionListener {
        void onHubSelected(HubLocation hubLocation);

        void onViewDetailsClicked(HubLocation hubLocation);
    }

    private final List<HubLocation> nearbyHubs = new ArrayList<>();
    private final OnHubActionListener onHubActionListener;

    public NearbyHubAdapter(OnHubActionListener onHubActionListener) {
        this.onHubActionListener = onHubActionListener;
    }

    public void submitList(List<HubLocation> hubs) {
        nearbyHubs.clear();
        nearbyHubs.addAll(hubs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hub_nearby, parent, false);
        return new HubViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HubViewHolder holder, int position) {
        HubLocation hubLocation = nearbyHubs.get(position);
        holder.txtHubName.setText(hubLocation.getName());
        holder.txtHubMeta.setText(hubLocation.getDistanceText());

        Glide.with(holder.imgHubType)
                .load(hubLocation.getImageUrl())
                .placeholder(R.drawable.ecotrekk_notfound)
                .error(R.drawable.ecotrekk_notfound)
                .centerCrop()
                .into(holder.imgHubType);

        holder.itemView.setOnClickListener(v -> {
            if (onHubActionListener != null) {
                onHubActionListener.onHubSelected(hubLocation);
            }
        });

        holder.btnViewHub.setOnClickListener(v -> {
            if (onHubActionListener != null) {
                onHubActionListener.onViewDetailsClicked(hubLocation);
            }
        });

    }

    @Override
    public int getItemCount() {
        return nearbyHubs.size();
    }

    static class HubViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgHubType;
        private final TextView txtHubName;
        private final TextView txtHubMeta;
        private final MaterialButton btnViewHub;

        HubViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHubType = itemView.findViewById(R.id.imgHubType);
            txtHubName = itemView.findViewById(R.id.txtHubName);
            txtHubMeta = itemView.findViewById(R.id.txtHubMeta);
            btnViewHub = itemView.findViewById(R.id.btnViewHub);
        }
    }
}
