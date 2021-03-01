USE sqldados;

DO @DATA := :dataInicial;
DO @DTREF := :dataFinal;
DO @PRD := '';
DO @LJ:=0;
DO @REF := MID(@DTREF, 1, 6);

drop table if exists Lojas;
create temporary table Lojas
(PRIMARY KEY (storeno))
    SELECT no as storeno from sqldados.store
    WHERE no = @LJ OR @LJ = 0;

drop table if exists uym;
create temporary table uym
(PRIMARY KEY (ym))
    select MAX(ym) as ym
    FROM sqldados.stkchk
    where date < @DATA
      and ym < MID(@DATA, 1, 6);

DO @MES:=(SELECT ym FROM uym);

drop table if exists Saldo;
create temporary table Saldo
(  cost  bigint(15) )
    select storeno as loja, prdno, grade, @DATA as date, 'SaldoAnterior' tipo, cost, qtty as quant
    FROM sqldados.stkchk
      INNER JOIN Lojas
      USING(storeno)
    where date = DATE_SUB(@DATA, INTERVAL 1 DAY)*1
          AND (prdno = @PRD OR @PRD='')
          AND ym = @MES;

drop table if exists PreSaida;
create temporary table PreSaida
(  cost  bigint(15) )
    select P.storeno, P.pdvno, P.xano, P.storeno as loja, prdno, grade, date,
                                       CONCAT(P.nfno, '/', P.nfse) as doc,
                                       (-(price)*100*qtty) as cost, 'NF Saida' AS tipo, (-qtty*1000) as quant
    from sqldados.xaprd AS P
      INNER JOIN Lojas
      USING(storeno)
      INNER join sqldados.nf AS N
        ON  P.storeno = N.storeno
            AND P.pdvno   = N.pdvno
            AND P.xano    = N.xano
    WHERE N.status <> 1
          AND date BETWEEN @DATA AND @DTREF
          AND N.cfo NOT IN (5922, 6922)
          AND (prdno = @PRD OR @PRD='');

DROP TABLE IF EXISTS TXA;
CREATE TEMPORARY TABLE TXA
(PRIMARY KEY(storeno, pdvno, xano))
    select storeno, pdvno, xano
    from PreSaida
    GROUP BY storeno, pdvno, xano;

drop table if exists NFSaida;
create temporary table NFSaida
(  cost  bigint(15) )
    select loja, prdno, grade, date,
      SUM(cost) as cost, 'NF Saida' AS tipo, SUM(quant) as quant
    FROM PreSaida
    GROUP BY loja, prdno, grade, date;

drop table if exists NFCupom;
create temporary table NFCupom
( cost  bigint(15) )
    select X.storeno as loja,
      X.prdno, X.grade, X.date, 'NF Cupom' as tipo,
      SUM(-X.price*100*X.qtty)/1000 as cost,
      SUM(-X.qtty) as quant, X.storeno, X.pdvno, X.xano
    from sqldados.xalog2 AS X
      LEFT JOIN TXA USING(storeno, pdvno, xano)
      inner join sqlpdv.pxa AS P
      USING(storeno, pdvno, xano)
      inner join Lojas AS L
        ON L.storeno = X.storeno
    where icm_aliq & 4 = 0
          AND X.xatype <> 11
          AND X.qtty > 0
          AND X.date BETWEEN @DATA AND @DTREF
          AND P.nfse in ('IF', '10')
          AND TXA.xano is null
          AND (prdno = @PRD OR @PRD='')
    GROUP BY X.storeno, prdno, grade, date;

drop table if exists Devolucao;
create temporary table Devolucao
( cost  bigint(15) )
    select X.storeno as loja,
      X.prdno, X.grade, X.date, 'Devolucao' as tipo,
      SUM(-X.price*100*X.qtty)/1000 as cost,
      SUM(-X.qtty) as quant, X.storeno, X.pdvno, X.xano
    from xalog2 AS X
      LEFT JOIN TXA USING(storeno, pdvno, xano)
      inner join Lojas AS L
        ON L.storeno = X.storeno
    where icm_aliq & 4 = 0
          AND X.xatype = 11
          AND (X.doc LIKE 'DEVOL%' OR X.qtty > 0)
          AND TXA.xano is null
          AND X.date BETWEEN @DATA AND @DTREF
          AND (prdno = @PRD OR @PRD='')
    GROUP BY X.storeno, prdno, grade, date;

drop table if exists MovManual;
create temporary table MovManual
(  cost  bigint(15) )
    select storeno as loja, prdno, grade, date,
           'Mov Manual' AS tipo,
           SUM(/*cm_real*qtty/1000*/0) as cost, SUM(qtty) as quant
    from sqldados.stkmov
      INNER JOIN Lojas
      USING(storeno)
    WHERE qtty <> 0
          AND MID(remarks, 36, 1) <> '1'
          AND date BETWEEN @DATA AND @DTREF
          AND (prdno = @PRD OR @PRD='')
    GROUP BY loja, prdno, grade, date;

drop temporary table if exists NFFutura;
create temporary table NFFutura
(primary key(storeno, nfNfno, nfNfse))
    select storeno, nfno as nfNfno, nfse as nfNfse
    from sqldados.nf
    WHERE cfo IN (5922, 6922)
    GROUP BY storeno, nfNfno, nfNfse;


