SELECT YEAR(CURRENT_DATE) * 10000 + 0101 AS dataInicial, MAX(date) AS dataFinal
FROM (SELECT date AS date
      FROM sqldados.stkchk
      WHERE ym = MID(CURRENT_DATE * 1, 1, 6) * 1
	AND storeno IN (1, 2, 3, 4, 5, 6, 7, 10)
      LIMIT 1000) AS D