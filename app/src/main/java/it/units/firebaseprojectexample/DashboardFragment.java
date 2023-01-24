package it.units.firebaseprojectexample;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;

import it.units.firebaseprojectexample.utils.Data;


public class DashboardFragment extends Fragment {


    LineChart lineChart;
    ArrayList<Entry> incomeLineEntries;
    ArrayList<Entry> expenseLineEntries;
    ArrayList<Entry> tempIncomeLineEntries;
    ArrayList<Entry> tempExpenseLineEntries;
    ArrayList<ILineDataSet> iLineDataSetArrayList;
    LineData lineData;
    LineDataSet lineDataSet2;
    LineDataSet lineDataSet1;

    final ArrayList<String> dashboardDate = new ArrayList<>();
    final float[][] totalIncomeDayAmount = new float[31][12];
    final float[][] totalExpenseDayAmount = new float[31][12];
    private static final DateFormatSymbols dfs = new DateFormatSymbols();
    private final String[] shortLocalMonths = dfs.getShortMonths();
    private ArrayList<PieEntry> entries;
    private PieChart pieChart;
    private int incomePieEntry;
    private int expensePieEntry;

    private TextView totalBalanceResult;
    private TextView totalIncome;
    private TextView totalExpense;

    static int totalsumexpense = 0;
    static int totalsumincome = 0;
    static int balance;

    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;


    private RecyclerView incomeRecyclerView;
    private RecyclerView expenseRecyclerView;

    public ArrayList<Data> incomeDataArrayList = new ArrayList<>();
    public ArrayList<Data> expenseDataArrayList = new ArrayList<>();

    private ArrayList<Integer> tempAmountExpense = new ArrayList<>();
    private ArrayList<String> tempIdExpense = new ArrayList<>();
    private ArrayList<String> tempCategoryExpense = new ArrayList<>();
    private ArrayList<String> tempDateExpense = new ArrayList<>();

    private ArrayList<Integer> tempAmountIncome = new ArrayList<>();
    private ArrayList<String> tempIdIncome = new ArrayList<>();
    private ArrayList<String> tempCategoryIncome = new ArrayList<>();
    private ArrayList<String> tempDateIncome = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myview = inflater.inflate(R.layout.fragment_dashboard, container, false);

        expenseRecyclerView = myview.findViewById(R.id.my_expense_recycler_view);
        incomeRecyclerView = myview.findViewById(R.id.my_income_recycler_view);

        totalBalanceResult = myview.findViewById(R.id.totalBalance);
        totalExpense = myview.findViewById(R.id.totalExpense);
        totalIncome = myview.findViewById(R.id.totalIncome);

        Drawable drawable1 = ContextCompat.getDrawable(getContext(), R.drawable.income_gradient_shape);
        Drawable drawable2 = ContextCompat.getDrawable(getContext(), R.drawable.expense_gradient_shape);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser mUser = mAuth.getCurrentUser();

