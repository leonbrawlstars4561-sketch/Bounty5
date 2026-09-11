# BountyPlugin

Ein produktionsreifes Paper-Plugin fuer ein Bounty-System mit
Confirmation-GUI, persistenter Speicherung und Vault-/EternalEconomy-
Unterstuetzung.

## Build

Voraussetzungen: JDK 17, Maven.

```bash
mvn clean package
```

Die fertige Jar-Datei liegt danach unter `target/BountyPlugin-1.0.0.jar`.

## Installation

1. `Vault.jar` und ein Vault-kompatibles Economy-Plugin installieren
   (z. B. `EternalEconomy.jar` - es registriert sich automatisch bei
   Vault, eine zusaetzliche direkte Integration ist nicht noetig).
2. `BountyPlugin-1.0.0.jar` in den `plugins`-Ordner legen.
3. Server starten. Beim ersten Start wird `config.yml` erzeugt.

## Befehle

| Befehl                          | Beschreibung                              | Permission                       |
|----------------------------------|--------------------------------------------|-----------------------------------|
| `/bounty add <Spieler> <Betrag>` | Oeffnet die Bestaetigungs-GUI fuer einen Bounty | `bountyplugin.command.bounty`    |
| `/bountyad reload`               | Laedt config.yml neu und prueft die Economy erneut | `bountyplugin.admin`             |

Unterstuetzte Betragsformate: `1000`, `1.5k`, `2b`, `2.75b`, `10t`, `1q`
(Gross-/Kleinschreibung egal). Intern wird ausschliesslich mit
`BigDecimal` gerechnet, sodass auch sehr grosse Bountys ohne
Rundungsfehler exakt bleiben. Nur direkt vor dem eigentlichen
Vault-Aufruf (der ausschliesslich `double` unterstuetzt) erfolgt die
technisch unvermeidliche Konvertierung.

## Ablauf

1. `/bounty add <Spieler> <Betrag>` prueft Berechtigung, Ziel,
   Betragsformat, Grenzwerte und Kontostand und oeffnet dann die
   Confirmation-GUI (Spielerkopf des Ziels, Bestaetigen-/Abbrechen-
   Button, mit Glas-Panes gepolstert - kein Item kann entnommen,
   verschoben oder per Shift-Klick gestohlen werden).
2. Bestaetigen: Der Kontostand wird erneut geprueft, der Betrag wird
   abgebucht und der Bounty in `bounties.yml` gespeichert (neuer
   Eintrag oder Erhoehung eines bestehenden).
3. Abbrechen: Es wird nichts abgebucht.
4. Toetet ein anderer Spieler das Ziel per PVP, wird der Bounty
   atomar entfernt (kein doppeltes Auszahlen moeglich) und der
   Gesamtbetrag an den Killer ausgezahlt.

## Persistenz & Sicherheit

- Bountys werden in `plugins/BountyPlugin/bounties.yml` gespeichert
  und ueberleben Reloads und Server-Neustarts.
- `/bountyad reload` laedt nur `config.yml` und die Economy-Anbindung
  neu - die Bounty-Daten bleiben davon unberuehrt.
- Die Auszahlung nutzt `ConcurrentHashMap#remove`, wodurch ein Bounty
  garantiert nur genau einmal entnommen werden kann.
- Spieler koennen sich standardmaessig kein Bounty auf sich selbst
  setzen (Bypass ueber `bountyplugin.bypass.selftarget`).

## Economy: Vault & EternalEconomy

EternalEconomy stellt keine eigene, separat zu integrierende Direkt-API
bereit - es registriert sich selbst als Vault-`Economy`-Provider. Die
Plugin-Logik spricht daher ausschliesslich mit Vault
(`dev.bountyplugin.economy.EconomyManager`); EternalEconomy (und jeder
andere Vault-kompatible Provider) funktioniert dadurch automatisch,
ohne Sonderfall-Code. Ist weder Vault noch ein Provider vorhanden,
deaktiviert sich das Plugin nicht, sondern meldet fehlende Economy in
Konsole und Spieler-Chat.

## Qualitaets-Checkliste (Selbstpruefung)

- [x] `/bounty add <Name> <Betrag>` mit Berechtigungspruefung
- [x] Confirmation-GUI mit Spielerkopf, Confirm- und Cancel-Button
- [x] Confirm bucht ab und speichert; Cancel bucht nichts ab
- [x] Auszahlung beim Kill, atomar, keine Doppel-Auszahlung
- [x] Persistente Speicherung in `bounties.yml`, ueberlebt Neustarts
- [x] `/bountyad reload` laedt Config & Economy neu
- [x] Suffixe `k, m, b, t, q` (Gross-/Kleinschreibung egal) und reine
      Zahlen sowie Dezimalwerte wie `1.5b`
- [x] Vault- und damit EternalEconomy-Unterstuetzung
- [x] Kein Selbst-Targeting ohne Bypass-Permission
- [x] GUI-Items koennen nicht entwendet werden (Klicks/Dragging in der
      GUI werden vollstaendig abgefangen)
