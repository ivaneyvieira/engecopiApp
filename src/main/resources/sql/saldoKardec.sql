USE sqldados;

DO @DATA := :dataInicial;
DO @DTREF := :dataFinal;
DO @PRD := '';
DO @LJ := 0;
DO @REF := MID(@DTREF, 1, 6);

DROP TABLE IF EXISTS Lojas;
CREATE TEMPORARY TABLE Lojas (
  PRIMARY KEY (storeno)
)
SELECT no AS storeno
FROM sqldados.store
WHERE no = @LJ
   OR @LJ = 0;

DROP TABLE IF EXISTS uym;
CREATE TEMPORARY TABLE uym (
  PRIMARY KEY (ym)
)
SELECT MAX(ym) AS ym
FROM sqldados.stkchk
WHERE date < @DATA
  AND ym < MID(@DATA, 1, 6) * 1;

DO @MES := (SELECT ym
	    FROM uym);

DROP TABLE IF EXISTS Saldo;
CREATE TEMPORARY TABLE Saldo (
  cost BIGINT(15)
)
SELECT storeno         AS loja,
       prdno,
       grade,
       @DATA           AS date,
       'SaldoAnterior' AS tipo,
       cost,
       qtty            AS quant
FROM sqldados.stkchk
  INNER JOIN Lojas
	       USING (storeno)
WHERE (prdno = @PRD OR @PRD = '')
  AND ym = @MES;

DROP TABLE IF EXISTS PreSaida;
CREATE TEMPORARY TABLE PreSaida (
  cost BIGINT(15)
)
SELECT P.storeno,
       P.pdvno,
       P.xano,
       P.storeno                   AS loja,
       prdno,
       grade,
       date,
       CONCAT(P.nfno, '/', P.nfse) AS doc,
       (-(price) * 100 * qtty)     AS cost,
       'NF Saida'                  AS tipo,
       (-qtty * 1000)              AS quant
FROM sqldados.xaprd      AS P
  INNER JOIN Lojas
	       USING (storeno)
  INNER JOIN sqldados.nf AS N
	       ON P.storeno = N.storeno AND P.pdvno = N.pdvno AND P.xano = N.xano
WHERE N.status <> 1
  AND date BETWEEN @DATA AND @DTREF
  AND N.cfo NOT IN (5922, 6922)
  AND (prdno = @PRD OR @PRD = '');

DROP TABLE IF EXISTS TXA;
CREATE TEMPORARY TABLE TXA (
  PRIMARY KEY (storeno, pdvno, xano)
)
SELECT P.storeno,
       P.pdvno,
       P.xano
FROM sqldados.xaprd      AS P
  INNER JOIN Lojas
	       USING (storeno)
  INNER JOIN sqldados.nf AS N
	       ON P.storeno = N.storeno AND P.pdvno = N.pdvno AND P.xano = N.xano
WHERE date BETWEEN @DATA AND @DTREF
  AND (prdno = @PRD OR @PRD = '')
GROUP BY storeno, pdvno, xano;

DROP TABLE IF EXISTS NFSaida;
CREATE TEMPORARY TABLE NFSaida (
  cost BIGINT(15)
)
SELECT loja,
       prdno,
       grade,
       date,
       SUM(cost)  AS cost,
       'NF Saida' AS tipo,
       SUM(quant) AS quant
FROM PreSaida
GROUP BY loja, prdno, grade, date;

DROP TABLE IF EXISTS NFCupom;
CREATE TEMPORARY TABLE NFCupom (
  cost BIGINT(15)
)
SELECT X.storeno                           AS loja,
       X.prdno,
       X.grade,
       X.date,
       'NF Cupom'                          AS tipo,
       SUM(-X.price * 100 * X.qtty) / 1000 AS cost,
       SUM(-X.qtty)                        AS quant,
       X.storeno,
       X.pdvno,
       X.xano
FROM xalog2             AS X
  LEFT JOIN  TXA
	       USING (storeno, pdvno, xano)
  INNER JOIN sqlpdv.pxa AS P
	       USING (storeno, pdvno, xano)
  INNER JOIN Lojas      AS L
	       ON L.storeno = X.storeno
WHERE icm_aliq & 4 = 0
  AND X.xatype <> 11
  AND X.qtty > 0
  AND X.date BETWEEN @DATA AND @DTREF
  AND P.nfse IN ('IF', '10')
  AND TXA.xano IS NULL
  AND (prdno = @PRD OR @PRD = '')
