SELECT E.storeno                                                      AS storeno,
       E.ordno                                                        AS numero,
       E.prdno                                                        AS prdno,
       E.grade                                                        AS grade,
       qtty / 1000                                                    AS quant,
       TRUNCATE(IF(S.cm_real = 0, S.cm_varejo, S.cm_real) / 10000, 4) AS preco,
       TRIM(MID(P.name, 1, 37))                                       AS descricao,
       IFNULL(GROUP_CONCAT(DISTINCT localizacao ORDER BY localizacao SEPARATOR '/'),
              '')                                                     AS localizacao,
       P.clno                                                         as cl,
       (S.qtty_varejo + S.qtty_atacado) / 1000                        as estoque,
       P.mfno                                                         as fornecedor,
       P.typeno                                                       as tipo,
       CAST(TRIM(MID(R.remarks__480, 1, 20)) AS CHAR)                 as obs
FROM sqldados.eoprd AS E
         INNER JOIN sqldados.eord O
                    USING (storeno, ordno)
         LEFT JOIN sqldados.eordrk AS R
                   USING (storeno, ordno)
         INNER JOIN sqldados.prd AS P
                    ON E.prdno = P.no
         INNER JOIN sqldados.stk AS S
                    ON S.storeno = E.storeno AND S.prdno = E.prdno AND S.grade = E.grade
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
       TRUNCATE(IF(S.cm_real = 0, S.cm_varejo, S.cm_real) / 10000, 4)        AS preco,
       TRIM(MID(P.name, 1, 37))                                              AS descricao,
       GROUP_CONCAT(DISTINCT localizacao ORDER BY localizacao SEPARATOR '/') AS localizacao,
       P.clno                                                                as cl,
       (S.qtty_varejo + S.qtty_atacado) / 1000                               as estoque,
       P.mfno                                                                as fornecedor,
       P.typeno                                                              as tipo,
       CAST(TRIM(MID(N.remarks, 1, 10)) AS CHAR)                             as obs
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