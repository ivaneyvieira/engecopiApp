select YEAR(current_date)*10000 + 0101 as dataInicial,
       MAX(date) as dataFinal
FROM(
    select date, count(*) as quant
    FROM sqldados.stkchk
    where ym = MID(current_date*1, 1, 6)*1
        and storeno in (1, 2, 3, 4, 5, 6, 7, 10)
    GROUP BY date
    HAVING quant > 10000
    ) AS D