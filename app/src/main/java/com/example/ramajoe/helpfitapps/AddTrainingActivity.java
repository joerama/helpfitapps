package com.example.ramajoe.helpfitapps;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.ramajoe.helpfitapps.Model.Training;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import dmax.dialog.SpotsDialog;

public class AddTrainingActivity extends AppCompatActivity implements  DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener{
    private Button createBtn;
    private TextInputEditText startDate, timeStart, timeFinished, trainerNotes;
    private MaterialEditText trainingTitle, trainingFee, maxParticipant;
    private RadioButton RBPersonal, RBGroup;
    private Spinner spn;
    private String trainingDate, trainingTime;
    private CardView personalCard, groupCard;
    private int indicate;

    private FirebaseDatabase database;
    private DatabaseReference trainingDB;
    private DatabaseReference users;
    //private DatabaseReference users;
    private FirebaseAuth auth;
    private Training newTraining;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_training);


        //Initiating material
        createBtn = findViewById(R.id.createBtn);
        startDate = findViewById(R.id.NewStartDate);
        timeStart = findViewById(R.id.NewStartTime);
        timeFinished = findViewById(R.id.NewFinishTime);
        trainingTitle = findViewById(R.id.trainingTitle);
        trainingFee = findViewById(R.id.trainingFee);
        maxParticipant = findViewById(R.id.maxParticipant);
        trainerNotes = findViewById(R.id.trainerNotes);
        RBPersonal = findViewById(R.id.RBPersonal);
        RBGroup = findViewById(R.id.RBGroup);
        spn = findViewById(R.id.spinner);
        personalCard = findViewById(R.id.PersonalCard);
        groupCard = findViewById(R.id.GroupCard);

        trainingDate = "";
        trainingTime = "";
        indicate = 0;

        database = FirebaseDatabase.getInstance();
        trainingDB = database.getReference("Training");
        //users = database.getReference("Users");
        users = database.getReference("UserData");
        auth = FirebaseAuth.getInstance();


        //Initiating Spinner
        String[] classType = new String[]{"---- Choose Class Type ----","MMA", "Dance", "Sport","Yoga"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout,classType);
        spn.setAdapter(adapter);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDate(0);
            }
        });

        timeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTime(0);
            }
        });

        timeFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTime(1);
            }
        });


        RBPersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                personalCard.setVisibility(View.VISIBLE);
                groupCard.setVisibility(View.GONE);
            }
        });

        RBGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                personalCard.setVisibility(View.GONE);
                groupCard.setVisibility(View.VISIBLE);
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTraining();
            }
        });

    }

    private void createTraining() {
        final SpotsDialog waitingDialog = new SpotsDialog(AddTrainingActivity.this);
        final String type;
        if(RBPersonal.isChecked()){ type = "personalT";}
        else type = "groupT";
        waitingDialog.show();

        //Check validation, baru nama, nanti sisanya
        if(TextUtils.isEmpty(trainingTitle.getText().toString()) ||
                TextUtils.isEmpty(trainingFee.getText().toString()) ||
                startDate.getText().toString().equals("")||
                timeStart.getText().toString().equals("")||
                timeFinished.getText().toString().equals("")||
                (!RBPersonal.isChecked() && !RBGroup.isChecked()) ||
                (RBPersonal.isChecked() && TextUtils.isEmpty(trainerNotes.getText().toString())) ||
                (RBGroup.isChecked() && (maxParticipant.getText().toString().equals("")))

                ){
            Toast.makeText(AddTrainingActivity.this, "Please fill up all data !!", Toast.LENGTH_SHORT).show();
            waitingDialog.dismiss();
        }


        else {
            String key = trainingDB.push().getKey();


            if(RBPersonal.isChecked()){
                String participant = "0,1";
                        newTraining = new Training(key,trainingTitle.getText().toString(),trainingDate,trainingTime,
                        trainingFee.getText().toString(),"Open","Personal","",trainerNotes.getText().toString(),participant);
            }
            else{
                String participant = "0,"+maxParticipant.getText().toString();
                newTraining = new Training(key,trainingTitle.getText().toString(),trainingDate,trainingTime,
                        trainingFee.getText().toString(),"Open","Group",spn.getSelectedItem().toString(),"",participant);
            }

            trainingDB.child(key).setValue(newTraining);
            trainingDB.child(key).child("trainingOwner").setValue(auth.getCurrentUser().getUid().toString());
            users.child(auth.getCurrentUser().getUid().toString()).child("trainingList").child(key).setValue("Open");


            Toast.makeText(this, "Success Create Training", Toast.LENGTH_SHORT).show();
            waitingDialog.dismiss();
            Intent intent = new Intent(AddTrainingActivity.this, HomeActivity.class);
            startActivity(intent);

        }


    }

    private void openTime(int i) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(AddTrainingActivity.this
                ,now.get(Calendar.HOUR_OF_DAY)
                ,now.get(Calendar.MINUTE)
                ,true);
        timePickerDialog.show(getFragmentManager(),"TimePicker");
        indicate = i;
    }

    private void openDate(int i) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                AddTrainingActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getFragmentManager(),"Datepickerdialog");
        indicate = i;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = String.format("%02d/%02d/%02d",dayOfMonth,(monthOfYear+1),year%100);
        if(indicate==0){startDate.setText(date);}
        trainingDate = startDate.getText().toString();
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        String time = String.format("%02d.%02d",hourOfDay,minute);
        if(indicate==0) {timeStart.setText(time);}
        else {timeFinished.setText(time);}
        trainingTime = timeStart.getText().toString()+","+timeFinished.getText().toString();
    }


}
