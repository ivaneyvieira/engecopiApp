DO @NUMERO := :numero;
DO @SERIE := 66;
DO @PEDIDO := 0;
DO @DOC := CONCAT('Ajustes ', @numero);

DROP TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
SELECT @LOJA := storeno AS storeno, ordno, prdno, grade, qtty, cost, vendno, custno, 1 AS empno
FROM sqldados.ajusteInventario
WHERE numero = @NUMERO AND (nfEntrada = '' OR nfSaida = '');

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET longReserva1 = qtty_atacado
WHERE longReserva2 <> T.ordno;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET qtty_atacado = qtty_atacado + T.qtty,
    last_date    = current_date * 1
WHERE longReserva2 <> T.ordno;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET longReserva2 = T.ordno;

DO @INVNO := (SELECT MAX(invno) + 1
              FROM sqldados.inv);

DO @NFNO := IFNULL((SELECT MAX(no) + 1
                    FROM lastno
                    WHERE se = @SERIE AND storeno = @LOJA), 1);

INSERT INTO sqldados.lastno (no, storeno, dupse, se, padbyte)
SELECT DISTINCT @NFNO, @LOJA, 0, @SERIE, ''
FROM T
WHERE qtty > 0;

UPDATE ajusteInventario
SET nfEntrada = IF(EXISTS(SELECT *
                          FROM T
                          WHERE qtty > 0), @NFNO, '')
WHERE numero = @NUMERO;

INSERT INTO sqldados.inv (invno, vendno, ordno, xfrno, issue_date, date, comp_date, ipi, icm,
                          freight, netamt, grossamt, subst_trib, discount, prdamt, despesas,
                          base_ipi, aliq, cfo, nfNfno,
                          auxLong1, auxLong2, auxMoney1, auxMoney2, dataSaida, amtServicos, amtIRRF,
                          amtINSS, amtISS,
                          auxMoney3, auxMoney4, auxMoney5, auxLong3, auxLong4, auxLong5, auxLong6,
                          auxLong7, auxLong8,
                          auxLong9, auxLong10, auxLong11, auxLong12, auxMoney6, auxMoney7,
                          auxMoney8, auxMoney9, auxMoney10,
                          auxMoney11, auxMoney12, auxMoney13, l1, l2, l3, l4, l5, l6, l7, l8, m1,
                          m2, m3, m4, m5, m6, m7,
                          m8, weight, carrno, packages, storeno, indxno, book_bits, type,
                          usernoFirst, usernoLast,
                          nfStoreno, bits, padbyte, auxShort1, auxShort2, auxShort3, auxShort4,
                          auxShort5, auxShort6,
                          auxShort7, auxShort8, auxShort9, auxShort10, auxShort11, auxShort12,
                          auxShort13, auxShort14,
                          bits2, bits3, bits4, bits5, s1, s2, s3, s4, s5, s6, s7, s8, nfname, invse,
                          account, remarks,
                          contaCredito, contaDebito, nfNfse, auxStr1, auxStr2, auxStr3, auxStr4,
                          auxStr5, auxStr6, c1, c2)
