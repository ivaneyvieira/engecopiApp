DO @LOJA := :loja;
DO @XANO := :xano;

SELECT storeno,
       xano,
       IF(SUM(qtty) > 0, 'E', 'S')     AS operacao,
       IF(SUM(nfno = 0) > 0, 'N', 'S') AS jaProcessado
FROM sqldados.stkmovh
WHERE storeno = @LOJA
  AND xano = @XANO
  AND IF(xano = 12553459, date = 20210420, TRUE)
GROUP BY storeno, xano
