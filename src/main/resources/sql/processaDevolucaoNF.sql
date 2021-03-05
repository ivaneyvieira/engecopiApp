DO @LOJA := :storeno;
DO @NFNO := :nfno;
DO @NFSE := :nfse;
DO @SERIE := 66;
DO @TIPO := 'E';
DO @FATOR := IF(@TIPO = 'E', 1, -1);
DO @DOC := IF(@TIPO = 'E', 'AJUS ENT', 'AJUS SAI');
DO @TIPO_NOTA := 7;
DO @OBS := CASE @TIPO_NOTA
	     WHEN 9
	       THEN '66'
	     WHEN 7
	       THEN 'GARANTIA'
	     ELSE ''
	   END;

DROP TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
SELECT X.storeno,
       X.nfno                                                         AS ordno,
       X.prdno,
       X.grade,
       ROUND(qtty * 1000)                                             AS qtty,
       ROUND(IF(I.last_cost = 0, I.cm_varejo_otn, I.last_cost)) / 100 AS cost,
       V.no                                                           AS vendno,
       IF(V.state = 'PI', '5949', '6949')                             AS cfopS,
       IF(V.state = 'PI', '1949', '2949')                             AS cfopE,
       CAST(CONCAT(F.no, ' ', MID(F.sname, 1, 4)) AS CHAR)            AS frabricante,
       C.no                                                           AS custno,
       N.empno
FROM sqldados.xaprd         AS X
  INNER JOIN sqldados.nf    AS N
	       USING (storeno, pdvno, xano)
  INNER JOIN sqldados.stk   AS I
	       ON I.storeno = X.storeno AND I.prdno = X.prdno AND I.grade = X.grade
  INNER JOIN sqldados.store AS S
	       ON S.no = X.storeno
  INNER JOIN sqldados.prd   AS P
	       ON P.no = X.prdno
  INNER JOIN sqldados.vend  AS F
	       ON F.no = P.mfno
  INNER JOIN sqldados.vend  AS V
	       ON V.cgc = S.cgc
  INNER JOIN sqldados.custp AS C
	       ON C.cpf_cgc = S.cgc
WHERE X.storeno = @LOJA
  AND X.nfno = @NFNO
  AND X.nfse = @NFSE;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET longReserva1 = qtty_atacado
WHERE longReserva2 <> T.ordno;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET qtty_atacado = qtty_atacado + @FATOR * T.qtty,
    last_date    = CURRENT_DATE * 1
WHERE longReserva2 <> T.ordno;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET longReserva2 = T.ordno;

DO @INVDEL := (SELECT MAX(invno)
	       FROM sqldados.inv
	       WHERE ordno = @NFNO
		 AND storeno = @LOJA
		 AND invse = @SERIE
		 AND c1 = @DOC);

DO @INVNO := (SELECT MAX(invno) + 1
	      FROM sqldados.inv);
DO @NUMERO := IFNULL((SELECT MAX(no) + 1
		      FROM sqldados.lastno
		      WHERE se = @SERIE
			AND storeno = @LOJA), 1);

INSERT INTO sqldados.inv (invno, vendno, ordno, xfrno, issue_date, date, comp_date, ipi, icm,
			  freight, netamt, grossamt, subst_trib, discount, prdamt, despesas,
			  base_ipi, aliq, cfo, nfNfno, auxLong1, auxLong2, auxMoney1, auxMoney2,
			  dataSaida, amtServicos, amtIRRF, amtINSS, amtISS, auxMoney3, auxMoney4,
			  auxMoney5, auxLong3, auxLong4, auxLong5, auxLong6, auxLong7, auxLong8,
			  auxLong9, auxLong10, auxLong11, auxLong12, auxMoney6, auxMoney7,
			  auxMoney8, auxMoney9, auxMoney10, auxMoney11, auxMoney12, auxMoney13, l1,
			  l2, l3, l4, l5, l6, l7, l8, m1, m2, m3, m4, m5, m6, m7, m8, weight,
			  carrno, packages, storeno, indxno, book_bits, type, usernoFirst,
			  usernoLast, nfStoreno, bits, padbyte, auxShort1, auxShort2, auxShort3,
			  auxShort4, auxShort5, auxShort6, auxShort7, auxShort8, auxShort9,
			  auxShort10, auxShort11, auxShort12, auxShort13, auxShort14, bits2, bits3,
			  bits4, bits5, s1, s2, s3, s4, s5, s6, s7, s8, nfname, invse, account,
			  remarks, contaCredito, contaDebito, nfNfse, auxStr1, auxStr2, auxStr3,
			  auxStr4, auxStr5, auxStr6, c1, c2)
