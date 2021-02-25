DO @LOJA := :storeno;
DO @NFNO := :nfno;
DO @NFSE := :nfse;
DO @SERIE := 66;
DO @TIPO := 'E';
DO @FATOR := IF(@TIPO = 'E', 1, -1);
DO @DOC := IF(@TIPO = 'E', 'AJUS ENT', 'AJUS SAI');
DO @TIPO_NOTA := 7;
DO @OBS := CASE @TIPO_NOTA
	     WHEN 9
	       THEN '66'
	     WHEN 7
	       THEN 'GARANTIA'
	     ELSE ''
	   END;

DROP TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
SELECT X.storeno,
       X.nfno                                                         AS ordno,
       X.prdno,
       X.grade,
       ROUND(qtty * 1000)                                             AS qtty,
       ROUND(IF(I.last_cost = 0, I.cm_varejo_otn, I.last_cost)) / 100 AS cost,
       V.no                                                           AS vendno,
       IF(V.state = 'PI', '5949', '6949')                             AS cfopS,
       IF(V.state = 'PI', '1949', '2949')                             AS cfopE,
       CAST(CONCAT(F.no, ' ', MID(F.sname, 1, 4)) AS CHAR)            AS frabricante,
       C.no                                                           AS custno,
       N.empno
FROM sqldados.xaprd         AS X
  INNER JOIN sqldados.nf    AS N
	       USING (storeno, pdvno, xano)
  INNER JOIN sqldados.stk   AS I
	       ON I.storeno = X.storeno AND I.prdno = X.prdno AND I.grade = X.grade
  INNER JOIN sqldados.store AS S
	       ON S.no = X.storeno
  INNER JOIN sqldados.prd   AS P
	       ON P.no = X.prdno
  INNER JOIN sqldados.vend  AS F
	       ON F.no = P.mfno
  INNER JOIN sqldados.vend  AS V
	       ON V.cgc = S.cgc
  INNER JOIN sqldados.custp AS C
	       ON C.cpf_cgc = S.cgc
WHERE X.storeno = @LOJA
  AND X.nfno = @NFNO
  AND X.nfse = @NFSE;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET longReserva1 = qtty_atacado
WHERE longReserva2 <> T.ordno;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET qtty_atacado = qtty_atacado + @FATOR * T.qtty,
    last_date    = CURRENT_DATE * 1
WHERE longReserva2 <> T.ordno;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET longReserva2 = T.ordno;

DO @INVDEL := (SELECT MAX(xano)
	       FROM sqldados.stkmov
	       WHERE remarks LIKE CONCAT('%:PED E', @PEDIDO)
		 AND storeno = @LOJA);

DO @XANO := (SELECT MAX(xano) + 1
	     FROM sqldados.stkmov);

INSERT INTO sqldados.stkmov(xano, qtty, date, cm_fiscal, cm_real, storeno, bits, prdno, grade,
			    remarks)
SELECT @XANO                                                             AS xano,
       qtty,
       CURRENT_DATE * 1                                                  AS date,
       cost * 100                                                        AS cm_fiscal,
       cost * 100                                                        AS cm_real,
       storeno,
       1                                                                 AS bits,
       prdno,
       grade,
       CONCAT(CAST(CONCAT(@OBS, ' ',
			  GROUP_CONCAT(DISTINCT frabricante ORDER BY frabricante SEPARATOR
				       ' ')) AS CHAR), ':PED E', @PEDIDO) AS remarks
FROM T
WHERE @TIPO = 'E';

DO @XANO := (SELECT MAX(xano) + 1
	     FROM sqldados.stkmov);

UPDATE sqldados.nf AS N
SET s16 = 4 /*Expirado*/
WHERE N.storeno = @LOJA
  AND N.nfno = @NFNO
  AND N.nfse = @NFSE
  AND N.tipo = 2;
