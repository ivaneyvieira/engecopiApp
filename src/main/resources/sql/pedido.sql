SELECT E.storeno             AS storeno,
       E.ordno               AS numero,
       cast(E.date AS DATE)  AS date,
       userno                AS userno,
       IFNULL(U.name, 'N/D') AS username,
       IFNULL(C.name, 'N/D') AS cliente,
       E.status              AS status
FROM sqldados.eord                AS E
         LEFT JOIN sqldados.users AS U
                   ON U.no = E.userno
         LEFT JOIN sqldados.custp AS C
                   ON C.no = E.custno
WHERE E.status IN (1, 4) AND
      E.storeno = :storeno AND
      E.ordno = :numero AND
      '' = :serie
GROUP BY storeno,
         ordno
UNION
SELECT O.storeno                                    AS storeno,
       CAST(CONCAT(O.nfname, '/', O.invse) AS CHAR) AS numero,
       cast(issue_date AS DATE)                     AS date,
       usernoFirst                                  AS userno,
       IFNULL(U.name, 'N/D')                        AS username,
       IFNULL(V.name, 'N/D')                        AS cliente,
       1                                            AS status
FROM sqldados.inv                    O
         LEFT JOIN sqldados.users AS U
                   ON U.no = O.usernoFirst
         LEFT JOIN sqldados.vend  AS V
                   ON V.no = O.vendno
WHERE O.storeno = :storeno AND
      O.nfname = :numero AND
      O.invse = :serie AND
      :serie = '66'
GROUP BY storeno,
         numero
UNION
SELECT O.storeno                                 AS storeno,
       CAST(CONCAT(O.nfno, '/', O.nfse) AS CHAR) AS numero,
       cast(issuedate AS DATE)                   AS date,
       0                                         AS userno,
       ''                                        AS username,
       IFNULL(C.name, 'N/D')                     AS cliente,
       1                                         AS status
FROM sqldados.nf                     O
         LEFT JOIN sqldados.custp AS C
                   ON C.no = O.custno
WHERE O.storeno = :storeno AND
      O.nfno = :numero AND
      O.nfse = :serie AND
      :serie = '66'
GROUP BY storeno,
         numero
