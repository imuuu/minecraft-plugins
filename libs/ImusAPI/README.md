# ImusAPI – kehittäjän opas

ImusAPI on kaikkien imu-plugareiden yhteinen kirjasto: menut, komennot, asetukset, NPC:t, itemien käsittely,
tietokanta ja yhteinen rekisteri custom-enchanteille ja -itemeille. Tämä opas kertoo, **mitä uudessa koodissa
käytetään** ja mitä vältetään.

> **Tärkeää:** kaikki ImusAPI:a käyttävät plugarit käännetään samassa Maven-reaktorissa. Kun ImusAPI:n julkinen
> rajapinta muuttuu, **päivitä kaikki plugarit serverille yhtä aikaa**, muuten vanha jar kaatuu
> (`NoClassDefFoundError` / `NoSuchMethodError`).

---

## Pikaopas: mitä käytän?

| Tarve | Käytä | Älä käytä |
|---|---|---|
| Värikoodit `&a` → `§a` | `TextUtils.Color(text)` | – |
| Itemin nimi, lore, enchantit, kunto | `ItemUtils` | – |
| Data itemiin, blokkiin tai entityyn | `DataUtils.Get/Set/RemovePersistentData` | omia `NamespacedKey`-kikkoja |
| Item pelaajalle tai maahan | `InvUtil.AddItemToInventoryOrDrop` | – |
| Menu / GUI | `CustomInventory` + `Button` | `FastInventory` (vanhentunut) |
| Komennot | `CommandHandler` + `CommandInterface` | omaa `CommandExecutor`-sekamelskaa |
| Asetukset pelissä ja konsolista | `ConfigMenu` | omia asetusmenuja |
| Tekstin kysyminen pelaajalta | `TextInputDialog.show(...)` | chat-keskustelut |
| NPC, joka tekee jotain klikattaessa | `NpcManager` + `NpcAction` | Citizens-riippuvuutta |
| Custom-enchantit tai -itemit muiden käyttöön | `CustomEnchants`, `CustomItems` | suoria riippuvuuksia plugarista toiseen |
| Tietokanta | `MySQL` (HikariCP-pooli) | omia yhteyksiä |

---

## Apuluokat (`imu.iAPI.Utilities`)

Kaikki ovat staattisia, ja metodien nimet alkavat isolla kirjaimella (`ItemUtils.SetDisplayName`).

| Luokka | Sisältö |
|---|---|
| `TextUtils` | `Color`, `Round` (kahteen desimaaliin), `FormatTime`, `Join`, `IsNullOrEmpty` |
| `ItemUtils` | nimi, lore (`AddLore`, `SetLores`, `HasLore`), enchantit, kunto (`GiveDamage`, `SetDamage`), glow, shulkerin sisältö, tagit, `EncodeItemStack`/`DecodeItemStack`, `IsTool`/`IsArmor` |
| `DataUtils` | persistent data itemeille, blokeille ja entityille avaimella `imusapi:<avain>` |
| `InvUtil` | itemin antaminen pelaajalle (ylijäämä maahan) |
| `PlayerUtils` | armorin enchanttien laskenta, klikattavat komentorivit chattiin, chat-keskustelut |
| `EnchantUtil` | satunnaiset enchantit tierin ja kategorian mukaan, enchanted bookit |
| `ItemUtilToolsArmors` | työkalujen ja armorien luokittelu |
| `ImusUtilities` | taulukot, sijainnit, pallot ja alustat, chunkit, keskitetty viesti |
| `RecipeUtils` | itemin resepti (ainesosat ja suhteet), smithing-reseptit |
| `WorldUtils` | hologrammi |
| `DebugUtils` | `PrintMap` konsoliin kehittäessä |
| `TextInputDialog` | Paperin dialogi tekstin kysymiseen (yksi- tai monirivinen) |

```java
ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
ItemUtils.SetDisplayName(sword, "&6Kuninkaan miekka");
ItemUtils.AddLore(sword, "&7Vanha ja painava", true);
DataUtils.SetPersistentData(sword, "owner", PersistentDataType.STRING, player.getUniqueId().toString());
player.sendMessage(TextUtils.Color("&aSait miekan!"));
InvUtil.AddItemToInventoryOrDrop(player, sword);
```

