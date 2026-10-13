# ImusSpawners

Spawnereita voi kaivaa silk touchilla:

- **Luonnon spawneri** pudottaa itsensä onnenheitolla, oletuksena 14 %. Jokainen huti nostaa pelaajan omaa
  todennäköisyyttä 13 prosenttiyksikköä, ja onnistuminen palauttaa sen pohjalle. Pudonnut spawneri on
  joskus (5 %) satunnainen mobi.
- **Pelaajan asettama spawneri** palaa aina, ja samana mobina.

Ilman silk touchia spawneri rikkoutuu kuten vanillassa. Luovassa tilassa heittoa ei tehdä.

Vaatii **imusAPI**:n.

## Komennot

| Komento | Kuka | Mitä |
|---|---|---|
| `/spawners` | kaikki | oma todennäköisyys ja tilastot |
| `/spawners give <mobi> [pelaaja] [määrä]` | admin | spawneri-itemejä, toimii myös konsolista |
| `/spawners stats <pelaaja>` | admin | jonkun todennäköisyys |

Oikeus `imusspawners.admin` (op).

## Asetukset

`config.yml`, tai pelissä ja konsolista `/ia config imusspawners` (`list` / `set <asetus> <arvo>`):

| Asetus | Oletus | |
|---|---|---|
| `silk-touch-chance` | 14 | % todennäköisyys luonnon spawnerille |
| `chance-increase-per-miss` | 13 | % jonka huti lisää pelaajan todennäköisyyteen |
| `max-chance` | 100 | todennäköisyys ei nouse tätä korkeammaksi |
| `random-type-chance` | 5 | % todennäköisyys, että pudonnut spawneri on satunnainen mobi |
| `random-type-excluded` | lista | mobit, joita satunnainen spawneri ei ole |

Viestit ovat `messages`-osiossa.

## Tiedot

- Asetettu spawneri muistaa omistajansa itse (`imusspawners:owner` blokin tiedoissa), joten erillistä listaa
  ei tarvita.
- Pelaajien todennäköisyydet ovat tiedostossa `playerData.yml` samassa muodossa kuin ennenkin.

## Päivitys vanhasta

- Vanhan `settings.yml`:n arvot siirtyvät `config.yml`:ään ensimmäisellä käynnistyksellä.
- Vanhan `spawnerData.yml`:n spawnerit lasketaan asetetuiksi niin kauan, kunnes ne rikotaan. Tiedosto
  poistuu, kun viimeinenkin on rikottu.
- Vanhat spawneri-itemit (`imusapi:spawner_type`) toimivat. Uusiin itemeihin mobi tallentuu myös vanillan
  tavalla, ja se näkyy tooltipissä.

## Testit

`imu.spawners.engine` (todennäköisyydet ja heitot) on Bukkitista erillään, ja sen testit ajetaan komennolla
`mvn -pl plugins/imusSpawners test`. Botti testaa pelissä: `tools/shopbot/test/spawners.test.js` ja
vanhojen tietojen siirron `old-plugins-upgrade.test.js` (`SHOPBOT_RESTART=1`).
