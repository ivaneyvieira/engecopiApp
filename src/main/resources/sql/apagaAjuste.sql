UPDATE sqldados.ajusteInventario
SET nfEntrada = 'DELETE',
    nfSaida   = 'DELETE'
WHERE numero = :numero AND prdno = :prdno AND grade = :grade