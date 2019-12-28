CREATE TABLE tila(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  viimeisin_twiitattu_pvm DATE,
  versio INTEGER
);

CREATE TABLE valihuudot(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  valihuuto TEXT,
  poytakirja_versio VARCHAR(36),
  huudettu DATE,
  twiitattu TIMESTAMP WITH TIME ZONE);