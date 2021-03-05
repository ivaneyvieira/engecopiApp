DO @LOJA := :storeno;
DO @NFNO := :nfno;
DO @NFSE := :nfse;
DO @SERIE := 66;
DO @TIPO := 'E';
DO @FATOR := IF(@TIPO = 'E', -1, +1);
DO @DOC := IF(@TIPO = 'E', 'AJUS ENT', 'AJUS SAI');

DROP TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
SELECT X.storeno,
       X.nfno                                                         AS ordno,
       X.prdno,
       X.grade,
       qtty,
       ROUND(IF(I.last_cost = 0, I.cm_varejo_otn, I.last_cost)) / 100 AS cost,
       V.no                                                           AS vendno,
       C.no                                                           AS custno,
       N.empno
FROM sqldados.xaprd         AS X
  INNER JOIN sqldados.nf    AS N
	       USING (storeno, pdvno, xano)
  INNER JOIN sqldados.stk   AS I
	       ON I.storeno = X.storeno AND I.prdno = X.prdno AND I.grade = X.grade
  INNER JOIN sqldados.store AS S
	       ON S.no = X.storeno
  INNER JOIN sqldados.vend  AS V
	       ON V.cgc = S.cgc
  INNER JOIN sqldados.custp AS C
	       ON C.cpf_cgc = S.cgc
WHERE X.storeno = @LOJA
  AND X.nfno = @NFNO
  AND X.nfse = @NFSE;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET longReserva1 = qtty_atacado;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET qtty_atacado = qtty_atacado + @FATOR * T.qtty,
    last_date    = CURRENT_DATE * 1,
    longReserva2 = 0;

DELETE
FROM sqldados.stkmov
WHERE remarks LIKE CONCAT('%:PED E', @PEDIDO)
  AND storeno = @LOJA
  AND @TIPO = 'E';

UPDATE sqldados.nf AS N
SET s16 = 1 /*Orçamento*/
WHERE N.storeno = @LOJA
  AND N.nfno = @NFNO
  AND N.nfse = @NFSE
  AND N.tipo = 2;
