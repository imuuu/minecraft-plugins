package imu.GS.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Restrictions;
import imu.GS.engine.shop.ShopSettings;
import imu.GS.engine.shop.StockRules;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * JSON of a listing's price, stock, restrictions and tags, and of shop settings. Plain data, safe off the main thread.
 */
public final class ListingCodec
{
    private ListingCodec() {}

    public record ListingData(PriceRule price, StockRules stock, Restrictions restrictions, Set<String> tags) {}

    public static String toJson(Listing listing)
    {
        JsonObject json = new JsonObject();
        json.add("price", price(listing.getPrice()));
        if (listing.getStock() != null)
        {
            JsonObject stock = new JsonObject();
            stock.addProperty("max", listing.getStock().maxAmount());
            stock.addProperty("fill", listing.getStock().fillAmount());
            stock.addProperty("delay", listing.getStock().fillDelayMinutes());
            json.add("stock", stock);
        }
        if (!listing.getRestrictions().isEmpty())
            json.add("restrictions", restrictions(listing.getRestrictions()));
        if (!listing.getTags().isEmpty())
        {
            JsonArray tags = new JsonArray();
            listing.getTags().forEach(tags::add);
            json.add("tags", tags);
        }
        return json.toString();
    }

    public static ListingData fromJson(String text)
    {
        JsonObject json = JsonParser.parseString(text).getAsJsonObject();
        PriceRule price = json.has("price") ? price(json.getAsJsonObject("price")) : PriceRule.MATERIAL;

        StockRules stock = null;
        if (json.has("stock"))
        {
            JsonObject s = json.getAsJsonObject("stock");
            stock = new StockRules(s.get("max").getAsInt(), s.get("fill").getAsInt(), s.get("delay").getAsInt());
        }

        Restrictions restrictions = json.has("restrictions") ? restrictions(json.getAsJsonObject("restrictions")) : Restrictions.NONE;

        Set<String> tags = new LinkedHashSet<>();
        if (json.has("tags"))
            json.getAsJsonArray("tags").forEach(tag -> tags.add(tag.getAsString()));
        return new ListingData(price, stock, restrictions, tags);
    }

    // Price ==========================================================================================================

    private static JsonObject price(PriceRule rule)
    {
        JsonObject json = new JsonObject();
        switch (rule)
        {
            case PriceRule.Material m -> json.addProperty("type", "MATERIAL");
            case PriceRule.Fixed fixed ->
            {
                json.addProperty("type", "FIXED");
                json.addProperty("price", fixed.price());
            }
            case PriceRule.Unique unique ->
            {
                json.addProperty("type", "UNIQUE");
                json.addProperty("price", unique.price());
            }
            case PriceRule.Custom custom ->
            {
                json.addProperty("type", "CUSTOM");
                json.addProperty("money", custom.money());
                json.addProperty("bundle", custom.bundleSize());
                JsonArray items = new JsonArray();
                for (PriceRule.ItemCost cost : custom.items())
                {
                    JsonObject item = new JsonObject();
                    item.addProperty("item", cost.item().id());
                    item.addProperty("amount", cost.amount());
                    items.add(item);
                }
                json.add("items", items);
            }
        }
        return json;
    }

    private static PriceRule price(JsonObject json)
    {
        return switch (json.get("type").getAsString())
        {
            case "FIXED" -> new PriceRule.Fixed(json.get("price").getAsDouble());
            case "UNIQUE" -> new PriceRule.Unique(json.get("price").getAsDouble());
            case "CUSTOM" ->
            {
                List<PriceRule.ItemCost> items = new ArrayList<>();
                for (JsonElement element : json.getAsJsonArray("items"))
                {
                    JsonObject item = element.getAsJsonObject();
                    items.add(new PriceRule.ItemCost(new ItemKey(item.get("item").getAsString()), item.get("amount").getAsInt()));
                }
                yield new PriceRule.Custom(items, json.get("money").getAsDouble(), json.get("bundle").getAsInt());
            }
            default -> PriceRule.MATERIAL;
        };
    }

