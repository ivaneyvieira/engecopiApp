SELECT E.storeno                                                 AS storeno,
       E.ordno                                                   AS numero,
       E.prdno                                                   AS prdno,
       E.grade                                                   AS grade,
       qtty / 1000                                               AS quant,
       IF(I.last_cost = 0, I.cm_varejo_otn, I.last_cost) / 10000 AS preco,
       TRIM(MID(P.name, 1, 37))                                  AS descricao,
       IFNULL(GROUP_CONCAT(DISTINCT localizacao ORDER BY localizacao SEPARATOR '/'),
              '')                                                AS localizacao
FROM sqldados.eoprd AS E
         INNER JOIN sqldados.eord O
                    USING (storeno, ordno)
         INNER JOIN sqldados.prd AS P
                    ON E.prdno = P.no
         INNER JOIN sqldados.stk AS I
                    ON I.storeno = E.storeno AND I.prdno = E.prdno AND I.grade = E.grade
         LEFT JOIN sqldados.prdloc AS L
                   ON E.storeno = L.storeno AND E.prdno = L.prdno AND L.localizacao <> 'CD00'
WHERE O.status IN (1, 4)
  AND O.storeno = :storeno
  AND O.ordno = :numero
  AND :serie = ''
GROUP BY storeno, ordno, prdno, grade
UNION
DISTINCT
SELECT X.storeno                                                             AS storeno,
       CAST(CONCAT(X.nfno, '/', X.nfse) AS CHAR)                             AS numero,
       X.prdno                                                               AS prdno,
       X.grade                                                               AS grade,
       ROUND(qtty, 3)                                                        AS quant,
       IF(S.last_cost = 0, S.cm_varejo_otn, S.last_cost) / 10000             AS preco,
       TRIM(MID(P.name, 1, 37))                                              AS descricao,
       GROUP_CONCAT(DISTINCT localizacao ORDER BY localizacao SEPARATOR '/') AS localizacao,
       P.clno                                                                as cl,
       S.qtty_varejo / 1000                                                  as estoque,
       P.mfno                                                                as fornecedor,
       P.typeno                                                              as tipo
FROM sqldados.xaprd AS X
         INNER JOIN sqldados.nf AS N
                    USING (storeno, pdvno, xano)
         INNER JOIN sqldados.prd AS P
                    ON X.prdno = P.no
         INNER JOIN sqldados.stk AS S
                    ON S.storeno = X.storeno AND S.prdno = X.prdno AND S.grade = X.grade
         LEFT JOIN sqldados.prdloc AS L
                   ON X.storeno = L.storeno AND X.prdno = L.prdno AND L.localizacao <> 'CD00'
WHERE N.tipo = 2
  AND N.storeno = :storeno
  AND N.nfno = :numero
  AND N.nfse = :serie
GROUP BY N.storeno, N.nfno, N.nfse, X.prdno, X.grade