> **Persistent datan avain** on aina `imusapi:<avain>`, plugarista riippumatta. Käytä avaimissa plugarin
> etuliitettä (esim. `"gs_owner"`), etteivät eri plugarit törmää toisiinsa.

---

## Menut: `CustomInventory`

```java
public class ShopMenu extends CustomInventory
{
    public ShopMenu(Plugin plugin)
    {
        super(plugin, "&6Kauppa", 54);          // otsikko (&-värit), koko
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_LOWER_INV;  // pelaaja ei voi siirrellä tavaroita
    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        refresh();
    }

    private void refresh()
    {
        clearButtons();
        addButton(new Button(13, new ItemStack(Material.EMERALD), e -> getPlayer().sendMessage("Klik!")));
        updateButtons(true);
    }
}

new ShopMenu(plugin).open(player);
```

- `onAwake()` ajetaan ennen ensimmäistä avausta, `onOpen()` jokaisella avauksella ja `onClose()` sulkiessa.
- `addButton` + `updateButtons(true)` piirtää; yhden napin vaihtamiseen riittää `addButton` + `updateButton(slot)`.
- Toisen menun avaaminen napista: `Bukkit.getScheduler().runTask(plugin, () -> new Toinen(...).open(player))`.
- `FastInventory` on vanhentunut, eikä uusia menuja tehdä sillä.

---

## Komennot: `CommandHandler`

```java
CommandHandler handler = new CommandHandler(this);
handler.registerCmd("shop", new MainCommand());             // pelkkä /shop
handler.registerSubCmd("shop", "open", new OpenCommand());  // /shop open ...
handler.setPermissionOnLastCmd("shop.open");
handler.registerSubCmd("shop", "reset stock", new ResetCommand());
handler.setPermissionOnLastCmd("shop.admin");
handler.allowConsole("shop", "reset stock");                // konsoli ja RCON saavat ajaa tämän
getCommand("shop").setExecutor(handler);
```

- Alikomento löytyy pisimmän osuman mukaan (`reset stock` voittaa `reset`:in), isoilla ja pienillä kirjaimilla ei ole väliä.
- `CommandInterface.onCommand` saa koko argumenttilistan, myös alikomennon omat sanat.
- Oletuksena komennot ovat vain pelaajille. `allowConsole` päästää konsolin; silloin `sender` ei ole `Player`.
- Jokaisella plugarilla on oma `CommandHandler`-instanssi ja omat komentonsa.
- Tab-täydennys: `ImusTabCompleter(pääkomento, argumentit, oikeus)` ja `SetRule(...)`.

---

## Asetukset: `ConfigMenu`

Yksi rivi asetusta kohden, ja asetus näkyy menussa (`/ia config <plugin>`) ja konsolissa
(`/ia config <plugin> list|set <asetus> <arvo>`):

```java
ConfigMenu menu = new ConfigMenu(this, "Kauppa", "shop.config", this::reloadSettings);
menu.addInt("shop.refill-minutes", "Täyttöväli", Material.CLOCK, 1, 1440)
        .description("Minuutit varaston täyttöjen välillä")
        .note("heti");
menu.addBoolean("shop.enabled", "Käytössä", Material.LEVER);
menu.addChoice("shop.mode", "Tila", Material.COMPARATOR, List.of("normal", "hard"));
menu.register();
// onDisable: menu.unregister();
```

Arvot tallennetaan plugarin `config.yml`:ään, ja muutoksen jälkeen ajetaan annettu `onChange` (esim. asetusten
uudelleenlataus). Väärä arvo (alueen ulkopuolella, ei numero) hylätään viestillä.

---

## NPC:t: `NpcManager` ja `NpcAction`

Plugari rekisteröi toiminnon, ja admin liittää sen NPC:hen NPC-editorissa (sneak + klikkaus) tai komennolla.

