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
| Menu / GUI | `Menu` + `PaperMenus` (`imu.iAPI.Menus`) | omia `InventoryClickEvent`-kuuntelijoita |
| Komennot | `CommandHandler` + `CommandInterface` | omaa `CommandExecutor`-sekamelskaa |
| Asetukset pelissä ja konsolista | `ConfigMenu` | omia asetusmenuja |
| Permissionit: kuvaus, vaarallisuus, antaminen menusta | `plugin.yml`:n `level:` + `/ia perms` (`imu.iAPI.Permissions`) | permissioneiden muistelua ja komentoja |
| Tekstin kysyminen pelaajalta | `TextInputDialog.show(...)` | chat-keskustelut |
| NPC, joka tekee jotain klikattaessa | `NpcManager` + `NpcAction` | Citizens-riippuvuutta |
| Custom-enchantit tai -itemit muiden käyttöön | `CustomEnchants`, `CustomItems` | suoria riippuvuuksia plugarista toiseen |
| Tietokanta | `Database.open(...)` (SQLite tai MySQL, HikariCP-pooli) | omia yhteyksiä |

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

## Menut: `Menu` (`imu.iAPI.Menus`)

Menu ei koske serverin inventoryihin. Se on tila ja `render(Canvas)`, joka piirtää napit tilan mukaan.
Menu piirretään uudelleen jokaisen klikkauksen jälkeen ja `refresh()`-kutsulla, ja pelaajalle lähtevät vain
muuttuneet slotit. Siksi menua voi klikkailla JUnit-testissä ilman serveriä (`MenuTester`), ja pelissä sen
näyttää `PaperMenus`.

```java
public class ShopMenu extends Menu
{
    private final Pager<Item> _pager = Pager.rows(0, 5);   // sivu säilyy uudelleenpiirrossa
    private int _amount = 1;

    public ShopMenu()
    {
        super("&6Kauppa", 6);                               // otsikko (&-värit), rivit
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fillRow(5, Icon.filler(Material.BLACK_STAINED_GLASS_PANE));
        _pager.render(canvas, items(), item -> Button.of(icon(item), click -> buy(click.viewer(), item, _amount)), 45, 53);
        canvas.set(49, Buttons.counter(Icon.of(Material.GOLD_NUGGET, "&eMäärä"), _amount, 1, 64, value -> _amount = value));
        canvas.set(50, Buttons.confirm(Icon.of(Material.TNT, "&cTyhjennä"), "&cTyhjennetäänkö?", click -> clear()));
        Buttons.back(canvas, 48);                           // vain jos tultiin toisesta menusta
    }
}

PaperMenus.open(player, new ShopMenu());
```

- Klikkaus saa `Click`-olion: `viewer()`, `kind()` (`isRight()`, `isShift()`), `open(menu)`, `back()`,
  `replace(menu)`, `close()` ja `prompt(Prompt)` (tekstin kysyminen dialogilla, menu palaa vastauksen jälkeen).
- `Icon` on itemin kuvaus ilman `ItemStack`ia: `Icon.of(Material.DIAMOND, "&bNimi", "&7lore")`. `Material` toimii myös
  testeissä; `ItemStack`ia ei voi tehdä ilman serveriä. Valmiin itemin, esim. plugarin oman custom-itemin, saa
  näkymään `PaperIcons.of(itemStack)`. Merkkijonoversio (`Icon.of("diamond")`) on configista luettuja nimiä varten.
- Valmiit napit (`Buttons`): `back`, `close`, `toggle`, `cycle`, `counter`, `confirm` (avaa `ConfirmMenu`n).
- `Viewer` on pelaaja ilman Bukkitia: `id()`, `name()`, `hasPermission()`, `message()`. Kun menu tarvitsee
  `Player`-olion (esim. itemin antamiseen), välitä se menulle rajapinnan takana, kuten imusTNT:n `TntMenu.Giver`.
- `onOpen`/`onClose` jokaisella näyttämisellä ja lopullisella sulkemisella; `refreshTicks()` + `onTick()` ajastettuun
  päivitykseen (`onTick` palauttaa `false`, kun mikään ei muuttunut); pelaajan oma inventory
  on lukittu menun ollessa auki; menu, johon pelaaja laittaa itemeitä omasta inventorystaan (esim. enchant-pöytä),
  palauttaa `locksOwnInventory()`-metodista `false`.
- **Itemit menussa.** `click.cursor()` on kursorin item ja `click.setCursor(item)` vaihtaa sen (pelissä `ItemStack`).
  `Button.empty(action)` on tyhjä slotti, joka kuulee klikkaukset, eli siihen voi laittaa itemin kursorista. Menu pitää
  itemit omassa tilassaan ja antaa ne takaisin `onClose`ssa (esim. ImusEnchantsin enchant-pöytä). Shift-klikkaus
  omasta inventorysta menuun on aina estetty; yhden slotin raahaus on sama kuin klikkaus.
