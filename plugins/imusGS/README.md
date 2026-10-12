# imusGS – ylläpitäjän opas

imusGS on General Store -kauppaplugari Paper 26.2 -serverille. Pelaajat ostavat kaupasta ja myyvät sinne tavaraa
rahalla (Vault + Essentials tai muu talousplugari). Kauppa elää: siellä on flash dealeja, wanted-pyyntöjä, päivän
hintatunnelma ja tavaraa, jota pelaajat löytävät pelatessaan. Samoja kauppoja voi pystyttää NPC-kyläksi.

Tämä opas käy läpi asennuksen, ensimmäisen kaupan, kylän, hinnoittelun ja tasapainotuksen.

---

## 1. Asennus

**Tarvitaan:** Paper 26.2, Java 25, **imusAPI**, **Vault** ja talousplugari (esim. EssentialsX), sekä MySQL tai
MariaDB.

1. Laita `ImusAPI.jar` ja `imusGS.jar` plugins-kansioon ja käynnistä serveri kerran.
2. Täytä tietokannan tiedot tiedostoon `plugins/imusGS/DataBaseSettings.yml` ja käynnistä uudelleen.
3. Ensimmäisellä käynnistyksellä imusGS hinnoittelee kaikki materiaalit valmiilla hintaprofiililla
   (timantti = 1000). Konsoliin tulee rivi `Loaded N shops`, kun kaupat ovat valmiina.

Valinnaiset plugarit, joita imusGS osaa käyttää, jos ne ovat asennettuina:
- **ImusEnchants:** custom-enchantit hinnoitellaan, niitä myydään kirjoina ja gearissa, boosterit Enchanterissa
- **imusTNT:** erikois-TNT:t hinnoitellaan ja niitä myydään Redstone-kaupassa

### Tiedostot `plugins/imusGS/`-kansiossa

| Tiedosto | Mitä |
|---|---|
| `config.yml` | kaikki asetukset; lähes kaikki voi vaihtaa myös pelissä `/gs config` |
| `messages_en.yml`, `messages_fi.yml` | pelaajille näkyvät tekstit; kieli valitaan configin `language`-asetuksella |
| `profiles/default.yml` | valmis hintaprofiili; **kirjoitetaan joka käynnistyksessä uudelleen**, joten tee muutokset kopioon |
| `presets/*.yml` | kylän kauppapohjat; kirjoitetaan vain, jos tiedosto puuttuu, joten omat muutokset säilyvät |

---

## 2. Ensimmäinen kauppa

Nopein tapa on pystyttää kylä (luku 3). Yksittäinen kauppa käsin:

```
/gs create shop General Store      tekee tyhjän kaupan
/gs create npc General Store       NPC kohtaan, jossa seisot; sneak + klikkaa NPC:tä muokataksesi sitä
/gs modify shop General Store      kaupan editori: tavarat, hinnat, asetukset
```

Kauppa täyttyy itsestään, kun pelaajat myyvät sinne. Pysyvää tavaraa (esim. 64 kiveä, joka täyttyy 30 minuutin
välein) saat lisättyä editorissa tai komennolla `/gs add hand|hotbar|inventory <kauppa>`.

**Kaupan asetukset** (editorin asetusvalikko):
- **Sell multiplier:** pelaajat ostavat hintaan arvo × tämä (oletus 5)
- **Buy multiplier:** kauppa maksaa myynnistä arvo × tämä (oletus 1)
- **Expire:** osa varastosta katoaa joka jakso, ettei kauppa tukkeudu
- **Lock:** vain adminit pääsevät sisään
- **Block Customer Sell:** pelaajat voivat vain ostaa
- **Live Events:** onko kauppa mukana flash dealeissa, kiertävässä valikoimassa ja löydetyissä itemeissä

> Jos asetat kiinteän hinnan halvemmaksi kuin mitä kauppa maksaa takaisin, saat varoituksen: pelaajat voisivat
> silloin ostaa ja myydä takaisin voitolla.

---

## 3. NPC-kylä

```
/gs village                         pystyttää kaikki puuttuvat kaupat kaareen eteesi
/gs create preset <pohja> [nimi]    yksi kauppa NPC:n kanssa kohtaan, jossa seisot
/gs presets                         pohjat ja mitkä on jo tehty
/gs remove shop <nimi> confirm      poistaa kaupan ja sen NPC:t
```

Valmiit pohjat:

| Pohja | Valikoima |
|---|---|
| `general` | General Store: ostaa ja myy, kaikkea |
| `builder` | kivi, maa, puu |
| `farmer` | viljat ja ruoka |
| `blacksmith` | työkalut, armor, enchantattu gear; löydetyt malmit tulevat tänne |
| `redstone` | redstone ja erikois-TNT (imusTNT) |
| `fisherman` | meri, onget, tridentit |
| `alchemist` | nether, mobien dropit, juomien ainekset |
| `librarian` | enchanted bookit |
| `enchanter` | custom enchant -kirjat ja boosterit (ImusEnchants), lapis, XP-pullot |
| `decorator` | koristeet |

