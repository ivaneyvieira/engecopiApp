UPDATE sqldados.ajusteInventario
SET inventario = :quant * 1000,
    qtty       = :quant * 1000 - ajusteInventario.saldo
WHERE numero = :numero
  AND prdno = :prdno
  AND grade = :grade