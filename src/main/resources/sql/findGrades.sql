select grade
FROM sqldados.stk
where prdno = LPAD(:codigo, 16, ' ')
      and grade <> ''
      and storeno in (1, 2, 3, 4, 5, 6, 7, 10)
GROUP BY grade
order by grade