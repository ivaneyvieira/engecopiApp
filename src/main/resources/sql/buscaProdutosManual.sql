USE sqldados;

DO @LJ := :loja;
DO @PR := :prdno;
DO @VEND := :vends;
DO @TYPE := :types;
DO @CL := :cl;
DO @DESCRICAO := :descricao;

DROP TEMPORARY TABLE IF EXISTS T_PRD;
CREATE TEMPORARY TABLE T_PRD
(
  PRIMARY KEY (prdno, grade)
)
SELECT stk.storeno                        AS loja,
       stk.prdno                          AS prdno,
       stk.grade                          AS grade,
       TRIM(MID(prd.name, 1, 37))         AS descricao,
       prd.mfno                           AS fornecedor,
       LPAD(prd.clno, 6, '0')             AS centrodelucro,
       prd.typeno                         AS tipo,
       stk.qtty_atacado + stk.qtty_varejo AS qtty,
       IFNULL(stk.cm_varejo, 0)           AS ultimocusto
FROM sqldados.stk
       INNER JOIN sqldados.prd
                  ON (stk.prdno = prd.no)
WHERE stk.storeno = @LJ
  AND dereg & POW(2, 2) <> POW(2, 2)
  AND (prdno = LPAD(@PR, 16, ' ') OR @PR = '')
  AND (FIND_IN_SET(prd.mfno, @VEND) > 0 OR @VEND = '')
  AND (FIND_IN_SET(prd.typeno, @TYPE) > 0 OR @TYPE = '')
  AND (prd.clno = @CL OR prd.deptno = @CL OR prd.groupno = @CL OR @CL = '')
  AND (prd.name LIKE CONCAT(@DESCRICAO, '%') OR @DESCRICAO = '')
GROUP BY prdno, grade;

DROP TEMPORARY TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
SELECT P.loja,
       P.prdno,
       P.grade,
       P.descricao,
       P.fornecedor,
       P.centrodelucro,
       P.tipo,
       P.qtty,
       P.ultimocusto
FROM T_PRD AS P;

SELECT loja                                    AS loja,
       TRIM(prdno)                             AS prdno,
       grade                                   AS grade,
       IFNULL(descricao, '')                   AS descricao,
       IFNULL(fornecedor, 0)                   AS fornecedor,
       CAST(IFNULL(centrodelucro, '') AS CHAR) AS centrodelucro,
       IFNULL(tipo, 0)                         AS tipo,
       qtty / 1000                             AS qtty,
       ultimocusto / 10000                     AS custo,
       (qtty / 1000) * (ultimocusto / 10000)   AS total
FROM T