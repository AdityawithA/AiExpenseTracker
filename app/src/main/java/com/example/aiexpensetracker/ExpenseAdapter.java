package com.example.aiexpensetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> expenseList;

    public ExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Expense expense = expenseList.get(position);

        holder.amountText.setText("₹ " + expense.getAmount());
        holder.descriptionText.setText(expense.getDescription());
        holder.categoryText.setText("Category: " + expense.getCategory());
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView amountText, descriptionText, categoryText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            amountText = itemView.findViewById(R.id.amountText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            categoryText = itemView.findViewById(R.id.categoryText);
        }
    }
}