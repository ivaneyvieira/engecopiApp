DO @LOJA := :storeno;
DO @PEDIDO := :ordno;
DO @SERIE := 66;
DO @TIPO := :tipo;
DO @FATOR := IF(@TIPO = 'E', 1, -1);
DO @DOC := IF(@TIPO = 'E', 'AJUS ENT', 'AJUS SAI');
DO @TIPO_NOTA := :t_nota;
DO @TIPO_NOTA_NOVO := CASE @TIPO_NOTA WHEN 9 THEN 9 WHEN 7 THEN 2 ELSE 0 END;
DO @OBS := CASE @TIPO_NOTA
	     WHEN 9
	       THEN '66'
	     WHEN 7
	       THEN 'GARANTIA'
	     ELSE ''
	   END;

DROP TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
SELECT E.storeno,
       E.ordno,
       E.prdno,
       E.grade,
       qtty,
       ROUND(IF(I.last_cost = 0, I.cm_varejo_otn, I.last_cost)) / 100 AS cost,
       V.no                                                           AS vendno,
       IF(V.state = 'PI', '5949', '6949')                             AS cfopSV,
       IF(V.state = 'PI', '1949', '2949')                             AS cfopEV,
       IF(CV.state = 'PI', '5949', '6949')                            AS cfopSC,
       IF(CV.state = 'PI', '1949', '2949')                            AS cfopEC,
       CAST(CONCAT(F.no, ' ', MID(F.sname, 1, 4)) AS CHAR)            AS fabricante,
       C.no                                                           AS custno,
       E.empno,
       CC.no                                                          AS custnoC,
       CV.no                                                          AS vendnoC
FROM sqldados.eoprd         AS E
  INNER JOIN sqldados.eord  AS O
	       ON O.ordno = E.ordno AND O.storeno = E.storeno
  INNER JOIN sqldados.stk   AS I
	       ON I.storeno = E.storeno AND I.prdno = E.prdno AND I.grade = E.grade
  INNER JOIN sqldados.store AS S
	       ON S.no = E.storeno
  INNER JOIN sqldados.prd   AS P
	       ON P.no = E.prdno
  INNER JOIN sqldados.vend  AS F/*Fabricante*/
	       ON F.no = P.mfno
  INNER JOIN sqldados.vend  AS V /*Loja*/
	       ON V.cgc = S.cgc
  INNER JOIN sqldados.custp AS C /*Loja*/
	       ON C.cpf_cgc = S.cgc
  LEFT JOIN  sqldados.custp AS CC /*Cliente*/
	       ON CC.no = O.custno
  LEFT JOIN  sqldados.vend  AS CV /*Cliente*/
	       ON CC.cpf_cgc = CV.cgc
WHERE E.storeno = @LOJA
  AND E.ordno = @PEDIDO;

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
SELECT @XANO            AS xano,
       qtty,
       CURRENT_DATE * 1 AS date,
       cost * 100       AS cm_fiscal,
       cost * 100       AS cm_real,
       storeno,
       1                AS bits,
       prdno,
       grade,
       CONCAT(IF(@OBS = '66', @OBS, CAST(CONCAT(@OBS, ' ', fabricante) AS CHAR)), ':PED E',
	      @PEDIDO)  AS remarks
FROM T
WHERE @TIPO = 'E';

DO @XANO := (SELECT MAX(xano) + 1
	     FROM sqldados.stkmov);

INSERT INTO sqldados.stkmov(xano, qtty, date, cm_fiscal, cm_real, storeno, bits, prdno, grade,
			    remarks)
SELECT @XANO            AS xano,
       -qtty            AS qtty,
       CURRENT_DATE * 1 AS date,
       cost * 100       AS cm_fiscal,
       cost * 100       AS cm_real,
       storeno,
       1                AS bits,
       prdno,
       grade,
       CONCAT(IF(@OBS = '66', @OBS, CAST(CONCAT(@OBS, ' ', fabricante) AS CHAR)), ':PED S',
	      @PEDIDO)  AS remarks
FROM T
WHERE @TIPO = 'S';


UPDATE sqldados.eord AS E
SET status = 4 /*Expirado*/
WHERE E.storeno = @LOJA
  AND E.ordno = @PEDIDO;
