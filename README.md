# valihuuto
Tässäpä välihuutobotin sorsat. 

Homma toimii niin, että botti katsoo tilan kannasta ja jos aiempia huutoja ei
 ole, hakee rss-feedistä viimeisimmän (rss-feedissä on abt puolen vuoden
  huudot). Koskapa pöytäkirjat eivät ole webissä yhdellä sivulla kivasti
  , niin haen pdf:n, jonka sitten muutan tekstiksi ja etsin sieltä hakasulkeiden välistä ne varsinaiset huudot. 

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

##AWS-juttuset 

Resources-kansiosta löytyy stack.yml, jota voi käyttää cloudformationin
 kanssa. Tämä luo ec2-instanssin sekä rds:ään kannan. Samalla asennellaan
  javat ja leiningenit. Tähän vielä askarreltavana ci-putki niin, ett
  ä masteriin tuupatessa pyöräytetään jarrista image ja tuossa
   cloudformationissa
   sitten haetaan se image tuonne dockeriin pyörimään samantein, niin ei
    tarvitse siellä ec2-purkissa mitään sorsia sitten pyöritell
    ä. Sallittakoon tälleen välipäivien askarteluksi kuitenkin vähän näitä k
    äsityövaiheitakin, niin homma pyörii edes.

## TO DO

Kantaa voisi varmaan miettiä vielä tarkemmin jatkokäyttöä varten: halutaanko
 identifioida jokaisen huudon tekijä puolueittain ja twitterkäyttäjineen tms?


## License

Copyright © 2019 Anne-Mari Silvast