GROUP BY X.storeno, prdno, grade, date;

DROP TABLE IF EXISTS Devolucao;
CREATE TEMPORARY TABLE Devolucao (
  cost BIGINT(15)
)
SELECT X.storeno                           AS loja,
       X.prdno,
       X.grade,
       X.date,
       'Devolucao'                         AS tipo,
       SUM(-X.price * 100 * X.qtty) / 1000 AS cost,
       SUM(-X.qtty)                        AS quant,
       X.storeno,
       X.pdvno,
       X.xano
FROM xalog2        AS X
  LEFT JOIN  TXA
	       USING (storeno, pdvno, xano)
  INNER JOIN Lojas AS L
	       ON L.storeno = X.storeno
WHERE icm_aliq & 4 = 0
  AND X.xatype = 11
  AND (X.doc LIKE 'DEVOL%' OR X.qtty > 0)
  AND TXA.xano IS NULL
  AND X.date BETWEEN @DATA AND @DTREF
  AND (prdno = @PRD OR @PRD = '')
GROUP BY X.storeno, prdno, grade, date;

DROP TABLE IF EXISTS MovManual;
CREATE TEMPORARY TABLE MovManual (
  cost BIGINT(15)
)
SELECT storeno                     AS loja,
       prdno,
       grade,
       date,
       'Mov Manual'                AS tipo,
       SUM(/*cm_real*qtty/1000*/0) AS cost,
       SUM(qtty)                   AS quant
FROM sqldados.stkmov
  INNER JOIN Lojas
	       USING (storeno)
WHERE qtty <> 0
  AND MID(remarks, 36, 1) <> '1'
  AND date BETWEEN @DATA AND @DTREF
  AND (prdno = @PRD OR @PRD = '')
GROUP BY loja, prdno, grade, date;

DROP TEMPORARY TABLE IF EXISTS NFFutura;
CREATE TEMPORARY TABLE NFFutura (
  PRIMARY KEY (storeno, nfNfno, nfNfse)
)
SELECT storeno,
       nfno AS nfNfno,
       nfse AS nfNfse
FROM sqldados.nf
WHERE cfo IN (5922, 6922)
GROUP BY storeno, nfNfno, nfNfse;

DROP TABLE IF EXISTS NFEntrada;
CREATE TEMPORARY TABLE NFEntrada (
  cost BIGINT(15)
)
SELECT I.storeno              AS loja,
       P.prdno,
       P.grade,
       I.date                 AS date,
       'NF Entrada'           AS tipo,
       IF(type IN (0, 1, 2, 3), SUM(
	     IF(cost4 = 0, IF(cost > fob * 10, 0, cost * 100), IF(cost4 > fob4 * 10, 0, cost4)) *
	     qtty / 1000), 0) AS cost,
       SUM(qtty)              AS quant
FROM sqldados.inv          AS I
  LEFT JOIN  NFFutura      AS F
	       USING (storeno, nfNfno, nfNfse)
  INNER JOIN Lojas
	       USING (storeno)
  INNER JOIN sqldados.iprd AS P
	       ON I.invno = P.invno
WHERE I.bits & POW(2, 4) = 0
  AND I.auxShort13 & POW(2, 15) = 0
  AND I.date BETWEEN @DATA AND @DTREF
  AND (P.prdno = @PRD OR @PRD = '')
  AND F.storeno IS NULL
GROUP BY loja, P.prdno, P.grade, I.date;

DROP TABLE IF EXISTS Kardec;
CREATE TEMPORARY TABLE Kardec (
  quant INT(10),
  cost  BIGINT(15)
)
SELECT loja AS storeno,
       prdno,
       grade,
       date,
       tipo,
       cost,
       quant
FROM Saldo
UNION ALL
SELECT loja AS storeno,
       prdno,
       grade,
       date,
       tipo,
       cost,
       quant
FROM NFCupom
UNION ALL
SELECT loja AS storeno,
       prdno,
       grade,
       date,
       tipo,
       cost,
       quant
FROM Devolucao
UNION ALL
SELECT loja AS storeno,
       prdno,
       grade,
       date,
       tipo,
       cost,
       quant
FROM NFSaida
UNION ALL
SELECT loja AS storeno,
       prdno,
       grade,
       date,
       tipo,
       cost,
       quant
FROM MovManual
UNION ALL
SELECT loja AS storeno,
       prdno,
       grade,
       date,
       tipo,
       cost,
       quant
FROM NFEntrada;

