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
GROUP BY numero
order by date desc, barcode*1 desc