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

UPDATE sqldados.stk
SET stk.qtty_atacado = (stk.qtty_atacado + @QTTD)
WHERE (stk.storeno = @LOJA)
  AND (stk.prdno = @PRDNO)
  AND (stk.grade = @GRADE)