# Android NewsApp
*Video: https://www.youtube.com/watch?v=RA5mbbbszp0*

## 1. Introduction
* Created a **Splash Screen** requesting location permission to get geographic information and calling OpenWeatherMap API to get weather data.
* Implemented showing articles with animation, dialog, **progress bar**, and **swipe refresh** via RecyclerView and ViewPager.
* Completed search functionality and **auto suggestions** via SearchView, AutoCompleteTextView, and ArrayAdapter.
* Used **Volley** for HTTP request, **Picasso** for downloading and caching images, and **SharedPreferences** for data storage.

## 2. Tech Stacks
**Volley**: http request  
**Picasso**: powerful image downloading and caching library  
**MPAndroidChart**: create graph  
Progress Bar  
SearchView and AutoCompleteTextView: search functionality  
Splash Screen: welcome page  
Adding ellipsis to long strings  
Multiple Tabs: ViewPager + Adapter + Fragment  
RecyclerView  
Toast  
Swipe Refresh  
Dialog  

## 3. API
OpenWeatherMap API  
Guardian News API  
Google Trends API  
Microsoft Azure Bing Autosuggest API  

## 4. Config
compileSdkVersion 29  
buildToolsVersion "29.0.3"  
minSdkVersion 21  
targetSdkVersion 29  

Emulator: **_Pixel 2XL_**  

## 5. Functionalities

### 5-1 Splash Activity (Welcome Page)
* check, request, and get location permission  
* after getting location permission, get geographic information, i.e. state and city  
* request **_OpenWeatherMap API_** to get weather information  
* transfer to MainActivity  

### 5-2 MainActivity
(1) Home  
* parse weather information  
* request **_Guardian News API_** to get latest news  
* load RecyclerView and show news list  
  `RecyclerView + LinearLayoutManager + MyRecyclerAdapter + ViewHolder`  
* show dialog  
* check and uncheck bookmark  
* transfer to twitter page  
* transfer to ArticleDetailActivity  
* swipe refresh (SwipeRefreshLayout)  
* progress bar  
    
(2) Headlines  
* initialize ViewPaper  
  `ViewPaper + FragmentStatePagerAdapter + TabFragment`  
* request **_Guardian News API_** to get news by section  
* switch sections with animation  
* show dialog  
* check and uncheck bookmark  
* transfer to twitter page  
* transfer to ArticleDetailActivity  
* swipe refresh (SwipeRefreshLayout)  
* progress bar  
    
(3) Trending  
* request **_Google Trends API_** to get trending data  
* draw and show Line Chart by **_MPAndroidChart_**  
    
(4) Bookmarks  
* get bookmark list from SharedPreferences  
* load RecyclerView and show bookmark list  
  `RecyclerView + GridLayoutManager + RecyclerViewGridAdapter + GridViewHolder`  
* show dialog  
* uncheck bookmark  
* transfer to twitter page  
* transfer to ArticleDetailActivity  
    
(5) Search Functionality  
* auto suggestions  
  `SearchView + AutoCompleteTextView + ArrayAdapter`  
* transfer to SearchResultActivity  

### 5-3 SearchResultActivity
* request **_Guardian News API_** to get news by keyword  
* show dialog  
* check and uncheck bookmark  
* transfer to twitter page  
* transfer to ArticleDetailActivity  
* swipe refresh (SwipeRefreshLayout)  
* progress bar  

### 5-4 ArticleDetailActivity
* request **_Guardian News API_** to get the article detail by id  
* show article detail  
* check and uncheck bookmark  
* transfer to twitter page  
* transfer to original article page by accessing article url  
