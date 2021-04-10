

SELECT 'S'                AS tipo,
       CAST(xano AS char) AS numero,
       '66'               AS serie,
       0                  AS cancelado
FROM sqldados.stkmov
WHERE remarks LIKE CONCAT('%:PED _', :ordno)
  AND storeno = :storeno
  AND :tipo = 'E'
  AND :status = 9
UNION
SELECT 'E'                AS tipo,
       CAST(xano AS char) AS numero,
       '66'               AS serie,
       0                  AS cancelado
FROM sqldados.stkmov
WHERE remarks LIKE CONCAT('%:PED _', :ordno)
  AND storeno = :storeno
  AND :tipo = 'S'
  AND :status = 9
UNION
SELECT 'S'                AS tipo,
       CAST(nfno AS CHAR) AS numero,
       nfse               AS serie,
       status = 1         AS cancelado
FROM sqldados.nf
WHERE eordno = :ordno
  AND pdvno = 0
  AND storeno = :storeno
  AND nfse = 66
  AND :tipo = 'S'
  AND :status = 7
UNION
SELECT 'E'                  AS tipo,
       nfname               AS numero,
       invse                AS serie,
       bits & POW(2, 4) > 0 AS cancelado
FROM sqldados.inv
WHERE ordno = :ordno
  AND storeno = :storeno
  AND invse = 66
  AND :tipo = 'E'
  AND :status = 7
ORDER BY 4, 2 DESC;