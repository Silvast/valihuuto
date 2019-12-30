# valihuuto
Tässäpä väälihuutobotin sorsat. 

Homma toimii niin, että botti katsoo tilan kannasta ja jos aiempia huutoja ei
 ole, hakee rss-feedistä viimeisimmän (rss-feedissä on abt puolen vuoden
  huudot). Koskapa pöytäkirjat eivät ole webissä yhdellä sivulla kivasti
  , niin haen pdf:n, jonka sitten muutan tekstiksi ja etsin sielt
  ä hakasulkeiden välistä ne varsinaiset huudot. 

## Installation
Kloonaa sorsat ja tee uberjar. Tämä appsi haluaa db-urlin ympäristömuuttujana
, kuten myös twitter-apin käyttäjähommelit.
 ``` 
lein uberjar
```
Kannan migraatiot voi ajaa helpohkosti:

``` 
lein dbmigrate
```
Kannan saa tyhjättyä 
     ``` 
    lein dbclean
    ```
    
## Usage

FIXME: explanation

    $ java -jar valihuuto-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.


## License

Copyright © 2019 Anne-Mari Silvast
