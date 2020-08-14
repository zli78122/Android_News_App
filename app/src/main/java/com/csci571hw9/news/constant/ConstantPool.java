package com.csci571hw9.news.constant;

public class ConstantPool {
//  public static final String SERVER_ADDRESS = "http://192.168.1.17:5000";
    public static final String SERVER_ADDRESS = "https://node-zli78122.appspot.com";
//  public static final String BING_REQUEST_KEY = "8ff2d8033a66431a97467891439fc584";
    public static final String BING_REQUEST_KEY = "4f1fd28f1afc4edd8a988bd6a6f7dc0e";
    public static final String GUARDIAN_DEFAULT_IMAGE = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
    public static final String BING_REQUEST_LINK = "https://api.cognitive.microsoft.com/bing/v7.0/suggestions?q=";
    public static final String SEARCH_BY_KEYWORD = SERVER_ADDRESS + "/guardian/searchByKeyword?keyword=";
    public static final String SEARCH_BY_ID = SERVER_ADDRESS + "/guardian/searchById?id=";
    public static final String LATEST_NEWS_PATH = SERVER_ADDRESS + "/guardian/latest_news";
    public static final String GOOGLE_TRENDING_PATH = SERVER_ADDRESS + "/trending?keyword=";
    public static final String WEATHER_REQUEST_LINK = "https://api.openweathermap.org/data/2.5/weather?units=metric&appid=9e46c291da3fd43389957463acbc8327&q=";
    public static final String LATEST_SECTION_NEWS_PATH = SERVER_ADDRESS + "/guardian/latest_section_news";
    public static final int ARTICLE_DETAIL_FROM_MAIN = 2;
    public static final int ARTICLE_DETAIL_FROM_SECTION = 4;
    public static final int ARTICLE_DETAIL_FROM_SEARCH = 8;
    public static final int ARTICLE_DETAIL_FROM_BOOKMARK = 16;
}
