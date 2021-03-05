SELECT users.no,
       users.name,
       login,
       CAST(CONCAT(CHAR(ASCII(SUBSTRING(pswd, 1, 1)) + ASCII('e') - ASCII('j')),
		   CHAR(ASCII(SUBSTRING(pswd, 2, 1)) + ASCII('a') - ASCII('h')),
		   CHAR(ASCII(SUBSTRING(pswd, 3, 1)) + ASCII('c') - ASCII('k')),
		   CHAR(ASCII(SUBSTRING(pswd, 4, 1)) + ASCII(' ') - ASCII(' ')),
		   CHAR(ASCII(SUBSTRING(pswd, 5, 1)) + ASCII(' ') - ASCII('B')),
		   CHAR(ASCII(SUBSTRING(pswd, 6, 1)) + ASCII(' ') - ASCII(')')),
		   CHAR(ASCII(SUBSTRING(pswd, 7, 1)) + ASCII(' ') - ASCII(')')),
		   CHAR(ASCII(SUBSTRING(pswd, 8, 1)) + ASCII(' ') - ASCII('-'))) AS CHAR) AS senha
FROM sqldados.users
WHERE login = :login
