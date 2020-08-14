package com.csci571hw9.news;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.csci571hw9.news.adapter.MyRecyclerAdapter;
import com.csci571hw9.news.adapter.RecyclerViewGridAdapter;
import com.csci571hw9.news.constant.ConstantPool;
import com.csci571hw9.news.entity.NewsItem;
import com.csci571hw9.news.fragment.TabFragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pixel 2 XL: 411 * 823, DPR = 3.5
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private int displayWidthPixels; // 1440
    private int displayHeightPixels; // 2712, not include system bar in the top screen

    private int curMainIndex = 0;

    private RequestQueue queue;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private List<NewsItem> newsItems = new ArrayList<>();
    private TextView fetchingNews;
    private ProgressBar progressBar;

    private String city;
    private String state;
    private String Summary;
    private String Temperature;
    private ImageView weatherImageView;
    private TextView weatherCity;
    private TextView weatherState;
    private TextView weatherSummary;
    private TextView weatherTemperature;

    private LinearLayout homeButton;
    private LinearLayout headlinesButton;
    private LinearLayout trendButton;
    private LinearLayout bookmarkButton;

    private RelativeLayout homeMain;
    private RelativeLayout headlinesMain;
    private RelativeLayout trendMain;
    private RelativeLayout bookmarksMain;

    private TextView homeNavText;
    private ImageView homeNavImage;
    private ImageView homeNavImageActive;
    private TextView headlinesNavText;
    private ImageView headlinesNavImage;
    private ImageView headlinesNavImageActive;
    private TextView trendNavText;
    private ImageView trendNavImage;
    private ImageView trendNavImageActive;
    private TextView bookmarksNavText;
    private ImageView bookmarksNavImage;
    private ImageView bookmarksNavImageActive;

    private ViewPager mVpMain;
    private List<String> mTitles = new ArrayList<>(Arrays.asList("world", "business", "politics", "sport", "technology", "science"));
    private SparseArray<TabFragment> mFragments = new SparseArray<>();

    private HorizontalScrollView sectionScroll;
    private List<Button> sectionTabs = new ArrayList<>(); // six section buttons
    private List<CardView> sectionBars = new ArrayList<>(); // six section bars
    private CardView sectionBar;
    private int[] sectionWidths = new int[]{252, 318, 300, 274, 399, 282};
    private int[] sectionSubSumWidths = new int[]{0, 252, 570, 870, 1144, 1543, 1825};
    private boolean scrollInLeft = true;
    private int[] sectionTabAndScrollBarInfo = new int[]{0, 0, 252, 252}; // [curPosition, left, right, width]

    private EditText chartEditText;
    private LineChart lineChart;

    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private SearchView searchView;
    private String[] searchItem;
    private ArrayAdapter<String> searchAdapter;
    private AutoCompleteTextView autoComplete;
    private Handler handler;
    private long lastRequest = 0;

    private ImageView headSearchIcon;
    private ImageView arrowBackIcon;
    private TextView headTitle;

    public static SharedPreferences sp;

    private RecyclerView bookmarkRecyclerView;
    private List<NewsItem> bookmarks;
    private RecyclerViewGridAdapter adapter;
    private TextView noBookmark;

    private SwipeRefreshLayout homeSwipeRefresh;
    private SwipeRefreshLayout headlinesSwipeRefresh;

    public static int ARTICLE_DETAIL_ORIGIN = 0;
    public static boolean SEARCH_ACTIVITY_TO_MAIN_ACTIVITY = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        displayWidthPixels = outMetrics.widthPixels;
        displayHeightPixels = outMetrics.heightPixels;

        queue = Volley.newRequestQueue(this);

        sp = getSharedPreferences("NewsApp", Context.MODE_PRIVATE);
        Set<String> bookmarksIds = sp.getStringSet("BOOKMARKS_IDS", null);
        if (bookmarksIds == null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putStringSet("BOOKMARKS_IDS", new HashSet<String>()).apply();
        }

        initHandler();

        initSearchBar();

        initHomeViews();
        parseWeatherInfo();
        getLatestNews();

        initHeadlinesView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // synchronize bookmark data from ArticleDetailActivity to MainActivity
        if (ARTICLE_DETAIL_ORIGIN == ConstantPool.ARTICLE_DETAIL_FROM_MAIN) {
            recyclerView.setAdapter(mAdapter);
            ARTICLE_DETAIL_ORIGIN = 0;
            return;
        } else if (ARTICLE_DETAIL_ORIGIN == ConstantPool.ARTICLE_DETAIL_FROM_SECTION) {
            TabFragment newFragment = mFragments.get(sectionTabAndScrollBarInfo[0]);
            newFragment.getRecyclerView().setAdapter(newFragment.getmAdapter());
            ARTICLE_DETAIL_ORIGIN = 0;
            return;
        } else if (ARTICLE_DETAIL_ORIGIN == ConstantPool.ARTICLE_DETAIL_FROM_BOOKMARK) {
            loadGridData();
            ARTICLE_DETAIL_ORIGIN = 0;
            return;
        }

        // synchronize bookmark data from SearchResultActivity to MainActivity
        if (SEARCH_ACTIVITY_TO_MAIN_ACTIVITY) {
            if (curMainIndex == 0) {
                recyclerView.setAdapter(mAdapter);
            } else if (curMainIndex == 1) {
                TabFragment newFragment = mFragments.get(sectionTabAndScrollBarInfo[0]);
                newFragment.getRecyclerView().setAdapter(newFragment.getmAdapter());
            } else if (curMainIndex == 3) {
                loadGridData();
            }
            SEARCH_ACTIVITY_TO_MAIN_ACTIVITY = false;
        }
    }



    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    // send request to Microsoft Bing Autosuggest API
                    String searchViewText = msg.getData().getString("searchViewText");
                    StringRequest request = new StringRequest(ConstantPool.BING_REQUEST_LINK + searchViewText, new Response.Listener<String>() {
                        @RequiresApi(api = Build.VERSION_CODES.Q)
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray suggestionGroups = jsonObject.getJSONArray("suggestionGroups");
                                if (suggestionGroups.length() > 0) {
                                    JSONArray searchSuggestions = ((JSONObject) suggestionGroups.get(0)).getJSONArray("searchSuggestions");
                                    if (searchSuggestions.length() > 0) {
                                        int length = Math.min(searchSuggestions.length(), 5);
                                        searchItem = new String[length];
                                        for (int i = 0; i < length; i++) {
                                            searchItem[i] = ((JSONObject) searchSuggestions.get(i)).getString("displayText");
                                        }
                                    }
                                }

                                searchAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, searchItem);
                                autoComplete.setAdapter(searchAdapter);
                                // refresh auto complete results
                                autoComplete.refreshAutoCompleteResults();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Request Error", error.toString());
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("Ocp-Apim-Subscription-Key", ConstantPool.BING_REQUEST_KEY);
                            return params;
                        }
                    };
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
                return false;
            }
        });
    }

    private void initSearchBar() {
        headTitle = findViewById(R.id.headTitle);
        headSearchIcon = findViewById(R.id.headSearchIcon);
        headSearchIcon.setOnClickListener(this);
        arrowBackIcon = findViewById(R.id.arrowBackIcon);
        arrowBackIcon.setOnClickListener(this);

        searchView = findViewById(R.id.searchView);
        searchView.onActionViewExpanded();
        searchView.clearFocus();

        // Focus Change Listener
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    autoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
                    // when the length of searchView's text is at least 3, there will be dropdown list
                    autoComplete.setThreshold(3);
                    autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                            // Notice: a very unobservable bug
                            //   after executing autoComplete.setText(), the code will not run handler.removeMessages() sequentially
                            //   the code will run onQueryTextChange() of searchView's QueryTextListener at first
                            //   because after executing autoComplete.setText(), searchView's text may be changed
                            //   therefore, onQueryTextChange() is triggered simultaneously
                            //   after that, the code will sequentially run handler.removeMessages()
                            autoComplete.setText((String) adapterView.getItemAtPosition(position));
                            // handler.removeMessages() must be executed here
                            // if we do not execute handler.removeMessages() here
                            // onQueryTextChange() will send message to handler, then handler request Microsoft Bing autosuggest API
                            // returned data from Bing will refresh dropdown list, finally dropdown list will appear again
                            // handler.removeMessages() here can remove the message sent to handler, therefore, solving the bug
                            handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                        }
                    });
                } else {
                    autoComplete.setText("");
                    autoComplete = null;
                    searchAdapter = null;
                    searchItem = null;
                }
            }
        });

        // Text Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Transfer to SearchResultActivity
                Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // send message to Handler, then Handler requests Microsoft Bing Autosuggest API to get search suggestions
                // Notice: do not send requests frequently, send at most one request to Microsoft Bing Autosuggest API each second
                if (newText.trim().length() >= 2) {
                    handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                    Message msg = new Message();
                    msg.what = TRIGGER_AUTO_COMPLETE;
                    Bundle bundle = new Bundle();
                    bundle.putString("searchViewText", newText.trim());
                    msg.setData(bundle);
                    long now = new Date().getTime();
                    if (now - lastRequest > 1050) {
                        handler.sendMessageDelayed(msg, 10);
                    } else {
                        handler.sendMessageDelayed(msg, 1050 - (now - lastRequest));
                    }
                    lastRequest = now;
                }
                return false;
            }
        });
    }



    private void initTrendingView() {
        chartEditText = findViewById(R.id.inputChartLine);
        // No keyboard shown when editText getting focus
        chartEditText.setShowSoftInputOnFocus(false);
        chartEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                // Trigger when clicking send button on keyboard
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    requestGoogleTrending(view.getText().toString());
                }
                // return true: keep keyboard
                // return false: hide keyboard
                return true;
            }
        });

        lineChart = findViewById(R.id.lineChart);
    }

    private void requestGoogleTrending(final String keyword) {
        StringRequest request = new StringRequest(ConstantPool.GOOGLE_TRENDING_PATH + keyword, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("status").equals("ok")) {
                        JSONArray data = jsonObject.getJSONArray("res");
                        // when data come back, however, user has left the trending page, do not render the chart
                        if (curMainIndex == 2) {
                            drawLineChart(data, keyword);
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

    private void drawLineChart(JSONArray data, String keyword) throws JSONException {
        // Generate Line Chart
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            entries.add(new Entry(i * 1.0f, data.getInt(i) * 1.0f));
        }
        LineDataSet lineDataSet = new LineDataSet(entries, "Trending Chart for " + keyword);
        lineDataSet.setCircleColor(Color.rgb(94, 62, 153));
        lineDataSet.setCircleHoleColor(Color.rgb(94, 62, 153));
        lineDataSet.setLineWidth(1);
        lineDataSet.setColor(Color.rgb(94, 62, 153));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        LineData lineData = new LineData(dataSets);
        lineData.setValueTextColor(Color.rgb(94, 62, 153));
        lineData.setValueTextSize(8);

        // Show Top Border
        lineChart.getXAxis().setDrawAxisLine(true);
        // Show Right Border
        lineChart.getAxisRight().setDrawAxisLine(true);
        // Remove Vertical Grids
        lineChart.getXAxis().setDrawGridLines(false);
        // Remove Left Border
        lineChart.getAxisLeft().setDrawAxisLine(false);
        // Remove Horizontal Grids
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawGridLines(false);

        Legend legend = lineChart.getLegend();
        // Set Label Offset
        legend.setXOffset(4);
        legend.setYOffset(8);
        // Set Label Form
        legend.setFormSize(17);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextColor(Color.BLACK);
        // Set Label Text
        legend.setTextSize(16);

        lineChart.setData(lineData);
        // Refresh Chart
        lineChart.invalidate();
    }



    private void initHeadlinesView() {
        sectionTabs.add((Button) findViewById(R.id.sectionWorld));
        sectionTabs.add((Button) findViewById(R.id.sectionBusiness));
        sectionTabs.add((Button) findViewById(R.id.sectionPolitics));
        sectionTabs.add((Button) findViewById(R.id.sectionSport));
        sectionTabs.add((Button) findViewById(R.id.sectionTechnology));
        sectionTabs.add((Button) findViewById(R.id.sectionScience));

        sectionBars.add((CardView) findViewById(R.id.worldBar));
        sectionBars.add((CardView) findViewById(R.id.businessBar));
        sectionBars.add((CardView) findViewById(R.id.politicsBar));
        sectionBars.add((CardView) findViewById(R.id.sportBar));
        sectionBars.add((CardView) findViewById(R.id.technologyBar));
        sectionBars.add((CardView) findViewById(R.id.scienceBar));

        sectionBar = findViewById(R.id.sectionBar);
        sectionScroll = findViewById(R.id.sectionScroll);

        headlinesSwipeRefresh = findViewById(R.id.headlinesSwipeRefresh);
        headlinesSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestNewsBySection(sectionTabAndScrollBarInfo[0]);
            }
        });
    }

    private void requestNewsBySection(final int position) {
        final TabFragment newFragment = mFragments.get(position);
        StringRequest request = new StringRequest(ConstantPool.LATEST_SECTION_NEWS_PATH + "?section=" + newFragment.getmTitle(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                newFragment.getNewsItems().clear();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("status").equals("ok")) {
                        JSONArray data = jsonObject.getJSONArray("data");
                        JSONObject object;
                        for (int i = 0; i < data.length(); i++) {
                            object = data.getJSONObject(i);
                            newFragment.getNewsItems().add(new NewsItem(object.getString("id"),
                                    object.getString("image"),
                                    object.getString("title"),
                                    object.getString("time"),
                                    object.getString("section")));
                        }

                        newFragment.getProgressBar().setVisibility(View.INVISIBLE);
                        newFragment.getFetchingNews().setVisibility(View.INVISIBLE);

                        newFragment.getRecyclerView().setAdapter(newFragment.getmAdapter());

                        if(headlinesSwipeRefresh.isRefreshing()) {
                            headlinesSwipeRefresh.setRefreshing(false);
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

    private void initViewPager() {
        mVpMain = findViewById(R.id.vp_main);
        mVpMain.setOffscreenPageLimit(mTitles.size());
        mVpMain.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                TabFragment fragment = TabFragment.newInstance(mTitles.get(position), queue, MainActivity.this);
                return fragment;
            }

            @Override
            public int getCount() {
                return mTitles.size();
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                TabFragment fragment = (TabFragment) super.instantiateItem(container, position);
                mFragments.put(position, fragment);
                return fragment;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                mFragments.remove(position);
                mFragments.delete(position);
                super.destroyItem(container, position, object);
            }
        });

        mVpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            /**
             * onPageSelected() will be called when switching the page of ViewPager
             */
            @Override
            public void onPageSelected(final int position) {
                // Reset Previous Tab
                TabFragment oldFragment = mFragments.get(sectionTabAndScrollBarInfo[0]);
                oldFragment.getFetchingNews().setVisibility(View.VISIBLE);
                oldFragment.getProgressBar().setVisibility(View.VISIBLE);
                oldFragment.getNewsItems().clear();
                oldFragment.getRecyclerView().setAdapter(oldFragment.getmAdapter());

                // Initialize New Tab (Request Data)
                requestNewsBySection(position);

                final int WIDTH = sectionWidths[position];
                final int LEFT;
                final int RIGHT;

                // Compute New Width, Left, and Right
                if (scrollInLeft && position > 3) {
                    // scroll bar moves to right
                    sectionScroll.arrowScroll(View.FOCUS_RIGHT);
                    scrollInLeft = false;

                    RIGHT = displayWidthPixels - (sectionSubSumWidths[sectionSubSumWidths.length - 1] - sectionSubSumWidths[position + 1]);
                    LEFT = RIGHT - WIDTH;
                } else if (!scrollInLeft && position < 2) {
                    // scroll bar moves to left
                    sectionScroll.arrowScroll(View.FOCUS_LEFT);
                    scrollInLeft = true;

                    LEFT = sectionSubSumWidths[position];
                    RIGHT = LEFT + WIDTH;
                } else if (scrollInLeft) {
                    // scroll is in left, and scroll bar does not need to move
                    LEFT = sectionSubSumWidths[position];
                    RIGHT = LEFT + WIDTH;
                } else {
                    // scroll is in right, and scroll bar does not need to move
                    RIGHT = displayWidthPixels - (sectionSubSumWidths[sectionSubSumWidths.length - 1] - sectionSubSumWidths[position + 1]);
                    LEFT = RIGHT - WIDTH;
                }
                Log.i("ScrollBar", "LEFT: " + LEFT + " , RIGHT: " + RIGHT + " , WIDTH: " + WIDTH);

                // Reset sectionBar's width, left and right
                ViewGroup.LayoutParams para = sectionBar.getLayoutParams();
                para.width = WIDTH;
                sectionBar.setLayoutParams(para);
                sectionBar.setLeft(sectionTabAndScrollBarInfo[1]);
                sectionBar.setRight(sectionTabAndScrollBarInfo[1] + WIDTH);

                // Animation
                TranslateAnimation animation = new TranslateAnimation(Animation.ABSOLUTE, sectionTabAndScrollBarInfo[1], Animation.ABSOLUTE, LEFT, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
                animation.setDuration(100);
                animation.setFillAfter(true);
                sectionBar.setVisibility(View.VISIBLE);
                sectionBars.get(sectionTabAndScrollBarInfo[0]).setVisibility(View.INVISIBLE); // hide previous section bar
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        sectionBar.clearAnimation();
                        sectionBar.setLeft(LEFT);
                        sectionBar.setRight(RIGHT);

                        sectionBars.get(position).setVisibility(View.VISIBLE); // show current section bar
                        sectionBar.setVisibility(View.INVISIBLE);
                    }
                });
                sectionBar.startAnimation(animation);



                // Synchronize data of scroll bar
                sectionTabAndScrollBarInfo[0] = position;
                sectionTabAndScrollBarInfo[1] = LEFT;
                sectionTabAndScrollBarInfo[2] = RIGHT;
                sectionTabAndScrollBarInfo[3] = WIDTH;
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    private void initHeadlinesClickEvent() {
        for (int i = 0; i < sectionTabs.size(); i++) {
            Button button = sectionTabs.get(i);
            final int finalI = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mVpMain.setCurrentItem(finalI, false);
                }
            });
        }
    }



    private void initHomeViews() {
        homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(this);
        headlinesButton = findViewById(R.id.headlinesButton);
        headlinesButton.setOnClickListener(this);
        trendButton = findViewById(R.id.trendButton);
        trendButton.setOnClickListener(this);
        bookmarkButton = findViewById(R.id.bookmarkButton);
        bookmarkButton.setOnClickListener(this);

        homeNavText = findViewById(R.id.homeNavText);
        homeNavImage = findViewById(R.id.homeNavImage);
        homeNavImageActive = findViewById(R.id.homeNavImageActive);
        headlinesNavText = findViewById(R.id.headlinesNavText);
        headlinesNavImage = findViewById(R.id.headlinesNavImage);
        headlinesNavImageActive = findViewById(R.id.headlinesNavImageActive);
        trendNavText = findViewById(R.id.trendNavText);
        trendNavImage = findViewById(R.id.trendNavImage);
        trendNavImageActive = findViewById(R.id.trendNavImageActive);
        bookmarksNavText = findViewById(R.id.bookmarksNavText);
        bookmarksNavImage = findViewById(R.id.bookmarksNavImage);
        bookmarksNavImageActive = findViewById(R.id.bookmarksNavImageActive);

        homeMain = findViewById(R.id.homeMain);
        headlinesMain = findViewById(R.id.headlinesMain);
        trendMain = findViewById(R.id.trendingMain);
        bookmarksMain = findViewById(R.id.bookmarksMain);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fetchingNews = (TextView) findViewById(R.id.fetchingNews);

        loadRecyclerView();

        homeSwipeRefresh = findViewById(R.id.homeSwipeRefresh);
        homeSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLatestNews();
            }
        });
    }

    private void loadRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerAdapter(this, newsItems, ConstantPool.ARTICLE_DETAIL_FROM_MAIN);
    }

    private void getLatestNews() {
        StringRequest request = new StringRequest(ConstantPool.LATEST_NEWS_PATH, new Response.Listener<String>() {
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
                        progressBar.setVisibility(View.INVISIBLE);
                        fetchingNews.setVisibility(View.INVISIBLE);

                        recyclerView.setAdapter(mAdapter);

                        if(homeSwipeRefresh.isRefreshing()) {
                            homeSwipeRefresh.setRefreshing(false);
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

    private void parseWeatherInfo() {
        ImageView weatherImageView = (ImageView) findViewById(R.id.weatherImageView);
        weatherCity = (TextView) findViewById(R.id.weatherCity);
        weatherState = (TextView) findViewById(R.id.weatherState);
        weatherSummary = (TextView) findViewById(R.id.weatherSummary);
        weatherTemperature = (TextView) findViewById(R.id.weatherTemperature);

        Intent intent = getIntent();
        city = intent.getStringExtra("city");
        state = intent.getStringExtra("state");
        Summary = intent.getStringExtra("Summary");
        Temperature = intent.getIntExtra("Temperature", 20) + " ℃";

        switch (Summary) {
            case "Clouds":
                weatherImageView.setBackgroundResource(R.drawable.cloudy_weather);
                break;
            case "Clear":
                weatherImageView.setBackgroundResource(R.drawable.clear_weather);
                break;
            case "Snow":
                weatherImageView.setBackgroundResource(R.drawable.snowy_weather);
                break;
            case "Rain":
            case "Drizzle":
            case "Rain / Drizzle":
                weatherImageView.setBackgroundResource(R.drawable.rainy_weather);
                break;
            case "Thunderstorm":
                weatherImageView.setBackgroundResource(R.drawable.thunder_weather);
                break;
            default:
                weatherImageView.setBackgroundResource(R.drawable.sunny_weather);
                break;
        }
        weatherCity.setText(city);
        weatherState.setText(state);
        weatherSummary.setText(Summary);
        weatherTemperature.setText(Temperature);
    }



    private void loadGridData() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setReverseLayout(false);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        bookmarkRecyclerView.setLayoutManager(gridLayoutManager);

        // get bookmark data from SharedPreference
        String title = "", image = "", section = "", date = "";
        Set<String> bookmarks_ids = sp.getStringSet("BOOKMARKS_IDS", null);
        bookmarks = new ArrayList<>();
        for (String id : bookmarks_ids) {
            Set<String> bookmarkArticles = sp.getStringSet(id, null);
            for(String articleInfo : bookmarkArticles) {
                if (articleInfo.startsWith("title:")) {
                    title = articleInfo.substring(6);
                } else if (articleInfo.startsWith("image:")) {
                    image = articleInfo.substring(6);
                } else if (articleInfo.startsWith("section:")) {
                    section = articleInfo.substring(8);
                } else if (articleInfo.startsWith("date:")) {
                    date = articleInfo.substring(5);
                }
            }
            bookmarks.add(new NewsItem(id, image, title, date, section));
        }
        if (bookmarks.size() == 0) {
            noBookmark.setVisibility(View.VISIBLE);
        }
        // if the size of bookmarks is odd, add a dummy bookmark
        if (bookmarks.size() % 2 != 0) {
            bookmarks.add(new NewsItem("_dummy_id", "", "", "", ""));
        }

        adapter = new RecyclerViewGridAdapter(this, bookmarks, bookmarkRecyclerView, noBookmark);
        bookmarkRecyclerView.setAdapter(adapter);
    }



    @Override
    public void onClick(View v) {
        if (v == homeButton) {
            clearFooter();
            resetFooter(homeNavText, homeNavImage, homeNavImageActive, homeMain);
            clearMain();
            initHome();
        } else if (v == headlinesButton) {
            clearFooter();
            resetFooter(headlinesNavText, headlinesNavImage, headlinesNavImageActive, headlinesMain);
            clearMain();
            initHeadlines();
        } else if (v == trendButton) {
            clearFooter();
            resetFooter(trendNavText, trendNavImage, trendNavImageActive, trendMain);
            clearMain();
            initTrending();
        } else if (v == bookmarkButton) {
            clearFooter();
            resetFooter(bookmarksNavText, bookmarksNavImage, bookmarksNavImageActive, bookmarksMain);
            clearMain();
            initBookmarks();
        } else if (v == headSearchIcon) {
            headSearchIcon.setVisibility(View.INVISIBLE);
            headTitle.setVisibility(View.INVISIBLE);
            arrowBackIcon.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
            searchView.requestFocus();
            // Open Keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) searchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
        } else if (v == arrowBackIcon) {
            headSearchIcon.setVisibility(View.VISIBLE);
            headTitle.setVisibility(View.VISIBLE);
            searchView.clearFocus();
            searchView.setVisibility(View.GONE);
            arrowBackIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void clearHome() {
        newsItems.clear();
        recyclerView.setAdapter(mAdapter);
        if(homeSwipeRefresh.isRefreshing()) {
            homeSwipeRefresh.setRefreshing(false);
        }
    }
    private void initHome() {
        curMainIndex = 0;

        progressBar.setVisibility(View.VISIBLE);
        fetchingNews.setVisibility(View.VISIBLE);
        getLatestNews();
    }

    private void clearHeadlines() {
        // NOTICE: must clear listener, otherwise, later mVpMain will have multiple listeners
        mVpMain.clearOnPageChangeListeners();
        mVpMain = null;
        mFragments.clear();
        sectionBars.get(sectionTabAndScrollBarInfo[0]).setVisibility(View.INVISIBLE);
        sectionBars.get(0).setVisibility(View.VISIBLE);
        sectionScroll.arrowScroll(View.FOCUS_LEFT);
        if(headlinesSwipeRefresh.isRefreshing()) {
            headlinesSwipeRefresh.setRefreshing(false);
        }
    }
    private void initHeadlines() {
        curMainIndex = 1;

        sectionTabAndScrollBarInfo = new int[]{0, 0, 252, 252};
        scrollInLeft = true;
        initViewPager();
        initHeadlinesClickEvent();
    }

    private void clearTrending() {
        lineChart.clear();
        lineChart = null;
        chartEditText.setText("");
        chartEditText = null;
    }
    private void initTrending() {
        curMainIndex = 2;
        initTrendingView();
        requestGoogleTrending("CoronaVirus");
    }

    private void clearBookmarks() {
        bookmarks.clear();
        bookmarkRecyclerView.setAdapter(adapter);
        noBookmark.setVisibility(View.INVISIBLE);
        bookmarks = null;
        adapter = null;
    }
    private void initBookmarks() {
        curMainIndex = 3;

        noBookmark = findViewById(R.id.noBookmark);
        bookmarkRecyclerView = findViewById(R.id.bookmarkRecyclerView);
        loadGridData();
    }

    private void clearMain() {
        switch (curMainIndex) {
            case 0:
                clearHome();
                break;
            case 1:
                clearHeadlines();
                break;
            case 2:
                clearTrending();
                break;
            case 3:
                clearBookmarks();
                break;
        }
    }

    private void resetFooter(TextView navText, ImageView navImage, ImageView navImageActive, RelativeLayout main) {
        navText.setVisibility(View.VISIBLE);
        navImage.setVisibility(View.INVISIBLE);
        navImageActive.setVisibility(View.VISIBLE);
        main.setVisibility(View.VISIBLE);
    }

    private void clearFooter() {
        homeNavText.setVisibility(View.INVISIBLE);
        headlinesNavText.setVisibility(View.INVISIBLE);
        trendNavText.setVisibility(View.INVISIBLE);
        bookmarksNavText.setVisibility(View.INVISIBLE);

        homeNavImage.setVisibility(View.VISIBLE);
        headlinesNavImage.setVisibility(View.VISIBLE);
        trendNavImage.setVisibility(View.VISIBLE);
        bookmarksNavImage.setVisibility(View.VISIBLE);

        homeNavImageActive.setVisibility(View.INVISIBLE);
        headlinesNavImageActive.setVisibility(View.INVISIBLE);
        trendNavImageActive.setVisibility(View.INVISIBLE);
        bookmarksNavImageActive.setVisibility(View.INVISIBLE);

        homeMain.setVisibility(View.INVISIBLE);
        headlinesMain.setVisibility(View.INVISIBLE);
        trendMain.setVisibility(View.INVISIBLE);
        bookmarksMain.setVisibility(View.INVISIBLE);
    }
}
