package com.example.aiexpensetracker;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {Expense.class},
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExpenseDao expenseDao();
}