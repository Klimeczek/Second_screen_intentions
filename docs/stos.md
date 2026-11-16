# Stos ekranów: ekran 1 → ekran 2 → Wstecz → ekran 3 → Wstecz → Wstecz

Oznaczenia: **Main** = ekran 1 (formularz), **Summary** = ekran 2 (podsumowanie), **About** = ekran 3 (O aplikacji).
Na rysunkach wierzch stosu jest na górze, a ekran widoczny dla użytkownika oznaczono `◄ widoczny`.

## Krok 0: start aplikacji

```
┌─────────────┐
│ Main        │ ◄ widoczny
└─────────────┘
```

Powstaje zadanie (task) z jednym ekranem. Wywołuje się `Main.onCreate`.

## Krok 1: ekran 1 → ekran 2 (`launcher.launch(intent)`)

```
┌─────────────┐
│ Summary     │ ◄ widoczny
├─────────────┤
│ Main        │ (zatrzymany)
└─────────────┘
```

Nowy ekran trafia na wierzch stosu. Najpierw wywołuje się `Summary.onCreate`, a dopiero gdy Summary jest widoczny, `Main.onStop`. Main **nie jest niszczony**, tylko leży pod spodem z zachowanym stanem formularza. Dlatego po powrocie dane nadal tam są.

## Krok 2: Wstecz na ekranie 2

```
┌─────────────┐
│ Main        │ ◄ widoczny
└─────────────┘
```

Summary zostaje zdjęty ze stosu i zniszczony (`Summary.onStop`, `Summary.onDestroy`). Main wraca na wierzch przez `onRestart → onStart → onResume`. **`Main.onCreate` nie wywołuje się ponownie**, bo ten egzemplarz nigdy nie został zniszczony. Wynik (`RESULT_CANCELED`) trafia do callbacku launchera.

## Krok 3: ekran 1 → ekran 3 (`startActivity`)

```
┌─────────────┐
│ About       │ ◄ widoczny
├─────────────┤
│ Main        │ (zatrzymany)
└─────────────┘
```

Tak samo jak w kroku 1: `About.onCreate`, potem `Main.onStop`. Summary już nie istnieje, więc na stosie jest o jeden ekran mniej. About niczego nie zwraca, bo startuje zwykłym `startActivity`.

## Krok 4: Wstecz na ekranie 3

```
┌─────────────┐
│ Main        │ ◄ widoczny
└─────────────┘
```

About jest zdejmowany i niszczony (`About.onStop`, `About.onDestroy`). Main wznawia się bez `onCreate`.

## Krok 5: Wstecz na ekranie 1 (ostatni na stosie)

```
┌─────────────┐
│ (pusty)     │
└─────────────┘
```

Main jest ostatnim ekranem zadania, więc po Wstecz zadanie się kończy i użytkownik wraca do launchera.

**Uwaga o wersjach Androida:** od Androida 12 (API 31) Wstecz na głównym ekranie launchera nie niszczy go od razu, tylko przenosi zadanie w tło. W takim przypadku w Logcacie pojawi się `Main onStop`, a `Main onDestroy` dopiero później, gdy system zwolni aplikację. Na starszych wersjach `onStop` i `onDestroy` idą od razu jeden po drugim.

## Podsumowanie

| Krok | Akcja | Stos po kroku (od wierzchu) | Wywołania, które widać w logach |
|------|-------|-----------------------------|----------------------------------|
| 0 | start | Main | Main onCreate |
| 1 | Main → Summary | Summary, Main | Summary onCreate, Main onStop |
| 2 | Wstecz | Main | Summary onStop, Summary onDestroy |
| 3 | Main → About | About, Main | About onCreate, Main onStop |
| 4 | Wstecz | Main | About onStop, About onDestroy |
| 5 | Wstecz | (pusty) | Main onStop, Main onDestroy (na API 31+ onDestroy może przyjść później) |

## Logcat

Filtr: tag `STOS` (w Logcacie: `tag:STOS`). Logi dodałem do `onCreate`, `onStop` i `onDestroy` wszystkich trzech ekranów.

> **Uwaga:** poniższy fragment to **przewidywany** wynik wyprowadzony z opisu cyklu życia. Nie uruchamiałem aplikacji na emulatorze, więc nie jest to prawdziwy zrzut. Przed oddaniem uruchom aplikację, wykonaj sześć kroków i **zastąp ten blok własnym wklejeniem z Logcatu**. Jeśli kolejność u Ciebie będzie inna, popraw opis powyżej.

```
D STOS : MainActivity onCreate          // krok 0
D STOS : SummaryActivity onCreate       // krok 1
D STOS : MainActivity onStop
D STOS : SummaryActivity onStop         // krok 2 (Main bez onCreate)
D STOS : SummaryActivity onDestroy
D STOS : AboutActivity onCreate         // krok 3
D STOS : MainActivity onStop
D STOS : AboutActivity onStop           // krok 4
D STOS : AboutActivity onDestroy
D STOS : MainActivity onStop            // krok 5
D STOS : MainActivity onDestroy         // (na API 31+ może pojawić się później)
```

Wniosek, który powinien potwierdzić Logcat: `MainActivity onCreate` pojawia się tylko raz, na początku. Przez cały czas nawigacji Main jest tylko zatrzymywany (`onStop`) i wznawiany, a nie tworzony od nowa.