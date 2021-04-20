DO @TIPO := :tipo;
DO @XANO := :xano;
DO @LOJA := :loja;
DO @YM := :ym;
DO @DI := CONCAT(@YM, '01') * 1;
DO @DF := CONCAT(@YM, '31') * 1;
DO @HOJE := CURRENT_DATE * 1;
DO @NOVO_XANO := (SELECT MAX(xano + 1)
		 FROM sqldados.stkmov);

DROP TEMPORARY TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
SELECT xano,
       qtty,
       date,
       nfno,
       cm_fiscal,
       cm_real,
       storeno,
       userno,
       prdno,
       grade,
       auxLong1 AS numPedido
FROM sqldados.stkmovh
WHERE xano = @XANO
  AND storeno = @LOJA
  AND nfno = 0
  AND CASE
	WHEN @TIPO = 'E'
	  THEN qtty > 0
	WHEN @TIPO = 'S'
	  THEN qtty < 0
	ELSE FALSE
      END;

DELETE
FROM sqldados.stkmov
WHERE storeno = @LOJA
  AND xano = @XANO
  AND EXISTS(SELECT *
	     FROM T);

DELETE
FROM sqldados.stkmovh
WHERE xano = @XANO
  AND storeno = @LOJA
  AND nfno = 0
  AND EXISTS(SELECT *
	     FROM T);

INSERT INTO sqldados.stkmov(xano, qtty, date, cm_fiscal, cm_real, storeno, bits, prdno, grade,
			    remarks)
SELECT @NOVO_XANO                         AS xano,
       qtty,
       @HOJE                              AS date,
       cm_fiscal,
       cm_real,
       storeno,
       1                                  AS bits,
       prdno,
       grade,
       CONCAT('66:PED', @TIPO, numPedido) AS remarks
FROM T;


UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET stk.qtty_atacado = (stk.qtty_atacado + T.qtty),
    stk.qtty_varejo  = (stk.qtty_varejo - T.qtty)