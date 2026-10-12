# HomeTele

Kodit, joihin pelaajat voivat teleportata. Teleportti latautuu hetken (laavapisarat pyörivät pelaajan ympärillä),
ja liikkuminen tai vahinko peruu sen. Adminit näkevät kaikkien kodit ja voivat käydä niissä.

Vaatii **imusAPI**:n.

## Komennot

| Komento | Kuka | Mitä |
|---|---|---|
| `/sethome [nimi]` | kaikki | koti tähän; olemassa oleva samanniminen siirtyy |
| `/home [nimi]` | kaikki | kotiin latautumisen jälkeen |
| `/delhome [nimi]` | kaikki | poistaa kodin |
| `/homes` | kaikki | omat kodit, klikkaamalla sinne |
| `/homelist` | admin | kaikkien kodit, klikkaamalla sinne |
| `/homeof <pelaaja> [koti]` | admin | heti jonkun kotiin |

Yhdellä kodilla nimeä ei tarvita. Jos koteja on useampi, pelkkä `/home` vie kotiin nimeltä `home`.

## Asetukset

`config.yml`, tai pelissä ja konsolista `/ia config hometele` (`list` / `set <asetus> <arvo>`):

| Asetus | Oletus | |
|---|---|---|
| `max-homes` | 1 | koteja pelaajaa kohden |
| `warmup-seconds` | 10 | kauanko pitää seistä paikallaan |
| `teleport-cooldown-seconds` | 300 | teleporttien väli |
| `sethome-cooldown-seconds` | 1800 | kotien asettamisen väli |
| `cancel-cooldown-seconds` | 15 | odotus peruuntuneen teleportin jälkeen |
| `cancel-distance` | 1.0 | montako blokkia saa liikkua latautuessa |
| `cancel-on-damage` | true | peruuko vahinko teleportin |

Cooldownit lasketaan viimeisestä käyttökerrasta nykyisellä asetuksella, joten asetuksen lyhentäminen lyhentää
myös jo käynnissä olevia odotuksia. Pelaajille näkyvät tekstit ovat `config.yml`:n `messages`-osiossa.

## Oikeudet

| Oikeus | Oletus | |
|---|---|---|
| `hometele.use` | kaikki | kodit |
| `hometele.admin` | op | `/homelist`, `/homeof`, asetukset |
| `hometele.bypass` | op | ei cooldowneja |

## Päivitys vanhasta HomeTelestä

Vanhan version `homes.yml` (yksi koti per pelaaja) luetaan sellaisenaan, ja vanhan koti saa nimen `home`.
`settings.yml`:n arvot siirtyvät `config.yml`:ään ensimmäisellä käynnistyksellä. Tallennus käyttää uutta muotoa,
jossa koti on maailman nimi ja koordinaatit; se ei hajoa, vaikka maailmaa ei olisi ladattu.

## Testit

`imu.hometele.engine` (kodit, cooldownit, latautuminen) on Bukkitista erillään, ja sen yksikkötestit ajetaan
komennolla `mvn -pl plugins/HomeTele test`. Botti testaa pelissä `tools/shopbot/test/hometele*.test.js`.
