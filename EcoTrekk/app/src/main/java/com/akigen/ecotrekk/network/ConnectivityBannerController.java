package com.akigen.ecotrekk.network;

import android.app.Activity;
import android.app.Application;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.activity.SplashActivity;
import com.akigen.ecotrekk.sync.LocalDataSyncScheduler;
import com.google.android.material.color.MaterialColors;

import java.lang.ref.WeakReference;

public final class ConnectivityBannerController
        implements Application.ActivityLifecycleCallbacks, NetworkChangeReceiver.Listener {

    private static final String HOST_PACKAGE_PREFIX = "com.akigen.ecotrekk";
    private static final long ONLINE_CONFIRMATION_MS = 2500L;
    private static final long ANIMATION_MS = 200L;
    private static final int ICON_SIZE_DP = 20;
    private static final int SLIDE_DP = 24;

    private static ConnectivityBannerController instance;

    private final Application app;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private WeakReference<Activity> resumedActivity = new WeakReference<>(null);
    private boolean online;
    private boolean showingOnlineConfirmation;

    private final Runnable hideOnlineConfirmation = () -> {
        showingOnlineConfirmation = false;
        refreshCurrentBanner(true);
    };

    private ConnectivityBannerController(@NonNull Application app) {
        this.app = app;
    }

    public static synchronized void install(@NonNull Application app) {
        if (instance != null) {
            return;
        }
        instance = new ConnectivityBannerController(app);
        instance.start();
    }

    @SuppressWarnings("deprecation")
    private void start() {
        online = NetworkUtils.isOnline(app);
        app.registerActivityLifecycleCallbacks(this);
        ContextCompat.registerReceiver(
                app,
                new NetworkChangeReceiver(this),
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
                ContextCompat.RECEIVER_EXPORTED
        );
    }

    // ---- NetworkChangeReceiver.Listener (called on the main thread) ----

    @Override
    public void onConnectivityChanged(boolean nowOnline) {
        if (nowOnline == online) {
            return;
        }
        online = nowOnline;
        mainHandler.removeCallbacks(hideOnlineConfirmation);

        if (online) {
            showingOnlineConfirmation = true;
            mainHandler.postDelayed(hideOnlineConfirmation, ONLINE_CONFIRMATION_MS);
            // Refresh Room (cart, wishlist) right away instead of waiting for the periodic sync.
            LocalDataSyncScheduler.syncNow(app);
        } else {
            showingOnlineConfirmation = false;
        }

        refreshCurrentBanner(true);
    }

    // ---- Banner handling ----

    private void refreshCurrentBanner(boolean animate) {
        Activity activity = resumedActivity.get();
        if (activity == null) {
            return;
        }
        TextView banner = findBanner(activity);
        if (banner != null) {
            applyState(banner, animate);
        }
    }

    private void applyState(@NonNull TextView banner, boolean animate) {
        boolean shouldShow = !online || showingOnlineConfirmation;
        if (shouldShow) {
            bindContent(banner, online);
            show(banner, animate);
        } else {
            hide(banner, animate);
        }
    }

    private void bindContent(@NonNull TextView banner, boolean isOnline) {
        int containerAttr = isOnline
                ? com.google.android.material.R.attr.colorPrimaryContainer
                : com.google.android.material.R.attr.colorErrorContainer;
        int contentAttr = isOnline
                ? com.google.android.material.R.attr.colorOnPrimaryContainer
                : com.google.android.material.R.attr.colorOnErrorContainer;
        int containerColor = MaterialColors.getColor(banner, containerAttr);
        int contentColor = MaterialColors.getColor(banner, contentAttr);

        banner.setBackgroundTintList(ColorStateList.valueOf(containerColor));
        banner.setTextColor(contentColor);
        banner.setText(isOnline ? R.string.online_banner_message : R.string.offline_banner_message);

        Drawable icon = AppCompatResources.getDrawable(
                banner.getContext(),
                isOnline ? R.drawable.check_circle_24px : R.drawable.wifi_off_24px
        );
        if (icon != null) {
            icon = DrawableCompat.wrap(icon.mutate());
            DrawableCompat.setTint(icon, contentColor);
            int size = dp(ICON_SIZE_DP);
            icon.setBounds(0, 0, size, size);
            banner.setCompoundDrawablesRelative(icon, null, null, null);
        }
    }

    private void show(@NonNull View banner, boolean animate) {
        banner.animate().cancel();
        if (banner.getVisibility() != View.VISIBLE) {
            banner.setAlpha(0f);
            banner.setTranslationY(-dp(SLIDE_DP));
            banner.setVisibility(View.VISIBLE);
        }
        if (animate) {
            banner.animate().alpha(1f).translationY(0f).setDuration(ANIMATION_MS).start();
        } else {
            banner.setAlpha(1f);
            banner.setTranslationY(0f);
        }
    }

    private void hide(@NonNull View banner, boolean animate) {
        banner.animate().cancel();
        if (banner.getVisibility() != View.VISIBLE) {
            return;
        }
        if (animate) {
            banner.animate()
                    .alpha(0f)
                    .translationY(-dp(SLIDE_DP))
                    .setDuration(ANIMATION_MS)
                    .withEndAction(() -> banner.setVisibility(View.GONE))
                    .start();
        } else {
            banner.setVisibility(View.GONE);
        }
    }

    @Nullable
    private TextView findBanner(@NonNull Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return null;
        }
        View existing = content.findViewById(R.id.connectivityBanner);
        return existing instanceof TextView ? (TextView) existing : null;
    }

    @Nullable
    private TextView findOrInstallBanner(@NonNull Activity activity) {
        TextView existing = findBanner(activity);
        if (existing != null) {
            return existing;
        }

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return null;
        }

        TextView banner = (TextView) LayoutInflater.from(activity)
                .inflate(R.layout.view_connectivity_banner, content, false);

        final int baseTopMargin = ((ViewGroup.MarginLayoutParams) banner.getLayoutParams()).topMargin;
        ViewCompat.setOnApplyWindowInsetsListener(banner, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            if (params.topMargin != baseTopMargin + bars.top) {
                params.topMargin = baseTopMargin + bars.top;
                view.setLayoutParams(params);
            }
            return insets;
        });

        content.addView(banner);
        ViewCompat.requestApplyInsets(banner);
        return banner;
    }

    private boolean isHostActivity(@NonNull Activity activity) {
        return activity.getClass().getName().startsWith(HOST_PACKAGE_PREFIX)
                && !(activity instanceof SplashActivity);
    }

    private int dp(int value) {
        return Math.round(value * app.getResources().getDisplayMetrics().density);
    }

    // ---- ActivityLifecycleCallbacks ----

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (!isHostActivity(activity)) {
            resumedActivity = new WeakReference<>(null);
            return;
        }
        resumedActivity = new WeakReference<>(activity);
        TextView banner = findOrInstallBanner(activity);
        if (banner != null) {
            applyState(banner, false);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (resumedActivity.get() == activity) {
            resumedActivity = new WeakReference<>(null);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
