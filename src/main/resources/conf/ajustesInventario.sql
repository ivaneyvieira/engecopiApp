select
  vendno,
  ordno,
  qtty / 1000        as qtty,
  A.cost / 10000     as cost,
  A.sp / 100         as sp,
  storeno,
  prdno,
  grade,
  custno,
  numero,
  date,
  nfEntrada,
  nfSaida,
  MID(P.name, 1, 37) AS descricao,
  A.barcode,
  operador,
  inventario / 1000  as inventario,
  saldo / 1000       as saldo
from ajusteInventario AS A
  inner join sqldados.prd AS P
    ON prdno = P.no
where numero = :numero