        if (mAuth.getCurrentUser() != null) {

            String uid = mUser.getUid();

            mIncomeDatabase = FirebaseDatabase.getInstance("https://fir-projectexample-158c2-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("IncomeData").child(uid);
            mExpenseDatabase = FirebaseDatabase.getInstance("https://fir-projectexample-158c2-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("ExpenseData").child(uid);

        }


        lineChart = myview.findViewById(R.id.linechart);
        pieChart = myview.findViewById(R.id.dashboardPiechart);
        entries = new ArrayList<>();
        incomeLineEntries = new ArrayList<>();
        expenseLineEntries = new ArrayList<>();
        tempIncomeLineEntries = new ArrayList<>();
        tempExpenseLineEntries = new ArrayList<>();
        iLineDataSetArrayList = new ArrayList<>();

        initLineChart();
        setupPieChart();

        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                DataAdapter expenseAdapter = new DataAdapter(getContext(), expenseDataArrayList);
                LinearLayoutManager expenseLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                expenseRecyclerView.setLayoutManager(expenseLinearLayoutManager);
                expenseRecyclerView.setAdapter(expenseAdapter);

                getExpenseCardViews(snapshot);

                totalsumexpense = 0;
                for (DataSnapshot mysnap : snapshot.getChildren()) {
                    Data data = mysnap.getValue(Data.class);
                    totalsumexpense += data.getAmount();
                }
                balance = totalsumincome - totalsumexpense;

                totalBalanceResult.setText("" + balance + "€");
                totalExpense.setText("" + totalsumexpense + "€");

                expensePieEntry = totalsumexpense;

                entries.add(new PieEntry(expensePieEntry, "Expense"));

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

                getMonthlyExpenseLineChartEntries(snapshot);

                lineDataSet1 = new LineDataSet(expenseLineEntries, "Expense");
                lineDataSet1.setLineWidth(0.0f);
                lineDataSet1.setColor(Color.parseColor("#FF4100"));
                lineDataSet1.setCircleColor(R.color.white);
                lineDataSet1.setHighLightColor(Color.RED);
                lineDataSet1.setDrawValues(false);
                lineDataSet1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                lineDataSet1.setCubicIntensity(0.5f);
                lineDataSet1.setDrawFilled(true);
                lineDataSet1.setFillAlpha(80);
                lineDataSet1.setFillDrawable(drawable2);
                Legend legend = lineChart.getLegend();
                legend.setEnabled(false);
                lineDataSet1.setDrawCircles(false);
                iLineDataSetArrayList.add(lineDataSet1);
                lineData = new LineData(iLineDataSetArrayList);
                lineData.setValueTextSize(13f);
                lineData.setValueTextColor(Color.GRAY);
                lineChart.getAxisLeft().setDrawGridLines(false);
                lineChart.getXAxis().setDrawGridLines(false);
                lineChart.getAxisRight().setDrawGridLines(false);
                lineChart.setData(lineData);
                lineChart.animateY(1000);
                lineChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                DataAdapter incomeAdapter = new DataAdapter(getContext(), incomeDataArrayList);
                LinearLayoutManager incomeLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                incomeRecyclerView.setLayoutManager(incomeLinearLayoutManager);
                incomeRecyclerView.setAdapter(incomeAdapter);

                getIncomeCardViews(snapshot);

                totalsumincome = 0;
                for (DataSnapshot mysnap : snapshot.getChildren()) {

                    Data data = mysnap.getValue(Data.class);
                    totalsumincome += data.getAmount();

                }
                balance = totalsumincome - totalsumexpense;

                totalBalanceResult.setText("" + balance + "€");
                totalIncome.setText("" + totalsumincome + "€");

                incomePieEntry = totalsumincome;


                entries.add(new PieEntry(incomePieEntry, "Income"));

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

                getMonthlyIncomeLineChartEntries(snapshot);

                lineDataSet2 = new LineDataSet(incomeLineEntries, "Income");
                lineDataSet2.setLineWidth(0.4f);
                lineDataSet2.setColor(Color.parseColor("#00FF82"));
                lineDataSet2.setCircleColor(R.color.white);
                lineDataSet2.setHighLightColor(Color.RED);
                lineDataSet2.setDrawValues(false);
                lineDataSet2.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                lineDataSet2.setCubicIntensity(0.5f);
                lineDataSet2.setDrawFilled(true);
                lineDataSet2.setFillAlpha(80);
                lineDataSet2.setFillDrawable(drawable1);
                Legend legend = lineChart.getLegend();
                legend.setEnabled(true);
                legend.setTextColor(Color.GRAY);
                lineDataSet2.setDrawCircles(false);
                iLineDataSetArrayList.add(lineDataSet2);
                lineData = new LineData(iLineDataSetArrayList);
                lineData.setValueTextSize(13f);
                lineData.setValueTextColor(Color.GRAY);
                lineChart.getAxisLeft().setDrawGridLines(false);
                lineChart.getXAxis().setDrawGridLines(false);
                lineChart.getAxisRight().setDrawGridLines(false);
                lineChart.setData(lineData);
                lineChart.animateY(1000);

                Description description = new Description();
                description.setText("");
                lineChart.setDescription(description);

                lineChart.invalidate();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return myview;
    }


    private void initLineChart() {

        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setMaxHighlightDistance(200);
        XAxis xLabels = lineChart.getXAxis();
        xLabels.setAxisLineColor(Color.TRANSPARENT);
        xLabels.setTextColor(Color.GRAY);
        lineChart.getAxisLeft().setTextColor(Color.GRAY);
    }


    public void getMonthlyExpenseLineChartEntries(DataSnapshot dataSnapshot) {

        if (dataSnapshot.exists()) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                Data data = ds.getValue(Data.class);
                dashboardDate.add(data.getDate().substring(0, 7));
                getExpenseDateAmount(data);
            }
        }

