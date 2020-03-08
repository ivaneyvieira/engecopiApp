DO @DATA := :DATA;
DO @NOTA := "";

/*Filtrar a tabela coletor*/
DROP TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T (
  INDEX (userno),
  INDEX (barcode),
  storeno INT,
  nota    VARCHAR(10)
)
SELECT barcode * 1 AS userno, LPAD(barcode, 16, ' ') AS barcode, qtty, seq, date,
       usuario AS coletor, id, RIGHT(usuario, 2) * 1 AS storeno,
       IF(usuario = 'NOTA', @NOTA := barcode, @NOTA) AS nota
FROM sqldados.coletor
WHERE date >= @DATA
ORDER BY id;

/*USUARIO*/
DROP TABLE IF EXISTS T2;
CREATE TEMPORARY TABLE T2
SELECT E.no AS empno, E.name AS sname, seq, date, coletor, T.storeno
FROM T
  INNER JOIN sqldados.users AS E
               ON E.no = T.userno
WHERE coletor <> 'NOTA'
GROUP BY seq, date, coletor;

DROP TABLE IF EXISTS TId;
CREATE TEMPORARY TABLE TId (
  PRIMARY KEY (nota, barcode, id)
)
SELECT nota, barcode, MAX(id) AS id
FROM T
GROUP BY nota, barcode;

DROP TABLE IF EXISTS T3;
/*Totalizador de quantidade por barcode*/
CREATE TEMPORARY TABLE T3 (
  PRIMARY KEY (nota, barcode),
  qtty INT
)
SELECT nota, barcode, MAX(qtty) * 1000 AS qtty
FROM T
  INNER JOIN TId
               USING (nota, barcode, id)
GROUP BY nota, barcode;

DROP TABLE IF EXISTS T4;
CREATE TEMPORARY TABLE T4
SELECT seq, coletor, barcode, empno, sname AS operador, T3.qtty AS inventario, T.storeno, nota
FROM T
  INNER JOIN T2
               USING (seq, date, coletor)
  INNER JOIN T3
               USING (nota, barcode);

DROP TABLE IF EXISTS T5;
CREATE TEMPORARY TABLE T5 (
  PRIMARY KEY (nota, storeno, prdno, grade),
  nota VARCHAR(10)
)
SELECT T4.*, P.prdno, P.grade, name AS descricao, S.qtty_varejo AS estoque,
       inventario - S.qtty_varejo AS diferenca
FROM T4
  INNER JOIN sqlpdv.prdstk AS P
               ON P.barcode = T4.barcode AND P.storeno = T4.storeno
  INNER JOIN sqldados.stk  AS S
               ON P.storeno = S.storeno AND P.prdno = S.prdno AND P.grade = S.grade
GROUP BY nota, T4.storeno, P.barcode;

DROP TABLE IF EXISTS T6;
CREATE TEMPORARY TABLE T6 (
  PRIMARY KEY (storeno)
)
SELECT S.no AS storeno, V.no AS vendno, C.no AS custno
FROM sqldados.store         AS S
  INNER JOIN sqldados.vend  AS V
               ON S.cgc = V.cgc
  INNER JOIN sqldados.custp AS C
               ON C.cpf_cgc = S.cgc
GROUP BY S.no;

DROP TABLE IF EXISTS T7;
/*NOTA*/
CREATE TEMPORARY TABLE T7 (
  PRIMARY KEY (nota),
  nota VARCHAR(10)
)
SELECT nota, MIN(date) AS date
FROM T
WHERE coletor = 'NOTA'
GROUP BY nota;

DELETE
FROM sqldados.ajusteInventario
WHERE date >= @DATA AND (nfSaida = '' AND nfEntrada = '');

INSERT IGNORE INTO sqldados.ajusteInventario (vendno, ordno, qtty, cost, sp, storeno, prdno, grade,
                                              custno, numero, date, operador, barcode, inventario,
                                              saldo)
SELECT vendno, 0 AS ordno, diferenca AS qtty, ROUND(P.cost / 100) AS cost, P.sp, T5.storeno, prdno,
       grade, custno, T7.nota AS numero, T7.date, operador, TRIM(T5.barcode) AS barcode, inventario,
       estoque AS saldo
FROM T5
  INNER JOIN T6
               USING (storeno)
  INNER JOIN sqldados.prd AS P
               ON P.no = T5.prdno
  INNER JOIN T7
               USING (nota);