SELECT X.storeno AS storeno, CAST(CONCAT(X.nfno, '/', X.nfse) AS CHAR) AS numero, X.prdno AS prdno,
       X.grade AS grade, qtty / 1000 AS quant,
       IF(S.last_cost = 0, S.cm_varejo_otn, S.last_cost) / 10000 AS preco,
       TRIM(MID(P.name, 1, 37)) AS descricao,
       GROUP_CONCAT(DISTINCT localizacao ORDER BY localizacao SEPARATOR '/') AS localizacao
FROM sqldados.xaprd          AS X
  INNER JOIN sqldados.nf     AS N
               USING (storeno, pdvno, xano)
  INNER JOIN sqldados.prd    AS P
               ON X.prdno = P.no
  INNER JOIN sqldados.stk    AS S
               ON S.storeno = X.storeno AND S.prdno = X.prdno AND S.grade = X.grade
  LEFT JOIN  sqldados.prdloc AS L
               ON X.storeno = L.storeno AND X.prdno = L.prdno AND L.localizacao <> 'CD00'
WHERE N.tipo = 2 AND N.storeno = :storeno AND N.nfno = :nfno AND N.nfse = :nfse
GROUP BY N.storeno, N.nfno, N.nfse, X.prdno, X.grade