SELECT @INVNO AS invno, vendno, ordno, 0 AS xfrno, current_date * 1 AS issue_date,
       current_date * 1 AS date, current_date * 1 AS comp_date, 0 AS ipi, 0 AS icm, 0 AS freight,
       0 AS netamt, ROUND(SUM(qtty * cost / 100000)) AS grossamt, 0 AS subst_trib, 0 AS discount,
       ROUND(SUM(qtty * cost / 100000)) AS prdamt, 0 AS despesas, 0 AS base_ipi, 0 AS aliq,
       1949 AS cfo, 0 AS nfNfno, 0 /*Valor desconhecido*/ AS auxLong1, 0 AS auxLong2,
       0 AS auxMoney1, 0 AS auxMoney2, 0 AS dataSaida, 0 AS amtServicos, 0 AS amtIRRF, 0 AS amtINSS,
       0 AS amtISS, 0 AS auxMoney3, 0 AS auxMoney4, 0 AS auxMoney5, 0 AS auxLong3, 0 AS auxLong4,
       0 AS auxLong5, 0 AS auxLong6, 0 AS auxLong7, current_date * 1 AS auxLong8, 0 AS auxLong9,
       0 AS auxLong10, 0 AS auxLong11, 0 AS auxLong12, 0 AS auxMoney6, 0 AS auxMoney7,
       0 AS auxMoney8, 0 AS auxMoney9, 0 AS auxMoney10, 0 AS auxMoney11, 0 AS auxMoney12,
       0 AS auxMoney13, 0 AS l1, 0 AS l2, 0 AS l3, 0 AS l4, 0 AS l5, current_date * 1 AS l6,
       0 AS l7, 0 AS l8, 0 AS m1, 0 AS m2, 0 AS m3, 0 AS m4, 0 AS m5, 0 AS m6, 0 AS m7, 0 AS m8,
       0 AS weight, 0 AS carrno, 0 AS packages, storeno, 0 AS indxno, 9 AS book_bits, 8 AS type,
       1 AS usernoFirst, 1 AS usernoLast, 0 AS nfStoreno, 3 AS bits, 0 AS padbyte,
       storeno AS auxShort1, 0 AS auxShort2, 0 AS auxShort3, 0 AS auxShort4, 0 AS auxShort5,
       0 AS auxShort6, 0 AS /*valor desconhecido*/ auxShort7, 0 AS /*valor desconhecido*/ auxShort8,
       0 AS auxShort9, 0 AS auxShort10, 0 AS auxShort11, 0 AS auxShort12, 0 AS auxShort13,
       0 AS auxShort14, 0 AS bits2, 0 AS bits3, 0 AS bits4, 0 AS bits5, 0 AS s1, 0 AS s2, 0 AS s3,
       0 AS s4, 0 AS s5, 0 AS s6, 0 AS s7, 0 AS s8, @NFNO AS nfname, @SERIE AS invse, 2 AS account,
       '66' AS remarks, '' AS contaCredito, '' AS contaDebito, '' AS nfNfse, '' AS auxStr1,
       '' AS auxStr2, '' AS auxStr3, '' AS auxStr4, '' AS auxStr5, '' AS auxStr6, @DOC AS c1,
       '' AS c2
FROM T
WHERE qtty > 0
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
SELECT @INVNO AS invno, qtty, ROUND(cost / 100) AS fob, ROUND(cost / 100) AS cost,
       current_date * 1 AS date, 0 AS ipi, 0 AS auxLong1, 0 AS auxLong2, 0 AS frete, 0 AS seguro,
       0 AS despesas, 0 AS freteIpi, 0 AS qttyRessar, 0 AS baseIcmsSubst, 0 AS icmsSubst, 0 AS icms,
       0 AS discount, cost AS fob4, cost AS cost4, 0 AS icmsAliq, 1949 AS cfop, 0 AS auxLong3,
       0 AS auxLong4, 0 AS auxLong5, 0 AS auxMy1, 0 AS auxMy2, 0 AS auxMy3, 0 AS baseIcms,
       0 AS baseIpi, 0 AS ipiAmt, 0 AS reducaoBaseIcms, 0 AS lucroTributado, 0 AS l1, 0 AS l2,
       0 AS l3, 0 AS l4, 0 AS l5, 0 AS l6, 0 AS l7, 0 AS l8, 0 AS m1, 0 AS m2, 0 AS m3, 0 AS m4,
       0 AS m5, 0 AS m6, 0 AS m7, 0 AS m8, storeno, 32 AS bits, 0 AS auxShort1, 0 AS auxShort2,
       0 AS taxtype, 0 AS auxShort3, 0 AS auxShort4, 0 AS auxShort5, 0 AS seqno, 0 AS bits2,
       0 AS bits3, 0 AS bits4, 0 AS s1, 0 AS s2, 0 AS s3, 0 AS s4, 0 AS s5, 0 AS s6, 0 AS s7,
       0 AS s8, prdno, grade, '' AS auxChar, '' AS auxChar2, '000' AS cstIcms, '' AS cstIpi,
       '' AS c1
FROM T
WHERE qtty > 0;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET last_doc = CONCAT(@NFNO, "66")
WHERE qtty > 0;


DO @NFNO := IFNULL((SELECT MAX(no) + 1
                    FROM lastno
                    WHERE se = @SERIE AND storeno = @LOJA), 1);

INSERT INTO sqldados.lastno (no, storeno, dupse, se, padbyte)
SELECT DISTINCT @NFNO, @LOJA, 0, @SERIE, ''
FROM T
WHERE qtty < 0;

UPDATE ajusteInventario
SET nfSaida = IF(EXISTS(SELECT *
                        FROM T
                        WHERE qtty < 0), @NFNO, '')
WHERE numero = @NUMERO;

