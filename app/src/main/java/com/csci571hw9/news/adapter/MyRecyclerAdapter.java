package com.csci571hw9.news.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.csci571hw9.news.ArticleDetailActivity;
import com.csci571hw9.news.MainActivity;
import com.csci571hw9.news.R;
import com.csci571hw9.news.SearchResultActivity;
import com.csci571hw9.news.constant.ConstantPool;
import com.csci571hw9.news.entity.NewsItem;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private List<NewsItem> newsItems;
    private Context context;
    private SharedPreferences sp = MainActivity.sp;
    private int type; // ARTICLE_DETAIL_FROM_MAIN = 2;
                      // ARTICLE_DETAIL_FROM_SECTION = 4;
                      // ARTICLE_DETAIL_FROM_SEARCH = 8;

    public MyRecyclerAdapter(Context context, List<NewsItem> newsItems, int type) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.newsItems = newsItems;
        this.type = type;
    }

    /**
     * Set item layout
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_recycler_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    /**
     * Render data to the corresponding list item
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // render title, date, section, image
        holder.itemTitle.setText(newsItems.get(position).getTitle());
        String time = transferTime(newsItems.get(position).getDate(), 7 * 3600 * 1000);
        holder.itemDate.setText(time);
        holder.itemSection.setText(newsItems.get(position).getSection());
        Picasso.with(context).load(newsItems.get(position).getImage()).resize(145, 130).into(holder.itemImage);

        // Notice: must initialize the visibilities to avoid a bug related to the reuse of RecyclerView
        holder.itemBookmark.setVisibility(View.GONE);
        holder.itemBookmarkChecked.setVisibility(View.GONE);

        // render bookmark icon
        Set<String> bookmarks_ids = sp.getStringSet("BOOKMARKS_IDS", null);
        if (!bookmarks_ids.contains(newsItems.get(position).getId())) {
            holder.itemBookmark.setVisibility(View.VISIBLE);
        } else {
            holder.itemBookmarkChecked.setVisibility(View.VISIBLE);
        }

        holder.itemContainer.setDefaultFocusHighlightEnabled(false);
        holder.itemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.ARTICLE_DETAIL_ORIGIN = type;

                // Transfer to ArticleDetailActivity
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("articleId", newsItems.get(position).getId());
                intent.putExtra("articleImage", newsItems.get(position).getImage());
                context.startActivity(intent);
            }
        });
        holder.itemBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add to Bookmarks

                SharedPreferences.Editor editor = sp.edit();

                Set<String> bookmarksIds = sp.getStringSet("BOOKMARKS_IDS", null);
                assert bookmarksIds != null;
                Set<String> newBookmarksIds = new HashSet<>(bookmarksIds);
                newBookmarksIds.add(newsItems.get(position).getId());
                editor.putStringSet("BOOKMARKS_IDS", newBookmarksIds).apply();

                Set<String> bookmarkInfo = new HashSet<>();
                bookmarkInfo.add("title:" + newsItems.get(position).getTitle());
                bookmarkInfo.add("image:" + newsItems.get(position).getImage());
                bookmarkInfo.add("section:" + newsItems.get(position).getSection());
                bookmarkInfo.add("date:" + transferBookmarkTime(newsItems.get(position).getDate(), 7 * 3600 * 1000));
                editor.putStringSet(newsItems.get(position).getId(), bookmarkInfo).apply();

                holder.itemBookmark.setVisibility(View.GONE);
                holder.itemBookmarkChecked.setVisibility(View.VISIBLE);

                Toast.makeText(context, "\"" + newsItems.get(position).getTitle() + "\" was added to Bookmarks", Toast.LENGTH_SHORT).show();
            }
        });
        holder.itemBookmarkChecked.setOnClickListener(new View.OnClickListener() {
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

                holder.itemBookmark.setVisibility(View.VISIBLE);
                holder.itemBookmarkChecked.setVisibility(View.GONE);

                Toast.makeText(context, "\"" + newsItems.get(position).getTitle() + "\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
            }
        });
        holder.itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stop bubble because we do not want to trigger click listener of itemContainer when clicking itemImage
            }
        });

        holder.itemContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // long click to show dialog

                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.long_click_dialog);

                TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
                dialogTitle.setText(newsItems.get(position).getTitle());
                ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
                Picasso.with(context).load(newsItems.get(position).getImage()).resize(300, 181).into(dialogImage);

                Set<String> bookmarks_ids = sp.getStringSet("BOOKMARKS_IDS", null);
                if (!bookmarks_ids.contains(newsItems.get(position).getId())) {
                    dialog.findViewById(R.id.dialogBookmark).setVisibility(View.VISIBLE);
                } else {
                    dialog.findViewById(R.id.dialogBookmarkChecked).setVisibility(View.VISIBLE);
                }

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
                dialog.findViewById(R.id.dialogBookmark).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // add to Bookmarks

                        SharedPreferences.Editor editor = sp.edit();

                        Set<String> bookmarksIds = sp.getStringSet("BOOKMARKS_IDS", null);
                        assert bookmarksIds != null;
                        Set<String> newBookmarksIds = new HashSet<>(bookmarksIds);
                        newBookmarksIds.add(newsItems.get(position).getId());
                        editor.putStringSet("BOOKMARKS_IDS", newBookmarksIds).apply();

                        Set<String> bookmarkInfo = new HashSet<>();
                        bookmarkInfo.add("title:" + newsItems.get(position).getTitle());
                        bookmarkInfo.add("image:" + newsItems.get(position).getImage());
                        bookmarkInfo.add("section:" + newsItems.get(position).getSection());
                        bookmarkInfo.add("date:" + transferBookmarkTime(newsItems.get(position).getDate(), 7 * 3600 * 1000));
                        editor.putStringSet(newsItems.get(position).getId(), bookmarkInfo).apply();

                        dialog.findViewById(R.id.dialogBookmark).setVisibility(View.GONE);
                        dialog.findViewById(R.id.dialogBookmarkChecked).setVisibility(View.VISIBLE);

                        holder.itemBookmark.setVisibility(View.GONE);
                        holder.itemBookmarkChecked.setVisibility(View.VISIBLE);

                        Toast.makeText(context, "\"" + newsItems.get(position).getTitle() + "\" was added to Bookmarks", Toast.LENGTH_SHORT).show();
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

                        dialog.findViewById(R.id.dialogBookmark).setVisibility(View.VISIBLE);
                        dialog.findViewById(R.id.dialogBookmarkChecked).setVisibility(View.GONE);

                        holder.itemBookmark.setVisibility(View.VISIBLE);
                        holder.itemBookmarkChecked.setVisibility(View.GONE);

                        Toast.makeText(context, "\"" + newsItems.get(position).getTitle() + "\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show();
                return true;
            }
        });
    }

    // 2020-05-06T18:47:39Z -> 06 May
    private String transferBookmarkTime(String time, long diffTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date = sdf.parse(time);
            long curZone = date.getTime() - diffTime;
            sdf = new SimpleDateFormat("dd MMM");
            date = sdf.parse(sdf.format(curZone));
            return sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2020-05-06T18:47:39Z -> 10s ago
    private String transferTime(String time, long diffTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        long diff = 1;
        try {
            Date date = sdf.parse(time);
            long old = date.getTime() - diffTime;
            long now = new Date().getTime();
            diff = (now - old) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (diff < 60) {
            return diff + "s ago";
        } else if (diff < 3600) {
            return (diff / 60) + "m ago";
        } else if (diff < 86400) {
            return (diff / 3600) + "h ago";
        } else {
            return (diff / 86400) + "d ago";
        }
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    // List Item Object
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemTitle;
        public TextView itemDate;
        public TextView itemSection;
        public ImageView itemImage;
        public ImageView itemBookmark;
        public ImageView itemBookmarkChecked;
        public CardView itemContainer;

        public ViewHolder(View view) {
            super(view);
            itemTitle = view.findViewById(R.id.itemTitle);
            itemDate = view.findViewById(R.id.itemDate);
            itemSection = view.findViewById(R.id.itemSection);
            itemImage = view.findViewById(R.id.itemImage);
            itemBookmark = view.findViewById(R.id.itemBookmark);
            itemBookmarkChecked = view.findViewById(R.id.itemBookmarkChecked);
            itemContainer = view.findViewById(R.id.itemContainer);
        }
    }
}
