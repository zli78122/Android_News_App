package com.csci571hw9.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.csci571hw9.news.constant.ConstantPool;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ArticleDetailActivity extends AppCompatActivity {
    private RequestQueue queue;
    private SharedPreferences sharedPreferences = MainActivity.sp;

    private Toolbar articleDetailToolbar;
    private ImageView articleDetailImage;
    private TextView articleDetailTitle;
    private TextView articleDetailSection;
    private TextView articleDetailDate;
    private TextView articleDetailContent;
    private TextView articleDetailViewFull;
    private ProgressBar articleDetailProgressBar;
    private TextView articleDetailFetchingNews;
    private ImageView bookmarkImg;
    private ImageView bookmarkImgChecked;
    private ImageView twitterImg;
    private String[] articleInfo = new String[4]; // [title, image, section, time]
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        queue = Volley.newRequestQueue(this);

        String articleId = getIntent().getStringExtra("articleId");

        initView(articleId);
        toolBarEvent();
        requestArticle(articleId);
    }

    private void initView(final String articleId) {
        articleDetailToolbar = findViewById(R.id.articleDetailToolbar);
        articleDetailToolbar = findViewById(R.id.articleDetailToolbar);
        articleDetailImage = findViewById(R.id.articleDetailImage);
        articleDetailTitle = findViewById(R.id.articleDetailTitle);
        articleDetailSection = findViewById(R.id.articleDetailSection);
        articleDetailDate = findViewById(R.id.articleDetailDate);
        articleDetailContent = findViewById(R.id.articleDetailContent);
        articleDetailViewFull = findViewById(R.id.articleDetailViewFull);
        articleDetailProgressBar = findViewById(R.id.articleDetailProgressBar);
        articleDetailFetchingNews = findViewById(R.id.articleDetailFetchingNews);
        bookmarkImg = findViewById(R.id.bookmarkImg);
        bookmarkImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add to Bookmarks

                SharedPreferences.Editor editor = sharedPreferences.edit();

                Set<String> bookmarksIds = sharedPreferences.getStringSet("BOOKMARKS_IDS", null);
                assert bookmarksIds != null;
                Set<String> newBookmarksIds = new HashSet<>(bookmarksIds);
                newBookmarksIds.add(articleId);
                editor.putStringSet("BOOKMARKS_IDS", newBookmarksIds).apply();

                Set<String> bookmarkInfo = new HashSet<>();
                bookmarkInfo.add("title:" + articleInfo[0]);
                bookmarkInfo.add("image:" + articleInfo[1]);
                bookmarkInfo.add("section:" + articleInfo[2]);
                bookmarkInfo.add("date:" + transferBookmarkTime(articleInfo[3], 7 * 3600 * 1000));
                editor.putStringSet(articleId, bookmarkInfo).apply();

                bookmarkImg.setVisibility(View.GONE);
                bookmarkImgChecked.setVisibility(View.VISIBLE);

                Toast.makeText(ArticleDetailActivity.this, "\"" + articleInfo[0] + "\" was added to Bookmarks", Toast.LENGTH_SHORT).show();
            }
        });
        bookmarkImgChecked = findViewById(R.id.bookmarkImgChecked);
        bookmarkImgChecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // remove from Bookmarks

                SharedPreferences.Editor editor = sharedPreferences.edit();

                Set<String> bookmarksIds = sharedPreferences.getStringSet("BOOKMARKS_IDS", null);
                assert bookmarksIds != null;
                Set<String> newBookmarksIds = new HashSet<>(bookmarksIds);
                newBookmarksIds.remove(articleId);
                editor.putStringSet("BOOKMARKS_IDS", newBookmarksIds).apply();

                editor.remove(articleId).apply();

                bookmarkImg.setVisibility(View.VISIBLE);
                bookmarkImgChecked.setVisibility(View.GONE);

                Toast.makeText(ArticleDetailActivity.this, "\"" + articleInfo[0] + "\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
            }
        });
        twitterImg = findViewById(R.id.twitterImg);
        twitterImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // transfer to twitter page

                Intent intent = new Intent();
                String link = "https://twitter.com/intent/tweet?text=Check out this Link: " + url + "&hashtags=CSCI571NewsSearch";
                intent.setData(Uri.parse(link));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
        });
    }

    private void toolBarEvent() {
        // trigger when clicking back button on Toolbar
        articleDetailToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void requestArticle(final String articleId) {
        StringRequest request = new StringRequest(ConstantPool.SEARCH_BY_ID + articleId, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("status").equals("ok")) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        String title = data.getString("title");
                        String image = data.getString("image");
                        String section = data.getString("section");
                        String time = data.getString("time");
                        String description = data.getString("description");
                        url = data.getString("url");

                        if (image.equals(ConstantPool.GUARDIAN_DEFAULT_IMAGE)) {
                            image = getIntent().getStringExtra("articleImage");
                        }
                        Picasso.with(ArticleDetailActivity.this).load(image).resize(431, 269).into(articleDetailImage);

                        articleInfo[0] = title;
                        articleInfo[1] = image;
                        articleInfo[2] = section;
                        articleInfo[3] = time;

                        articleDetailTitle.setText(title);
                        articleDetailSection.setText(section);
                        articleDetailDate.setText(transferTime(time, 7 * 3600 * 1000));
                        articleDetailContent.setText(HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY));

                        String viewFullText = "View Full Article";
                        SpannableString sp = new SpannableString(viewFullText);
                        // Bind a url to TextView
                        sp.setSpan(new URLSpan(url), 0, viewFullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        // Set color of TextView
                        sp.setSpan(new ForegroundColorSpan(Color.rgb(91, 91, 91)),0, viewFullText.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        articleDetailViewFull.setText(sp);
                        // Click TextView, go to the url
                        articleDetailViewFull.setMovementMethod(LinkMovementMethod.getInstance());

                        articleDetailToolbar.setTitle(title);

                        Set<String> bookmarks_ids = sharedPreferences.getStringSet("BOOKMARKS_IDS", null);
                        if (!bookmarks_ids.contains(articleId)) {
                            bookmarkImg.setVisibility(View.VISIBLE);
                        } else {
                            bookmarkImgChecked.setVisibility(View.VISIBLE);
                        }

                        twitterImg.setVisibility(View.VISIBLE);

                        articleDetailProgressBar.setVisibility(View.GONE);
                        articleDetailFetchingNews.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Request Error", error.toString());
                Log.e("Request Error", "change IP address, do not use localhost or 127.0.0.1");
            }
        });
        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(request);
    }

    // 2020-05-06T18:47:39Z -> 06 May 2020
    private String transferTime(String time, long diffTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date = sdf.parse(time);
            long curZone = date.getTime() - diffTime;
            sdf = new SimpleDateFormat("dd MMM yyyy");
            date = sdf.parse(sdf.format(curZone));
            return sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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
}
