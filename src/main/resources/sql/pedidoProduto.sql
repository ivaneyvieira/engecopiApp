SELECT
  E.storeno                                                 AS storeno,
  E.ordno                                                   AS ordno,
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
WHERE E.storeno = :storeno
      AND E.ordno = :ordno
      AND O.status IN (1, 4)
GROUP BY storeno, ordno, prdno, grade;