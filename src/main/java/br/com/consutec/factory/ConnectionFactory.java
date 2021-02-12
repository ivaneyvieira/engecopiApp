package br.com.consutec.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
	private static ConnectionFactory connectionFactory;
	private Connection connection;

	public static ConnectionFactory getInstance() {
		if (connectionFactory == null) {
			connectionFactory = new ConnectionFactory();
		}
		return connectionFactory;
	}

	public Connection obterConexao() throws Exception {
		String host = "";
		String db = "";
		String user = "";
		String pass = "";
		try {
			String local = (new File("./bdconfig.txt")).getCanonicalFile().toString();
			File arq = new File(local);
			boolean existe = arq.exists();
			if (existe) {
				FileReader fr = new FileReader(arq);
				BufferedReader br = new BufferedReader(fr);
				while (br.ready()) {
					String linha = br.readLine();
					if (linha.contains("Host:")) {
						host = linha.replace("Host:", "").replace(" ", "");
					}
					if (linha.contains("db:")) {
						db = linha.replace("db:", "").replace(" ", "");
					}
					if (linha.contains("user:")) {
						user = linha.replace("user:", "").replace(" ", "");
					}
					if (linha.contains("pass:")) {
						pass = linha.replace("pass:", "").replace(" ", "");
					}
				}
			}
		} catch (Exception e) {
			StackTraceElement[] st = e.getStackTrace();
			String erro = "";
			for (StackTraceElement stackTraceElement : st) {
				erro = erro + stackTraceElement.toString() + "\n";
			}
		}
		if (this.connection == null || this.connection.isClosed()) {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + host + "/" + db;
			try {
				this.connection = DriverManager.getConnection(url, user, pass);
				if (this.connection != null) {
					return this.connection;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new Exception("Não foi possível conectar ao banco");
			}
			return this.connection;
		}
		return this.connection;
	}
}


/* Location:              /home/ivaneyvieira/Dropbox/engecopi/ajustes/ajusteEstoque.jar!/br/com/consutec/factory/ConnectionFactory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */