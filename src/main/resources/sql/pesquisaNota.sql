SELECT 'S'  AS tipo,
       xano AS numero,
       '66' AS serie,
       0    AS cancelado
FROM sqldados.stkmov
WHERE remarks LIKE CONCAT('%:PED _', :ordno)
  AND storeno = :storeno
  AND :tipo = 'E'
UNION
SELECT 'E'  AS tipo,
       xano AS numero,
       '66' AS serie,
       0    AS cancelado
FROM sqldados.stkmov
WHERE remarks LIKE CONCAT('%:PED _', :ordno)
  AND storeno = :storeno
  AND :tipo = 'S'
ORDER BY 4, 2 DESC;