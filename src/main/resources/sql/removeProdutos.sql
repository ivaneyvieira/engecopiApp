DELETE
FROM sqldados.stkmov
WHERE storeno = :loja
  AND prdno = LPAD(:prdno, 16, ' ')
  AND grade = :grade
  AND xano = :transacao;

DELETE
FROM sqldados.stkmovh
WHERE storeno = :loja
  AND prdno = LPAD(:prdno, 16, ' ')
  AND grade = :grade
  AND xano = :transacao;
