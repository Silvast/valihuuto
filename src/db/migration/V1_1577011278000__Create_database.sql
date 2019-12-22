CREATE TABLE tila(
  id SERIAL PRIMARY KEY,
  viimeisin_twiitattu_pvm DATE,
  versio INTEGER
);

CREATE TABLE kansanedustajat(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  etunimi TEXT,
  sukunimi TEXT,
  ensikertainen_hyvaksyminen DATE,
  hyvaksytty TIMESTAMP WITH TIME ZONE,
  twitterhandle TEXT,
  puolue TEXT,
  aktiivinen BOOLEAN
);

CREATE TABLE valihuudot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  valihuuto TEXT,
  hyvaksytty TIMESTAMP WITH TIME ZONE,
  huutaja_name TEXT,
  huutaja_id INTEGER REFERENCES kansanedustajat(id),
  poytakirja VARCHAR(36),
  twiitattu TIMESTAMP WITH TIME ZONE);