drop table if exists NFEntrada;
create temporary table NFEntrada
(  cost  bigint(15) )
    select I.storeno as loja, P.prdno, P.grade, I.date AS date, 'NF Entrada' as tipo,
           IF(type IN (0, 1, 2, 3),
              SUM( IF(cost4 = 0,
                      IF(cost > fob*10, 0, cost*100),
                      IF(cost4 > fob4*10, 0, cost4)
                   )*qtty/1000)
           , 0) as cost,
           SUM(qtty) as quant
    from sqldados.inv AS I
      LEFT JOIN NFFutura AS F
      USING(storeno, nfNfno, nfNfse)
      INNER JOIN Lojas
      USING(storeno)
      inner join sqldados.iprd AS P
        ON I.invno = P.invno
    WHERE I.bits & POW(2, 4) = 0
          AND I.auxShort13 & pow(2, 15) = 0
          AND I.date BETWEEN @DATA AND @DTREF
          AND (P.prdno = @PRD OR @PRD='')
          AND F.storeno IS NULL
    GROUP BY loja, P.prdno, P.grade, I.date;

drop table if exists Kardec;
create temporary table Kardec
(
  quant  int(10),
  cost   bigint(15)
)
    SELECT loja as storeno, prdno, grade, date, tipo, cost, quant FROM Saldo
    UNION ALL
    SELECT loja as storeno, prdno, grade, date, tipo, cost, quant FROM NFCupom
    UNION ALL
    SELECT loja as storeno, prdno, grade, date, tipo, cost, quant FROM Devolucao
    UNION ALL
    SELECT loja as storeno, prdno, grade, date, tipo, cost, quant FROM NFSaida
    UNION ALL
    SELECT loja as storeno, prdno, grade, date, tipo, cost, quant FROM MovManual
    UNION ALL
    SELECT loja as storeno, prdno, grade, date, tipo, cost, quant FROM NFEntrada;

drop table if exists SaldoData;
create table SaldoData
(saldoAcumulado  int(10),
 quantCustoAcu   int(10),
 custoAcumulado  bigint(20),
 qttyCusto  int(10),
 qtty  int(10),
 custo   bigint(20),
  UNIQUE  KEY (prdno, grade, storeno, date),
  PRIMARY KEY (date, prdno, grade, storeno))
    SELECT storeno, prdno, grade, date, SUM(IF(cost > 0, cost, 0)) AS custo,
                                        SUM(IF(cost > 0, quant,0)) AS qttyCusto,
                                        SUM(quant) as qtty, 0 as saldoAcumulado, 0 AS custoAcumulado, 0 AS quantCustoAcu
    FROM Kardec
    GROUP BY date, storeno, prdno, grade;

DROP TABLE IF EXISTS SaldoDataMes;
CREATE TABLE SaldoDataMes
(PRIMARY KEY(prdno, grade, storeno, ym))
    SELECT storeno, prdno, grade,  MID(date, 1, 6) as ym,  SUM(qtty) as qtty
    FROM SaldoData AS S
    WHERE date <= @DTREF
    GROUP BY prdno, grade, storeno, ym;

/**************************************************************************************
SEGUNDA PARTE
**************************************************************************************/

DROP TABLE IF EXISTS COMP_SALDO;
CREATE TEMPORARY TABLE COMP_SALDO
(PRIMARY KEY(storeno, prdno, grade))
    SELECT storeno, prdno, grade, SUM(qtty) as quant
    from sqldados.stkchk
      INNER JOIN Lojas USING(storeno)
    WHERE ym = @REF
          AND date = @DTREF
          AND (prdno = @PRD OR @PRD='')
    GROUP BY storeno, prdno, grade;

DROP TABLE IF EXISTS COMP_KARDEC;
CREATE TEMPORARY TABLE COMP_KARDEC
(PRIMARY KEY(storeno, prdno, grade))
    SELECT ym, storeno, prdno, grade, SUM(qtty) as quant
    from SaldoDataMes
      INNER JOIN Lojas USING(storeno)
    WHERE (prdno = @PRD OR @PRD='')
    GROUP BY storeno, prdno, grade;

DROP TABLE IF EXISTS COMP_MESTRE;
CREATE TEMPORARY TABLE COMP_MESTRE
(PRIMARY KEY(storeno, prdno, grade))
    SELECT storeno, prdno, grade FROM COMP_SALDO
    UNION
    SELECT storeno, prdno, grade FROM COMP_KARDEC;

DROP TABLE IF EXISTS COMP;
CREATE TEMPORARY TABLE COMP
(PRIMARY KEY(storeno, prdno, grade))
    SELECT storeno, prdno, grade,
      IFNULL(S.quant, 0) as quantS,
      IFNULL(K.quant, 0) as quantK
    FROM COMP_MESTRE
      LEFT JOIN COMP_SALDO   AS S USING(storeno, prdno, grade)
      LEFT JOIN COMP_KARDEC  AS K USING(storeno, prdno, grade);

DROP TABLE IF EXISTS saldoKardec;
CREATE TABLE saldoKardec
(
  codigo VARCHAR(6)
)
    SELECT
      LPAD(prdno * 1, 6, '0')              AS codigo,
      grade                                AS grade,
      storeno                              AS loja,
      DATE_FORMAT(@REF * 100 + 1, '%m/%Y') AS mes_ano,
      quantS / 1000                        AS saldoEstoque,
      quantK / 1000                        AS saldoKardec,
      (quantS - quantK) / 1000             AS diferecenca
    FROM COMP
    WHERE quantS <> quantK
    ORDER BY storeno, prdno, grade

