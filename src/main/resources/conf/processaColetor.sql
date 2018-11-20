DO @DATA := :DATA;
DO @NOTA := "";

/*Filtrar a tabela coletor*/
DROP TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
(
  INDEX (userno),
  INDEX (barcode),
  storeno int,
  nota    varchar(10)
)
    select
      barcode * 1                                   as userno,
      LPAD(barcode, 16, ' ')                        as barcode,
      qtty,
      seq,
      date,
      usuario                                       as coletor,
      id,
      RIGHT(usuario, 2) * 1                         as storeno,
      IF(usuario = "NOTA", @NOTA := barcode, @NOTA) as nota
    from sqldados.coletor
    where date >= @DATA
    ORDER BY id;

/*USUARIO*/
DROP TABLE IF EXISTS T2;
CREATE TEMPORARY TABLE T2
    select
      E.no   as empno,
      E.name as sname,
      seq,
      date,
      coletor,
      T.storeno
    from T
      inner join sqldados.users AS E
        on E.no = T.userno
    WHERE coletor <> 'NOTA'
    group by seq, date, coletor;

DROP TABLE IF EXISTS TId;
CREATE TEMPORARY TABLE TId
(
  PRIMARY KEY (nota, barcode, id)
)
    select
      nota,
      barcode,
      MAX(id) as id
    from T
    group by nota, barcode;

DROP TABLE IF EXISTS T3;
/*Totalizador de quantidade por barcode*/
CREATE TEMPORARY TABLE T3
(
  PRIMARY KEY (nota, barcode),
  qtty int
)
    select
      nota,
      barcode,
      MAX(qtty) * 1000 as qtty
    from T
      INNER JOIN TId
      USING (nota, barcode, id)
    group by nota, barcode;

DROP TABLE IF EXISTS T4;
CREATE TEMPORARY TABLE T4
    SELECT
      seq,
      coletor,
      barcode,
      empno,
      sname   as operador,
      T3.qtty as inventario,
      T.storeno,
      nota
    FROM T
      INNER JOIN T2
      USING (seq, date, coletor)
      INNER JOIN T3
      USING (nota, barcode);

DROP TABLE IF EXISTS T5;
CREATE TEMPORARY TABLE T5
(
  PRIMARY KEY (nota, storeno, prdno, grade),
  nota varchar(10)
)
    select
      T4.*,
      P.prdno,
      P.grade,
      name                       as descricao,
      S.qtty_varejo              as estoque,
      inventario - S.qtty_varejo as diferenca
    from T4
      inner join sqlpdv.prdstk AS P
        ON P.barcode = T4.barcode
           AND P.storeno = T4.storeno
      inner join sqldados.stk AS S
        ON P.storeno = S.storeno
           AND P.prdno = S.prdno
           AND P.grade = S.grade
    GROUP BY nota, T4.storeno, P.barcode;

DROP TABLE IF EXISTS T6;
CREATE TEMPORARY TABLE T6
(
  PRIMARY KEY (storeno)
)
    select
      S.no as storeno,
      V.no as vendno,
      C.no as custno
    from sqldados.store AS S
      inner join sqldados.vend AS V
        ON S.cgc = V.cgc
      inner join sqldados.custp AS C
        ON C.cpf_cgc = S.cgc
    GROUP BY S.no;

DROP TABLE IF EXISTS T7;
/*NOTA*/
CREATE TEMPORARY TABLE T7
(
  PRIMARY KEY (nota),
  nota varchar(10)
)
    select
      nota,
      MIN(date) as date
    from T
    where coletor = 'NOTA'
    GROUP BY nota;

DELETE FROM ajusteInventario
WHERE date >= @DATA AND (nfSaida = "" AND nfEntrada = "");

INSERT IGNORE INTO ajusteInventario (vendno, ordno, qtty, cost,
                                     sp, storeno, prdno, grade, custno, numero, date, operador, barcode,
                                     inventario, saldo)
  select
    vendno,
    0                   as ordno,
    diferenca           as qtty,
    ROUND(P.cost / 100) as cost,
    P.sp,
    T5.storeno,
    prdno,
    grade,
    custno,
    T7.nota             as numero,
    T7.date,
    operador,
    TRIM(T5.barcode)    as barcode,
    inventario,
    estoque             as saldo
  from T5
    inner join T6
    USING (storeno)
    inner join sqldados.prd AS P
      ON P.no = T5.prdno
    inner join T7
    using (nota);