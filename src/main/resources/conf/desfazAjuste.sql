DO @numero := :numero;
DO @SERIE := 66;
DO @PEDIDO := 0;
DO @DOC := CONCAT("Ajustes ", @numero);

DROP TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
    SELECT
      @LOJA := storeno as storeno,
      ordno,
      prdno,
      grade,
      qtty,
      cost,
      vendno,
      custno,
      1                as empno
    FROM sqldados.ajusteInventario
    WHERE numero = @numero
      AND (nfEntrada <> '' OR nfSaida <> '');

DROP TEMPORARY TABLE IF EXISTS TNotas;
CREATE TEMPORARY TABLE TNotas
    select distinct storeno, nfEntrada, nfSaida, @SERIE as serie
    from sqldados.ajusteInventario
    WHERE numero = @numero;

UPDATE sqldados.stk
  INNER JOIN T
  USING (storeno, prdno, grade)
SET longReserva1 = qtty_atacado;

UPDATE sqldados.stk
  INNER JOIN T
  USING (storeno, prdno, grade)
SET qtty_atacado = qtty_atacado - T.qtty,
  last_date      = current_date * 1,
  longReserva2   = 0;

UPDATE sqldados.inv AS I
  INNER JOIN TNotas AS N
    ON  I.nfname  = N.nfEntrada
        AND I.invse   = N.serie
        AND I.storeno = N.storeno
SET bits = bits | POW(2, 4);

UPDATE sqldados.nf
  INNER JOIN TNotas AS N
    ON nf.nfno = N.nfSaida
       AND nf.nfse = N.serie
       AND nf.storeno =  N.storeno
SET status = 1;

UPDATE sqldados.ajusteInventario
SET nfEntrada = '',
  nfSaida   = ''
WHERE numero = @numero
