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

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ScrapedNewsAdapter extends RecyclerView.Adapter<ScrapedNewsAdapter.RecyclerViewHolder> {
    private final static String TAG = "FireStoreUpdate";
    private static List<NewsData> newsDataList;
    @SuppressLint("StaticFieldLeak")
    private static View classView;
    private int position;

    private void setPosition(int position) {
        this.position = position;
    }

    ScrapedNewsAdapter(List<NewsData> newsDataList, Context context) {
        ScrapedNewsAdapter.newsDataList = newsDataList;
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener{
        CardView cardView;
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewPubDate;

        RecyclerViewHolder(View v) {
            super(v);
            classView = v;
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
            MenuItem scrap = menu.add(Menu.NONE, 1003, 1, "목록에서 제거");
            MenuItem openLink = menu.add(Menu.NONE, 1004, 2, "링크로 열기");
            scrap.setOnMenuItemClickListener(menuItemClickListener);
            openLink.setOnMenuItemClickListener(menuItemClickListener);
        }

        private final MenuItem.OnMenuItemClickListener menuItemClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1003:

                        break;
                    case 1004:
                        String url = newsDataList.get(getAdapterPosition()).getOriginalLink();
                        Intent intent = new Intent(classView.getContext(), WebViewActivity.class);
                        intent.putExtra("url", url);
                        classView.getContext().startActivity(intent);
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
    public ScrapedNewsAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_data, parent, false);
        return new RecyclerViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull final ScrapedNewsAdapter.RecyclerViewHolder holder, int position) {
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
        Snackbar.make(ScrapedNewsActivity.view, text, Snackbar.LENGTH_SHORT).show();
    }
}