```java
public class OpenShopAction implements NpcAction
{
    public String getId() { return "myshop"; }
    public String getName() { return "Avaa kaupan"; }
    public void use(Player player, LivingEntity npc, String data) { openShop(player, data); }
}

ImusAPI._instance.GetNpcManager().registerAction(new OpenShopAction());
// NPC koodista:
LivingEntity npc = npcs.spawn(NpcManager.NpcType.VILLAGER, location, "myshop", "shopId", "&6Kauppias");
```

NPC voi olla villager (ammatti) tai pelaajahahmo (skin). Se kääntyy lähellä olevia pelaajia kohti, ei liiku eikä
ota vahinkoa.

---

## Custom-enchantit ja -itemit muiden plugareiden käyttöön

Plugari kertoo omat enchanttinsa tai iteminsä rekisteriin, ja muut (esim. imusGS) voivat hinnoitella, myydä ja
seurata niitä **tuntematta plugaria**. Ilman rekisteröityjä plugareita kaikki on tyhjää ja toimii silti.

```java
// Tarjoaja, onEnable / onDisable
CustomItems.register(myItems);      // CustomItemProvider: items(), idOf(stack)
CustomEnchants.register(myEnchants); // CustomEnchantProvider: enchants(), read, canApply, apply, book
CustomItems.unregister(myItems);

// Käyttäjä
String id = CustomItems.idOf(stack);              // "imustnt:chunk_tnt" tai null
Map<String, Integer> enchants = CustomEnchants.read(stack);
List<CustomItemProvider.CustomItem> tnts = CustomItems.withTags(List.of("explosives"));
```

- Avaimet ja id:t ovat muotoa `plugari:nimi`.
- **Tier** (1–3) kertoo arvon; kauppa hinnoittelee sen mukaan, kunnes admin asettaa oman hinnan.
- **Tagit** kertovat, millaiset kaupat itemiä myyvät (`redstone`, `explosives`, `enchanting`…).
- `CustomEnchants.apply` lisää vain tason I tarjoajan omilla säännöillä; korkeammat tasot ovat tarjoajan omaa
  progressiota.

Esimerkit: `ImusEnchantsProvider` (ImusEnchants) ja `TntItems` (imusTNT).

---

## Tietokanta

```java
MySQL sql = new MySQL(this, 10, "myplugin");   // poolin koko, tietokanta; asetukset DataBaseSettings.yml:stä
try (Connection con = sql.GetConnection()) { ... }
```

Älä aja tietokantakutsuja pääsäikeellä: lue `runTaskAsynchronously`-tehtävässä ja palaa pääsäikeelle `runTask`:lla
ennen kuin kosket Bukkit-olioihin.

---

## Testit

Testit ovat kansiossa `src/test`, ja ne ajetaan komennolla `mvn -pl libs/ImusAPI test`. Ne kattavat osat, jotka
eivät tarvitse serveriä: `ConfigEntry`-arvojen jäsennys, custom-rekisterit, `CommandHandler` ja `TextUtils`.
Item-apurit tarvitsevat Paper-serverin, joten niitä testataan pelissä tai imusGS:n bottitesteillä
(`tools/shopbot`).

---

## Historiaa: vanhat nimet

Vanhassa koodissa tai ohjeissa voi näkyä vanhoja nimiä. Ne vastaavat nykyisiä näin:

| Vanha | Nyt |
|---|---|
| `Metods._ins.xxx(...)`, `ImusAPI._metods` | jaettu aiheittain: `TextUtils`, `ItemUtils`, `DataUtils`, `InvUtil`, `PlayerUtils`, `RecipeUtils`, `WorldUtils`, `DebugUtils` |
| `Metods.msgC(text)` | `TextUtils.Color(text)` |
| `getPersistenData` / `setPersistenData` | `DataUtils.GetPersistentData` / `SetPersistentData` |
| `InventoryAddItemOrDrop` | `InvUtil.AddItemOrDropGetSlots` |
| `GetRecipe` | `RecipeUtils.GetRecipe` |
| `imu.iAPI.Handelers` | `imu.iAPI.Handlers` |
| `IBUTTONN` | `IMenuButton` |
