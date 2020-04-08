package com.zion.newsscraper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private static boolean complete = false;
    private String searchKeyword = "레드벨벳";
    public static List<NewsData> scrapedNewsDataList = new ArrayList<>();
    static boolean documentExist = false;
    @SuppressLint("StaticFieldLeak")
    public static View view;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        view = inflater.inflate(R.layout.activity_main, null);
        setContentView(view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadScrapedNews();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchKeyword = query;
                recyclerView.setVisibility(View.INVISIBLE);
                getAndDisplayNews();
                try {
                    Thread.sleep(128);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                recyclerView.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class NaverNewsGetter extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            adapter = new RecyclerViewAdapter(getNaverNews(searchKeyword), MainActivity.this);
            complete = true;
            return null;
        }
    }

    private List<NewsData> getNaverNews(String searchWord) {
        String clientId = getString(R.string.naver_api_client_id);
        String clientSecret = getString(R.string.naver_api_client_secret);
        int display = 20;
        List<NewsData> newsDataList = new ArrayList<>();

        try {
            String text = URLEncoder.encode(searchWord, "utf-8");
            String apiURL = "https://openapi.naver.com/v1/search/news.json?query=" + text + "&display=" + display + "&";
            URL url = new URL(apiURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("X-Naver-Client-Id", clientId);
            httpURLConnection.setRequestProperty("X-Naver-Client-Secret", clientSecret);

            int responseCode = httpURLConnection.getResponseCode();
            BufferedReader bufferedReader;
            if(responseCode == 200) {
                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
            }

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            JSONObject jsonObject = new JSONObject(String.valueOf(stringBuilder));
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            for(int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonObjectItem = jsonArray.getJSONObject(i);
                NewsData newsData = new NewsData();
                newsData.setTitle(jsonObjectItem.getString("title"));
                newsData.setOriginalLink(jsonObjectItem.getString("originallink"));
                newsData.setDescription(jsonObjectItem.getString("description"));
                newsData.setPubDate(jsonObjectItem.getString("pubDate"));
                newsDataList.add(newsData);
            }
            bufferedReader.close();
            httpURLConnection.disconnect();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return newsDataList;
    }

    private class RecyclerViewer extends Thread {
        public void start() {
            recyclerView.setAdapter(adapter);
        }
    }

    public void getAndDisplayNews() {
        new NaverNewsGetter().execute();
        while(!complete) {
            try {
                Thread.sleep(64);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        RecyclerViewer recyclerViewer = new RecyclerViewer();
        recyclerViewer.setDaemon(true);
        recyclerViewer.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scraped_news:
                Intent intent = new Intent(MainActivity.this, ScrapedNewsActivity.class);
                intent.putExtra("scrapedNewsList", (Serializable) scrapedNewsDataList);
                startActivity(intent);
                return true;

            case R.id.logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("로그아웃");
                builder.setMessage("로그아웃 하시겠습니까?");
                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signOut();
                        scrapedNewsDataList.clear();
                        documentExist = false;
                    }
                });
                builder.setNegativeButton("아니요", null);
                builder.create().show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    public void deleteAccount() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showToast("계정이 삭제되었습니다.");
                    }
                });
    }

    private void showToast(final String text) {
      this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
      });
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
                        if (document.getId().equals(user.getUid())) {
                            List list;
                            try {
                                list = (List) document.getData().get("scrapedNewsData");
                                documentExist = list.size() != 0;
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
                                documentExist = false;
                            }
                        }
                    }
                } else {
                    documentExist = false;
                }
            }
        });
    }

    private List<String> crawlThumbnailUrl(String url) {
        List<String> stringList = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();
            Elements imageElements = document.select("div.thumb");;
            for (Element imageElement : imageElements) {
                String urlSized = imageElement.select("img").attr("src");
                stringList.add(urlSized.substring(0, urlSized.indexOf("&")));
            }
        } catch (IOException|Selector.SelectorParseException e) {
            e.printStackTrace();
            Log.d("ThumbnailCrawler", e.toString());
        }

        return stringList;
    }
}