        for (int j = 0; j < 12; j++) {
            if (shortLocalMonths[j].equals(getCurrentMonth())) {
                for (int i = 0; i < 31; i++) {
                    expenseLineEntries.add(new Entry(i + 1, totalExpenseDayAmount[i][j]));
                }
            }
        }
        for (int j = 0; j < 12; j++) {
            for (int i = 0; i < 31; i++) {
                totalExpenseDayAmount[i][j] = 0;
            }
        }
    }

    public void getMonthlyIncomeLineChartEntries(DataSnapshot dataSnapshot) {

        if (dataSnapshot.exists()) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                Data data = ds.getValue(Data.class);
                dashboardDate.add(data.getDate().substring(0, 7));
                getIncomeDateAmount(data);
            }
        }

        for (int j = 0; j < 12; j++) {
            if (shortLocalMonths[j].equals(getCurrentMonth())) {
                for (int i = 0; i < 31; i++) {
                    incomeLineEntries.add(new Entry(i + 1, totalIncomeDayAmount[i][j]));
                }
            }
        }
        for (int j = 0; j < 12; j++) {
            for (int i = 0; i < 31; i++) {
                totalIncomeDayAmount[i][j] = 0;
            }
        }

    }


    public static String getCurrentMonth() {
        Formatter fmt;
        Calendar cal = Calendar.getInstance();
        fmt = new Formatter();
        fmt.format("%tb", cal);
        return String.valueOf(fmt);
    }


    public void getExpenseDateAmount(Data data) {
        for (int j = 0; j < 12; j++) {
            if (shortLocalMonths[j].equals(data.getDate().substring(2, 6).trim())) {
                for (int i = 0; i < 31; i++) {
                    if (i < 9) {
                        String firstTeenDay = String.valueOf(i + 1);
                        if (firstTeenDay.equals(data.getDate().substring(0, 2).trim())) {
                            totalExpenseDayAmount[i][j] -= data.getAmount();
                        }
                    }
                    if (i >= 9) {
                        if (i + 1 == Integer.parseInt(data.getDate().substring(0, 2).trim())) {
                            totalExpenseDayAmount[i][j] -= data.getAmount();
                        }
                    }
                }
            }
        }


    }

    public void getIncomeDateAmount(Data data) {
        System.out.println(data.getDate().substring(2, 6).trim());
        System.out.println(data.getDate().substring(3, 5));
        for (int j = 0; j < 12; j++) {
            if (shortLocalMonths[j].equals(data.getDate().substring(2, 6).trim())) {
                for (int i = 0; i < 31; i++) {
                    if (i < 9) {
                        String firstTeenDay = String.valueOf(i + 1);
                        if (firstTeenDay.equals(data.getDate().substring(0, 2).trim())) {
                            totalIncomeDayAmount[i][j] += data.getAmount();
                        }
                    }
                    if (i >= 9) {
                        if (i + 1 == Integer.parseInt(data.getDate().substring(0, 2).trim())) {
                            totalIncomeDayAmount[i][j] += data.getAmount();
                        }
                    }
                }
            }
        }

    }


    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(10);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);

        Legend l = pieChart.getLegend();
        l.setEnabled(false);
    }


    public void getIncomeCardViews(DataSnapshot dataSnapshot) {

        tempAmountIncome = new ArrayList<>();
        tempIdIncome = new ArrayList<>();
        tempCategoryIncome = new ArrayList<>();
        tempDateIncome = new ArrayList<>();

        incomeDataArrayList = new ArrayList<>();

        if (dataSnapshot.exists()) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                Data data = ds.getValue(Data.class);
                tempAmountIncome.add(data.getAmount());
                tempCategoryIncome.add(data.getCategory());
                tempDateIncome.add(data.getDate());
                tempIdIncome.add(data.getId());
            }
        }
        if (tempAmountIncome.size() != 0) {
            for (int i = tempAmountIncome.size() - 1; i > -1; i--) {
                incomeDataArrayList.add(new Data(tempAmountIncome.get(i), "Income", tempCategoryIncome.get(i), tempDateIncome.get(i)));
            }
        }
    }

    public void getExpenseCardViews(DataSnapshot dataSnapshot) {
        tempAmountExpense = new ArrayList<>();
        tempIdExpense = new ArrayList<>();
        tempCategoryExpense = new ArrayList<>();
        tempDateExpense = new ArrayList<>();

        expenseDataArrayList = new ArrayList<>();

        if (dataSnapshot.exists()) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                Data data = ds.getValue(Data.class);
                tempAmountExpense.add(-(data.getAmount()));
                tempCategoryExpense.add(data.getCategory());
                tempDateExpense.add(data.getDate());
                tempIdExpense.add(data.getId());
            }
        }
        if (tempAmountExpense.size() != 0) {
            for (int i = tempAmountExpense.size() - 1; i > -1; i--) {
                expenseDataArrayList.add(new Data(tempAmountExpense.get(i), "Expense", tempCategoryExpense.get(i), tempDateExpense.get(i)));
            }
        }
    }
}