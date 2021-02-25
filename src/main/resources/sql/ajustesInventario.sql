SELECT vendno,
       ordno,
       qtty / 1000        AS qtty,
       A.cost / 10000     AS cost,
       A.sp / 100         AS sp,
       storeno,
       prdno,
       grade,
       custno,
       numero,
       date,
       nfEntrada,
       nfSaida,
       MID(P.name, 1, 37) AS descricao,
       A.barcode,
       operador,
       inventario / 1000  AS inventario,
       saldo / 1000       AS saldo
FROM sqldados.ajusteInventario AS A
  INNER JOIN sqldados.prd      AS P
	       ON prdno = P.no
WHERE numero = :numero
  AND (nfEntrada <> 'DELETE' AND nfSaida <> 'DELETE')
