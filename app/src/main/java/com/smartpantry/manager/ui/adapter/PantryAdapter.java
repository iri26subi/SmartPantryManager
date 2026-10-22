package com.smartpantry.manager.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.smartpantry.manager.R;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.util.ExpiryUtils;
import com.smartpantry.manager.util.QuantityFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter that binds PantryItem objects (loaded from
 * SQLite) to the item_pantry card layout.
 *
 * Click events are passed back to the Activity through the {@link Listener}
 * interface, so the adapter only deals with displaying data.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /** Callbacks implemented by the Activity that hosts the list. */
    public interface Listener {
        void onEditItem(PantryItem item);

        void onDeleteItem(PantryItem item);
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean expiryAlertsEnabled;
    private int expiryWindowDays;
    private LocalDate today = LocalDate.now();

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replaces the displayed items. The list is small, so a full refresh is fine. */
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<PantryItem> newItems, boolean alertsEnabled, int windowDays, LocalDate today) {
        items.clear();
        items.addAll(newItems);
        this.expiryAlertsEnabled = alertsEnabled;
        this.expiryWindowDays = windowDays;
        this.today = today;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.name.setText(item.getName());
        holder.quantity.setText(QuantityFormatter.format(item.getQuantity(), item.getUnit()));
        holder.expiry.setText(describeExpiry(context, item));
        bindBadge(holder, item);

        holder.itemView.setOnClickListener(v -> listener.onEditItem(item));
        holder.editButton.setOnClickListener(v -> listener.onEditItem(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteItem(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String describeExpiry(Context context, PantryItem item) {
        LocalDate expiry = ExpiryUtils.parse(item.getExpiryDate());
        if (expiry == null) {
            return context.getString(R.string.expiry_none);
        }
        long days = ExpiryUtils.daysUntilExpiry(item, today);
        String date = expiry.format(DATE_FORMAT);
        if (days < 0) {
            return context.getString(R.string.expired_on, date);
        } else if (days == 0) {
            return context.getString(R.string.expiry_today);
        } else if (days == 1) {
            return context.getString(R.string.expiry_tomorrow, date);
        }
        return context.getString(R.string.expiry_in_days, (int) days, date);
    }

    /** Shows an "Expired" or "Expiring soon" badge when expiry alerts are switched on. */
    private void bindBadge(ViewHolder holder, PantryItem item) {
        if (expiryAlertsEnabled && ExpiryUtils.isExpired(item, today)) {
            holder.badge.setVisibility(View.VISIBLE);
            holder.badge.setText(R.string.badge_expired);
            holder.badge.setBackgroundResource(R.drawable.bg_badge_error);
            holder.badge.setTextColor(MaterialColors.getColor(holder.badge,
                    com.google.android.material.R.attr.colorOnErrorContainer));
        } else if (expiryAlertsEnabled && ExpiryUtils.isExpiringSoon(item, today, expiryWindowDays)) {
            holder.badge.setVisibility(View.VISIBLE);
            holder.badge.setText(R.string.badge_expiring_soon);
            holder.badge.setBackgroundResource(R.drawable.bg_badge_warning);
            holder.badge.setTextColor(ContextCompat.getColor(holder.badge.getContext(),
                    R.color.on_warning_container));
        } else {
            holder.badge.setVisibility(View.GONE);
        }
    }

    /** Caches the views of one row so findViewById runs once per row, not on every bind. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final TextView badge;
        final ImageButton editButton;
        final ImageButton deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            quantity = itemView.findViewById(R.id.itemQuantity);
            expiry = itemView.findViewById(R.id.itemExpiry);
            badge = itemView.findViewById(R.id.expiryBadge);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
