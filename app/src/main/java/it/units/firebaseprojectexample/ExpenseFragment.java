package it.units.firebaseprojectexample;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import it.units.firebaseprojectexample.utils.Data;


public class ExpenseFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;

    private Button expenseButton;
    private EditText expenseAmountText;

    private PieChart pieChart;

    private ArrayList<PieEntry> entries;
    private String newItem;

    private List<String> category;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myview = inflater.inflate(R.layout.fragment_expense, container, false);
        mAuth = FirebaseAuth.getInstance();

        category = Arrays.asList("Food", "Fuel", "Shopping", "Fun", "Bill", "Health", "Learning", "Other");
        final Spinner spinner = myview.findViewById(R.id.spinner);

        SpinnerAdapter adapter = new SpinnerAdapter(getActivity().getApplicationContext(), category);
        adapter.setDropDownViewResource(R.layout.dropdown_item);

        spinner.setAdapter(adapter);


        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mAuth != null) {
            String uid = mUser.getUid();

            mExpenseDatabase = FirebaseDatabase.getInstance("https://fir-projectexample-158c2-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("ExpenseData").child(uid);
        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                newItem = category.get(i);
                Toast.makeText(getActivity().getApplicationContext(), newItem + " selected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        pieChart = myview.findViewById(R.id.pieChart);
        expenseButton = myview.findViewById(R.id.button);


        mExpenseDatabase.addValueEventListener(new ValueEventListener() {

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setupPieChart();
                getPieEntries(dataSnapshot);

                ArrayList<Integer> colors = new ArrayList<>();
                for (int color : ColorTemplate.MATERIAL_COLORS) {
                    colors.add(color);
                }

                for (int color : ColorTemplate.VORDIPLOM_COLORS) {
                    colors.add(color);
                }

                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(colors);

                PieData pieData = new PieData(dataSet);
                pieData.setDrawValues(true);
                pieData.setValueFormatter(new PercentFormatter(pieChart));
                pieData.setValueTextSize(12f);
                pieData.setValueTextColor(Color.BLACK);

                pieChart.setData(pieData);

                pieChart.invalidate();

                pieChart.animateY(1400, Easing.EaseInOutQuad);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        expenseButton.setOnClickListener(view -> {
            expenseAmountText = myview.findViewById(R.id.expense);
            if (expenseAmountText.getText().toString().trim().equals("")) {
                Toast.makeText(getActivity(), "You need to insert an amount...", Toast.LENGTH_SHORT).show();
                return;
            }
            String amount = expenseAmountText.getText().toString().trim();

            if (TextUtils.isEmpty(amount)) {
                expenseAmountText.setError("Required Field..");
            }
            int amountIntValue = Integer.parseInt(amount);

            if (mAuth.getCurrentUser() != null) {
                String id = mExpenseDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(amountIntValue, id, mDate, newItem);


                mExpenseDatabase.child(id).setValue(data);
                Toast.makeText(getActivity(), "Data ADDED", Toast.LENGTH_SHORT).show();
            }
            expenseAmountText.setText("");
            expenseAmountText.setHint("€");
            expenseAmountText.onEditorAction(EditorInfo.IME_ACTION_DONE);


        });

        return myview;
    }

    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(35f);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Categories");
        pieChart.setCenterTextSize(18);
        pieChart.getDescription().setEnabled(false);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setTextColor(Color.GRAY);
        l.setDrawInside(false);
        l.setEnabled(true);
    }

    private void getPieEntries(DataSnapshot dataSnapshot) {
        int totalvalue = 0;
        entries = new ArrayList<>();
        float a = 1f;
        int a1 = 1;
        float[] categoryAmount = new float[8];

        for (DataSnapshot mysnapshot : dataSnapshot.getChildren()) {
            Data data = mysnapshot.getValue(Data.class);
            totalvalue -= data.getAmount();
            String selectedCategory = data.getCategory();
            for (int i = 0; i < categoryAmount.length; i++) {
                if (selectedCategory.equals(category.get(i))) {
                    categoryAmount[i] += data.getAmount();
                }
            }

            a = a + 1;
            a1 = a1 + 1;
        }
        for (int i = 0; i < categoryAmount.length; i++) {
            if (categoryAmount[i] != 0) {
                entries.add(new PieEntry(categoryAmount[i], category.get(i), a));
            }
        }

    }
}