package com.zion.newsscraper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
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

    private static void showSnackBar(View view, String text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
