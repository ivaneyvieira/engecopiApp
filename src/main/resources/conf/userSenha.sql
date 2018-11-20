SELECT
  users.no,
  users.name,
  login,
  cast(
      concat(CHAR(ascii(SUBSTRING(pswd, 1, 1)) + ascii('e') - ascii('j')), CHAR(
          ascii(SUBSTRING(pswd, 2, 1)) + ascii('a') - ascii('h')), CHAR(ascii(
                                                                            SUBSTRING(pswd, 3, 1)) + ascii('c') -
                                                                        ascii('k')), CHAR(ascii(SUBSTRING(pswd
      , 4, 1)) + ascii(' ') - ascii(' ')), CHAR(ascii(SUBSTRING(pswd, 5, 1)) +
                                                ascii(' ') - ascii('B')),
             CHAR(ascii(SUBSTRING(pswd, 6, 1)) + ascii(' ') -
                  ascii(')')), CHAR(ascii(SUBSTRING(pswd, 7, 1)) + ascii(' ') - ascii(')')),
             CHAR(ascii(SUBSTRING(pswd, 8, 1)) + ascii(' ') - ascii('-')))
      AS CHAR) AS senha
FROM users
WHERE login = :login