DROP TABLE IF EXISTS SaldoData;
CREATE TABLE SaldoData (
  saldoAcumulado INT(10),
  quantCustoAcu  INT(10),
  custoAcumulado BIGINT(20),
  qttyCusto      INT(10),
  qtty           INT(10),
  custo          BIGINT(20),
  UNIQUE KEY (prdno, grade, storeno, date),
  PRIMARY KEY (date, prdno, grade, storeno)
)
SELECT storeno,
       prdno,
       grade,
       date,
       SUM(IF(cost > 0, cost, 0))  AS custo,
       SUM(IF(cost > 0, quant, 0)) AS qttyCusto,
       SUM(quant)                  AS qtty,
       0                           AS saldoAcumulado,
       0                           AS custoAcumulado,
       0                           AS quantCustoAcu
FROM Kardec
GROUP BY date, storeno, prdno, grade;

DROP TABLE IF EXISTS SaldoDataMes;
CREATE TABLE SaldoDataMes (
  PRIMARY KEY (prdno, grade, storeno, ym)
)
SELECT storeno,
       prdno,
       grade,
       MID(date, 1, 6) * 1 AS ym,
       SUM(qtty)           AS qtty
FROM SaldoData AS S
WHERE date <= @DTREF
GROUP BY prdno, grade, storeno, ym;

/**************************************************************************************
SEGUNDA PARTE
**************************************************************************************/

DROP TABLE IF EXISTS COMP_SALDO;
CREATE TEMPORARY TABLE COMP_SALDO (
  PRIMARY KEY (storeno, prdno, grade)
)
SELECT storeno,
       prdno,
       grade,
       SUM(qtty) AS quant
FROM sqldados.stkchk
  INNER JOIN Lojas
	       USING (storeno)
WHERE ym = @REF
  AND storeno IN (1, 2, 3, 4, 5, 6, 7, 10)
  AND (prdno = @PRD OR @PRD = '')
GROUP BY storeno, prdno, grade;


DROP TABLE IF EXISTS COMP_SALDO;
CREATE TEMPORARY TABLE COMP_SALDO (
  PRIMARY KEY (storeno, prdno, grade)
)
SELECT storeno,
       prdno,
       grade,
       SUM(qtty_varejo) AS quant
FROM sqldados.stk
  INNER JOIN Lojas
	       USING (storeno)
WHERE (prdno = @PRD OR @PRD = '')
GROUP BY storeno, prdno, grade;

DROP TABLE IF EXISTS COMP_KARDEC;
CREATE TEMPORARY TABLE COMP_KARDEC (
  PRIMARY KEY (storeno, prdno, grade)
)
SELECT ym,
       storeno,
       prdno,
       grade,
       SUM(qtty) AS quant
FROM SaldoDataMes
  INNER JOIN Lojas
	       USING (storeno)
WHERE (prdno = @PRD OR @PRD = '')
GROUP BY storeno, prdno, grade;

DROP TABLE IF EXISTS COMP_MESTRE;
CREATE TEMPORARY TABLE COMP_MESTRE (
  PRIMARY KEY (storeno, prdno, grade)
)
SELECT storeno,
       prdno,
       grade
FROM COMP_SALDO
UNION
SELECT storeno,
       prdno,
       grade
FROM COMP_KARDEC;

DROP TABLE IF EXISTS COMP;
CREATE TEMPORARY TABLE COMP (
  PRIMARY KEY (storeno, prdno, grade)
)
SELECT storeno,
       prdno,
       grade,
       IFNULL(S.quant, 0) AS quantS,
       IFNULL(K.quant, 0) AS quantK
FROM COMP_MESTRE
  LEFT JOIN COMP_SALDO  AS S
	      USING (storeno, prdno, grade)
  LEFT JOIN COMP_KARDEC AS K
	      USING (storeno, prdno, grade);

DROP TABLE IF EXISTS saldoKardec;
CREATE TABLE saldoKardec (
  codigo VARCHAR(6)
)
SELECT LPAD(prdno * 1, 6, '0')              AS codigo,
       grade                                AS grade,
       storeno                              AS loja,
       DATE_FORMAT(@REF * 100 + 1, '%m/%Y') AS mes_ano,
       quantS / 1000                        AS saldoEstoque,
       quantK / 1000                        AS saldoKardec,
       (quantS - quantK) / 1000             AS diferecenca
FROM COMP
WHERE quantS <> quantK
ORDER BY storeno, prdno, grade;
