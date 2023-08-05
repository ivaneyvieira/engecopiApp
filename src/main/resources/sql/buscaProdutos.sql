DO @LJ := :loja;
DO @PR := :prdno;
DO @VEND := :vends;
DO @TYPE := :types;
DO @OP := :operacao;
DO @YM := :ym;
DO @DI := CONCAT(@YM, '01') * 1;
DO @DF := CONCAT(@YM, '31') * 1;
DO @PEDIDO := :numPedido;

DROP TEMPORARY TABLE IF EXISTS T_PRD_NF;
CREATE TEMPORARY TABLE T_PRD_NF
(
  PRIMARY KEY (prdno, grade)
)
SELECT prdno,
       grade,
       SUM(qtty / 1000) AS qtty
FROM sqldados.stkmov
WHERE (storeno = @LJ)
  AND ((remarks LIKE CONCAT('%:PED _%') OR xano = @PEDIDO) OR
       (remarks LIKE 'AJUSTE%'))
  AND ((remarks NOT LIKE '%:PED A%') OR
       (remarks LIKE 'AJUSTE%'))
  AND (prdno = LPAD(@PR, 16, ' ') OR @PR = '')
  AND date BETWEEN @DI AND @DF
  AND ((remarks LIKE CONCAT('%', @PEDIDO) OR xano = @PEDIDO OR @PEDIDO = '') OR
       (remarks LIKE 'AJUSTE%'))
GROUP BY prdno, grade;

DROP TEMPORARY TABLE IF EXISTS T_PRD;
CREATE TEMPORARY TABLE T_PRD
(
  PRIMARY KEY (prdno, grade)
)
SELECT stk.prdno                  AS prdno,
       stk.grade                  AS grade,
       TRIM(MID(prd.name, 1, 37)) AS descricao,
       prd.mfno                   AS fornecedor,
       LPAD(prd.clno, 6, '0')     AS centrodelucro,
       prd.typeno                 AS tipo,
       stk.qtty_atacado           AS qttyatacado,
       IFNULL(stk.cm_varejo, 0)   AS ultimocusto
FROM sqldados.stk
       INNER JOIN sqldados.prd
                  ON (stk.prdno = prd.no)
WHERE stk.storeno = @LJ
  AND dereg & POW(2, 2) <> POW(2, 2)
  AND ((qtty_atacado > 0 AND @OP = 'entrada') OR (qtty_atacado < 0 AND @OP != 'entrada'))
  AND (prdno = LPAD(@PR, 16, ' ') OR @PR = '')
  AND (FIND_IN_SET(prd.mfno, @VEND) > 0 OR @VEND = '')
  AND (FIND_IN_SET(prd.typeno, @TYPE) > 0 OR @TYPE = '')
GROUP BY prdno, grade;

DROP TEMPORARY TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
SELECT P.prdno,
       P.grade,
       P.descricao,
       P.fornecedor,
       P.centrodelucro,
       P.tipo,
       IFNULL(N.qtty * 1000, 0) AS qttynfs,
       P.qttyatacado,
       P.ultimocusto
FROM T_PRD AS P
       LEFT JOIN T_PRD_NF AS N
                 USING (prdno, grade)
HAVING qttynfs <> 0;

SELECT TRIM(prdno)                                  AS prdno,
       grade                                        AS grade,
       IFNULL(descricao, '')                        AS descricao,
       IFNULL(fornecedor, 0)                        AS fornecedor,
       CAST(IFNULL(centrodelucro, '') AS CHAR)      AS centrodelucro,
       IFNULL(tipo, 0)                              AS tipo,
       qttynfs / 1000                               AS qtdNfForn,
       qttyatacado / 1000                           AS qtdAtacado,
       (qttyatacado - qttynfs) / 1000               AS qtdConsiderada,
       ultimocusto / 10000                          AS custo,
       (qttyatacado / 1000) * (ultimocusto / 10000) AS total
FROM T