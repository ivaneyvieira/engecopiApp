SELECT grade
FROM sqldados.stk
WHERE prdno = LPAD(:codigo, 16, ' ')
  AND grade <> ''
  AND storeno IN (1, 2, 3, 4, 5, 6, 7, 10)
GROUP BY grade
ORDER BY grade