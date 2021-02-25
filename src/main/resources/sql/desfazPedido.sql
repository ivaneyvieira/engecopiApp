DO @LOJA := :storeno;
DO @PEDIDO := :ordno;
DO @SERIE := 66;
DO @TIPO := :tipo;
DO @FATOR := IF(@TIPO = 'E', -1, +1);
DO @DOC := IF(@TIPO = 'E', 'AJUS ENT', 'AJUS SAI');

DROP TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
SELECT E.storeno,
       E.ordno,
       E.prdno,
       E.grade,
       qtty,
       ROUND(IF(I.last_cost = 0, I.cm_varejo_otn, I.last_cost)) / 100 AS cost,
       V.no                                                           AS vendno,
       C.no                                                           AS custno,
       E.empno
FROM sqldados.eoprd         AS E
  INNER JOIN sqldados.eord  AS O
	       ON O.ordno = E.ordno AND O.storeno = E.storeno
  INNER JOIN sqldados.stk   AS I
	       ON I.storeno = E.storeno AND I.prdno = E.prdno AND I.grade = E.grade
  INNER JOIN sqldados.store AS S
	       ON S.no = E.storeno
  INNER JOIN sqldados.vend  AS V
	       ON V.cgc = S.cgc
  INNER JOIN sqldados.custp AS C
	       ON C.cpf_cgc = S.cgc
WHERE E.storeno = @LOJA
  AND E.ordno = @PEDIDO;

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

DELETE
FROM sqldados.stkmov
WHERE remarks LIKE CONCAT('%:PED S', @PEDIDO)
  AND storeno = @LOJA
  AND @TIPO = 'S';

UPDATE sqldados.eord AS E
SET status = 1 /*Orçamento*/
WHERE E.storeno = @LOJA
  AND E.ordno = @PEDIDO;
