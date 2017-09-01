package com.example.thingsalarm;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String DEBUG_TAG = "tag";

    private DatabaseReference mDatabaseRef;

    private List<MotionEntry> motionEntryList = new ArrayList<>();

    private StaggeredGridLayoutManager mStaggeredLayoutManager;

    private RecyclerView mRecyclerView;
    private MotionEntryAdapter mAdapter;

   // private Toolbar toolbar;
    private Menu menu;
    private  boolean isListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        isListView = true;

        mRecyclerView = findViewById(R.id.motionView);



        mStaggeredLayoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mStaggeredLayoutManager);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this,DividerItemDecoration.VERTICAL);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("logs");

        ItemClickSupport itemClickSupport = ItemClickSupport.addTo(mRecyclerView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.d(DEBUG_TAG, "List CLicked");
                final ArrayList<MotionEntry> motionEntryArrayList = new ArrayList<>();
                mDatabaseRef .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            motionEntryArrayList.add (snapshot.getValue(MotionEntry.class));
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                Context context = v.getContext();

                Intent intent = new Intent (context, DetailActivity.class);
                context.startActivity(intent);

            }
        });



       /* LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);


        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("logs");


        */




    }

   /* private void setActionBar (Toolbar toolbar) {
        if (toolbar != null) {
            setActionBar(toolbar);
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(true);
            getActionBar().setElevation(7);
        }
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toggle) {
            toggle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggle() {
        mStaggeredLayoutManager.setSpanCount(2);
        MenuItem item = menu.findItem(R.id.action_toggle);
        if (isListView) {
            item.setIcon(R.drawable.ic_action_list);
            item.setTitle("Show as list");
            isListView = false;
        } else {
            mStaggeredLayoutManager.setSpanCount(1);
            item.setIcon(R.drawable.ic_action_grid);
            item.setTitle("Show as grid");
            isListView = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mAdapter = new MotionEntryAdapter(this,mDatabaseRef);
        mRecyclerView.setAdapter(mAdapter);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MotionEntry motionEntry = dataSnapshot.getValue(MotionEntry.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };



       mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();

        // Tear down Firebase listeners in adapter
        if (mAdapter != null) {
            mAdapter.cleanup();
            mAdapter = null;
        }
    }
}

