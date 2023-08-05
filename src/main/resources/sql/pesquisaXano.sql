DO @TIPO := :tipo;
DO @XANO := :xano;
DO @LOJA := :loja;

SELECT distinct xano
FROM sqldados.stkmov
WHERE storeno = @LOJA
  AND xano = @XANO
  AND remarks LIKE CONCAT('AJUSTE', @TIPO, @XANO)