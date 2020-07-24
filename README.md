# valihuuto
Tässäpä välihuutobotin sorsat. Härpäke on tehty clojurella, koska clojure on
 rakkautta. 

Homma toimii niin, että botti katsoo tilan kannasta ja jos aiempia huutoja ei
 ole, hakee rss-feedistä viimeisimmän (rss-feedissä on abt puolen vuoden
  huudot). Koskapa pöytäkirjat eivät ole webissä yhdellä sivulla kivasti, niin haen
  pdf:n, jonka sitten muutan tekstiksi ja etsin sieltä hakasulkeiden välistä ne varsinaiset huudot. 

## Asennushommelit
Kloonaa sorsat ja tee uberjar. Tämä appsi haluaa db-urlin ympäristömuuttujana
, kuten myös twitter-apin käyttäjähommelit.
 ``` 
lein uberjar
```
Tietokantana postgres ja kannattaa käyttää esmes 10.6. -versiota, niin tuo
 flywayn cleankin vielä toimii kivasti. Resources-kansiosta löytyy kannan docker
  devausta varten. Kannan migraatiot voi
  ajaa helpohkosti:

``` 
lein dbmigrate
```
Kannan saa tyhjättyä 

 ``` 
lein dbclean
```
    
## Käyttö


    $ java -jar valihuuto-0.1.0-standalone.jar [args]

## Muuta
Tämä botti ei hyödynnä mitään aws lamda-henkisiä hienouksia, vaan pörr
ää perinteisesti kerran vuorokaudessa, kun cronjob potkaisee käyntiin
 skriptin, joka hakee tästä reposta uusimmat koodit, buildaa jarrin ja sen j
 älkeen leipoo tuon jarrin dockerkonttiin ajoon. Old school siis. 

## TO DO

Kantaa voisi varmaan miettiä vielä tarkemmin jatkokäyttöä varten: halutaanko
 identifioida jokaisen huudon tekijä puolueittain ja twitterkäyttäjineen tms?


## License

Copyright © 2019 Anne-Mari Silvast
