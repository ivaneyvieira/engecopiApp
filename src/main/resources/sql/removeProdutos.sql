DO @saldo := IF(:ajustaSaldo = 'S',
                IFNULL((SELECT qtty
                        FROM sqldados.stkmov
                        WHERE storeno = :loja
                          AND prdno = LPAD(:prdno, 16, ' ')
                          AND grade = :grade
                          AND xano = :transacao), 0),
                0);

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

UPDATE sqldados.stk
SET qtty_varejo = qtty_varejo - @saldo
WHERE storeno = :loja
  AND prdno = LPAD(:prdno, 16, ' ')
  AND grade = :grade
