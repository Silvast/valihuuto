# valihuuto
Tässäpä väälihuutobotin sorsat. 
Homma toimii niin, että botti katsoo tilan kannasta ja jos aiempia huutoja ei
 ole, hakee rss-feedistä viimeisimmän (rss-feedissä on abt puolen vuoden
  huudot). 

## Installation
Kloonaa sorsat ja tee uberjar. Tämä appsi haluaa db-urlin ympäristömuuttujana.
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

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2019 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