SELECT @INVNO                                             AS                        invno,
       vendno,
       ordno,
       0                                                  AS                        xfrno,
       CURRENT_DATE * 1                                   AS                        issue_date,
       CURRENT_DATE * 1                                   AS                        date,
       CURRENT_DATE * 1                                   AS                        comp_date,
       0                                                  AS                        ipi,
       0                                                  AS                        icm,
       0                                                  AS                        freight,
       0                                                  AS                        netamt,
       SUM(qtty * cost / 1000)                            AS                        grossamt,
       0                                                  AS                        subst_trib,
       0                                                  AS                        discount,
       SUM(qtty * cost / 1000)                            AS                        prdamt,
       0                                                  AS                        despesas,
       0                                                  AS                        base_ipi,
       0                                                  AS                        aliq,
       T.cfopE                                            AS                        cfo,
       0                                                  AS                        nfNfno,
       0 /*Valor desconhecido*/                           AS                        auxLong1,
       0                                                  AS                        auxLong2,
       0                                                  AS                        auxMoney1,
       0                                                  AS                        auxMoney2,
       0                                                  AS                        dataSaida,
       0                                                  AS                        amtServicos,
       0                                                  AS                        amtIRRF,
       0                                                  AS                        amtINSS,
       0                                                  AS                        amtISS,
       0                                                  AS                        auxMoney3,
       0                                                  AS                        auxMoney4,
       0                                                  AS                        auxMoney5,
       0                                                  AS                        auxLong3,
       0                                                  AS                        auxLong4,
       0                                                  AS                        auxLong5,
       0                                                  AS                        auxLong6,
       0                                                  AS                        auxLong7,
       CURRENT_DATE * 1                                   AS                        auxLong8,
       0                                                  AS                        auxLong9,
       0                                                  AS                        auxLong10,
       0                                                  AS                        auxLong11,
       0                                                  AS                        auxLong12,
       0                                                  AS                        auxMoney6,
       0                                                  AS                        auxMoney7,
       0                                                  AS                        auxMoney8,
       0                                                  AS                        auxMoney9,
       0                                                  AS                        auxMoney10,
       0                                                  AS                        auxMoney11,
       0                                                  AS                        auxMoney12,
       0                                                  AS                        auxMoney13,
       0                                                  AS                        l1,
       0                                                  AS                        l2,
       0                                                  AS                        l3,
       0                                                  AS                        l4,
       0                                                  AS                        l5,
       CURRENT_DATE * 1                                   AS                        l6,
       0                                                  AS                        l7,
       0                                                  AS                        l8,
       0                                                  AS                        m1,
       0                                                  AS                        m2,
       0                                                  AS                        m3,
       0                                                  AS                        m4,
       0                                                  AS                        m5,
       0                                                  AS                        m6,
       0                                                  AS                        m7,
       0                                                  AS                        m8,
       0                                                  AS                        weight,
       0                                                  AS                        carrno,
       0                                                  AS                        packages,
       storeno,
       0                                                  AS                        indxno,
       9                                                  AS                        book_bits,
       8                                                  AS                        type,
       1                                                  AS                        usernoFirst,
       1                                                  AS                        usernoLast,
       0                                                  AS                        nfStoreno,
       3                                                  AS                        bits,
       0                                                  AS                        padbyte,
       storeno                                            AS                        auxShort1,
       0                                                  AS                        auxShort2,
       0                                                  AS                        auxShort3,
       0                                                  AS                        auxShort4,
       0                                                  AS                        auxShort5,
       0                                                  AS                        auxShort6,
       0                                                  AS /*valor desconhecido*/ auxShort7,
       0                                                  AS /*valor desconhecido*/ auxShort8,
       0                                                  AS                        auxShort9,
       0                                                  AS                        auxShort10,
       0                                                  AS                        auxShort11,
       0                                                  AS                        auxShort12,
       0                                                  AS                        auxShort13,
       0                                                  AS                        auxShort14,
       0                                                  AS                        bits2,
       0                                                  AS                        bits3,
       0                                                  AS                        bits4,
       0                                                  AS                        bits5,
       0                                                  AS                        s1,
       0                                                  AS                        s2,
       0                                                  AS                        s3,
       0                                                  AS                        s4,
       0                                                  AS                        s5,
       0                                                  AS                        s6,
       0                                                  AS                        s7,
       0                                                  AS                        s8,
       @NUMERO                                            AS                        nfname,
       @SERIE                                             AS                        invse,
       2                                                  AS                        account,
       CAST(CONCAT(@OBS, ' ', GROUP_CONCAT(DISTINCT frabricante ORDER BY frabricante SEPARATOR
					   ' ')) AS CHAR) AS                        remarks,
       ''                                                 AS                        contaCredito,
       ''                                                 AS                        contaDebito,
       ''                                                 AS                        nfNfse,
       ''                                                 AS                        auxStr1,
       ''                                                 AS                        auxStr2,
       ''                                                 AS                        auxStr3,
       ''                                                 AS                        auxStr4,
       ''                                                 AS                        auxStr5,
       ''                                                 AS                        auxStr6,
       @DOC                                               AS                        c1,
       ''                                                 AS                        c2