Kaikki paitsi General Store ovat **vain ostamista varten**. Jokaisella on pysyvä varasto, joka täyttyy itsestään,
ja sen päälle kiertävä valikoima omalta alaltaan.

**NPC:iden siirtäminen:** sneak + klikkaa NPC:tä → editori, josta voi siirtää, kääntää, vaihtaa nimen ja ammatin
tai skinin.

**Oma kauppapohja:** kopioi esim. `presets/farmer.yml` nimellä `presets/kalastaja2.yml` ja muokkaa. Tiedoston alussa
on selitys jokaisesta kentästä. Muutokset vaikuttavat uusiin kauppoihin heti; jo tehtyjen kauppojen varasto ei
muutu, mutta niiden live-tapahtumat seuraavat tiedostoa.

> **Päivityksen jälkeen:** jos pluginin mukana tulee uusi versio pohjasta, se ei korvaa sinun tiedostoasi. Poista
> vanha `presets/<pohja>.yml`, niin uusi kirjoitetaan tilalle seuraavassa uudelleenlatauksessa.

---

## 4. Hinnoittelu

Jokaisella materiaalilla on **arvo**. Pelaaja ostaa hintaan *arvo × sell multiplier* ja myy hintaan
*arvo × buy multiplier*. Enchantit, kunto (kuluneet työkalut) ja custom-enchantit lisätään arvoon.

### Hintaprofiili
```
/gs profile list
/gs profile apply default 1000            näyttää ensin mitä tapahtuisi
/gs profile apply default 1000 confirm    hinnoittelee kaikki materiaalit
```
Luku on timantin hinta; kaikki muu skaalautuu sen mukaan. Profiili asettaa myös **tagit** (ores, farming, wood…)
ja **myyntirajat**.

### Yksittäiset hinnat
```
/g getprice hand                                  minkä arvoinen kädessä oleva item on (kaikille pelaajille)
/gs set material price hand|hotbar|inventory <hinta>
/gs tag materials set_price|increase_price <tagi> <arvo>    koko tagin hinnat kerralla
/gs set material overflow ...                     hinta laskee, kun kaupassa on paljon samaa
/gs set material smart_price ...                  hinta resepti-ainesosista
/gs modify enchantments                           enchanttien hinnat
/gs create unique [hinta] / /gs modify uniques    tietyn itemin oma hinta (esim. nimetty miekka)
```

### Custom-enchantit ja -itemit (ImusEnchants, imusTNT)
```
/gs customprices                                  kaikki, niiden tier ja hinta
/gs set custom price <avain> <hinta|default>      oma hinta; default palauttaa tierin mukaisen
```
Oletushinta on *tier × timantin hinta*: enchanteilla 0.3 / 0.8 / 2.0 tasoa kohden, itemeillä 0.5 / 1.5 / 4.0
(configissa `pricing.custom-*-tiers`).

### Myyntirajat
Farmit eivät saa tehdä rahaa loputtomasti. Jokainen kauppa ostaa ryhmää (crops, iron, wood…) täydellä hinnalla
tiettyyn määrään asti; sen jälkeen hinta laskee, ja raja täyttyy ajan myötä. Kaikki myyjät jakavat saman rajan.
```
/gs limits <kauppa>          paljonko kustakin ryhmästä on myyty
/gs reset limits <kauppa>    nollaa rajat
```
Rajat ja niiden ryhmät ovat hintaprofiilissa (`sell-limits`).

---

## 5. Live-tapahtumat

Kaikki voi kytkeä pois `/gs config` -valikossa tai `config.yml`:n `live`-osiossa. Kaupan oma "Live Events"
-kytkin ratkaisee, onko se mukana.

| Tapahtuma | Mitä tapahtuu |
|---|---|
| **Flash deals** | 2–5 tunnin välein yhdessä kaupassa kerrallaan 20 minuutin tarjoukset. Broadcast ei kerro tuotteita, vaan **[Peek]**-nappi näyttää kaksi. Pelaajakohtainen raja per item, eikä ostettua voi myydä takaisin. |
| **Wanted** | Kauppa maksaa 2 tuntia 30–80 % enemmän parista itemistä. Vain kaupat, jotka ostavat. |
| **Mood** | Jokainen tagi on päivittäin ±10 % ylös tai alas, sekä osto- että myyntihinnassa. |
| **Rotation** | Kolmen tunnin välein muutama satunnainen item kaupan alalta. |
| **Found items** | Mitä pelaajat kaivavat, farmaavat, tappavat tai kalastavat, ilmestyy joskus oikeaan kauppaan. Mitä harvinaisempi, sitä harvemmin. Pelaajille ei kerrota. |
| **Progress** | Flash dealeissa on vain sitä, mitä ei-OP-pelaajat ovat jo saaneet: netherite vasta, kun joku on löytänyt sitä, enchantit enintään nähdyllä tasolla. |

