UPDATE sqldados.eord
SET status = 1
WHERE storeno = :loja
  AND ordno = :numPedido