FROM T
WHERE @TIPO = 'E'
GROUP BY storeno, ordno;

INSERT INTO sqldados.iprd (invno, qtty, fob, cost, date, ipi, auxLong1, auxLong2, frete, seguro,
			   despesas, freteIpi, qttyRessar, baseIcmsSubst, icmsSubst, icms, discount,
			   fob4, cost4, icmsAliq,
			   cfop, auxLong3, auxLong4, auxLong5, auxMy1, auxMy2, auxMy3, baseIcms,
			   baseIpi, ipiAmt,
			   reducaoBaseIcms, lucroTributado, l1, l2, l3, l4, l5, l6, l7, l8, m1, m2,
			   m3, m4, m5, m6, m7, m8,
			   storeno, bits, auxShort1, auxShort2, taxtype, auxShort3, auxShort4,
			   auxShort5, seqno, bits2, bits3,
			   bits4, s1, s2, s3, s4, s5, s6, s7, s8, prdno, grade, auxChar, auxChar2,
			   cstIcms, cstIpi, c1)
SELECT @INVNO           AS invno,
       qtty,
       cost             AS fob,
       cost,
       CURRENT_DATE * 1 AS date,
       0                AS ipi,
       0                AS auxLong1,
       0                AS auxLong2,
       0                AS frete,
       0                AS seguro,
       0                AS despesas,
       0                AS freteIpi,
       0                AS qttyRessar,
       0                AS baseIcmsSubst,
       0                AS icmsSubst,
       0                AS icms,
       0                AS discount,
       cost * 100       AS fob4,
       cost * 100       AS cost4,
       0                AS icmsAliq,
       T.cfopE          AS cfop,
       0                AS auxLong3,
       0                AS auxLong4,
       0                AS auxLong5,
       0                AS auxMy1,
       0                AS auxMy2,
       0                AS auxMy3,
       0                AS baseIcms,
       0                AS baseIpi,
       0                AS ipiAmt,
       0                AS reducaoBaseIcms,
       0                AS lucroTributado,
       0                AS l1,
       0                AS l2,
       0                AS l3,
       0                AS l4,
       0                AS l5,
       0                AS l6,
       0                AS l7,
       0                AS l8,
       0                AS m1,
       0                AS m2,
       0                AS m3,
       0                AS m4,
       0                AS m5,
       0                AS m6,
       0                AS m7,
       0                AS m8,
       storeno,
       32               AS bits,
       0                AS auxShort1,
       0                AS auxShort2,
       0                AS taxtype,
       0                AS auxShort3,
       0                AS auxShort4,
       0                AS auxShort5,
       0                AS seqno,
       0                AS bits2,
       0                AS bits3,
       0                AS bits4,
       0                AS s1,
       0                AS s2,
       0                AS s3,
       0                AS s4,
       0                AS s5,
       0                AS s6,
       0                AS s7,
       0                AS s8,
       prdno,
       grade,
       ''               AS auxChar,
       ''               AS auxChar2,
       '000'            AS cstIcms,
       ''               AS cstIpi,
       ''               AS c1
FROM T
WHERE @TIPO = 'E';

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET last_doc = CONCAT(@NUMERO, '/', '66')
WHERE @TIPO = 'E';

UPDATE sqldados.nf AS N
SET s16 = 4 /*Expirado*/
WHERE N.storeno = @LOJA
  AND N.nfno = @NFNO
  AND N.nfse = @NFSE
  AND N.tipo = 2;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET last_doc = '';

INSERT INTO sqldados.lastno(no, storeno, dupse, se, padbyte)
VALUES (@NUMERO, @LOJA, 0, @SERIE, '')