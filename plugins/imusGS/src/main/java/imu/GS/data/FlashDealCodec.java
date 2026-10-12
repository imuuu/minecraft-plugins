package imu.GS.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import imu.GS.engine.ItemKey;
import imu.GS.engine.live.FlashDeal;
import imu.GS.engine.live.FlashOffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A flash deal, its offers and who bought how many, as JSON for gs_flash_deals.
 */
public final class FlashDealCodec
{
    private FlashDealCodec() {}

    public static String toJson(FlashDeal deal)
    {
        JsonObject json = new JsonObject();
        json.addProperty("id", deal.id().toString());
        json.addProperty("start", deal.startMillis());
        json.addProperty("end", deal.endMillis());
        JsonArray offers = new JsonArray();
        for (FlashOffer offer : deal.offers())
        {
            JsonObject o = new JsonObject();
            o.addProperty("id", offer.getId().toString());
            o.addProperty("item", offer.getItem().id());
            o.addProperty("stock", offer.getStock());
            o.addProperty("price", offer.getUnitPrice());
            o.addProperty("limit", offer.getPerPlayerLimit());
            JsonObject bought = new JsonObject();
            for (Map.Entry<UUID, Integer> entry : offer.getBought().entrySet())
                bought.addProperty(entry.getKey().toString(), entry.getValue());
            o.add("bought", bought);
            offers.add(o);
        }
        json.add("offers", offers);
        return json.toString();
    }

    public static FlashDeal fromJson(UUID shopId, String text)
    {
        JsonObject json = JsonParser.parseString(text).getAsJsonObject();
        List<FlashOffer> offers = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("offers"))
        {
            JsonObject o = element.getAsJsonObject();
            FlashOffer offer = new FlashOffer(UUID.fromString(o.get("id").getAsString()), new ItemKey(o.get("item").getAsString()),
                    o.get("stock").getAsInt(), o.get("price").getAsDouble(), o.get("limit").getAsInt());
            for (Map.Entry<String, JsonElement> bought : o.getAsJsonObject("bought").entrySet())
                offer.restoreBought(UUID.fromString(bought.getKey()), bought.getValue().getAsInt());
            offers.add(offer);
        }
        return new FlashDeal(UUID.fromString(json.get("id").getAsString()), shopId, json.get("start").getAsLong(),
                json.get("end").getAsLong(), offers);
    }
}
