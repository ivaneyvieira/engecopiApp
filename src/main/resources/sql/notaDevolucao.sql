SELECT N.storeno AS storeno, CAST(CONCAT(N.nfno, '/', N.nfse) AS CHAR) AS numero,
       cast(N.issuedate AS DATE) AS date, E.no AS userno, IFNULL(E.name, 'N/D') AS username,
       IFNULL(C.name, 'N/D') AS cliente, N.status AS status
FROM sqldados.nf           AS N
  LEFT JOIN sqldados.emp   AS E
              ON E.no = N.empno
  LEFT JOIN sqldados.custp AS C
              ON C.no = N.custno
WHERE N.tipo = 2 AND N.storeno = :storeno AND N.nfno = :nfno AND N.nfse = :nfse
GROUP BY N.storeno, N.nfno, N.nfse