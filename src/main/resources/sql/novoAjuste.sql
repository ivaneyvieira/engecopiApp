DO @ULTIMO := (SELECT CONCAT(MAX(barcode * 1))
	       FROM sqldados.coletor
	       WHERE usuario = 'NOTA');

DO @ID := (SELECT MAX(id)
	   FROM sqldados.coletor);


INSERT INTO sqldados.coletor (barcode, qtty, seq, date, usuario)
SELECT CAST(barcode + 1 AS CHAR),
       1,
       1,
       CURRENT_DATE() * 1,
       'NOTA'
FROM sqldados.coletor
WHERE barcode = @ULTIMO
  AND usuario = 'NOTA'
  AND id <> @ID
GROUP BY barcode