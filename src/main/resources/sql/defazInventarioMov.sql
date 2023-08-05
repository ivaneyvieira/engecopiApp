DO @TIPO := :tipo;
DO @XANO := :xano;
DO @LOJA := :loja;

DROP TEMPORARY TABLE IF EXISTS T_STKMOV;
CREATE TEMPORARY TABLE T_STKMOV
(
  PRIMARY KEY (storeno, prdno, grade, xano)
)
SELECT storeno,
       prdno,
       grade,
       xano,
       qtty,
       remarks
FROM sqldados.stkmov
WHERE storeno = @LOJA
  AND xano = @XANO
  AND remarks LIKE CONCAT('AJUSTE', @TIPO, @XANO);

DELETE
FROM sqldados.stkmov
WHERE storeno = @LOJA
  AND xano = @XANO
  AND remarks LIKE CONCAT('AJUSTE', @TIPO, @XANO);

UPDATE sqldados.stk AS S
  INNER JOIN T_STKMOV AS M
  USING (storeno, prdno, grade)
SET S.qtty_atacado = (S.qtty_atacado - M.qtty)
WHERE (S.storeno = @LOJA)