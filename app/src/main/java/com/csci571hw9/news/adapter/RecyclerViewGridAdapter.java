package com.csci571hw9.news.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.csci571hw9.news.ArticleDetailActivity;
import com.csci571hw9.news.MainActivity;
import com.csci571hw9.news.R;
import com.csci571hw9.news.constant.ConstantPool;
import com.csci571hw9.news.entity.NewsItem;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Why we need a dummy item?
 *
 * when the size of list is even, we do not need the dummy item
 * when the size of list is odd, we do need the dummy item
 * because odd items mean that there is only one item in the last row
 * and rubric ask me to draw lines across the RecyclerView and under every item
 */
public class RecyclerViewGridAdapter extends RecyclerView.Adapter<RecyclerViewGridAdapter.GridViewHolder> {
    private Context context;
    private List<NewsItem> newsItems;
    private SharedPreferences sp = MainActivity.sp;
    private RecyclerView bookmarkRecyclerView;
    private TextView noBookmark;

    public RecyclerViewGridAdapter(Context context, List<NewsItem> bookmarks, RecyclerView bookmarkRecyclerView, TextView noBookmark) {
        this.context = context;
        this.newsItems = bookmarks;
        this.bookmarkRecyclerView = bookmarkRecyclerView;
        this.noBookmark = noBookmark;
    }

    /**
     * Set item layout
     */
    @NonNull
    @Override
    public RecyclerViewGridAdapter.GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(context, R.layout.item_grid_bookmarks, null);
        return new GridViewHolder(itemView);
    }

    /**
     * Render data to the corresponding list item
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewGridAdapter.GridViewHolder holder, final int position) {
        // if the current item is dummy, set its visibility is invisible and terminate the method
        if (position == newsItems.size() - 1 && newsItems.get(position).getId().equals("_dummy_id")) {
            holder.bookmarkContainer.setVisibility(View.INVISIBLE);
            return;
        }

        // render title, date, section, image
        holder.bookmarkTitle.setText(newsItems.get(position).getTitle());
        holder.bookmarkDate.setText(newsItems.get(position).getDate());
        holder.bookmarkSection.setText(newsItems.get(position).getSection());
        Picasso.with(context).load(newsItems.get(position).getImage()).resize(195, 150).into(holder.bookmarkImage);

        holder.bookmarkContainer.setDefaultFocusHighlightEnabled(false);
        holder.bookmarkContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.ARTICLE_DETAIL_ORIGIN = ConstantPool.ARTICLE_DETAIL_FROM_BOOKMARK;

                // Transfer to ArticleDetailActivity
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("articleId", newsItems.get(position).getId());
                intent.putExtra("articleImage", newsItems.get(position).getImage());
                context.startActivity(intent);
            }
        });
        holder.bookmarkIconChecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // remove from Bookmarks

                SharedPreferences.Editor editor = sp.edit();
                Set<String> bookmarksIds = sp.getStringSet("BOOKMARKS_IDS", null);
                assert bookmarksIds != null;
                Set<String> newBookmarksIds = new HashSet<>(bookmarksIds);
                newBookmarksIds.remove(newsItems.get(position).getId());
                editor.putStringSet("BOOKMARKS_IDS", newBookmarksIds).apply();
                editor.remove(newsItems.get(position).getId()).apply();

                Toast.makeText(context, "\"" + newsItems.get(position).getTitle() + "\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();

                // delete dummy bookmark at first if existing
                if (newsItems.get(newsItems.size() - 1).getId().equals("_dummy_id")) {
                    newsItems.remove(newsItems.size() - 1);
                }
                // delete target bookmark
                newsItems.remove(position);
                // Case 1. after delete, if there is no bookmarks, show noBookmark
                if (newsItems.size() == 0) {
                    noBookmark.setVisibility(View.VISIBLE);
                }
                // Case 2. after delete, if the size of bookmarks is odd, add a dummy bookmark
                if (newsItems.size() % 2 != 0) {
                    newsItems.add(new NewsItem("_dummy_id", "", "", "", ""));
                }
                bookmarkRecyclerView.setAdapter(RecyclerViewGridAdapter.this);
            }
        });
        holder.bookmarkImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stop bubble because we do not want to trigger click listener of itemContainer when clicking itemImage
            }
        });

        holder.bookmarkContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // long click to show dialog

                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.long_click_dialog);

                TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
                dialogTitle.setText(newsItems.get(position).getTitle());
                ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
                Picasso.with(context).load(newsItems.get(position).getImage()).resize(300, 181).into(dialogImage);
                dialog.findViewById(R.id.dialogBookmarkChecked).setVisibility(View.VISIBLE);

                dialog.findViewById(R.id.dialogTwitter).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // transfer to twitter page

                        Intent intent = new Intent();
                        String url = "https://www.theguardian.com/" + newsItems.get(position).getId();
                        String link = "https://twitter.com/intent/tweet?text=Check out this Link: " + url + "&hashtags=CSCI571NewsSearch";
                        intent.setData(Uri.parse(link));
                        intent.setAction(Intent.ACTION_VIEW);
                        context.startActivity(intent);
                    }
                });
                dialog.findViewById(R.id.dialogBookmarkChecked).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // remove from Bookmarks

                        SharedPreferences.Editor editor = sp.edit();

                        Set<String> bookmarksIds = sp.getStringSet("BOOKMARKS_IDS", null);
                        assert bookmarksIds != null;
                        Set<String> newBookmarksIds = new HashSet<>(bookmarksIds);
                        newBookmarksIds.remove(newsItems.get(position).getId());
                        editor.putStringSet("BOOKMARKS_IDS", newBookmarksIds).apply();

                        editor.remove(newsItems.get(position).getId()).apply();

                        Toast.makeText(context, "\"" + newsItems.get(position).getTitle() + "\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();

                        // delete dummy bookmark at first if existing
                        if (newsItems.get(newsItems.size() - 1).getId().equals("_dummy_id")) {
                            newsItems.remove(newsItems.size() - 1);
                        }
                        // delete target bookmark
                        newsItems.remove(position);
                        // Case 1. after delete, if there is no bookmarks, show noBookmark
                        if (newsItems.size() == 0) {
                            noBookmark.setVisibility(View.VISIBLE);
                        }
                        // Case 2. after delete, if the size of bookmarks is odd, add a dummy bookmark
                        if (newsItems.size() % 2 != 0) {
                            newsItems.add(new NewsItem("_dummy_id", "", "", "", ""));
                        }
                        bookmarkRecyclerView.setAdapter(RecyclerViewGridAdapter.this);

                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (newsItems != null && newsItems.size() > 0) {
            return newsItems.size();
        }
        return 0;
    }

    // List Item Object
    public static class GridViewHolder extends RecyclerView.ViewHolder {
        public TextView bookmarkTitle;
        public TextView bookmarkDate;
        public TextView bookmarkSection;
        public ImageView bookmarkImage;
        public ImageView bookmarkIconChecked;
        public CardView bookmarkContainer;

        public GridViewHolder(View view) {
            super(view);
            bookmarkTitle = view.findViewById(R.id.bookmarkTitle);
            bookmarkDate = view.findViewById(R.id.bookmarkDate);
            bookmarkSection = view.findViewById(R.id.bookmarkSection);
            bookmarkImage = view.findViewById(R.id.bookmarkImage);
            bookmarkIconChecked = view.findViewById(R.id.bookmarkIconChecked);
            bookmarkContainer = view.findViewById(R.id.bookmarkContainer);
        }
    }
}
