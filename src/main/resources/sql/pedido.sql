SELECT
  E.storeno                                                 AS storeno,
  E.ordno                                                   AS numero,
  E.prdno                                                   AS prdno,
  E.grade                                                   AS grade,
  qtty / 1000                                               AS quant,
  IF(I.last_cost = 0, I.cm_varejo_otn, I.last_cost) / 10000 AS preco,
  TRIM(MID(P.name, 1, 37))                                  AS descricao,
  GROUP_CONCAT(DISTINCT localizacao ORDER BY localizacao separator '/')   AS localizacao
FROM sqldados.eoprd AS E
  INNER JOIN sqldados.eord O
  USING (storeno, ordno)
  INNER JOIN sqldados.prd AS P
    ON E.prdno = P.no
  INNER JOIN sqldados.stk AS I
    ON I.storeno = E.storeno
       AND I.prdno = E.prdno
       AND I.grade = E.grade
  LEFT JOIN sqldados.prdloc AS L
    ON E.storeno = L.storeno
    AND E.prdno  = L.prdno
WHERE O.status IN (1, 4)
      AND O.storeno = 4
      AND O.ordno   = ':numero'
      AND ""        = ':serie'
GROUP BY storeno, ordno, prdno, grade
UNION
SELECT
  E.storeno                                                 AS storeno,
  CONCAT(O.nfname, '/', O.invse)                            AS numero,
  E.prdno                                                   AS prdno,
  E.grade                                                   AS grade,
  qtty / 1000                                               AS quant,
  IF(I.last_cost = 0, I.cm_varejo_otn, I.last_cost) / 10000 AS preco,
  TRIM(MID(P.name, 1, 37))                                  AS descricao,
  GROUP_CONCAT(DISTINCT localizacao ORDER BY localizacao separator '/')   AS localizacao
FROM sqldados.iprd AS E
  INNER JOIN sqldados.inv O
  USING (invno)
  INNER JOIN sqldados.prd AS P
    ON E.prdno = P.no
  INNER JOIN sqldados.stk AS I
    ON I.storeno = E.storeno
       AND I.prdno = E.prdno
       AND I.grade = E.grade
  LEFT JOIN sqldados.prdloc AS L
    ON E.storeno = L.storeno
    AND E.prdno  = L.prdno
WHERE O.storeno = 4
  AND O.nfname  = 'numero'
  AND O.invse   = 'serie'
  AND 'serie'   = '66'
GROUP BY storeno, ordno, prdno, grade
UNION
SELECT
  E.storeno                                                 AS storeno,
  CONCAT(O.nfno, '/', O.nfse)                               AS numero,
  E.prdno                                                   AS prdno,
  E.grade                                                   AS grade,
  qtty / 1000                                               AS quant,
  IF(I.last_cost = 0, I.cm_varejo_otn, I.last_cost) / 10000 AS preco,
  TRIM(MID(P.name, 1, 37))                                  AS descricao,
  GROUP_CONCAT(DISTINCT localizacao ORDER BY localizacao separator '/')   AS localizacao
FROM sqldados.xaprd2 AS E
  INNER JOIN sqldados.nf O
  USING (storeno, pdvno, xano)
  INNER JOIN sqldados.prd AS P
    ON E.prdno = P.no
  INNER JOIN sqldados.stk AS I
    ON I.storeno = E.storeno
       AND I.prdno = E.prdno
       AND I.grade = E.grade
  LEFT JOIN sqldados.prdloc AS L
    ON E.storeno = L.storeno
    AND E.prdno  = L.prdno
WHERE O.storeno = 4
  AND O.nfno    = 'numero'
  AND O.nfse    = 'serie'
  AND 'serie'   = '66'
GROUP BY storeno, ordno, prdno, grade
