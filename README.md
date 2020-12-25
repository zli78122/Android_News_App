# Android NewsApp
*Video: https://www.youtube.com/watch?v=RA5mbbbszp0*

## Tech Stacks
**Volley**: http request  
**Picasso**: powerful image downloading and caching library  
**MPAndroidChart**: create graph  
**Progress Bar**  
**SearchView** and **AutoCompleteTextView**: search functionality  
**Splash Screen**: welcome page  
Adding ellipsis to long strings  
Multiple Tabs: **ViewPager** + **Adapter** + **Fragment**  
**RecyclerView**  
**Toast**  
**Swipe Refresh**  
**Dialog**  

## API
OpenWeatherMap API  
Guardian News API  
Google Trends API  
Microsoft Azure Bing Autosuggest API  

## 1. Config
compileSdkVersion 29  
buildToolsVersion "29.0.3"  
minSdkVersion 21  
targetSdkVersion 29  

Emulator: **_Pixel 2XL_**  

## 2. Functionalities

### 2-1 Splash Activity (Welcome Page)
①. check, request, and get location permission  
②. after getting location permission, get geographic information, i.e. state and city  
③. request **_OpenWeatherMap API_** to get weather information  
④. transfer to MainActivity  

### 2-2 MainActivity
①. Home  
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
    
②. Headlines  
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
    
③. Trending  
* request **_Google Trends API_** to get trending data  
* draw and show Line Chart by **_MPAndroidChart_**  
    
④. Bookmarks  
* get bookmark list from SharedPreferences  
* load RecyclerView and show bookmark list  
  `RecyclerView + GridLayoutManager + RecyclerViewGridAdapter + GridViewHolder`  
* show dialog  
* uncheck bookmark  
* transfer to twitter page  
* transfer to ArticleDetailActivity  
    
⑤. Search Functionality  
* auto suggestions  
  `SearchView + AutoCompleteTextView + ArrayAdapter`  
* transfer to SearchResultActivity  
    
### 2-3 SearchResultActivity
①. request **_Guardian News API_** to get news by keyword  
②. show dialog  
③. check and uncheck bookmark  
④. transfer to twitter page  
⑤. transfer to ArticleDetailActivity  
⑥. swipe refresh (SwipeRefreshLayout)  
⑦. progress bar  

### 2-4 ArticleDetailActivity
①. request **_Guardian News API_** to get the article detail by id  
②. show article detail  
③. check and uncheck bookmark  
④. transfer to twitter page  
⑤. transfer to original article page by accessing article url  

## 3. Data Structure in SharedPreference
```
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <set name="BOOKMARKS_IDS">
        <string>football/live/2020/may/06/internazionale-v-sampdoria-serie-a-1990-91-live</string>
        <string>world/live/2020/may/07/coronavirus-live-news-trump-says-covid-19-task-force-to-continue-indefinitely-as-us-china-rift-widens</string>
        <string>world/live/2020/may/06/coronavirus-update-cases-uk-live-news-us-task-force-trump-covid-19-usa-world-europe-deaths-brazil-brasil-israel</string>
    </set>
    <set name="football/live/2020/may/06/internazionale-v-sampdoria-serie-a-1990-91-live">
        <string>section:Football</string>
        <string>image:https://media.guim.co.uk/34e21d496b7e5c80871002bcbdd756e2c0cd69a0/0_185_2477_1486/500.jpg</string>
        <string>title:Internazionale v Sampdoria: Serie A, 1990-91 – live!</string>
        <string>date:06 May</string>
    </set>
    <set name="world/live/2020/may/07/coronavirus-live-news-trump-says-covid-19-task-force-to-continue-indefinitely-as-us-china-rift-widens">
        <string>section:World news</string>
        <string>image:https://media.guim.co.uk/1fe97c578f0883d85c51fc27d02a2786f8bddc91/0_213_6000_3600/500.jpg</string>
        <string>title:Coronavirus live news: Trump says Covid-19 taskforce to continue 'indefinitely' as US-China rift widens</string>
        <string>date:06 May</string>
    </set>
    <set name="world/live/2020/may/06/coronavirus-update-cases-uk-live-news-us-task-force-trump-covid-19-usa-world-europe-deaths-brazil-brasil-israel">
        <string>section:World news</string>
        <string>image:https://media.guim.co.uk/03cdefa2bfd37e0cefe32c1ed8392726d9218286/0_37_6620_3970/500.jpg</string>
        <string>title:Trump suggests more deaths necessary price to reopen economy – as it happened</string>
        <string>date:06 May</string>
    </set>
</map>
```
