SELECT YEAR(current_date) * 10000 + 0101 AS dataInicial, MAX(date) AS dataFinal
<<<<<<< HEAD
FROM (SELECT date, count(*) AS quant
      FROM sqldados.stkchk
      WHERE ym = MID(current_date * 1, 1, 6) * 1 AND storeno IN (1, 2, 3, 4, 5, 6, 7, 10)
      GROUP BY date
      HAVING quant > 10000) AS D
=======
FROM (SELECT date AS date
      FROM sqldados.stkchk
      WHERE ym = MID(current_date * 1, 1, 6) * 1 AND storeno IN (1, 2, 3, 4, 5, 6, 7, 10)
      LIMIT 1000) AS D
>>>>>>> develop
