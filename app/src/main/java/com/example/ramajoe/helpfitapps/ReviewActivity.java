package com.example.ramajoe.helpfitapps;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ramajoe.helpfitapps.Common.Common;
import com.example.ramajoe.helpfitapps.Model.Review;
import com.example.ramajoe.helpfitapps.Model.Training;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import java.util.Arrays;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference training;
    private DatabaseReference users;
    private RecyclerView reviewRecycler;
    private FirebaseRecyclerAdapter<Review,ReviewActivity.ReviewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        auth = FirebaseAuth.getInstance();
        users = FirebaseDatabase.getInstance().getReference().child("UserData");
        training = FirebaseDatabase.getInstance().getReference().child("Training");
        training.keepSynced(true);

        reviewRecycler = (RecyclerView) findViewById(R.id.reviewRecycler);
        reviewRecycler.setHasFixedSize(true);
        reviewRecycler.setLayoutManager(new LinearLayoutManager(this));

        displayAllReview();
    }

    private void displayAllReview() {
        Bundle bundle = getIntent().getExtras();
        String key = bundle.getString("trainingKey");
        Query qry = training.child(key).child("memberList");

        final FirebaseRecyclerOptions<Review> firebaseRecyclerOptions = new
                FirebaseRecyclerOptions.Builder<Review>().setQuery(qry,Review.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Review, ReviewActivity.ReviewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ReviewActivity.ReviewHolder holder, int position, @NonNull final Review model) {
                holder.setReview(model);
                holder.ratingBar.setEnabled(false);
            }

            @NonNull
            @Override
            public ReviewActivity.ReviewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.review_item,viewGroup,false);
                return new ReviewActivity.ReviewHolder(view);
            }


        };
        reviewRecycler.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private class ReviewHolder extends RecyclerView.ViewHolder{
        public TextView memberName, timeStamp, comment;
        public SimpleRatingBar ratingBar;

        ReviewHolder(View itemView){
            super(itemView);
            memberName = itemView.findViewById(R.id.memberName);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            comment = itemView.findViewById(R.id.memberReview);
            ratingBar = itemView.findViewById(R.id.starRating);

            //itemView.setOnClickListener(this);
        }

        void setReview(Review rv){
            String names = rv.getName();
            memberName.setText(names);

            String comments = rv.getComment();
            comment.setText(comments);

            String ratings = rv.getRating();
            float ratingsFloat = Float.parseFloat(ratings);
            //int ratingsInt = Math.round(ratingsFloat);
            ratingBar.setRating(ratingsFloat);

            String timeStamps = rv.getTimeStamp();
            List<String> timeStampss = Arrays.asList(timeStamps.split("-"));
            String date = timeStampss.get(0);
            String[] dateMontYear = date.split(",");
            String dates = dateMontYear[0];
            String month = dateMontYear[1];
            String year = dateMontYear[2];

            String time = timeStampss.get(1);
            String[] hourMinute = time.split(",");
            String hour = hourMinute[0];
            String minutes = hourMinute[1];

            timeStamp.setText(dates+"-"+month+"-"+year+"  "+hour+":"+minutes);

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
}
