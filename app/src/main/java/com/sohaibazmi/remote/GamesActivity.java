package com.sohaibazmi.remote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class GamesActivity extends AppCompatActivity {

    private GridView gamesGridView;
    private List<GameItem> gameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);

        gamesGridView = findViewById(R.id.gamesGridView);
        gameList = getGames();

        List<String> gameNames = new ArrayList<>();
        for (GameItem game : gameList) {
            gameNames.add(game.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gameNames);
        gamesGridView.setAdapter(adapter);

        gamesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GameItem selectedGame = gameList.get(position);
                Intent intent = new Intent(GamesActivity.this, GameWebViewActivity.class);
                intent.putExtra("url", selectedGame.getUrl());
                intent.putExtra("name", selectedGame.getName());
                startActivity(intent);
            }
        });
    }

    private List<GameItem> getGames() {
        List<GameItem> games = new ArrayList<>();
        games.add(new GameItem("🎯 Darts", "https://www.crazygames.com/game/darts"));
        games.add(new GameItem("🚗 Car Racing", "https://www.crazygames.com/game/madalin-stunt-cars-2"));
        games.add(new GameItem("🏹 Archery", "https://www.crazygames.com/game/stickman-archery"));
        games.add(new GameItem("🧩 Puzzle", "https://www.crazygames.com/game/2048"));
        games.add(new GameItem("⚽ Football", "https://www.crazygames.com/game/soccer-random"));
        games.add(new GameItem("🏎️ Formula Racing", "https://www.crazygames.com/game/extreme-car-driving-simulator"));
        games.add(new GameItem("🚀 Space Shooter", "https://www.crazygames.com/game/spaceguard-io"));
        games.add(new GameItem("🎲 Ludo", "https://www.crazygames.com/game/ludo-hero"));
        games.add(new GameItem("🕹️ Snake", "https://playsnake.org/"));
        games.add(new GameItem("🧱 Brick Breaker", "https://www.crazygames.com/game/brick-breaker"));
        games.add(new GameItem("🏀 Basketball", "https://www.crazygames.com/game/basketball-stars"));
        games.add(new GameItem("🃏 Solitaire", "https://www.crazygames.com/game/solitaire-classic"));
        games.add(new GameItem("🚴 BMX Rider", "https://www.crazygames.com/game/bike-trials-offroad-1"));
        games.add(new GameItem("🎡 Roller Coaster", "https://www.crazygames.com/game/uphill-climb-racing-3"));
        games.add(new GameItem("💣 Bomberman", "https://www.crazygames.com/game/bomb-it"));
        games.add(new GameItem("🐍 Slither.io", "https://slither.io/"));
        games.add(new GameItem("🔫 Shooting Arena", "https://www.crazygames.com/game/counter-craft-2-zombies"));
        games.add(new GameItem("👊 Boxing", "https://www.crazygames.com/game/boxing-physics-2"));
        games.add(new GameItem("🎳 Bowling", "https://www.crazygames.com/game/classic-bowling"));
        games.add(new GameItem("🏰 Tower Defense", "https://www.crazygames.com/game/cursed-treasure"));
        games.add(new GameItem("🛹 Skater", "https://www.crazygames.com/game/stickman-skater"));
        games.add(new GameItem("🦖 Dinosaur", "https://chromedino.com/"));
        games.add(new GameItem("👽 Alien Invasion", "https://www.crazygames.com/game/alien-invaders-io"));
        games.add(new GameItem("💎 Bejeweled", "https://www.crazygames.com/game/diamond-rush"));
        games.add(new GameItem("🧗 Climbing", "https://www.crazygames.com/game/getaway-shootout"));
        return games;
    }

    private static class GameItem {
        private final String name;
        private final String url;

        public GameItem(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }
}
