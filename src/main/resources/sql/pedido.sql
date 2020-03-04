SELECT E.storeno AS storeno, E.ordno AS numero, cast(date AS DATE) AS date, userno AS userno,
       IFNULL(U.name, 'N/D') AS username, IFNULL(C.name, 'N/D') AS cliente, E.status AS status
FROM sqldados.eord         AS E
  LEFT JOIN sqldados.users AS U
              ON U.no = E.userno
  LEFT JOIN sqldados.custp AS C
              ON C.no = E.custno
WHERE E.status IN (1, 4) AND E.storeno = :storeno AND E.ordno = :ordno
GROUP BY storeno, ordno
