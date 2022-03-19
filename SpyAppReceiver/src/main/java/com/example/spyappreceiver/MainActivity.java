package com.example.spyappreceiver;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore mFirestore;
    RecyclerViewAdapter mAdapter;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView rv_activities;
    private List<Activity> activityList = new ArrayList<>();;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        rv_activities = (RecyclerView) findViewById(R.id.rv_activities);
        rv_activities.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rv_activities.setLayoutManager(layoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirestore = FirebaseFirestore.getInstance();
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                String token_id = s;
                Map<String, Object> tokenMap = new HashMap<>();
                tokenMap.put("token_id", token_id);
                mFirestore.collection("Users").document("userABC").update(tokenMap);
            }
        });
        activityList.clear();
        mFirestore.collection("Users/userABC/Notifications").addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for(DocumentChange doc: value.getDocumentChanges()) {
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        Activity activity = new Activity(doc.getDocument().get("datetime").toString(),doc.getDocument().get("message").toString(),doc.getDocument().get("title").toString());
                        activityList.add(activity);
                        mAdapter = new RecyclerViewAdapter(activityList, MainActivity.this);
                        rv_activities.setAdapter(mAdapter);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        MenuItem item = menu.findItem(R.id.app_bar_search);
        item.setTitle("Search By Date");
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}