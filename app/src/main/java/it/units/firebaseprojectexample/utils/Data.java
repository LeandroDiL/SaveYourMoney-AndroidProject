package it.units.firebaseprojectexample.utils;

//import androidx.recyclerview.widget.RecyclerView;

public class Data {
    //extends RecyclerView.ViewHolder
    private int amount;
    private String id;
    private String category;
    private String date;

    public Data(int amount, String id, String date, String category)
    {
        this.amount = amount;
        this.id = id;
        this.date = date;
        this.category = category;
    }

    public Data(){}

    public String getCategory() {return category;}

    public void setCategory(String category) {this.category = category;}

    public int getAmount(){return amount;}

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}