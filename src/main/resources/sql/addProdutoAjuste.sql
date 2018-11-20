DO @LOJA := :loja;
DO @CODIGO := LPAD(:codigo, 16, ' ');
DO @GRADE := :grade;
DO @NOTA := :nota;
DO @QTTY := :qtty;
DO @DATE := :data;

DROP TEMPORARY TABLE IF EXISTS TLOJA;
CREATE TEMPORARY TABLE TLOJA
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
    WHERE S.no = @LOJA
    GROUP BY S.no;

DELETE FROM ajusteInventario
WHERE numero = @NOTA AND prdno = @CODIGO AND grade = @GRADE;

insert ignore into ajusteInventario (vendno, ordno, qtty, cost, sp, storeno, prdno,
                                     grade, custno, numero, date, nfEntrada, nfSaida, operador, barcode,
                                     inventario, saldo)
  select
    vendno,
    @NOTA                                                    AS ordno,
    @QTTY * 1000 - IFNULL(S.qtty_atacado + S.qtty_varejo, 0) AS qtty,
    IFNULL(cm_real, P.cost)                                  as cost,
    sp,
    @LOJA                                                    AS storeno,
    @CODIGO                                                  AS prdno,
    @GRADE                                                   AS grade,
    custno,
    @NOTA                                                    AS numero,
    @DATE                                                    AS date,
    ''                                                       as nfEntrada,
    ''                                                       as nfSaida,
    'APP'                                                    as operador,
    IFNULL(TRIM(B.barcode), '')                              AS barcode,
    @QTTY * 1000                                             AS inventario,
    IFNULL(S.qtty_atacado + S.qtty_varejo, 0)                as saldo
  from sqldados.prd AS P
    left join sqldados.stk AS S
      on S.prdno = P.no
         and S.storeno = @loja
         and S.grade = @GRADE
    left join sqlpdv.prdstk AS B
      on B.prdno = S.prdno
         AND B.grade = S.grade
         AND B.storeno = S.storeno
    inner join TLOJA AS L
      on L.storeno = S.storeno
  where no = @CODIGO;