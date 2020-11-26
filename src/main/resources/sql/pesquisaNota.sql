SELECT 'S' AS tipo, cast(nfno AS CHAR) AS numero, nfse AS serie, status = 1 AS cancelado
FROM sqldados.nf
WHERE eordno = :ordno AND pdvno = 0 AND storeno = :storeno AND nfse = 66 AND :tipo = 'S'
UNION
SELECT 'E' AS tipo, nfname AS numero, invse AS serie, bits & POW(2, 4) > 0 AS cancelado
FROM sqldados.inv
WHERE ordno = :ordno AND storeno = :storeno AND invse = 66 AND :tipo = 'E'
ORDER BY 4, 2 DESC