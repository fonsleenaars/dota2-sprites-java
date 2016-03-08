package com.fonsleenaars.dota2sprites;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * Simple class to download the latest Dota2 Hero and Item data and creates a sprite and stylesheet.
 *
 * @author Fons Leenaars
 */
public class SpriteCreator {

    // SET THIS KEY TO YOUR OWN API KEYL
    private static final String API_KEY = "5D7EB0CED040EE00AFF7E30FD910A10C";

    // SPRITE / CSS VARS
    private static final String HEROES_CSS = "dota2-heroes.css";
    private static final String HEROES_SPRITE = "dota2-heroes-sprite.png";
    private static final int HERO_SPRITE_COLS = 10;
    private static final int HERO_SPRITE_WIDTH = 50;
    private static final int HERO_SPRITE_HEIGHT = 28;
    private static final String HERO_PREFIX_REMOVE = "npc_dota_hero_";
    private static final String ITEMS_CSS = "dota2-items.css";
    private static final String ITEMS_SPRITE = "dota2-items-sprite.png";
    private static final String ITEM_PREFIX_REMOVE = "item_";
    private static final int SPRITE_COLUMNS = 10;
    private static final int SPRITE_WIDTH = 37;
    private static final int SPRITE_HEIGHT = 28;
    private static final String SPRITE_FOLDER = "dota2sprites";

    // JSON KEYS
    private static final String JSON_HEROES = "heroes";
    private static final String JSON_ITEMS = "items";
    private static final String JSON_RESULT = "result";

    // API URLS
    private static final String API_HEROES_URL = "https://api.steampowered.com/IEconDOTA2_570/GetHeroes/v1/"
        + "?key=%s&language=en_us";
    private static final String API_ITEMS_URL = "https://api.steampowered.com/IEconDOTA2_570/GetGameItems/v1/"
        + "?key=%s" + "&language=en_us";
    private static final String HERO_IMG_URL = "http://cdn.dota2.com/apps/dota2/images/heroes/:name_lg.png";
    private static final String ITEM_IMAGE_DOWNLOAD = "http://cdn.dota2.com/apps/dota2/images/items/:name_lg.png";

    public static void main(String[] args) {
        String dir = System.getProperty("user.home") + File.separator + SPRITE_FOLDER + File.separator;
        if (args.length > 1) {
            dir = args[1];
        }

        String url = String.format(API_HEROES_URL, API_KEY);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            // Map JSON data from API to the node:
            JsonNode node = mapper.readTree(SpriteCreator.get(url));
            if (node.has(JSON_RESULT)) {
                JsonNode result = node.get(JSON_RESULT);
                List<Dota2Hero> heroes = mapper.convertValue(result.get(JSON_HEROES),
                    new TypeReference<List<Dota2Hero>>() {
                    });

                // Prepare Sprite & Sprite CSS
                int width = HERO_SPRITE_COLS * HERO_SPRITE_WIDTH;
                int height = (int) Math.ceil(heroes.size() / (double) HERO_SPRITE_COLS) * HERO_SPRITE_HEIGHT;
                BufferedImage heroesSprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = (Graphics2D) heroesSprite.getGraphics();

                // CSS per hero:
                String heroesSpriteCSS = String.format(
                    ".dota2-hero { background: url('%s'); "
                    + "width: %dpx; height: %dpx; "
                    + "border: 1px solid #454545; }\n",
                    HEROES_SPRITE,
                    HERO_SPRITE_WIDTH,
                    HERO_SPRITE_HEIGHT);

                int i = 0;
                for (Dota2Hero hero : heroes) {
                    URL heroURL = new URL(HERO_IMG_URL.replace(":name", hero.getName().replace(HERO_PREFIX_REMOVE, "")));
                    BufferedImage heroImage = ImageIO.read(heroURL);
                    Image resized = heroImage.getScaledInstance(HERO_SPRITE_WIDTH, HERO_SPRITE_HEIGHT, Image.SCALE_SMOOTH);

                    int x = (i % HERO_SPRITE_COLS) * HERO_SPRITE_WIDTH;
                    int y = (i / HERO_SPRITE_COLS) * HERO_SPRITE_HEIGHT;

                    heroesSpriteCSS += String.format(
                        ".dota2-hero-%d { background-position: %spx %spx; }\n",
                        hero.getId(),
                        x > 0 ? "-" + x : x,
                        y > 0 ? "-" + y : y);

                    g.drawImage(resized, x, y, HERO_SPRITE_WIDTH, HERO_SPRITE_HEIGHT, null);
                    i++;
                }

                // Clear the Graphics
                g.dispose();

                // Write the heroes sprite to the file:
                ImageIO.write(heroesSprite, "png", new File(dir + HEROES_SPRITE));
                FileUtils.writeStringToFile(new File(dir + HEROES_CSS), heroesSpriteCSS);
            }

            // Synchronize item data from API:
            url = String.format(API_ITEMS_URL, API_KEY);
            node = mapper.readTree(SpriteCreator.get(url));
            if (node.has(JSON_RESULT)) {
                JsonNode result = node.get(JSON_RESULT);
                List<Dota2Item> items = mapper.convertValue(result.get(JSON_ITEMS),
                    new TypeReference<List<Dota2Item>>() {
                    });

                // Prepare Sprite & Sprite CSS
                int width = SPRITE_COLUMNS * SPRITE_WIDTH;
                int height = (int) Math.ceil(items.size() / (double) SPRITE_COLUMNS) * SPRITE_HEIGHT;
                BufferedImage itemsSprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = (Graphics2D) itemsSprite.getGraphics();
                String itemsSpriteCSS = String.format(
                    ".dota2-item { background: url('%s'); "
                    + "width: %dpx; height: %dpx; "
                    + "border: 1px solid #454545; }\n",
                    ITEMS_SPRITE,
                    SPRITE_WIDTH,
                    SPRITE_HEIGHT);

                int i = 0;
                for (Dota2Item item : items) {
                    BufferedImage itemImage;
                    try {
                        URL itemURL = new URL(ITEM_IMAGE_DOWNLOAD.replace(":name", item.getName().replace(ITEM_PREFIX_REMOVE, "")));
                        itemImage = ImageIO.read(itemURL);
                    } catch (IOException ex) {
                        // No image for this item
                        continue;
                    }

                    Image resized = itemImage.getScaledInstance(SPRITE_WIDTH, SPRITE_HEIGHT, Image.SCALE_SMOOTH);

                    int x = (i % SPRITE_COLUMNS) * SPRITE_WIDTH;
                    int y = (i / SPRITE_COLUMNS) * SPRITE_HEIGHT;

                    itemsSpriteCSS += String.format(
                        ".dota2-item-%d { background-position: %spx %spx; }\n",
                        item.getId(),
                        x > 0 ? "-" + x : x,
                        y > 0 ? "-" + y : y);

                    g.drawImage(resized, x, y, SPRITE_WIDTH, SPRITE_HEIGHT, null);
                    i++;
                }

                g.dispose();

                ImageIO.write(itemsSprite, "png", new File(dir + ITEMS_SPRITE));
                FileUtils.writeStringToFile(new File(dir + ITEMS_CSS), itemsSpriteCSS);

            }
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            System.exit(1);
        }
    }

    public static String get(String url) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet apiRequest = new HttpGet(url);
            HttpResponse apiResponse = client.execute(apiRequest);

            int apiResponseCode = apiResponse.getStatusLine().getStatusCode();
            if (apiResponseCode == HttpStatus.SC_OK) {
                return EntityUtils.toString(apiResponse.getEntity());
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }

        return null;
    }
}
