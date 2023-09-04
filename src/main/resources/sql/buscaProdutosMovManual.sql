USE sqldados;

DO @LJ := :loja;
DO @TRANSACAO := :transacao;

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
       IFNULL(stk.cm_varejo, 0)           AS ultimocusto
FROM sqldados.stk
       INNER JOIN sqldados.prd
                  ON (stk.prdno = prd.no)
WHERE stk.storeno = @LJ
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
       ultimocusto
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
(
  PRIMARY KEY (prdno, grade)
)
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
       P.ultimocusto
FROM T_PRD AS P
       LEFT JOIN T_SALDO AS S
                 USING (prdno, grade)
       LEFT JOIN T_LOC AS L
                 USING (prdno, grade);

DROP TEMPORARY TABLE IF EXISTS T_MOV;
CREATE TEMPORARY TABLE T_MOV
(
  PRIMARY KEY (prdno, grade)
)
SELECT prdno,
       grade,
       MAX(qtty)    AS qtty,
       MAX(remarks) AS remarks
FROM (SELECT storeno,
             prdno,
             grade,
             xano,
             qtty,
             remarks
      FROM stkmov
      WHERE storeno = @LJ
        AND xano = @TRANSACAO
      UNION
      SELECT storeno,
             prdno,
             grade,
             xano,
             qtty,
             '' AS remarks
      FROM stkmovh
      WHERE storeno = @LJ
        AND xano = @TRANSACAO) AS H
GROUP BY storeno, prdno, grade, xano;


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
       remarks                                 AS obs
FROM T
       INNER JOIN T_MOV AS M
                  USING (prdno, grade)


