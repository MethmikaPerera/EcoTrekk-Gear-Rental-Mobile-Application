package com.akigen.ecotrekk.adapter;

import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.model.GearProduct;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GearProductAdapter extends RecyclerView.Adapter<GearProductAdapter.GearViewHolder> {

    public interface OnGearClickListener {
        void onGearClick(GearProduct product);
    }

    /** Called when the user double-taps a gear card (used to add the gear to the wishlist). */
    public interface OnGearDoubleTapListener {
        void onGearDoubleTap(GearProduct product);
    }

    private static final String PAYLOAD_FAVORITE_REFRESH = "favorite_refresh";
    private static final String PAYLOAD_FAVORITE_POP = "favorite_pop";

    private final List<GearProduct> gearProducts = new ArrayList<>();
    private final OnGearClickListener onGearClickListener;
    private OnGearDoubleTapListener onGearDoubleTapListener;
    private Set<Long> favoriteIds = new HashSet<>();
    private boolean favoritesInitialized = false;

    public GearProductAdapter(OnGearClickListener onGearClickListener) {
        this.onGearClickListener = onGearClickListener;
    }

    public void setOnGearDoubleTapListener(OnGearDoubleTapListener listener) {
        this.onGearDoubleTapListener = listener;
    }

    public void submitList(List<GearProduct> products) {
        gearProducts.clear();
        gearProducts.addAll(products);
        notifyDataSetChanged();
    }

    /**
     * Updates which gears show the wishlist heart. A gear that newly became a favorite gets a
     * small "pop" animation; the very first set of favorites is shown without animation.
     */
    public void setFavoriteIds(@NonNull Set<Long> newFavoriteIds) {
        Set<Long> previous = favoriteIds;
        boolean animate = favoritesInitialized;
        favoriteIds = new HashSet<>(newFavoriteIds);
        favoritesInitialized = true;

        for (int i = 0; i < gearProducts.size(); i++) {
            long id = gearProducts.get(i).getId();
            boolean wasFavorite = previous.contains(id);
            boolean isFavorite = favoriteIds.contains(id);
            if (wasFavorite == isFavorite && animate) {
                continue;
            }
            notifyItemChanged(i, animate && isFavorite ? PAYLOAD_FAVORITE_POP : PAYLOAD_FAVORITE_REFRESH);
        }
    }

    @NonNull
    @Override
    public GearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gear_product, parent, false);
        return new GearViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GearViewHolder holder, int position) {
        GearProduct product = gearProducts.get(position);
        holder.currentProduct = product;
        holder.txtGearName.setText(product.getName());
        holder.txtGearCategory.setText(product.getCategory());
        holder.txtGearPrice.setText(String.format(Locale.US, "LKR %,.2f / day", product.getPerDayPrice()));
        Glide.with(holder.imgGear)
                .load(product.getImageUrl())
                .placeholder(product.getImageResId())
                .error(product.getImageResId())
                .centerCrop()
                .into(holder.imgGear);
        bindFavoriteBadge(holder, product, false);
    }

    @Override
    public void onBindViewHolder(@NonNull GearViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }

        GearProduct product = gearProducts.get(position);
        holder.currentProduct = product;
        bindFavoriteBadge(holder, product, payloads.contains(PAYLOAD_FAVORITE_POP));
    }

    private void bindFavoriteBadge(@NonNull GearViewHolder holder, @NonNull GearProduct product, boolean pop) {
        ImageView badge = holder.imgFavorite;
        badge.animate().cancel();
        boolean isFavorite = favoriteIds.contains(product.getId());

        if (!isFavorite) {
            badge.setVisibility(View.GONE);
            return;
        }

        badge.setVisibility(View.VISIBLE);
        if (!pop) {
            badge.setScaleX(1f);
            badge.setScaleY(1f);
            return;
        }

        badge.setScaleX(0f);
        badge.setScaleY(0f);
        badge.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150L)
                .withEndAction(() -> badge.animate().scaleX(1f).scaleY(1f).setDuration(100L).start())
                .start();
    }

    @Override
    public int getItemCount() {
        return gearProducts.size();
    }

    class GearViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgGear;
        private final ImageView imgFavorite;
        private final TextView txtGearName;
        private final TextView txtGearCategory;
        private final TextView txtGearPrice;
        private GearProduct currentProduct;

        @SuppressLint("ClickableViewAccessibility")
        GearViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGear = itemView.findViewById(R.id.imgGear);
            imgFavorite = itemView.findViewById(R.id.imgGearFavorite);
            txtGearName = itemView.findViewById(R.id.txtGearName);
            txtGearCategory = itemView.findViewById(R.id.txtGearCategory);
            txtGearPrice = itemView.findViewById(R.id.txtGearPrice);

            // Single tap opens the details; double tap adds the gear to the wishlist.
            GestureDetector gestureDetector = new GestureDetector(itemView.getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDown(@NonNull MotionEvent e) {
                            return true;
                        }

                        @Override
                        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                            openDetails();
                            return true;
                        }

                        @Override
                        public boolean onDoubleTap(@NonNull MotionEvent e) {
                            addToWishlist();
                            return true;
                        }
                    });

            itemView.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setPressed(false);
                        break;
                    default:
                        break;
                }
                return gestureDetector.onTouchEvent(event);
            });

            // Accessibility: TalkBack users get explicit actions instead of gestures.
            ViewCompat.replaceAccessibilityAction(
                    itemView,
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK,
                    itemView.getContext().getString(R.string.gears_open_details_action),
                    (view, arguments) -> {
                        openDetails();
                        return true;
                    });
            ViewCompat.addAccessibilityAction(
                    itemView,
                    itemView.getContext().getString(R.string.gears_wishlist_add_action),
                    (view, arguments) -> {
                        addToWishlist();
                        return true;
                    });
        }

        private void openDetails() {
            if (currentProduct != null && onGearClickListener != null) {
                onGearClickListener.onGearClick(currentProduct);
            }
        }

        private void addToWishlist() {
            if (currentProduct != null && onGearDoubleTapListener != null) {
                onGearDoubleTapListener.onGearDoubleTap(currentProduct);
            }
        }
    }
}
