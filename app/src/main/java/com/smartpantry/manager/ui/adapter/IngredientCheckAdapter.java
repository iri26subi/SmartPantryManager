package com.smartpantry.manager.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.manager.R;
import com.smartpantry.manager.matching.IngredientCheck;
import com.smartpantry.manager.model.RecipeIngredient;
import com.smartpantry.manager.util.QuantityFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows each ingredient of a recipe on the detail screen with a tick or a
 * cross, depending on whether the pantry covers it.
 */
public class IngredientCheckAdapter extends RecyclerView.Adapter<IngredientCheckAdapter.ViewHolder> {

    private final List<IngredientCheck> checks = new ArrayList<>();

    public IngredientCheckAdapter(List<IngredientCheck> checks) {
        this.checks.addAll(checks);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IngredientCheck check = checks.get(position);
        RecipeIngredient ingredient = check.getIngredient();
        Context context = holder.itemView.getContext();

        holder.ingredientText.setText(context.getString(R.string.ingredient_line,
                QuantityFormatter.format(ingredient.getQuantity(), ingredient.getUnit()), ingredient.getName()));

        String available = QuantityFormatter.format(check.getAvailableQuantity(), ingredient.getUnit());
        switch (check.getStatus()) {
            case AVAILABLE:
                holder.status.setText(context.getString(R.string.ingredient_have, available));
                break;
            case INSUFFICIENT:
                holder.status.setText(context.getString(R.string.ingredient_short, available));
                break;
            case INCOMPATIBLE_UNIT:
                holder.status.setText(R.string.ingredient_incompatible);
                break;
            default:
                holder.status.setText(R.string.ingredient_missing);
                break;
        }

        boolean ok = check.isSatisfied();
        holder.icon.setImageResource(ok ? R.drawable.ic_check_circle : R.drawable.ic_cancel);
        holder.icon.setColorFilter(ContextCompat.getColor(context, ok ? R.color.status_ok : R.color.status_bad));
        holder.icon.setContentDescription(context.getString(ok ? R.string.cd_available : R.string.cd_unavailable));
    }

    @Override
    public int getItemCount() {
        return checks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView ingredientText;
        final TextView status;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.statusIcon);
            ingredientText = itemView.findViewById(R.id.ingredientText);
            status = itemView.findViewById(R.id.ingredientStatus);
        }
    }
}
