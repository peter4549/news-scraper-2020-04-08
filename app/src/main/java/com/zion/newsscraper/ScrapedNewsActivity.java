package com.zion.newsscraper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScrapedNewsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private static List<NewsData> scrapedNewsDataList = new ArrayList<>();
    public static View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scraped_news);
        view = findViewById(R.id.scraped_news_layout);

        Intent intent = getIntent();
        scrapedNewsDataList = (ArrayList<NewsData>) intent.getSerializableExtra("scrapedNewsList");

        recyclerView = findViewById(R.id.recycler_view_scraped_news);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        if(MainActivity.documentExist) {
            ScrapedNewsAdapter adapter = new ScrapedNewsAdapter(scrapedNewsDataList, ScrapedNewsActivity.this);
            recyclerView.setAdapter(adapter);
        } else {
            showSnackBar(view, "스크랩한 뉴스가 없습니다.");
        }
    }

    private class RecyclerViewer extends Thread {
        public void start() {
            recyclerView.removeAllViewsInLayout();
            RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> adapter = new RecyclerViewAdapter(scrapedNewsDataList, ScrapedNewsActivity.this);
            recyclerView.setAdapter(adapter);
        }
    }

    private static void showSnackBar(View view, String text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
    }

    private void loadScrapedNews() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        assert user != null;
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("Current user id", user.getUid());
                        Log.d("Document id", document.getId());
                        if (document.getId().equals(user.getUid())) {
                            List<HashMap> list;
                            try {
                                list = (List<HashMap>) document.getData().get("scrapedNewsData");
                                scrapedNewsDataList.clear();
                                for (int i =0; i < list.size(); ++i)
                                {
                                    HashMap map = (HashMap) list.get(i);
                                    NewsData newsData = new NewsData();
                                    newsData.setTitle(map.get("title").toString());
                                    newsData.setDescription(map.get("description").toString());
                                    newsData.setOriginalLink(map.get("originalLink").toString());
                                    newsData.setPubDate(map.get("pubDate").toString());
                                    scrapedNewsDataList.add(newsData);
                                }
                            } catch (NullPointerException e) {
                                e.getStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
