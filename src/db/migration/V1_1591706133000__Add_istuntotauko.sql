CREATE TABLE istuntotauko_tila(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  istuntokausi_year INTEGER,
  istuntokausi_part INTEGER,
  poytakirja_versio VARCHAR(36),
  huudettu DATE,
  uudelleen_twiitattu DATE);