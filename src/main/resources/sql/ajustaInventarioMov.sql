DO @TIPO := :tipo;
DO @XANO := :xano;
DO @QTTD := IF(@TIPO = 'E', 1, -1) * :qttd;
DO @DATA := CURRENT_DATE * 1;
DO @CUSTO := :custo;
DO @LOJA := :loja;
DO @PRDNO := LPAD(:prdno, 16, ' ');
DO @GRADE := :grade;

INSERT INTO sqldados.stkmov (xano, qtty, date, cm_fiscal, cm_real, storeno, bits, prdno, grade,
                             remarks)
SELECT @XANO                          AS xano,
       @QTTD                          AS qtty,
       @DATA                          AS date,
       @CUSTO                         AS cm_fiscal,
       @CUSTO                         AS cm_real,
       @LOJA                          AS storeno,
       1                              AS bits,
       @PRDNO                         AS prdno,
       @GRADE                         AS grade,
       CONCAT('AJUSTE', @TIPO, @XANO) AS remarks
FROM DUAL;

INSERT INTO sqldados.stkmovh (xano, qtty, date, nfno, cm_fiscal, cm_real, auxLong1, auxLong2,
                              auxLong3, auxLong4, auxLong5, auxMy1, auxMy2, auxMy3, auxMy4, auxMy5,
                              storeno, userno, tipo, bits, auxShort1, auxShort2, auxShort3,
                              auxShort4, auxShort5, prdno, grade, nfse, auxStr1, auxStr2, auxStr3,
                              auxStr4)
SELECT @XANO  AS xano,
       @QTTD  AS qtty,
       @DATA  AS date,
       0      AS nfno,
       @CUSTO AS cm_fiscal,
       @CUSTO AS cm_real,
       0      AS auxLong1,
       0      AS auxLong2,
       0      AS auxLong3,
       0      AS auxLong4,
       0      AS auxLong5,
       0      AS auxMy1,
       0      AS auxMy2,
       0      AS auxMy3,
       0      AS auxMy4,
       0      AS auxMy5,
       @LOJA  AS storeno,
       1      AS userno,
       0      AS tipo,
       0      AS bits,
       0      AS auxShort1,
       0      AS auxShort2,
       0      AS auxShort3,
       0      AS auxShort4,
       0      AS auxShort5,
       @PRDNO AS prdno,
       @GRADE AS grade,
       0      AS nfse,
       0      AS auxStr1,
       0      AS auxStr2,
       0      AS auxStr3,
       0      AS auxStr4
FROM DUAL;

UPDATE sqldados.stk
SET stk.qtty_atacado = (stk.qtty_atacado + @QTTD)
WHERE (stk.storeno = @LOJA)
  AND (stk.prdno = @PRDNO)
  AND (stk.grade = @GRADE)