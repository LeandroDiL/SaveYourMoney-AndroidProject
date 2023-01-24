package it.units.firebaseprojectexample;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {

    Context context;
    List<String> statesList;

    public SpinnerAdapter(Context context, List<String> states) {
        super(context, R.layout.selected_item, states);
        this.context = context;
        this.statesList = states;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.dropdown_item, parent, false);

        TextView state = row.findViewById(R.id.text);
        ImageView flag = row.findViewById(R.id.img);

        Resources res = context.getResources();
        String drawableName = statesList.get(position).toLowerCase(); // tx
        int resId = res.getIdentifier(drawableName, "drawable", context.getPackageName());
        Drawable drawable = res.getDrawable(resId);

        state.setText(statesList.get(position));
        flag.setImageDrawable(drawable);

        return row;
    }

}