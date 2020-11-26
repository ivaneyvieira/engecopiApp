<<<<<<< HEAD
select distinct nota as numero, MAX(date) as date, MAX(storeno) as storeno
from (
       select
         id,
         seq,
         barcode * 1                                   as userno,
         LPAD(barcode, 16, ' ')                        as barcode,
         qtty,
         date,
         usuario                                       as coletor,
         RIGHT(usuario, 2) * 1                         as storeno,
         IF(usuario = 'NOTA', @NOTA := barcode, @NOTA) AS nota
       from sqldados.coletor
       where date > 20180101
       ORDER BY id
     ) AS T
=======
SELECT DISTINCT nota AS numero, MAX(date) AS date, MAX(storeno) AS storeno
FROM (SELECT id, seq, barcode * 1 AS userno, LPAD(barcode, 16, ' ') AS barcode, qtty, date,
             usuario AS coletor, RIGHT(usuario, 2) * 1 AS storeno,
             IF(usuario = 'NOTA', @NOTA := barcode, @NOTA) AS nota
      FROM sqldados.coletor
      WHERE date > 20180101
      ORDER BY id) AS T
>>>>>>> develop
GROUP BY numero
ORDER BY date DESC, barcode * 1 DESC