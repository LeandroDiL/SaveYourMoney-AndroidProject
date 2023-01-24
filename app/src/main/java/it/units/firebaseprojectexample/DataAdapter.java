package it.units.firebaseprojectexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import it.units.firebaseprojectexample.utils.Data;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.Viewholder> {

    private Context context;
    private ArrayList<Data> dataArrayList;

    public DataAdapter(Context context, ArrayList<Data> datalArrayList) {
        this.context = context;
        this.dataArrayList = datalArrayList;
    }

    @NonNull
    @Override
    public DataAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataAdapter.Viewholder holder, int position) {
        Data model = dataArrayList.get(position);
        holder.category.setText(model.getCategory());
        holder.amount.setText("" + model.getAmount() + "€");
        holder.date.setText("" + model.getDate());
        holder.type.setText("" + model.getId());
    }

    @Override
    public int getItemCount() {
        return dataArrayList.size();
    }


    public class Viewholder extends RecyclerView.ViewHolder {
        private final TextView category, amount, date, type;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date);
            category = itemView.findViewById(R.id.category);
            type = itemView.findViewById(R.id.type);

        }
    }
}
