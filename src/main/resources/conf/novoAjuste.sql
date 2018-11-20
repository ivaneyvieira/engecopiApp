DO @ULTIMO := (select CONCAT(MAX(barcode*1))
               from sqldados.coletor
               where usuario = 'NOTA');

DO @ID := (SELECT MAX(id)
           from sqldados.coletor);


INSERT INTO sqldados.coletor (barcode, qtty, seq, date, usuario)
  SELECT
    CAST(barcode + 1 as char),
    1,
    1,
    current_date() * 1,
    'NOTA'
  FROM sqldados.coletor
  WHERE barcode = @ULTIMO
        AND usuario = 'NOTA'
        AND id <> @ID
  GROUP BY barcode