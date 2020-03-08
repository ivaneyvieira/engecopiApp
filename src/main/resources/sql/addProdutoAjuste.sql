DO @LOJA := :loja;
DO @CODIGO := LPAD(:codigo, 16, ' ');
DO @GRADE := :grade;
DO @NOTA := :nota;
DO @QTTY := :qtty;
DO @DATE := :data;

DROP TEMPORARY TABLE IF EXISTS TLOJA;
CREATE TEMPORARY TABLE TLOJA (
  PRIMARY KEY (storeno)
)
SELECT S.no AS storeno, V.no AS vendno, C.no AS custno
FROM sqldados.store         AS S
  INNER JOIN sqldados.vend  AS V
               ON S.cgc = V.cgc
  INNER JOIN sqldados.custp AS C
               ON C.cpf_cgc = S.cgc
WHERE S.no = @LOJA
GROUP BY S.no;

DELETE
FROM ajusteInventario
WHERE numero = @NOTA AND prdno = @CODIGO AND grade = @GRADE;

INSERT IGNORE INTO ajusteInventario (vendno, ordno, qtty, cost, sp, storeno, prdno, grade, custno,
                                     numero, date, nfEntrada, nfSaida, operador, barcode,
                                     inventario, saldo)
SELECT vendno, @NOTA AS ordno, @QTTY * 1000 - IFNULL(S.qtty_atacado + S.qtty_varejo, 0) AS qtty,
       IFNULL(cm_real, P.cost) AS cost, sp, @LOJA AS storeno, @CODIGO AS prdno, @GRADE AS grade,
       custno, @NOTA AS numero, @DATE AS date, '' AS nfEntrada, '' AS nfSaida, 'APP' AS operador,
       IFNULL(TRIM(B.barcode), '') AS barcode, @QTTY * 1000 AS inventario,
       IFNULL(S.qtty_atacado + S.qtty_varejo, 0) AS saldo
FROM sqldados.prd          AS P
  LEFT JOIN  sqldados.stk  AS S
               ON S.prdno = P.no AND S.storeno = @loja AND S.grade = @GRADE
  LEFT JOIN  sqlpdv.prdstk AS B
               ON B.prdno = S.prdno AND B.grade = S.grade AND B.storeno = S.storeno
  INNER JOIN TLOJA         AS L
               ON L.storeno = S.storeno
WHERE no = @CODIGO;