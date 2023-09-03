DO @TIPO := :tipo;
DO @XANO := :xano;
DO @LOJA := :loja;
DO @FATOR := IF(@TIPO = 'E', 1, -1);

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
  AND remarks LIKE CONCAT('66:PED ', @TIPO, @XANO);

DROP TEMPORARY TABLE IF EXISTS T_STKMOVH;
CREATE TEMPORARY TABLE T_STKMOVH
SELECT *
FROM sqldados.stkmov
WHERE storeno = @LOJA
  AND xano = @XANO
  AND remarks LIKE CONCAT('66:PED ', @TIPO, @XANO);

DELETE
FROM sqldados.stkmovh
WHERE storeno = @LOJA
  AND xano = @XANO
  AND EXISTS(SELECT * FROM T_STKMOV)
  AND NOT EXISTS(SELECT * FROM T_STKMOVH);

UPDATE sqldados.stk AS S
  INNER JOIN T_STKMOV AS M
  USING (storeno, prdno, grade)
SET S.qtty_atacado = (S.qtty_atacado - (M.qtty * @FATOR))
WHERE (S.storeno = @LOJA)
  AND NOT EXISTS(SELECT * FROM T_STKMOVH)