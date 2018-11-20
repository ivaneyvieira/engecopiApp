select barcode as numero, date, RIGHT(usuario, 2)*1 as storeno
from coletor
where usuario = 'NOTA'
GROUP BY numero
order by date desc, barcode*1 desc