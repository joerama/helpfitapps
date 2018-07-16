package com.example.ramajoe.helpfitapps;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ramajoe.helpfitapps.Common.Common;
import com.example.ramajoe.helpfitapps.Model.Training;
import com.example.ramajoe.helpfitapps.ViewHolder.TrainingViewHolder;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private DatabaseReference training;
    private DatabaseReference users;
    private RecyclerView trainingRV;
    private FirebaseRecyclerAdapter<Training,TrainingHolder> firebaseRecyclerAdapter;
    private TextView txtCurrentUser, txtUserEmail;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        users = FirebaseDatabase.getInstance().getReference().child("UserData");
        training = FirebaseDatabase.getInstance().getReference().child("Training");
        training.keepSynced(true);

        trainingRV = (RecyclerView) findViewById(R.id.trainingRecycler);
        trainingRV.setHasFixedSize(true);
        trainingRV.setLayoutManager(new LinearLayoutManager(this));


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent register = new Intent(HomeActivity.this, AddTrainingActivity.class);
                startActivity(register);
            }
        });

        if(Common.currentUser.getType().toString().equals("member")){
            fab.setVisibility(View.GONE);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        View navigationHeaderView = navigationView.getHeaderView(0);
        txtCurrentUser = navigationHeaderView.findViewById(R.id.userName);
        txtCurrentUser.setText(String.format("%s",Common.currentUser.getFullname()));
        txtUserEmail = navigationHeaderView.findViewById(R.id.userEmail);
        txtUserEmail.setText(String.format("%s",Common.currentUser.getEmail()));



        displayAllTraining(training);

    }


    private void displayAllTraining(Query qry) {

        /*final FirebaseRecyclerOptions<Training> firebaseRecyclerOptions = new
                FirebaseRecyclerOptions.Builder<Training>().setQuery(training,Training.class).build();*/

        final FirebaseRecyclerOptions<Training> firebaseRecyclerOptions = new
                FirebaseRecyclerOptions.Builder<Training>().setQuery(qry,Training.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Training, TrainingHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final TrainingHolder holder, int position, @NonNull final Training model) {
                holder.setTraining(model);

                if(model.getStatus().toString().equals("Finished")){
                    holder.hideTraining.setVisibility(View.GONE);
                }


                if(holder.statusTV.getText().toString().equalsIgnoreCase("Full")){
                    holder.joinBtn.setVisibility(View.GONE);
                }

                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(HomeActivity.this, model.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                });

                holder.joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeActivity.this);
                        builder.setTitle("Confirmation");
                        builder.setMessage("Are you sure join this training?");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final String key = model.getSessionID();
                                users.child(auth.getCurrentUser().getUid().toString()).child("trainingList").child(key).setValue("Joined");
                                training.child(key).child("memberList").child(auth.getCurrentUser().getUid().toString()).setValue("Joined");

                                //update jumlah participant
                                DatabaseReference updateParticipant =  training.child(key).child("maxParticipant").getRef();
                                updateParticipant.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            String participant = dataSnapshot.getValue().toString();
                                            List<String> maxParticipant = Arrays.asList(participant.split(","));
                                            int oldParticipant = Integer.parseInt(maxParticipant.get(0));
                                            int newParticipant = oldParticipant+1;
                                            int allParticipant = Integer.parseInt(maxParticipant.get(1));
                                            String finalParticipant = String.valueOf(newParticipant);
                                            training.child(key).child("maxParticipant").setValue(finalParticipant+","+maxParticipant.get(1));

                                            if(newParticipant == allParticipant){
                                                training.child(key).child("status").setValue("Full");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                                Toast.makeText(HomeActivity.this, "Success register training session !", Toast.LENGTH_SHORT).show();
                            }
                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        android.app.AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

                if(Common.currentUser.getType().toString().equals("trainer")){
                    holder.joinBtn.setVisibility(View.GONE);
                }
                else{
                    users.child(auth.getCurrentUser().getUid().toString()).child("trainingList").child(model.getSessionID().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                holder.joinBtn.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }


            }

            @NonNull
            @Override
            public TrainingHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.training_item,viewGroup,false);
                return new TrainingHolder(view);
            }


        };
        trainingRV.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private class TrainingHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView titleTV, dateTV, timeTV, feeTV, statusTV, modeTV, classTypeTV, notesTV, maxParticipantTV;
        public Button joinBtn;
        public CardView cardView;
        public LinearLayout hideTraining;

        TrainingHolder(View itemView){
            super(itemView);
            titleTV = itemView.findViewById(R.id.itemTrainingTitle);
            dateTV = itemView.findViewById(R.id.itemTrainingDate);
            timeTV = itemView.findViewById(R.id.itemTrainingTime);
            feeTV = itemView.findViewById(R.id.itemTrainingFee);
            statusTV = itemView.findViewById(R.id.itemTrainingStatus);
            modeTV = itemView.findViewById(R.id.itemTrainingType);
            joinBtn = itemView.findViewById(R.id.joinBtn);
            cardView = itemView.findViewById(R.id.trainingCardView);
            maxParticipantTV = itemView.findViewById(R.id.itemTrainingMaxCapacity);
            hideTraining = itemView.findViewById(R.id.hideCoba);

            itemView.setOnClickListener(this);
        }

        void setTraining(Training tr){
            String title = tr.getTitle();
            titleTV.setText(title);
            String date = tr.getDate();
            dateTV.setText(date);
            String fee = tr.getFee();
            feeTV.setText(fee);
            String status = tr.getStatus();
            statusTV.setText(status);
            String mode = tr.getMode();
            modeTV.setText(mode);

            String participant = tr.getMaxParticipant();
            List<String> maxParticipant = Arrays.asList(participant.split(","));
            maxParticipantTV.setText(maxParticipant.get(0)+"/"+maxParticipant.get(1));


            String traTime = tr.getTime();
            List<String> TimeList = Arrays.asList(traTime.split(","));

            timeTV.setText(TimeList.get(0)+" - "+TimeList.get(1));

        }


        @Override
        public void onClick(View view) {
            Toast.makeText(HomeActivity.this, "hmm", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();

    };

    @Override
    protected void onStop(){
        super.onStop();

        if(firebaseRecyclerAdapter != null){
            firebaseRecyclerAdapter.stopListening();
        }
    }

    //query for sorting
    private Query getQuery(String SortType) {
        Query sortQuery = null;
        if(SortType.equalsIgnoreCase("Personal")){
            sortQuery = training.orderByChild("mode").equalTo("Personal");
            displayAllTraining(sortQuery);

        }
        if(SortType.equalsIgnoreCase("Group")){
            sortQuery = training.orderByChild("mode").equalTo("Group");
            displayAllTraining(sortQuery);

        }
        if(SortType.equalsIgnoreCase("All")){
            sortQuery = training;
            displayAllTraining(sortQuery);

        }
        return sortQuery;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.Sorting) {
            final CharSequence sortType[] = new CharSequence[] {"All", "Group", "Personal"};

            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("Sort Training ");
            builder.setItems(sortType, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getQuery(sortType[which].toString());
                    //displayAllTraining(sortType[which].toString());
                }
            });
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.history) {
            if(Common.currentUser.getType().equals("trainer")){
                Intent history = new Intent(HomeActivity.this, TrainerHistory.class);
                startActivity(history);
            }
            else {
                Intent history = new Intent(HomeActivity.this, MemberHistory.class);
                startActivity(history);
            }

            // Handle the camera action
        } else if (id == R.id.logout) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
            alertDialog.setMessage("Are you sure to logout ?");
            alertDialog.setPositiveButton("LOGOUT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();
        }




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
