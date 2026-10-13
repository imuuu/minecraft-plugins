# imusTNT

Erikois-TNT:t, jotka sytytetään tulukselilla (flint and steel):

- **Chunk TNT** tyhjentää chunkin yhden kerroksen (16 x 1 x 16), myös laavan ja veden. Rikotut blokit eivät
  pudota mitään.
- **Chunk Stone TNT** muuttaa saman kerroksen kiveksi ja täyttää myös kolot ja nesteet.

Kerros on se, jossa TNT räjähtää. Arkut, käytettävät blokit (uunit, ovet, napit…), hyvin räjähdyksenkestävät
blokit (obsidiaani, ancient debris, bedrock) ja listan blokit (timanttimalmi ja -blokki, netheriteblokki, spawneri)
jäävät paikalleen. Samoin muut erikois-TNT:t.

Vaatii **imusAPI**:n. Kaupat (imusGS) tuntevat TNT:t custom-itemeinä: `imustnt:chunk_tnt` ja
`imustnt:chunk_stone_tnt`.

## Pelissä

- Erikois-TNT sytytetään vain tulukselilla. Redstone, tuli ja muut räjähdykset eivät sytytä sitä, eikä
  mäntä liikuta sitä.
- Rikottu erikois-TNT pudottaa itsensä, ei tavallista TNT:tä.
- Räjähdys kysyy suoja-plugareilta (claimit, alueet) luvan blokki kerrallaan sytyttäjän nimissä. Jos sytyttäjä
  lähtee ennen räjähdystä, TNT ei tee mitään.
- Waystoneihin räjähdys ei koske.

## Komennot

| Komento | Mitä |
|---|---|
| `/imustnt` tai `/it` | menu: klikkaus antaa yhden, oikea tai shift-klikkaus 64 |
| `/imustnt give <tyyppi> [pelaaja] [määrä]` | tyypit `chunk-tnt` ja `chunk-stone-tnt`, toimii myös konsolista |

Oikeus `imustnt.admin` (op). Vanha `it.inv` antaa saman.

## Asetukset

`config.yml`, tai pelissä ja konsolista `/ia config imustnt` (`list` / `set <asetus> <arvo>`):

| Asetus | Oletus | |
|---|---|---|
| `fuse-ticks` | 80 | sytytyksestä räjähdykseen, 20 = sekunti |
| `layers` | 1 | montako kerrosta räjähdys vie, TNT:n kerroksesta alaspäin |
| `blast-resistance-limit` | 1200 | näin kestävät ja kestävämmät blokit jäävät |
| `keep-usable-blocks` | true | käytettävät blokit jäävät |
| `protected-blocks` | lista | nämä jäävät aina |

Itemien nimet ja lore ovat `items`-osiossa ja viestit `messages`-osiossa.

## Päivitys vanhasta

Vanhat TNT-itemit ja maailmaan asetetut erikois-TNT:t toimivat sellaisenaan. Item tunnistetaan
`imusapi:tnt_type`-tiedosta, ja asetettu TNT chunkin tiedosta `imustnt:tnt_x_y_z`.

## Testit

`imu.imustnt.engine` (mitkä blokit muuttuvat) on Bukkitista erillään, ja sen testit ajetaan komennolla
`mvn -pl plugins/imusTNT test`. Botti testaa pelissä: `tools/shopbot/test/tnt.test.js`.
