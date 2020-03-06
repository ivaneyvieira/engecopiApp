SELECT E.storeno AS storeno, E.ordno AS numero, cast(date AS DATE) AS date, userno AS userno,
       IFNULL(U.name, 'N/D') AS username, IFNULL(C.name, 'N/D') AS cliente, E.status AS status,
       'PEDIDO' AS tipo
FROM sqldados.eord         AS E
  LEFT JOIN sqldados.users AS U
              ON U.no = E.userno
  LEFT JOIN sqldados.custp AS C
              ON C.no = E.custno
WHERE E.status IN (1, 4) AND E.storeno = :storeno AND E.ordno = :numero AND :serie = ''
GROUP BY storeno, ordno
UNION
DISTINCT
SELECT N.storeno AS storeno, CAST(CONCAT(N.nfno, '/', N.nfse) AS CHAR) AS numero,
       cast(N.issuedate AS DATE) AS date, E.no AS userno, IFNULL(E.name, 'N/D') AS username,
       IFNULL(C.name, 'N/D') AS cliente, IF(N.s16 = 0, 1, N.s16) AS status, 'DEVOLUCAO' AS tipo
FROM sqldados.nf           AS N
  LEFT JOIN sqldados.emp   AS E
              ON E.no = N.empno
  LEFT JOIN sqldados.custp AS C
              ON C.no = N.custno
WHERE N.tipo = 2 AND N.storeno = :storeno AND N.nfno = :numero AND N.nfse = :serie
GROUP BY N.storeno, N.nfno, N.nfse
