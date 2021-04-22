DO @XANO := (SELECT (MAX(xano) + 1) AS xano
	     FROM sqldados.xa);

INSERT INTO sqldados.xa
VALUES (@XANO);

SELECT @XANO