    // Restrictions ===================================================================================================

    private static JsonObject restrictions(Restrictions restrictions)
    {
        JsonObject json = new JsonObject();
        if (restrictions.sellTime() != null)
        {
            JsonArray time = new JsonArray();
            time.add(restrictions.sellTime().start());
            time.add(restrictions.sellTime().end());
            json.add("sellTime", time);
        }
        JsonArray areas = new JsonArray();
        for (Restrictions.Area area : restrictions.areas())
        {
            JsonObject a = new JsonObject();
            a.addProperty("world", area.world());
            a.addProperty("x", area.x());
            a.addProperty("y", area.y());
            a.addProperty("z", area.z());
            a.addProperty("radius", area.radius());
            areas.add(a);
        }
        json.add("areas", areas);
        JsonArray permissions = new JsonArray();
        restrictions.permissions().forEach(permissions::add);
        json.add("permissions", permissions);
        return json;
    }

    private static Restrictions restrictions(JsonObject json)
    {
        Restrictions.SellTime sellTime = null;
        if (json.has("sellTime"))
        {
            JsonArray time = json.getAsJsonArray("sellTime");
            sellTime = new Restrictions.SellTime(time.get(0).getAsInt(), time.get(1).getAsInt());
        }
        List<Restrictions.Area> areas = new ArrayList<>();
        if (json.has("areas"))
        {
            for (JsonElement element : json.getAsJsonArray("areas"))
            {
                JsonObject a = element.getAsJsonObject();
                areas.add(new Restrictions.Area(a.get("world").getAsString(), a.get("x").getAsDouble(),
                        a.get("y").getAsDouble(), a.get("z").getAsDouble(), a.get("radius").getAsInt()));
            }
        }
        List<String> permissions = new ArrayList<>();
        if (json.has("permissions"))
            json.getAsJsonArray("permissions").forEach(p -> permissions.add(p.getAsString()));
        return new Restrictions(sellTime, areas, permissions);
    }

    // Shop settings ==================================================================================================

    public static String toJson(ShopSettings settings)
    {
        JsonObject json = new JsonObject();
        json.addProperty("sellMultiplier", settings.sellMultiplier());
        json.addProperty("buyMultiplier", settings.buyMultiplier());
        json.addProperty("expirePercent", settings.expirePercent());
        json.addProperty("expireCooldownMinutes", settings.expireCooldownMinutes());
        json.addProperty("locked", settings.locked());
        json.addProperty("customersCanOnlyBuy", settings.customersCanOnlyBuy());
        json.addProperty("absolutePositions", settings.absolutePositions());
        json.addProperty("liveEvents", settings.liveEvents());
        if (!settings.isGeneral())
            json.addProperty("kind", settings.kind());
        return json.toString();
    }

    public static ShopSettings settingsFromJson(String text)
    {
        JsonObject json = JsonParser.parseString(text).getAsJsonObject();
        ShopSettings d = ShopSettings.DEFAULT;
        return new ShopSettings(
                json.has("sellMultiplier") ? json.get("sellMultiplier").getAsDouble() : d.sellMultiplier(),
                json.has("buyMultiplier") ? json.get("buyMultiplier").getAsDouble() : d.buyMultiplier(),
                json.has("expirePercent") ? json.get("expirePercent").getAsDouble() : d.expirePercent(),
                json.has("expireCooldownMinutes") ? json.get("expireCooldownMinutes").getAsInt() : d.expireCooldownMinutes(),
                json.has("locked") && json.get("locked").getAsBoolean(),
                json.has("customersCanOnlyBuy") && json.get("customersCanOnlyBuy").getAsBoolean(),
                !json.has("absolutePositions") || json.get("absolutePositions").getAsBoolean(),
                !json.has("liveEvents") || json.get("liveEvents").getAsBoolean(),
                json.has("kind") ? json.get("kind").getAsString() : null);
    }
}
