package com.csci571hw9.news.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.csci571hw9.news.R;
import com.csci571hw9.news.adapter.MyRecyclerAdapter;
import com.csci571hw9.news.constant.ConstantPool;
import com.csci571hw9.news.entity.NewsItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TabFragment extends Fragment{
    private static final String BUNDLE_KEY_TITLE = "SECTION";

    private static RequestQueue queue = null;
    private static Context context;

    private String mTitle;
    private RecyclerView recyclerView;
    private TextView fetchingNews;
    private ProgressBar progressBar;

    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private List<NewsItem> newsItems = new ArrayList<>();

    public static TabFragment newInstance(String section, RequestQueue queue, Context context) {
        if (TabFragment.queue == null) {
            TabFragment.queue = queue;
        }
        if (TabFragment.context == null) {
            TabFragment.context = context;
        }

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_TITLE, section);
        TabFragment fragment = new TabFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.headlinesRecyclerView);
        progressBar = view.findViewById(R.id.headlinesProgressBar);
        fetchingNews = view.findViewById(R.id.headlinesFetchingNews);

        // load RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(context,DividerItemDecoration.VERTICAL));
        mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerAdapter(context, newsItems, ConstantPool.ARTICLE_DETAIL_FROM_SECTION);

        // if mTitle = world, which means that we need to request server now and show initialized world section news
        Bundle arguments = getArguments();
        if (arguments != null) {
            mTitle = arguments.getString(BUNDLE_KEY_TITLE, "");
        }
        if (!mTitle.equals("world")) {
            return;
        }

        // request server and show initialized world section news
        StringRequest request = new StringRequest(ConstantPool.LATEST_SECTION_NEWS_PATH + "?section=world", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
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
                        progressBar.setVisibility(View.INVISIBLE);
                        fetchingNews.setVisibility(View.INVISIBLE);

                        recyclerView.setAdapter(mAdapter);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mTitle = arguments.getString(BUNDLE_KEY_TITLE, "");
        }
    }



    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public TextView getFetchingNews() {
        return fetchingNews;
    }

    public void setFetchingNews(TextView fetchingNews) {
        this.fetchingNews = fetchingNews;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public RecyclerView.Adapter getmAdapter() {
        return mAdapter;
    }

    public void setmAdapter(RecyclerView.Adapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    public LinearLayoutManager getmLayoutManager() {
        return mLayoutManager;
    }

    public void setmLayoutManager(LinearLayoutManager mLayoutManager) {
        this.mLayoutManager = mLayoutManager;
    }

    public List<NewsItem> getNewsItems() {
        return newsItems;
    }

    public void setNewsItems(List<NewsItem> newsItems) {
        this.newsItems = newsItems;
    }
}
