package com.csci571hw9.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.csci571hw9.news.adapter.MyRecyclerAdapter;
import com.csci571hw9.news.constant.ConstantPool;
import com.csci571hw9.news.entity.NewsItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {
    private RequestQueue queue;
    private RecyclerView searchResultRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private List<NewsItem> newsItems = new ArrayList<>();

    private TextView searchResultFetchingNews;
    private ProgressBar searchResultProgressBar;
    private Toolbar toolbar;

    private SwipeRefreshLayout searchResultSwipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        MainActivity.SEARCH_ACTIVITY_TO_MAIN_ACTIVITY = true;

        queue = Volley.newRequestQueue(this);
        String keyword = getIntent().getStringExtra("query");

        initView(keyword);
        loadRecyclerView();
        requestNews(keyword);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // synchronize bookmark data from ArticleDetailActivity to SearchResultActivity
        if (MainActivity.ARTICLE_DETAIL_ORIGIN == ConstantPool.ARTICLE_DETAIL_FROM_SEARCH) {
            searchResultRecyclerView.setAdapter(mAdapter);
            MainActivity.ARTICLE_DETAIL_ORIGIN = 0;
        }
    }

    private void initView(final String keyword) {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Search Results for " + keyword);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24px);

        // trigger when clicking back button on Toolbar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchResultFetchingNews = findViewById(R.id.searchResultFetchingNews);
        searchResultFetchingNews.setVisibility(View.VISIBLE);
        searchResultProgressBar = findViewById(R.id.searchResultProgressBar);
        searchResultProgressBar.setVisibility(View.VISIBLE);

        searchResultSwipeRefresh = findViewById(R.id.searchResultSwipeRefresh);
        searchResultSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestNews(keyword);
            }
        });
    }

    private void loadRecyclerView() {
        searchResultRecyclerView = findViewById(R.id.searchResultRecyclerView);
        searchResultRecyclerView.setHasFixedSize(true);
        searchResultRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mLayoutManager = new LinearLayoutManager(this);
        searchResultRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerAdapter(this, newsItems, ConstantPool.ARTICLE_DETAIL_FROM_SEARCH);
    }

    private void requestNews(String keyword) {
        // request news by keyword
        StringRequest request = new StringRequest(ConstantPool.SEARCH_BY_KEYWORD + keyword, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                newsItems.clear();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("status").equals("ok")) {
                        JSONArray data = jsonObject.getJSONArray("data");
                        JSONObject object;
                        for (int i = 0; i < data.length(); i++) {
                            object = data.getJSONObject(i);
                            newsItems.add(new NewsItem(object.getString("id"),
                                    object.getString("image"),
                                    object.getString("title"),
                                    object.getString("time"),
                                    object.getString("section")));
                        }
                        searchResultProgressBar.setVisibility(View.INVISIBLE);
                        searchResultFetchingNews.setVisibility(View.INVISIBLE);

                        searchResultRecyclerView.setAdapter(mAdapter);

                        if(searchResultSwipeRefresh.isRefreshing()) {
                            searchResultSwipeRefresh.setRefreshing(false);
                        }
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
}
