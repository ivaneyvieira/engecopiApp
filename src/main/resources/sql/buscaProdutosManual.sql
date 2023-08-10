USE sqldados;

DO @LJ := :loja;
DO @PEDIDO := :pedido;
DO @PR := :prdno;
DO @VEND := :vends;
DO @TYPE := :types;
DO @CL := :cl;
DO @DESCRICAO := :descricao;


DROP TEMPORARY TABLE IF EXISTS T_ORD;
CREATE TEMPORARY TABLE T_ORD
(
  PRIMARY KEY (storeno, prdno, grade)
)
SELECT storeno,
       prdno,
       grade,
       SUM(qtty)                                                  AS quant,
       IFNULL(CAST(TRIM(MID(R.remarks__480, 1, 20)) AS CHAR), '') AS obs
FROM sqldados.eoprd AS E
       LEFT JOIN sqldados.eordrk AS R
                 USING (storeno, ordno)
WHERE @PEDIDO != 0
  AND storeno = @LJ
  AND ordno = @PEDIDO
GROUP BY prdno, grade;

DROP TEMPORARY TABLE IF EXISTS T_PRDCAD;
CREATE TEMPORARY TABLE T_PRDCAD
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
       stk.qtty_atacado + stk.qtty_varejo AS saldo,
       0                                  AS qtty,
       IFNULL(stk.cm_varejo, 0)           AS ultimocusto,
       ''                                 AS obs
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
  AND @PEDIDO = 0
GROUP BY prdno, grade;

DROP TEMPORARY TABLE IF EXISTS T_PRD_STK;
CREATE TEMPORARY TABLE T_PRD_STK
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
       stk.qtty_atacado + stk.qtty_varejo AS saldo,
       T_ORD.quant                        AS qtty,
       IFNULL(stk.cm_varejo, 0)           AS ultimocusto,
       obs                                AS obs
FROM sqldados.stk
       INNER JOIN T_ORD
                  USING (storeno, prdno, grade)
       JOIN sqldados.prd
            ON (stk.prdno = prd.no)
WHERE @PEDIDO <> 0
GROUP BY prdno, grade;

DROP TEMPORARY TABLE IF EXISTS T_PRD;
CREATE TEMPORARY TABLE T_PRD
(
  PRIMARY KEY (prdno, grade)
)
SELECT loja,
       prdno,
       grade,
       descricao,
       fornecedor,
       centrodelucro,
       tipo,
       saldo,
       qtty,
       ultimocusto,
       obs
FROM T_PRD_STK
UNION
SELECT loja,
       prdno,
       grade,
       descricao,
       fornecedor,
       centrodelucro,
       tipo,
       saldo,
       qtty,
       ultimocusto,
       obs
FROM T_PRDCAD;

DROP TEMPORARY TABLE IF EXISTS T_SALDO;
CREATE TEMPORARY TABLE T_SALDO
(
  PRIMARY KEY (prdno, grade)
)
SELECT prdno, grade, SUM(qtty_varejo + qtty_atacado) AS saldoEstoque
FROM sqldados.stk
       INNER JOIN T_PRD USING (prdno, grade)
GROUP BY prdno, grade;

DROP TEMPORARY TABLE IF EXISTS T_LOC;
CREATE TEMPORARY TABLE T_LOC
(
  PRIMARY KEY (prdno, grade)
)
SELECT prdno, grade, MAX(MID(localizacao, 1, 4)) AS loc
FROM sqldados.prdloc
WHERE storeno = 4
  AND localizacao NOT LIKE 'CD00%'
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
       IFNULL(S.saldoEstoque, 0)       AS saldoTotal,
       IFNULL(CAST(L.loc AS CHAR), '') AS loc,
       P.saldo,
       P.qtty,
       P.ultimocusto,
       P.obs
FROM T_PRD AS P
       LEFT JOIN T_SALDO AS S
                 USING (prdno, grade)
       LEFT JOIN T_LOC AS L
                 USING (prdno, grade);

SELECT loja                                    AS loja,
       TRIM(prdno)                             AS prdno,
       grade                                   AS grade,
       IFNULL(descricao, '')                   AS descricao,
       IFNULL(fornecedor, 0)                   AS fornecedor,
       CAST(IFNULL(centrodelucro, '') AS CHAR) AS centrodelucro,
       loc                                     AS loc,
       IFNULL(tipo, 0)                         AS tipo,
       saldoTotal / 1000                       AS saldoTotal,
       saldo / 1000                            AS saldo,
       qtty / 1000                             AS qtty,
       ultimocusto / 10000                     AS custo,
       obs                                     AS obs
FROM T