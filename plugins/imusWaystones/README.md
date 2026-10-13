# imusWaystones

Pelaajat rakentavat waystoneja ja matkustavat niiden välillä. Jokainen pelaaja päivittää käyttämiään
waystoneja itselleen: nopeampi lataus, lyhyempi cooldown ja matkat muihin ulottuvuuksiin.

Vaatii **imusAPI**:n ja MySQL/MariaDB-tietokannan (`DataBaseSettings.yml`, tietokanta `imusWaystones`).

## Pelissä

1. **Rakenna**: pohjablokki (kupari, smaragdi, rauta, kulta, timantti tai netherite), sen päälle mikä
   tahansa muuri ja päälle mikä tahansa laatta. Viimeisen blokin jälkeen chattiin tulee **[Confirm]**; klikkaa
   sitä waystonen vieressä.
2. **Löydä**: muiden waystonet löydetään oikealla klikkauksella. Ensimmäinen klikkaus vain löytää sen.
3. **Matkusta**: oikea klikkaus avaa menun, jossa ovat waystonet, joihin pääsee. Klikkaa kohdetta ja seiso
   paikallasi latauksen ajan (oletuksena 30 s). Liikkuminen peruu matkan.
4. **Cooldown**: lähdön jälkeen samasta waystonesta voi lähteä uudelleen vasta cooldownin jälkeen
   (oletuksena 90 min).

Omien waystonejen välissä pitää olla 300 blokkia. Waystonen nimi leijuu sen yllä 20 blokin päähän.

### Kuka näkee kohteen

| Asetus | Kohteena näkyy |
|---|---|
| koskeneille (oletus) | niille, jotka ovat löytäneet sen |
| kaikille | kaikille |
| ei kenellekään | ei kenellekään; siitä voi vain lähteä |
| kaikille, vain saapuminen | kaikille, mutta siitä ei voi lähteä |

Löydettyjä waystoneja muissa ulottuvuuksissa näkee vain Dimensions-päivityksellä. Kaikille näkyviä näkee
aina.

### Päivitykset

Menun **Upgrades** avaa pelaajan omat päivitykset tälle waystonelle. Jokainen taso maksaa itemejä:

| Päivitys | Vaikutus | Tasoja |
|---|---|---|
| Cast Time | lataus 3 s lyhyemmäksi per taso (30 s → 3 s) | 9 |
| Cooldown | 5 % pois cooldownista per taso | 10 |
| Dimensions | taso 1: Nether, taso 2: End | 2 |
| Foundation | cooldownin pohja: 90 min → 40 min → 25 min → 1 min | 3 |
| XP Usage | matka maksaa vähemmän XP-tasoja (vain jos XP-hinta on päällä) | 10 |

- **Siirto toiseen waystoneen**: shift + oikea klikkaus grindstoneen siirtää päivitykset paperille, jolla
  oikea klikkaus toiseen waystoneen antaa ne sinne. Korkeammat tasot pysyvät.
- **Uusi nimi**: omistaja ja admin voivat nimetä waystonen uudelleen. &-värikoodit toimivat.

## Admin

- `/waystones list`: kaikki waystonet. Klikkaus vie heti perille, shift + oikea klikkaus poistaa.
- `/waystones reload`: asetukset ja waystonet tietokannasta uudelleen.
- Waystonen menussa adminilla on lisänapit:
  - kuka näkee kohteen
  - rikkomaton
  - max out (ei cooldownia, latausta eikä XP:tä)
  - päällä/pois
  - poisto (shift-klikkaus)
- Adminit saavat päivitykset ilmaiseksi shift + oikealla klikkauksella.
- Luovassa tilassa waystonea ei voi rikkoa; se poistetaan menusta.

| Oikeus | Oletus | |
|---|---|---|
| `imuswaystones.use` | kaikki | waystonejen rakentaminen |
| `imuswaystones.admin` | op | lista, poisto, asetukset, ilmaiset päivitykset |
| `imuswaystones.build-anywhere` | op | omat waystonet lähekkäin |
| `imuswaystones.bypass` | op | sekunnin matkat ilman cooldownia ja XP:tä |

Vanhat oikeudet toimivat: `iw.build.ingnore` antaa build-anywhere-oikeuden, `iw.list` ja `iw.reload`
admin-oikeuden.

## Asetukset

`config.yml`, tai pelissä ja konsolista `/ia config imuswaystones` (`list` / `set <asetus> <arvo>`). Listat
(pohjablokit, päivitysten hinnat, Foundationin cooldownit) muokataan tiedostossa, minkä jälkeen ajetaan
`/waystones reload`.

| Asetus | Oletus | |
|---|---|---|
| `structure.base-blocks` | lista | pohjablokit |
| `min-distance-between-own` | 300 | blokkeja omien waystonejen välissä |
| `name-view-distance` | 20 | mistä nimi näkyy |
| `cancel-distance` | 0.5 | paljonko latauksen aikana saa liikkua |
| `teleport.cast-seconds` | 30 | lataus ilman päivityksiä |
| `teleport.cast-seconds-per-tier` | 3 | Cast Time -tason vaikutus |
| `teleport.min-cast-seconds` | 1 | lyhin lataus |
| `teleport.foundation-cooldown-seconds` | [5400, 2400, 1500, 60] | cooldown Foundationin tasoilla 0–3 |
| `teleport.cooldown-percent-per-tier` | 5 | Cooldown-tason vaikutus |
| `teleport.xp-levels` | 0 | XP-tasoja per matka; 0 = ilmainen, ja XP Usage on pois |
| `upgrades.<päivitys>` | lista | seuraavan tason hinta, esim. `"LAPIS_BLOCK:40 DIAMOND:2"` |

Pelaajien tekstit ovat `messages`-osiossa.

## Päivitys vanhasta

- Vanhat taulut luetaan sellaisenaan: waystonet, omistajat, löydöt ja päivitykset.
- Puuttuvat sarakkeet (`max_out`, `unbreakable`, `enable`) ja näkyvyysvaihtoehdot lisätään. Vanha versio
  jätti ne lisäämättä uuteen tietokantaan, jolloin waystonen tallennus epäonnistui.
- Taulut muutetaan utf8mb4-merkistöön.
- Vanhat armor stand -nimet poistetaan chunkien latautuessa, ja tilalle tulee tekstinäyttö, jota ei
  tallenneta maailmaan.
- Vanhat päivityspaperit toimivat.

## Testit

`imu.waystones.engine` on Bukkitista erillään, ja sen testit ajetaan komennolla
`mvn -pl plugins/imusWaystones test`. Testit kattavat:
- päivitysten hinnat ja vaikutukset
- kohdelistan ja ulottuvuudet
- rakennusetäisyyden
- latauksen ja cooldownit

Botti testaa pelissä: `tools/shopbot/test/waystones.test.js` sekä vanhojen tietojen siirron
`old-plugins-upgrade.test.js` (`SHOPBOT_RESTART=1`). Waystone-testi tyhjentää testiserverin waystone-taulut.
