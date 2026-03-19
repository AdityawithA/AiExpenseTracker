package com.example.aiexpensetracker;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText amountInput, descriptionInput;
    private Button saveButton, exportButton;
    private TextView totalText, foodTotalText, transportTotalText, entertainmentTotalText;
    private RecyclerView recyclerView;
    private PieChart pieChart;
    private Switch darkModeSwitch;

    private ExpenseAdapter adapter;
    private List<Expense> expenseList = new ArrayList<>();
    private AppDatabase database;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "expense_db_v3"
                ).fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        amountInput = findViewById(R.id.amountInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        saveButton = findViewById(R.id.saveButton);
        exportButton = findViewById(R.id.exportButton);
        totalText = findViewById(R.id.totalText);
        foodTotalText = findViewById(R.id.foodTotalText);
        transportTotalText = findViewById(R.id.transportTotalText);
        entertainmentTotalText = findViewById(R.id.entertainmentTotalText);
        recyclerView = findViewById(R.id.recyclerView);
        pieChart = findViewById(R.id.pieChart);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);

        adapter = new ExpenseAdapter(expenseList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        darkModeSwitch.setChecked(isDark);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        saveButton.setOnClickListener(v -> saveExpense());
        exportButton.setOnClickListener(v -> exportToCSV());

        enableSwipeToDelete();
        loadExpenses();
    }

    private void saveExpense() {

        String amountStr = amountInput.getText().toString();
        String description = descriptionInput.getText().toString();

        if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String category = classify(description);

        Expense expense = new Expense(
                amount,
                description,
                category,
                System.currentTimeMillis(),
                "localUser"
        );

        database.expenseDao().insert(expense);

        amountInput.setText("");
        descriptionInput.setText("");

        loadExpenses();
    }

    private void loadExpenses() {

        expenseList.clear();
        expenseList.addAll(database.expenseDao().getAllExpenses());
        adapter.notifyDataSetChanged();

        updateSummary();
        updatePieChart();
    }

    private void updateSummary() {

        double total = 0, food = 0, transport = 0, entertainment = 0;

        for (Expense e : expenseList) {

            total += e.getAmount();

            switch (e.getCategory()) {
                case "Food":
                    food += e.getAmount();
                    break;
                case "Transport":
                    transport += e.getAmount();
                    break;
                case "Entertainment":
                    entertainment += e.getAmount();
                    break;
            }
        }

        totalText.setText("Total: ₹" + total);
        foodTotalText.setText("Food: ₹" + food);
        transportTotalText.setText("Transport: ₹" + transport);
        entertainmentTotalText.setText("Entertainment: ₹" + entertainment);
    }

    private void updatePieChart() {

        float food = 0, transport = 0, entertainment = 0;

        for (Expense e : expenseList) {

            switch (e.getCategory()) {
                case "Food":
                    food += e.getAmount();
                    break;
                case "Transport":
                    transport += e.getAmount();
                    break;
                case "Entertainment":
                    entertainment += e.getAmount();
                    break;
            }
        }

        List<PieEntry> entries = new ArrayList<>();

        if (food > 0) entries.add(new PieEntry(food, "Food"));
        if (transport > 0) entries.add(new PieEntry(transport, "Transport"));
        if (entertainment > 0) entries.add(new PieEntry(entertainment, "Entertainment"));

        PieDataSet dataSet = new PieDataSet(entries, "Expenses");
        dataSet.setColors(Color.GREEN, Color.YELLOW, Color.RED);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(8f);

        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(16f);
        pieChart.animateY(1200);
        pieChart.invalidate();
    }

    private void enableSwipeToDelete() {

        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                        int position = viewHolder.getAdapterPosition();
                        Expense expense = expenseList.get(position);

                        database.expenseDao().delete(expense);
                        loadExpenses();
                    }
                };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private void exportToCSV() {

        try {

            File file = new File(getExternalFilesDir(null), "expenses.csv");
            FileWriter writer = new FileWriter(file);

            writer.append("Amount,Description,Category\n");

            for (Expense e : expenseList) {
                writer.append(e.getAmount() + ","
                        + e.getDescription() + ","
                        + e.getCategory() + "\n");
            }

            writer.flush();
            writer.close();

            Toast.makeText(this,
                    "CSV Exported to: " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private String classify(String desc) {

        desc = desc.toLowerCase();

        if (desc.contains("pizza") || desc.contains("food"))
            return "Food";

        if (desc.contains("uber") || desc.contains("fuel"))
            return "Transport";

        if (desc.contains("netflix") || desc.contains("movie"))
            return "Entertainment";

        return "Other";
    }
}