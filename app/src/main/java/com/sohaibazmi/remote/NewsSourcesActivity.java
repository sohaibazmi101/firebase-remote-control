package com.sohaibazmi.remote;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewsSourcesActivity extends AppCompatActivity {

    private ListView newsListView;
    private Map<String, String> newsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_sources);

        newsListView = findViewById(R.id.newsListView);

        newsMap = new HashMap<>();
        newsMap.put("Al Jazeera", "https://www.aljazeera.com/news/");
        newsMap.put("BBC News", "https://www.bbc.com/news");
        newsMap.put("NDTV", "https://www.ndtv.com/latest");
        newsMap.put("The Hindu", "https://www.thehindu.com/news/");
        newsMap.put("Reuters India", "https://www.reuters.com/places/india/");
        newsMap.put("MoneyControl", "https://www.moneycontrol.com/news/business/");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(newsMap.keySet()));
        newsListView.setAdapter(adapter);

        newsListView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String newsName = adapter.getItem(position);
            String newsUrl = newsMap.get(newsName);

            Intent intent = new Intent(NewsSourcesActivity.this, GameWebViewActivity.class);
            intent.putExtra("url", newsUrl);
            intent.putExtra("name", newsName);
            startActivity(intent);
        });
    }
}
