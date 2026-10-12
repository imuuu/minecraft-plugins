package imu.GS.engine.live;

import imu.GS.engine.Fakes;
import imu.GS.engine.ItemCategory;
import imu.GS.engine.customer.Customer;
import imu.GS.engine.pricing.PriceCalculator;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.ShopSettings;
import imu.GS.engine.trade.TradeResult;
import imu.GS.engine.trade.TradeResult.Status;
import imu.GS.engine.trade.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static imu.GS.engine.Fakes.item;
import static org.junit.jupiter.api.Assertions.*;

class FlashDealTest
{
    private static final long MINUTE = 60_000L;

    private Fakes.Rules _rules;
    private Fakes.Bank _bank;
    private TradeService _trades;
    private FlashDeals _deals;
    private Shop _shop;
    private FlashOffer _sword;
    private FlashOffer _stone;
    private final List<UUID> _changes = new ArrayList<>();
    private final long _now = 1_000_000;

    @BeforeEach
    void setUp()
    {
        _rules = new Fakes.Rules().value("flash_sword", 100).value("flash_stone", 1);
        _rules.neverBought.add(item("flash_sword"));
        _rules.neverBought.add(item("flash_stone"));
        _bank = new Fakes.Bank();
        _trades = new TradeService(new PriceCalculator(_rules), _bank);
        _shop = new Shop(UUID.randomUUID(), "Test", "Test", ShopSettings.DEFAULT);
        _deals = new FlashDeals();
        _deals.addListener(_changes::add);
        _sword = new FlashOffer(UUID.randomUUID(), item("flash_sword"), 3, 250, 2);
        _stone = new FlashOffer(UUID.randomUUID(), item("flash_stone"), 300, 2, 128);
        _deals.start(new FlashDeal(UUID.randomUUID(), _shop.getId(), _now, _now + 20 * MINUTE, List.of(_sword, _stone)));
    }

    private Customer customer(String name, double money)
    {
        Customer customer = Fakes.customer(name, new Fakes.Inventory(), new Fakes.Context());
        _bank.balances.put(customer.id(), money);
        return customer;
    }

    @Test
    void buyingPaysTheDealPriceAndGivesTheItem()
    {
        Customer alex = customer("Alex", 1000);

        TradeResult result = _trades.buyFlash(_shop, alex, _deals, _sword.getId(), 1, _now);

        assertTrue(result.isSuccess());
        assertEquals(750, _bank.balance(alex.id()));
        assertEquals(1, alex.inventory().count(item("flash_sword")));
        assertEquals(2, _sword.getStock());
    }

    @Test
    void aPlayerCantBuyOverTheirLimit()
    {
        Customer alex = customer("Alex", 10_000);
        assertTrue(_trades.buyFlash(_shop, alex, _deals, _sword.getId(), 2, _now).isSuccess());

        assertEquals(Status.OVER_LIMIT, _trades.buyFlash(_shop, alex, _deals, _sword.getId(), 1, _now).status());
        assertEquals(0, _sword.canBuy(alex.id()));
    }

    @Test
    void theLimitIsPerItem()
    {
        Customer alex = customer("Alex", 10_000);
        _trades.buyFlash(_shop, alex, _deals, _sword.getId(), 2, _now);
        assertTrue(_trades.buyFlash(_shop, alex, _deals, _stone.getId(), 64, _now).isSuccess());
    }

    @Test
    void theStockIsShared()
    {
        Customer alex = customer("Alex", 10_000);
        Customer steve = customer("Steve", 10_000);
        _trades.buyFlash(_shop, alex, _deals, _sword.getId(), 2, _now);

        assertEquals(Status.NOT_ENOUGH_STOCK, _trades.buyFlash(_shop, steve, _deals, _sword.getId(), 2, _now).status());
        assertTrue(_trades.buyFlash(_shop, steve, _deals, _sword.getId(), 1, _now).isSuccess());
        assertEquals(0, _sword.getStock());
    }

    @Test
    void nothingCanBeBoughtAfterTheDeal()
    {
        Customer alex = customer("Alex", 1000);
        assertEquals(Status.DEAL_OVER, _trades.buyFlash(_shop, alex, _deals, _sword.getId(), 1, _now + 20 * MINUTE).status());
        assertEquals(1000, _bank.balance(alex.id()));
    }

    @Test
    void notEnoughMoneyChangesNothing()
    {
        Customer alex = customer("Alex", 100);
        assertEquals(Status.NOT_ENOUGH_MONEY, _trades.buyFlash(_shop, alex, _deals, _sword.getId(), 1, _now).status());
        assertEquals(3, _sword.getStock());
        assertEquals(0, _sword.boughtBy(alex.id()));
    }

    @Test
    void flashItemsCantBeSoldBack()
    {
        Fakes.Inventory inventory = new Fakes.Inventory().put(10, "flash_stone", 64, ItemCategory.BLOCKS);
        Customer alex = Fakes.customer("Alex", inventory, new Fakes.Context());

        assertEquals(Status.NOT_BOUGHT, _trades.sell(_shop, alex, item("flash_stone"), 64).status());
        assertFalse(_trades.buysItem(_shop, item("flash_stone")));
        assertEquals(64, inventory.count(item("flash_stone")));
    }

    @Test
    void aShulkerKeepsItsFlashItems()
    {
        _rules.value("stone", 1);
        Fakes.Inventory inventory = new Fakes.Inventory().putShulker(15, "box", Map.of(item("flash_stone"), 10, item("stone"), 10));
        Customer alex = Fakes.customer("Alex", inventory, new Fakes.Context());

        assertTrue(_trades.sellShulker(_shop, alex, 15).isSuccess());
        assertEquals(Map.of(item("flash_stone"), 10), inventory.shulkerContents(15));
    }

    @Test
    void listenersHearOfPurchasesAndTheEnd()
    {
        _changes.clear();
        _trades.buyFlash(_shop, customer("Alex", 1000), _deals, _sword.getId(), 1, _now);
        assertEquals(List.of(_shop.getId()), _changes);

        assertEquals(List.of(_shop.getId()), _deals.removeEnded(_now + 20 * MINUTE));
        assertNull(_deals.active(_shop.getId(), _now));
    }
}