- Toisen menun avaamista ei tarvitse viivästää itse; `PaperMenus` avaa sen seuraavalla tickillä.
- **Ulkopuoliset muutokset odottavat hetken.** Klikkaus piirtää menun heti. `menu.refresh()` (esim. toisen pelaajan
  kauppa tai tietokannasta tullut data) piirtää vasta `refreshDelayTicks()` jälkeen (oletus 5 tickiä), ja monta
  muutosta siinä välissä tekee yhden piirron. Näin kiireinen kauppa ei piirrä jokaisen katsojan menua jokaisesta
  kaupasta. Testissä `MenuTester.flush()` piirtää odottavan muutoksen.
- **Vikasietoisuus.** Jos menu heittää poikkeuksen (piirto, klikkaus, promptin vastaus, ajastin), sen pelaajan kaikki
  menut suljetaan heti ja virhe kirjataan konsoliin: rikkinäinen menu ei jää auki, ja `onClose` antaa itemit takaisin.
  Yhden menun kaatuva `onClose` ei estä muita sulkeutumasta. Kun plugin sammuu (myös serverin sammuessa), sen menut
  suljetaan sen koodin vielä toimiessa (`PaperMenus.closeAll(plugin)`), ja ImusAPI sulkee lopuksi kaikki.
- **Feikki-itemit pelaajan ruudulla (`imu.iAPI.Packets.ClientItems`).** Näyttää slotissa eri itemin vain pelaajan
  ruudulla pakettina; oikea item serverillä ei muutu. `showOwn(player, slot, item)` oman inventoryn slottiin (0–8
  hotbar, 9–35 muut), `coverOwn(player, item)` kaikkiin 36:een ja `restore(player)` lähettää oikeat takaisin. Menu,
  jonka `ownInventoryCover()` palauttaa ikonin, peittää pelaajan oman inventoryn sillä koko ajan kun se on auki
  (imusGS:n kaupat mustilla paneeleilla); oikeat itemit palaavat sulkiessa ja promptin ajaksi. Enchant-pöytä
  vilkuttaa pöytään sopivia itemeitä vihreinä paneeleina. Ei koskaan creativessa, jossa client päättää itemeistä
  itse. Paperilla ei ole tähän API:a, joten se käyttää Paperin sisäosia reflektiolla; jos ne muuttuvat
  päivityksessä, `ClientItems.available()` on `false`, mitään ei näytetä ja konsoliin tulee yksi varoitus.

Testissä:

```java
MenuTester menu = MenuTester.open(new ShopMenu(), TestViewer.with("Guest", "shop.use"));
menu.click("Määrä", ClickKind.SHIFT_LEFT);
assertTrue(menu.lore(49).contains("11"));
menu.click("Tyhjennä").assertShowing(ConfirmMenu.class);
System.out.println(menu.dump());                         // menu tekstiruudukkona
```

**Pelin osat rajapinnan takana.** Menu, joka tarvitsee plugaria tai `Player`-oliota (itemin antaminen, tietokanta,
config.yml:n tekstit), saa ne plugarin omasta rajapinnasta, esim. imusWaystonesin `WaystoneGame`. Rajapinnalla on
kaksi toteutusta: `Paper...Game` serverillä ja testin oma feikki, joka kirjaa, mitä menu pyysi. Tietokannasta
tuleva data annetaan callbackina (`Consumer`), ja menu kutsuu `refresh()`, kun se saapuu.

```java
interface WaystoneGame
{
    String text(String key, Object... placeholders);
    List<Waystone> destinations(Waystone from, UUID player);
    void travel(Viewer viewer, Waystone from, Waystone to);
}
```

Kaikki menut ovat tällä systeemillä. Testeihin asti rajapinnan takana ovat imusTNT, imusWaystones, ImusMiniQuests,
ImusChallenges, ImusUnstableFearSystem, ImusEnchantsin `/ien admin` ja enchant-kauppa sekä `/ia config`. imusGS:n
menut (`GsMenu`), enchant-pöytä, NPC-editori ja `/ia lootTables` käyttävät Bukkitia suoraan, ja niitä testataan
botilla. `/ia inv` (`Fast_Inventory`) näyttää listat `ItemGalleryMenu`llä.

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

Testeissä `ConfigMenu` saa plugarin tilalle `ConfigMenu.Store`n (esim. `YamlConfiguration`), ja sen menut
(`ConfigEntriesMenu`, `ConfigListMenu`) toimivat `MenuTester`illa, katso `ConfigMenusTest`.

---

## Permissionit: `/ia perms` (`imu.iAPI.Permissions`)

Jokaisen plugarin permissionit näkyvät yhdessä menussa: admin valitsee plugarin, sitten pelaajan lähellä olevista
(tai hakee nimellä), ja klikkaa permissionit päälle tai pois. Jokaisessa näkyy kuvaus, vaarallisuus ja onko se
pelaajalla nyt (annettu täällä, otettu pois täällä vai oletus). Erillistä permissions-plugaria ei tarvita.

Plugari ei kirjoita koodia: imusAPI lukee permissionit plugarin `plugin.yml`:stä, kun plugari riippuu imusAPI:sta
(`depend`/`softdepend`) tai sen permissioneilla on `level`. Bukkit ei välitä ylimääräisistä avaimista.

