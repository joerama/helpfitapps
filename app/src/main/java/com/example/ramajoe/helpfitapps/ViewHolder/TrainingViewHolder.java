package com.example.ramajoe.helpfitapps.ViewHolder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ramajoe.helpfitapps.Interface.TrainingClickListener;
import com.example.ramajoe.helpfitapps.R;



public class TrainingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView title, date, time, fee, status, mode, classType, notes, maxParticipant;
    public Button joinBtn;

    private TrainingClickListener TCL;

    public TrainingViewHolder(View itemView) {
        super(itemView);
        //default
        title = itemView.findViewById(R.id.itemTrainingTitle);
        date = itemView.findViewById(R.id.itemTrainingDate);
        time = itemView.findViewById(R.id.itemTrainingTime);
        fee = itemView.findViewById(R.id.itemTrainingFee);
        status = itemView.findViewById(R.id.itemTrainingStatus);

        joinBtn = itemView.findViewById(R.id.joinBtn);
        itemView.setOnClickListener(this);

    }

    public void setTrainingClickListener(TrainingClickListener trainingClickListener){
        this.TCL = trainingClickListener;
    }

    @Override
    public void onClick(View view) {
        TCL.onClick(view,getAdapterPosition(),false);
    }
}
