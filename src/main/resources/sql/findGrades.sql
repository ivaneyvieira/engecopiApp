<<<<<<< HEAD
select grade
FROM sqldados.stk
where prdno = LPAD(:codigo, 16, ' ')
      and grade <> ''
      and storeno in (1, 2, 3, 4, 5, 6, 7, 10)
=======
SELECT grade
FROM sqldados.stk
WHERE prdno = LPAD(:codigo, 16, ' ') AND grade <> '' AND storeno IN (1, 2, 3, 4, 5, 6, 7, 10)
>>>>>>> develop
GROUP BY grade
ORDER BY grade