```yaml
permissions:
  imustnt.admin:
    description: Take and give special TNT, change the settings   # näkyy menussa
    default: op
    level: high          # low, medium, high, very_high
    icon: tnt            # valinnainen; muuten vihreä/harmaa väriaine päällä/pois
  it.inv:
    description: Same as imustnt.admin
    hidden: true         # vanha alias, ei näy menussa
```

| Taso | Mitä yleensä |
|---|---|
| `low` | Arjen ominaisuudet, eivät haittaa muita (kodit, waystonen rakentaminen) |
| `medium` | Pieniä etuja tai muiden huomattavissa (cooldownin ohitus, kauppa mistä vain) |
| `high` | Admin-työkalut: ilmaiset itemit, muiden data, NPC:t |
| `very_high` | Täysi valta: asetukset, serverin komennot, permissionit itse |

- **Komennot:** `/ia perms` (plugarilista), `/ia perms <plugari>` (lähellä olevat pelaajat), `/ia perms <plugari> <pelaaja>`
  (suoraan pelaajan permissionit, myös offline). Oikeus `ia.permissions`. Etäisyys: `permissions.nearby-range` imusAPI:n
  configissa (oletus 32).
- **Jokaisella plugarilla** on oma `permissions`-alikomento: `/gs`, `/ic`, `/imq`, `/ien`, `/imustnt`, `/spawners`,
  `/waystones permissions`. `CommandHandler`illa yksi rivi: `handler.registerPermissionsCmd("gs")`; omassa
  komentoluokassa `PaperPermissions.open(player, getName())`. Plugarit ilman pääkomentoa (HomeTele, ImusDifficult,
  ImusHellGear…) löytyvät `/ia perms`ista.
- **Ilman restarttia:** menusta vaihdettu permission on voimassa heti, myös komentojen tab-täydennys. Käsin muokattu
  `permissions.yml` luetaan uudelleen komennolla `/ia perms reload` (myös konsolista).
- **Klikkaus** antaa tai ottaa pois, **oikea klikkaus** palauttaa plugarin oletukseen, **Reset all** kaikki kerralla.
  `high` ja `very_high` kysyvät ennen antamista. Kukaan ei voi antaa permissionia, jota hänellä itsellään ei ole.
- **Tallennus:** `plugins/imusAPI/permissions.yml` (UUID, nimi, `[node, -pois_otettu]`). Ne annetaan pelaajalle
  `PermissionAttachment`ina liittyessä ja heti muutettaessa. Pois otettu
  voittaa myös yläpermissionin (`gs.admin`) lapset ja op-oletuksen. LuckPermsin kanssa Bukkit laskee molemmat yhteen.
- **Koodista** voi lisätä permissioneita, joita `plugin.yml` ei listaa:
  `Permissions.register(PluginPermissions.of("MyPlugin", PermissionInfo.of("my.node", "Mitä antaa", PermissionLevel.LOW)))`.
- Menut ovat `PermissionGame`-rajapinnan takana (`PaperPermissions` serverillä), joten niitä testataan `MenuTester`illa
  (`PermissionsTest`) ja pelissä botilla (`permissions.test.js`).

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

Kaikki plugarit käyttävät samaa asetusta, imusAPI:n `config.yml`:n `database`-osiota: `type: sqlite` (oletus,
`data.db` plugarin kansioon) tai `type: mysql` (palvelin, tunnukset `mysql`-kohdassa; jokainen plugari saa oman
tietokantansa nimellään, ja `mysql.databases` antaa sille toisen nimen).

```java
Database db;
try
{
    db = Database.open(this, "myplugin", 10);   // MySQL:n poolin koko; SQLitellä muutama yhteys
} catch (DatabaseException e)
{
    getLogger().severe(e.getMessage());        // syy ja mitä tarkistaa
    getServer().getPluginManager().disablePlugin(this);
    return;
}
createTables(db);            // db.isSqlite() niille lauseille, jotka eroavat
db.copyFromIfAsked();        // taulujen jälkeen, ennen kuin mitään luetaan
try (Connection con = db.getConnection()) { ... }
// ORMLite: db.connectionSource(); onDisable: db.close()
```

- **Erot SQLiten ja MySQL:n välillä:** SQLite: `INTEGER PRIMARY KEY AUTOINCREMENT` eikä `AUTO_INCREMENT`, ei `ENUM`ia,
  `ON CONFLICT(...) DO UPDATE` eikä `ON DUPLICATE KEY UPDATE`, ajat `datetime('now','localtime')`, indeksit
  `CREATE INDEX IF NOT EXISTS`. `REPLACE INTO`, `LIMIT` ja backtickit käyvät molemmissa. Ajat tallentuvat SQLiteen
  tekstinä samassa muodossa kuin MySQL ne näyttää. ORMLite valitsee murteen itse.
- **Datan siirto:** `copy-from: sqlite` (tai `mysql`) kopioi seuraavalla käynnistyksellä jokaisen plugarin datan toisesta
  tietokannasta. Kopio tehdään vain tyhjään tietokantaan, ja lähde jää ennalleen. Konsoliin tulee rivimäärät taulu
  kerrallaan; poista `copy-from` sen jälkeen.

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