Komennot testaamiseen ja hallintaan (toimivat myös konsolista):
```
/gs flash start|end <kauppa>
/gs wanted start|end <kauppa>
/gs restock <kauppa>              kiertävä valikoima heti
/gs progress [scan|reset]         mitä pelaajat ovat saaneet
/gs peek <kauppa>                 kurkistus flash dealeihin (kaikille pelaajille)
```

---

## 6. Tasapainotus: `/gs stats`

```
/gs stats                     viimeiset 7 päivää, kaikki kaupat
/gs stats 30 General Store    30 päivää, yksi kauppa
```
Näyttää, paljonko pelaajat maksoivat kaupoille ja kaupat pelaajille, nettosumman, flash dealien myynnin, eniten
myydyt ja ostetut itemit, top-myyjät, päiväkohtaiset rahavirrat sekä live-tapahtumien tilanteen.

**Mitä katsoa:**
- **Net "put into the economy"** kasvaa nopeasti → kaupat maksavat liikaa. Laske buy multiplieria, tiukenna
  myyntirajoja tai laske farmattavien itemien hintoja.
- **Yksi item hallitsee "Sold to shops most" -listaa** → sen farmi on liian tuottoisa. Laske hintaa tai lisää se
  myyntiraja-ryhmään.
- **Flash dealit eivät myy** → alennus on liian pieni tai ne tulevat liian harvoin (`live.flash-deals`).
- **Raha vain kasaantuu pelaajille** → kaupat ovat hyvä rahanielu: lisää kiinnostavaa ostettavaa
  (kiertävä valikoima, flash dealit).

---

## 7. Asetusten vaihto ilman peliä

Kaikki `/gs config` -valikon asetukset voi vaihtaa myös konsolista:
```
/ia config imusgs list
/ia config imusgs set live.flash-deals.enabled false
/ia config imusgs set language fi
```

Avaa kauppa pelaajalle esim. komentoblokista tai toisesta plugarista:
```
/gsopenfor <pelaaja> <kauppa>
```

---

## 8. Oikeudet

Kaikki adminkomennot ovat oletuksena vain OP:ille, ja `gs.admin` antaa ne kaikki.

| Oikeus | Komento |
|---|---|
| `gs.create.shop` | `/gs create shop`, `/gs create preset`, `/gs village`, `/gs presets` |
| `gs.remove.shop` | `/gs remove shop` |
| `gs.modify.shop` | `/gs modify shop` |
| `gs.create.npc` | `/gs create npc` |
| `gs.open.shop` | `/gs open shop` (avaa minkä tahansa kaupan mistä tahansa) |
| `gs.openfor` | `/gsopenfor` |
| `gs.add` | `/gs add` |
| `gs.set.material.price` | materiaalien hinnat, `/gs customprices`, `/gs set custom price` |
| `gs.set.material.overflow`, `gs.set.material.smart_price` (+ `unset`) | overflow ja smart price |
| `gs.tag` | `/gs tag` |
| `gs.create.uniques`, `gs.modify.uniques`, `gs.modify.enchants` | unique-itemit ja enchanttien hinnat |
| `gs.profile` | `/gs profile` |
| `gs.limits` | `/gs limits`, `/gs reset limits` |
| `gs.flash` | `/gs flash`, `/gs wanted`, `/gs restock`, `/gs progress` |
| `gs.stats` | `/gs stats` |
| `gs.config` | `/gs config` |
| `gs.peek` | `/gs peek` (**oletuksena kaikille**) |

---

## 9. Ongelmia?

| Oire | Syy ja korjaus |
|---|---|
| "The shops are still opening" | Tietokanta latautuu vielä; jos viesti ei häviä, katso konsolista tietokantavirhe. |
| Kaikki hinnat ovat 0 | Profiilia ei ole ajettu: `/gs profile apply default 1000 confirm`. |
| Flash deal ei ala ("Nothing to offer") | Edistyminen on päällä, eikä yksikään ei-OP-pelaaja ole vielä saanut mitään. Katso `/gs progress`, tai kytke pois `live.progress.flash-deals`. |
| Uusi kauppapohjan rivi ei näy | Vanha `presets/<pohja>.yml` on jäänyt; poista se (luku 3). |
| Pelaaja ei voi myydä itemiä | Kauppa on vain ostamista varten, item on flash dealista tai sen hinta on 0 (`shop.allow-zero-price-sells`). |
| Konsolissa "Couldn't save ..." | Tietokantayhteys katkesi; tallennus yritetään uudelleen automaattisesti. |