DO @XANO := IFNULL((SELECT MAX(xano) + 1
                    FROM sqldados.nf
                    WHERE pdvno = 0 AND storeno = @LOJA), 1);

INSERT INTO sqldados.nf (xano, nfno, custno, issuedate, delivdate, sec_amt, fre_amt, netamt,
                         grossamt,
                         discount, icms_amt, tax_paid, ipi_amt, base_calculo_ipi, iss_amt,
                         base_iss_amt, isento_amt,
                         subst_amt, baseIcmsSubst, icmsSubst, vol_no, vol_qtty, cfo, invno, cfo2,
                         auxLong1, auxLong2,
                         auxLong3, auxLong4, auxMy1, auxMy2, auxMy3, auxMy4, eordno, l1, l2, l3, l4,
                         l5, l6, l7, l8, m1,
                         m2, m3, m4, m5, m6, m7, m8, vol_gross, vol_net, mult, storeno, pdvno,
                         carrno, empno, status,
                         natopno, xatype, storeno_from, tipo, padbits, bits, usernoCancel,
                         custno_addno, empnoDiscount,
                         auxShort1, auxShort2, auxShort3, auxShort4, auxShort5, paymno, s1, s2, s3,
                         s4, s5, s6, s7, s8,
                         nfse, ship_by, vol_make, vol_kind, remarks, padbyte, print_remarks,
                         remarksCancel, c1, c2, wshash)
SELECT @XANO AS xano, @NFNO AS nfno, custno, current_date * 1 AS issuedate,
       current_date * 1 AS delivdate, 0 AS sec_amt, 0 AS fre_amt, 0 AS netamt,
       SUM(-qtty * cost / 100000) AS grossamt, 0 AS discount, 0 AS icms_amt, 0 AS tax_paid,
       0 AS ipi_amt, 0 AS base_calculo_ipi, 0 AS iss_amt, 0 AS base_iss_amt, 0 AS isento_amt,
       0 AS subst_amt, 0 AS baseIcmsSubst, 0 AS icmsSubst, 0 AS vol_no, 0 AS vol_qtty, 5949 AS cfo,
       0 AS invno, 0 AS cfo2, 0 AS auxLong1, 0 AS auxLong2, 0 AS auxLong3, 0 AS auxLong4,
       0 AS auxMy1, 0 AS auxMy2, 0 AS auxMy3, 0 AS auxMy4, ordno AS eordno, 0 AS l1, 0 AS l2,
       0 AS l3, 0 AS l4, 0 AS l5, 0 AS l6, 0 AS l7, 0 AS l8, 0 AS m1, 0 AS m2, 0 AS m3, 0 AS m4,
       0 AS m5, 0 AS m6, 0 AS m7, 0 AS m8, 0 AS vol_gross, 0 AS vol_net, 1 AS mult, storeno,
       0 AS pdvno, 0 AS carrno, 900 + storeno AS empno, 0 AS status, 14 AS natopno, 0 AS xatype,
       0 AS storeno_from, 9 AS tipo, 0 AS padbits, 0 AS bits, 0 AS usernoCancel, 0 AS custno_addno,
       0 AS empnoDiscount, 0 AS auxShort1, 0 AS auxShort2, 0 AS auxShort3, 0 AS auxShort4,
       0 AS auxShort5, 0 AS paymno, 0 AS s1, 0 AS s2, 0 AS s3, 0 AS s4, 0 AS s5, 0 AS s6, 0 AS s7,
       0 AS s8, @SERIE AS nfse, '' AS ship_by, '' AS vol_make, '' AS vol_kind, '66' AS remarks,
       '' AS padbyte, '' AS print_remarks, '' AS remarksCancel, @DOC AS c1, '' AS c2, '' AS wshash
FROM T
WHERE qtty < 0
GROUP BY storeno, ordno;

INSERT INTO sqldados.xaprd (xano, nfno, price, date, qtty, storeno, pdvno, prdno, grade, nfse,
                            padbyte, wshash)
SELECT @XANO AS xano, @NFNO AS nfno, ROUND(cost / 100) AS price, current_date * 1 AS date,
       -qtty / 1000 AS qtty, storeno, 0 AS pdvno, prdno, grade, @SERIE AS nfse, '' AS padbyte,
       @DOC AS wshash
FROM T
WHERE qtty < 0;

UPDATE sqldados.stk INNER JOIN T USING (storeno, prdno, grade)
SET last_doc = '';

