package com.smartpantry.manager.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.manager.R;
import com.smartpantry.manager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter for the Suggested Recipes list.
 * Each row shows a recipe and, in the "Almost there" tab, what is missing.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    /** A recipe plus an optional explanation of the missing ingredient. */
    public static class Row {
        final Recipe recipe;
        final String missingText; // null for strict matches

        public Row(Recipe recipe, String missingText) {
            this.recipe = recipe;
            this.missingText = missingText;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRows(List<Row> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Row row = rows.get(position);
        Recipe recipe = row.recipe;

        holder.name.setText(recipe.getName());
        holder.description.setText(recipe.getDescription());
        holder.meta.setText(holder.itemView.getContext().getString(R.string.recipe_meta,
                recipe.getIngredients().size(), recipe.getPrepMinutes(), recipe.getServings()));

        if (row.missingText != null) {
            holder.missing.setVisibility(View.VISIBLE);
            holder.missing.setText(holder.itemView.getContext().getString(R.string.missing_line, row.missingText));
        } else {
            holder.missing.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView description;
        final TextView meta;
        final TextView missing;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipeName);
            description = itemView.findViewById(R.id.recipeDescription);
            meta = itemView.findViewById(R.id.recipeMeta);
            missing = itemView.findViewById(R.id.recipeMissing);
        }
    }
}
