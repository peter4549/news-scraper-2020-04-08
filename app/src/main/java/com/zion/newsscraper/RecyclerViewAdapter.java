package com.zion.newsscraper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {
    private final static String TAG = "FireStoreUpdate";
    private static List<NewsData> newsDataList;
    private int position;

    private void setPosition(int position) {
        this.position = position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    RecyclerViewAdapter(List<NewsData> newsDataList, Context context) {
        RecyclerViewAdapter.newsDataList = newsDataList;
        Fresco.initialize(context);
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener{
        CardView cardView;
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewPubDate;

        RecyclerViewHolder(View v) {
            super(v);
            textViewTitle = v.findViewById(R.id.text_view_title);
            textViewDescription = v.findViewById(R.id.text_view_description);
            textViewPubDate = v.findViewById(R.id.text_view_pub_date);
            cardView = v.findViewById(R.id.card_view);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = newsDataList.get(getAdapterPosition()).getOriginalLink();
                    Intent intent = new Intent(v.getContext(), WebViewActivity.class);
                    intent.putExtra("url", url);
                    v.getContext().startActivity(intent);
                }
            });
            v.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem scrap = menu.add(Menu.NONE, 1001, 1, "스크랩하기");
            MenuItem openLink = menu.add(Menu.NONE, 1002, 2, "링크로 열기");
            scrap.setOnMenuItemClickListener(menuItemClickListener);
            openLink.setOnMenuItemClickListener(menuItemClickListener);
        }

        private final MenuItem.OnMenuItemClickListener menuItemClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1001:
                        updateScrapedNews(getAdapterPosition());
                        break;
                    case 1002:
                        String url = newsDataList.get(getAdapterPosition()).getOriginalLink();
                        Intent intent = new Intent(MainActivity.view.getContext(), WebViewActivity.class);
                        intent.putExtra("url", url);
                        MainActivity.view.getContext().startActivity(intent);
                        break;
                    default:
                        break;
                }
                return true;
            }
        };
    };

    @NonNull
    @Override
    public RecyclerViewAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_data, parent, false);
        return new RecyclerViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewAdapter.RecyclerViewHolder holder, int position) {
        NewsData newsData = newsDataList.get(position);
        holder.textViewTitle.setText(newsData.getTitle());
        holder.textViewDescription.setText(newsData.getDescription());
        holder.textViewPubDate.setText(newsData.getPubDate());
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(holder.getAdapterPosition());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsDataList == null ? 0 : newsDataList.size();
    }

    private static void showSnackBar(String text) {
            Snackbar.make(MainActivity.view, text, Snackbar.LENGTH_SHORT).show();
    }

    private static void updateScrapedNews(int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        NewsData newsData = newsDataList.get(position);
        Map<String, Object> map = new HashMap<>();
        if(user != null) {
            DocumentReference documentReference = db.collection("users").document(user.getUid());
            if (!MainActivity.documentExist) {
                MainActivity.scrapedNewsDataList.add(newsData); // 대체할것..
                Log.d("updateScrapedNews", "Create new document.");
                map.put("scrapedNewsData", MainActivity.scrapedNewsDataList);
                documentReference
                        .set(map)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Scraped news successfully written.");
                                MainActivity.documentExist = true;
                                showSnackBar("해당 뉴스가 스크랩되었습니다.");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document.", e);
                            }
                        });
            } else {
                MainActivity.scrapedNewsDataList.add(newsData);
                Log.d("updateScrapedNews", "Add new news.");
                documentReference.update("scrapedNewsData", FieldValue.arrayUnion(newsData));
                showSnackBar("해당 뉴스가 스크랩되었습니다.");
            }
        }
    }
}
