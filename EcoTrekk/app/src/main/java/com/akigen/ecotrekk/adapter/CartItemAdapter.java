package com.akigen.ecotrekk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.model.CartItem;
import com.bumptech.glide.Glide;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {

    private static final float DISABLED_ALPHA = 0.45f;
    private static final float ENABLED_ALPHA = 1.0f;

    public interface OnCartItemActionListener {
        void onCartChanged();

        void onItemRemoved(int position, CartItem item);
    }

    private final List<CartItem> cartItems = new ArrayList<>();
    private final OnCartItemActionListener actionListener;

    public CartItemAdapter(OnCartItemActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void submitList(List<CartItem> items) {
        cartItems.clear();
        cartItems.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_item, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        if (!TextUtils.isEmpty(cartItem.getImageUrl())) {
            Glide.with(holder.imgCartItem)
                    .load(cartItem.getImageUrl())
                    .placeholder(cartItem.getImageResId())
                    .error(cartItem.getImageResId())
                    .centerCrop()
                    .into(holder.imgCartItem);
        } else {
            holder.imgCartItem.setImageResource(cartItem.getImageResId());
        }
        holder.tvCartItemName.setText(cartItem.getName());
        holder.tvCartItemCategory.setText(cartItem.getCategory());
        holder.tvCartItemQuantity.setText(String.format(Locale.US, "%d", cartItem.getQuantity()));
        holder.tvCartItemUnitPrice.setText(String.format(Locale.US, "LKR %,.2f / day", cartItem.getPerDayPrice()));
        holder.tvCartItemTotal.setText(String.format(Locale.US, "LKR %,.2f", cartItem.getLineTotal()));
        bindStepperState(holder, cartItem);

        holder.btnDecreaseQty.setOnClickListener(v -> {
            cartItem.decreaseQuantity();
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(adapterPosition);
            }
            actionListener.onCartChanged();
        });

        holder.btnIncreaseQty.setOnClickListener(v -> {
            int availableCap = cartItem.getAvailableQty();
            if (availableCap == Integer.MAX_VALUE || cartItem.getQuantity() < availableCap) {
                cartItem.increaseQuantity();
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_ID) {
                    notifyItemChanged(adapterPosition);
                }
                actionListener.onCartChanged();
            }
        });

        holder.btnDeleteItem.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                actionListener.onItemRemoved(adapterPosition, cartItem);
            }
        });
    }

    private void bindStepperState(@NonNull CartItemViewHolder holder, @NonNull CartItem cartItem) {
        boolean canDecreaseQty = cartItem.getQuantity() > 1;
        int availableCap = cartItem.getAvailableQty();
        boolean canIncreaseQty = availableCap == Integer.MAX_VALUE || cartItem.getQuantity() < availableCap;

        holder.btnDecreaseQty.setEnabled(canDecreaseQty);
        holder.btnDecreaseQty.setAlpha(canDecreaseQty ? ENABLED_ALPHA : DISABLED_ALPHA);
        holder.btnIncreaseQty.setEnabled(canIncreaseQty);
        holder.btnIncreaseQty.setAlpha(canIncreaseQty ? ENABLED_ALPHA : DISABLED_ALPHA);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgCartItem;
        private final TextView tvCartItemName;
        private final TextView tvCartItemCategory;
        private final TextView tvCartItemQuantity;
        private final TextView tvCartItemUnitPrice;
        private final TextView tvCartItemTotal;
        private final MaterialButton btnDecreaseQty;
        private final MaterialButton btnIncreaseQty;
        private final MaterialButton btnDeleteItem;

        CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCartItem = itemView.findViewById(R.id.imgCartItem);
            tvCartItemName = itemView.findViewById(R.id.tvCartItemName);
            tvCartItemCategory = itemView.findViewById(R.id.tvCartItemCategory);
            tvCartItemQuantity = itemView.findViewById(R.id.tvCartItemQuantity);
            tvCartItemUnitPrice = itemView.findViewById(R.id.tvCartItemUnitPrice);
            tvCartItemTotal = itemView.findViewById(R.id.tvCartItemTotal);
            btnDecreaseQty = itemView.findViewById(R.id.btnDecreaseQty);
            btnIncreaseQty = itemView.findViewById(R.id.btnIncreaseQty);
            btnDeleteItem = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}
