package br.com.consutec.dao;

import br.com.engecopi.app.model.Base;
import br.com.engecopi.app.model.Produtos;

import javax.swing.JOptionPane;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GestorDADOS {
	final private Connection conexao;

	public GestorDADOS(Connection conexao) {
		this.conexao = conexao;
	}

	public Connection getConnection()  {
		return this.conexao;
	}

	public Integer pegaTransacao() throws Exception {
		Integer transacao = 0;
		Connection cx = getConnection();
		cx.setAutoCommit(false);
		String sql1 = "SELECT (MAX(xano) + 1) AS xano FROM xa ";
		String sql2 = "INSERT INTO xa VALUES (?)";
		PreparedStatement stmt = cx.prepareStatement(sql1);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			try {
				rs.findColumn("xano");
			} catch (Exception ignored) {
			}
			PreparedStatement stmt2 = cx.prepareStatement(sql2);
			stmt2.setLong(1, rs.getInt("xano"));
			stmt2.executeUpdate();
			transacao = rs.getInt("xano");
			cx.commit();
			return transacao;
		}
		return null;
	}

	public Integer pegaNumNf(Integer loja) throws Exception {
		Integer numNf;
		Connection cx = getConnection();
		cx.setAutoCommit(false);
		String sql1 = "SELECT (MAX(no) + 1) AS numero FROM lastno WHERE storeno = ? AND se = 66";
		String sql2 = "INSERT INTO lastno (no,storeno,dupse,se,padbyte) VALUES (?,?,0,'66','')";
		PreparedStatement stmt = cx.prepareStatement(sql1);
		stmt.setInt(1, loja);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			try {
				rs.findColumn("numero");
			} catch (Exception ignored) {
			}
			PreparedStatement stmt2 = cx.prepareStatement(sql2);
			stmt2.setInt(1, rs.getInt("numero"));
			stmt2.setInt(2, loja);
			stmt2.executeUpdate();
			numNf = rs.getInt("numero");
			cx.commit();
			return numNf;
		}
		return null;
	}

	public Integer buscaFornecedor(Integer lojaid) throws Exception {
		StringBuilder sql1 = new StringBuilder();
		Integer codFornecedor = 0;
		sql1.append(
						" SELECT vend.no AS no  FROM vend  inner join store on (vend.cgc = store.cgc)  where store.no = ? ");
		PreparedStatement stmt1 = getConnection().prepareStatement(sql1.toString());
		stmt1.setInt(1, lojaid);
		stmt1.executeQuery();
		ResultSet rs1 = stmt1.getResultSet();
		while (rs1.next()) {
			codFornecedor = rs1.getInt("no");
		}
		return codFornecedor;
	}

	public Integer buscaCliente(Integer lojaid) throws Exception {
		StringBuilder sql1 = new StringBuilder();
		Integer codCliente = 0;
		sql1.append(
						" SELECT custp.no AS no  FROM custp  inner join store on (custp.cpf_cgc = store.cgc)  where store.no = ? ");
		PreparedStatement stmt1 = getConnection().prepareStatement(sql1.toString());
		stmt1.setInt(1, lojaid);
		stmt1.executeQuery();
		ResultSet rs1 = stmt1.getResultSet();
		while (rs1.next()) {
			codCliente = rs1.getInt("no");
		}
		return codCliente;
	}

	public List<Produtos> listar(Base base) throws Exception {
		List<Produtos> listagem = new ArrayList<>();
		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		String colunaCusto = "";
		String colunaQtdNotas = "";
		if (base.getTipoCusto().equals("est")) {
			colunaCusto = "     , ifnull(stk.cm_varejo,0) AS ultimocusto";
		} else {
			colunaCusto = "     , ifnull(stk.cm_real,0) AS ultimocusto";
		}
		if (base.getIncluiNfFornecedor()) {
			colunaQtdNotas = " 0 ";
		} else if (base.getOperacao().equals("entrada")) {
			colunaQtdNotas = " ifnull(y.qtty*1000,0) ";
		} else {
			colunaQtdNotas = " ifnull(y.qtty*(-1000),0) ";
		}
		sql1.append(
						"SELECT z.prdno, z.grade, z.descricao, z.fornecedor, z.centrodelucro, z.tipo, z.qttynfs, z.qttyatacado, (z.qttyatacado - z.qttynfs) qttyconsiderada, z.ultimocusto, ");
		sql1.append(" (((z.qttyatacado - z.qttynfs)/1000) * z.ultimocusto) AS total ");
		sql1.append(" FROM ( ");
		sql1.append(" SELECT x.prdno, x.grade, x.descricao, x.fornecedor, x.centrodelucro, x.tipo, ");
		sql1.append(colunaQtdNotas);
		sql1.append(" qttynfs, x.qttyatacado, x.ultimocusto ");
		sql1.append(" FROM ( ");
		sql1.append(
						" SELECT stk.prdno AS prdno      , stk.grade AS grade      , prd.name AS descricao      , prd.mfno AS fornecedor      , LPAD(prd.clno,6,'0') AS centrodelucro      , prd.typeno AS tipo     , stk.qtty_atacado AS qttyatacado ");
		sql1.append(colunaCusto);
		sql1.append(
						" FROM stk  INNER JOIN prd on (stk.prdno = prd.no)  LEFT JOIN prp on (stk.prdno = prp.prdno AND                    stk.storeno = prp.storeno) LEFT JOIN prdloc on (prdloc.prdno = stk.prdno and                                   prdloc.grade = stk.grade) WHERE stk.storeno = ?  AND dereg&POW(2,2) <> POW(2,2) ");
		if (base.getOperacao().equals("entrada")) {
			sql1.append(" AND (qtty_atacado > 0 )");
		} else {
			sql1.append(" AND (qtty_atacado < 0 )");
		}
		sql2.append(sql1.toString());
		sql2.append(" GROUP BY 1,2 ");
		sql2.append(" ) AS x");
		String sqlNotas = " LEFT JOIN ( SELECT  xaprd.prdno        , xaprd.grade        , SUM(xaprd.qtty) qtty  FROM  nf  INNER JOIN xaprd ON (nf.storeno = xaprd.storeno AND  nf.pdvno = xaprd.pdvno AND  nf.xano = xaprd.xano)  INNER JOIN custp ON (custp.no = nf.custno)  INNER JOIN vend ON (custp.cpf_cgc = vend.cgc) LEFT  JOIN store ON (store.cgc = custp.cpf_cgc)  WHERE (nf.storeno = ? )  AND   (nf.nfse = '66')  AND   (nf.tipo = 2)  AND   (store.name IS NULL)  AND   (nf.cfo in (6949,5949))  AND   (nf.c1 <> '1')";
		if (!base.getFornecedorNf().equals("")) {
			sqlNotas = sqlNotas + " AND   (vend.no = ? ) ";
		}
		if (base.getDtNfIni() != 0) {
			sqlNotas = sqlNotas + " AND   (nf.issuedate between ? AND ?) ";
		}
		sqlNotas = sqlNotas +
		           " GROUP BY xaprd.prdno, xaprd.grade  ) y ON (x.prdno = y.prdno and x.grade = y.grade) ";
		if (!base.getIncluiNfFornecedor()) {
			sql2.append(sqlNotas);
		}
		sql2.append(" ) z ");
		boolean filtro1 = false;
		boolean filtro2 = false;
		boolean filtro3 = false;
		boolean filtro4 = false;
		boolean filtro5 = false;
		boolean filtro6 = false;
		boolean filtro7 = false;
		boolean filtro8 = false;
		if (base.getCodprd().equals("")) {
			filtro1 = false;
		} else {
			filtro1 = true;
			sql1.append(" AND (ROUND(prd.no) = ? ) ");
		}
		if (base.getDescIni().equals("")) {
			filtro2 = false;
		} else {
			filtro2 = true;
			sql1.append(" AND (MID(prd.name,1,3) BETWEEN ? AND ? ) ");
		}
		if (base.getFornecedores().equals("")) {
			filtro3 = false;
		} else {
			filtro3 = true;
			sql1.append(" AND (prd.mfno IN ( ").append(base.getFornecedores()).append(" )) ");
		}
		if (base.getTipos().equals("")) {
			filtro4 = false;
		} else {
			filtro4 = true;
			sql1.append(" AND (prd.typeno IN ( ").append(base.getTipos()).append(" )) ");
		}
		if (base.getAreas().equals("")) {
			filtro5 = false;
		} else {
			filtro5 = true;
			sql1.append(" AND (prdloc.storeno = ? ) ");
			sql1.append(" AND (prdloc.localizacao LIKE ? ) ");
		}
		if (base.getCentrodeLucro().equals("")) {
			filtro6 = false;
		} else {
			filtro6 = true;
			if (base.getCentrodeLucro().endsWith("0000")) {
				sql1.append(" AND (prd.groupno = ? ) ");
			} else if (base.getCentrodeLucro().endsWith("00")) {
				sql1.append(" AND (prd.deptno = ? ) ");
			} else {
				sql1.append(" AND (prd.clno = ? ) ");
			}
		}
		if (base.getSinalQtd().equals("todos")) {
			filtro7 = false;
		} else {
			filtro7 = true;
			switch (base.getSinalQtd()) {
				case ">":
					sql1.append(" AND (stk.qtty_atacado > ? ) ");
					break;
				case "<":
					sql1.append(" AND (stk.qtty_atacado < ? ) ");
					break;
				case "=":
					sql1.append(" AND (stk.qtty_atacado = ? ) ");
					break;
				default:
					sql1.append(" AND (stk.qtty_atacado BETWEEN ? AND ? ) ");
					break;
			}
		}
		if (base.getSinalCusto().equals("todos")) {
			filtro8 = false;
		} else {
			filtro8 = true;
			switch (base.getSinalCusto()) {
				case ">":
					sql1.append(" AND (stk.last_cost > ? ) ");
					break;
				case "<":
					sql1.append(" AND (stk.last_cost < ? ) ");
					break;
				case "=":
					sql1.append(" AND (stk.last_cost = ? ) ");
					break;
				default:
					sql1.append(" AND (stk.last_cost BETWEEN ? AND ? ) ");
					break;
			}
		}
		sql1.append(" GROUP BY 1,2 ");
		sql1.append(" ) AS x");
		if (!base.getIncluiNfFornecedor()) {
			sql1.append(sqlNotas);
		}
		sql1.append(" ) z");
		String sql = "";
		if (base.getUsaFiltrosProduto()) {
			sql = sql1.toString();
		} else {
			sql = sql2.toString();
		}
		PreparedStatement stmt = getConnection().prepareStatement(sql);
		stmt.setInt(1, base.getLojaDestino());
		int index = 1;
		if (filtro1) {
			index++;
			stmt.setString(index, base.getCodprd());
		}
		if (filtro2) {
			index++;
			stmt.setString(index, base.getDescIni());
			index++;
			stmt.setString(index, base.getDescFim());
		}
		if (filtro3)
			;
		if (filtro4)
			;
		if (filtro5) {
			index++;
			stmt.setInt(index, base.getLojaArea());
			index++;
			stmt.setString(index, "%" + base.getAreas() + "%");
		}
		if (filtro6) {
			index++;
			stmt.setInt(index, Integer.parseInt(base.getCentrodeLucro()));
		}
		if (filtro7) {
			switch (base.getSinalQtd()) {
				case ">": {
					index++;
					BigDecimal qtd1 = base.getQtd1().multiply(new BigDecimal("1000"));
					stmt.setLong(index, qtd1.longValue());
					break;
				}
				case "<": {
					index++;
					BigDecimal qtd1 = base.getQtd1().multiply(new BigDecimal("1000"));
					stmt.setLong(index, qtd1.longValue());
					break;
				}
				case "=": {
					index++;
					BigDecimal qtd1 = base.getQtd1().multiply(new BigDecimal("1000"));
					stmt.setLong(index, qtd1.longValue());
					break;
				}
				default: {
					index++;
					BigDecimal qtd1 = base.getQtd1().multiply(new BigDecimal("1000"));
					stmt.setLong(index, qtd1.longValue());
					index++;
					BigDecimal qtd2 = base.getQtd2().multiply(new BigDecimal("1000"));
					stmt.setLong(index, qtd2.longValue());
					break;
				}
			}
		}
		if (filtro8) {
			switch (base.getSinalCusto()) {
				case ">": {
					index++;
					BigDecimal custo1 = base.getCusto1().multiply(new BigDecimal("10000"));
					stmt.setLong(index, custo1.longValue());
					break;
				}
				case "<": {
					index++;
					BigDecimal custo1 = base.getCusto1().multiply(new BigDecimal("10000"));
					stmt.setLong(index, custo1.longValue());
					break;
				}
				case "=": {
					index++;
					BigDecimal custo1 = base.getCusto1().multiply(new BigDecimal("10000"));
					stmt.setLong(index, custo1.longValue());
					break;
				}
				default: {
					index++;
					BigDecimal custo1 = base.getCusto1().multiply(new BigDecimal("10000"));
					stmt.setLong(index, custo1.longValue());
					index++;
					BigDecimal custo2 = base.getCusto2().multiply(new BigDecimal("10000"));
					stmt.setLong(index, custo2.longValue());
					break;
				}
			}
		}
		if (!base.getIncluiNfFornecedor()) {
			index++;
			stmt.setInt(index, base.getLojaDestino());
			if (!base.getFornecedorNf().equals("")) {
				index++;
				stmt.setInt(index, Integer.valueOf(base.getFornecedorNf()));
			}
			if (base.getDtNfIni() != 0) {
				index++;
				stmt.setInt(index, base.getDtNfIni());
				index++;
				stmt.setInt(index, base.getDtNfFim());
			}
		}
		stmt.executeQuery();
		ResultSet rs = stmt.getResultSet();
		while (rs.next()) {
			if (rs.findColumn("prdno") > 0) {
				Produtos prd = new Produtos();
				prd.setPrdno(rs.getString("prdno"));
				prd.setGrade(rs.getString("grade"));
				prd.setDescricao(rs.getString("descricao"));
				prd.setFornecedor(rs.getLong("fornecedor"));
				prd.setCentrodelucro(rs.getString("centrodelucro"));
				prd.setTipo(rs.getLong("tipo"));
				prd.setQtdNfForn(rs.getBigDecimal("qttynfs").divide(new BigDecimal("1000"))
								.setScale(4, RoundingMode.CEILING));
				prd.setQtdAtacado(rs.getBigDecimal("qttyatacado").divide(new BigDecimal("1000"))
								.setScale(4, RoundingMode.CEILING));
				prd.setQtdConsiderada(rs.getBigDecimal("qttyconsiderada").divide(new BigDecimal("1000"))
								.setScale(4, RoundingMode.CEILING));
				prd.setCusto(rs.getBigDecimal("ultimocusto").divide(new BigDecimal("10000"))
								.setScale(4, RoundingMode.CEILING));
				prd.setTotal(rs.getBigDecimal("total").divide(new BigDecimal("10000"))
								.setScale(4, RoundingMode.CEILING));
				listagem.add(prd);
			}
		}
		stmt.close();
		return listagem;
	}

	public Integer executar(Base base) throws Exception {
		Integer pedido = 0;
		if (base.getOperacao().equals("entrada")) {
			pedido = gerarEntrada(base);
		} else {
			pedido = gerarSaida(base);
		}
		return pedido;
	}

	public Boolean validarNfEntrada(Integer loja, Integer nota) throws Exception {
		Boolean ehValido = Boolean.FALSE;
		StringBuilder sql = new StringBuilder();
		sql.append(
						" SELECT count(*) qtd from nf  where storeno = ? and nfno = ? and print_remarks like ? and nfse = '66' ");
		PreparedStatement stmt = getConnection().prepareStatement(sql.toString());
		stmt.setInt(1, loja);
		stmt.setInt(2, nota);
		stmt.setString(3, "CNF");
		stmt.executeQuery();
		ResultSet rs = stmt.getResultSet();
		if (rs.next() && rs.getInt("qtd") > 0) {
			ehValido = Boolean.TRUE;
		}
		stmt.close();
		return ehValido;
	}

	public Boolean validarNfSaida(Integer loja, Integer nota) throws Exception {
		Boolean ehValido = Boolean.FALSE;
		StringBuilder sql = new StringBuilder();
		sql.append(
						"SELECT invno FROM inv WHERE storeno = ? AND nfname = ? AND  vendno = ? AND auxStr1 = ? AND invse = '66'");
		PreparedStatement stmt = getConnection().prepareStatement(sql.toString());
		stmt.setInt(1, loja);
		stmt.setInt(2, nota);
		stmt.setInt(3, buscaFornecedor(loja));
		stmt.setString(4, "CNF");
		stmt.executeQuery();
		ResultSet rs = stmt.getResultSet();
		if (rs.next() && rs.getInt("invno") > 0) {
			ehValido = Boolean.TRUE;
		}
		stmt.close();
		return ehValido;
	}

	public void desfazerEntrada(Integer loja, Integer nota) throws Exception {
		Connection cx = null;
		try {
			cx = getConnection();
			cx.setAutoCommit(false);
			StringBuilder sql1 = new StringBuilder();
			StringBuilder sql2 = new StringBuilder();
			StringBuilder sql3 = new StringBuilder();
			StringBuilder sql4 = new StringBuilder();
			StringBuilder sql5 = new StringBuilder();
			StringBuilder sql6 = new StringBuilder();
			sql1.append(
							"UPDATE stk  INNER JOIN xaprd ON (stk.storeno = xaprd.storeno AND  stk.prdno = xaprd.prdno AND  stk.grade = xaprd.grade)  SET stk.qtty_varejo = (stk.qtty_varejo - (xaprd.qtty * 1000)), stk.qtty_atacado = (stk.qtty_atacado + (xaprd.qtty * 1000))  WHERE stk.storeno = ? AND xaprd.nfno = ? AND xaprd.nfse = 66 ");
			PreparedStatement stmt1 = cx.prepareStatement(sql1.toString());
			stmt1.setInt(1, loja);
			stmt1.setInt(2, nota);
			stmt1.executeUpdate();
			sql2.append(
							"SELECT xano, ROUND(remarks) as xastk FROM nf WHERE storeno = ? AND nfno = ? AND auxLong1 = ? AND nfse = 66");
			PreparedStatement stmt2 = cx.prepareStatement(sql2.toString());
			stmt2.setInt(1, loja);
			stmt2.setInt(2, nota);
			stmt2.setString(3, "CNF");
			stmt2.executeQuery();
			ResultSet rs = stmt2.getResultSet();
			Integer transacaoNF = 0;
			Integer transacaoES = 0;
			if (rs.next()) {
				transacaoNF = rs.getInt("xano");
				transacaoES = Integer.valueOf(rs.getString("xastk"));
			}
			sql3.append("DELETE FROM nf WHERE storeno = ? AND nfno = ? AND nfse = '66' AND xano = ? ");
			PreparedStatement stmt3 = cx.prepareStatement(sql3.toString());
			stmt3.setInt(1, loja);
			stmt3.setInt(2, nota);
			stmt3.setInt(3, transacaoNF);
			stmt3.executeUpdate();
			sql4.append("DELETE FROM xaprd WHERE storeno = ? AND nfno = ? AND nfse = '66' AND xano = ? ");
			PreparedStatement stmt4 = cx.prepareStatement(sql4.toString());
			stmt4.setInt(1, loja);
			stmt4.setInt(2, nota);
			stmt4.setInt(3, transacaoNF);
			stmt4.executeUpdate();
			sql5.append("DELETE FROM stkmov WHERE storeno = ? AND xano = ? ");
			PreparedStatement stmt5 = cx.prepareStatement(sql5.toString());
			stmt5.setInt(1, loja);
			stmt5.setInt(2, transacaoES);
			stmt5.executeUpdate();
			sql6.append("DELETE FROM stkmovh WHERE storeno = ? AND xano = ? ");
			PreparedStatement stmt6 = cx.prepareStatement(sql6.toString());
			stmt6.setInt(1, loja);
			stmt6.setInt(2, transacaoES);
			stmt6.executeUpdate();
			cx.commit();
		} catch (SQLException se) {
			cx.rollback();
			JOptionPane.showMessageDialog(null, " Erro:" + se);
			se.printStackTrace();
			throw new RuntimeException(se);
		} catch (Exception e) {
			cx.rollback();
			JOptionPane.showMessageDialog(null, " Erro:" + e);
			throw new RuntimeException(e);
		} finally {
		}
	}

	public void desfazerSaida(Integer loja, Integer nota) throws SQLException {
		Connection cx = null;
		try {
			cx = getConnection();
			cx.setAutoCommit(false);
			StringBuilder sql1 = new StringBuilder();
			StringBuilder sql2 = new StringBuilder();
			StringBuilder sql3 = new StringBuilder();
			StringBuilder sql4 = new StringBuilder();
			StringBuilder sql5 = new StringBuilder();
			StringBuilder sql6 = new StringBuilder();
			sql1.append(
							"SELECT invno, auxStr2 FROM inv WHERE storeno = ? AND nfname = ? AND auxStr1 = ? AND invse = 66");
			PreparedStatement stmt1 = cx.prepareStatement(sql1.toString());
			stmt1.setInt(1, loja);
			stmt1.setInt(2, nota);
			stmt1.setString(3, "CNF");
			stmt1.executeQuery();
			ResultSet rs = stmt1.getResultSet();
			Integer invno = 0;
			Integer transacao = 0;
			if (rs.next()) {
				invno = rs.getInt("invno");
				transacao = rs.getInt("auxStr2");
			}
			sql2.append(
							"UPDATE stk  INNER JOIN iprd ON (stk.storeno = iprd.storeno AND  stk.prdno = iprd.prdno AND  stk.grade = iprd.grade)  SET   stk.qtty_varejo = (stk.qtty_varejo + iprd.qtty), stk.qtty_atacado = (stk.qtty_atacado + (iprd.qtty * (-1)))  WHERE iprd.storeno = ? AND iprd.invno = ? ");
			PreparedStatement stmt2 = cx.prepareStatement(sql2.toString());
			stmt2.setInt(1, loja);
			stmt2.setInt(2, invno);
			stmt2.executeUpdate();
			sql3.append("DELETE FROM inv WHERE storeno = ? AND invno = ? AND invse = 66 ");
			PreparedStatement stmt3 = cx.prepareStatement(sql3.toString());
			stmt3.setInt(1, loja);
			stmt3.setInt(2, invno);
			stmt3.executeUpdate();
			sql4.append("DELETE FROM iprd WHERE storeno = ? AND invno = ? ");
			PreparedStatement stmt4 = cx.prepareStatement(sql4.toString());
			stmt4.setInt(1, loja);
			stmt4.setInt(2, invno);
			stmt4.executeUpdate();
			sql5.append("DELETE FROM stkmov WHERE storeno = ? AND xano = ? ");
			PreparedStatement stmt5 = cx.prepareStatement(sql5.toString());
			stmt5.setInt(1, loja);
			stmt5.setInt(2, transacao);
			stmt5.executeUpdate();
			sql6.append("DELETE FROM stkmovh WHERE storeno = ? AND xano = ? ");
			PreparedStatement stmt6 = cx.prepareStatement(sql6.toString());
			stmt6.setInt(1, loja);
			stmt6.setInt(2, transacao);
			stmt6.executeUpdate();
			cx.commit();
		} catch (SQLException se) {
			cx.rollback();
			JOptionPane.showMessageDialog(null, " Erro:" + se);
			se.printStackTrace();
			throw new RuntimeException(se);
		} catch (Exception e) {
			cx.rollback();
			JOptionPane.showMessageDialog(null, " Erro:" + e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Integer gerarEntrada(Base base) throws Exception {
		Connection cx = null;
		Integer cliente = 0;
		Integer fornecedor = 0;
		cliente = buscaCliente(base.getLojaDestino());
		fornecedor = buscaFornecedor(base.getLojaDestino());
		Integer transacaoNF = pegaTransacao();
		Integer transacaoEstoque = pegaTransacao();
		Integer numnf = pegaNumNf(base.getLojaDestino());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			cx = getConnection();
			cx.setAutoCommit(false);
			List<Produtos> lista = new ArrayList<>();
			lista = listar(base);
			BigDecimal valorTotal = BigDecimal.ZERO;
			for (Produtos prd : lista) {
				StringBuilder sql1 = new StringBuilder();
				StringBuilder sql2 = new StringBuilder();
				BigDecimal qtd = BigDecimal.ZERO;
				qtd = prd.getQtdConsiderada();
				if (qtd.compareTo(BigDecimal.ZERO) < 0) {
					qtd.multiply(new BigDecimal(-1));
				}
				if (qtd.compareTo(BigDecimal.ZERO) != 0) {
					sql1.append(
									"INSERT INTO stkmov (xano,qtty,date,cm_fiscal,cm_real,storeno,bits,prdno,grade,remarks)  values (?,?,?,?,?,?,?,?,?,?) ");
					PreparedStatement stmt1 = cx.prepareStatement(sql1.toString());
					stmt1.setInt(1, transacaoEstoque);
					stmt1.setInt(2, qtd.multiply(new BigDecimal("1000")).intValue());
					stmt1.setInt(3, Integer.valueOf(sdf.format(new Date())));
					stmt1.setLong(4, prd.getCusto().multiply(new BigDecimal("10000")).longValue());
					stmt1.setLong(5, prd.getCusto().multiply(new BigDecimal("10000")).longValue());
					stmt1.setInt(6, base.getLojaDestino());
					stmt1.setInt(7, 1);
					stmt1.setString(8, prd.getPrdno());
					stmt1.setString(9, prd.getGrade());
					stmt1.setString(10, "CONFERENCIA DE ESTOQUE");
					stmt1.executeUpdate();
					sql2.append(
									"INSERT INTO stkmovh (xano,qtty,date,nfno,cm_fiscal,cm_real,auxLong1,auxLong2,  auxLong3,auxLong4,auxLong5,auxMy1,auxMy2,auxMy3,auxMy4,auxMy5,storeno,userno,  tipo,bits,auxShort1,auxShort2,auxShort3,auxShort4,auxShort5,prdno,grade,nfse,  auxStr1,auxStr2,auxStr3,auxStr4)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
					PreparedStatement stmt2 = cx.prepareStatement(sql2.toString());
					stmt2.setInt(1, transacaoEstoque);
					stmt2.setInt(2, qtd.multiply(new BigDecimal("1000")).intValue());
					stmt2.setInt(3, Integer.valueOf(sdf.format(new Date())));
					stmt2.setInt(4, 0);
					stmt2.setLong(5, prd.getCusto().multiply(new BigDecimal("10000")).longValue());
					stmt2.setLong(6, prd.getCusto().multiply(new BigDecimal("10000")).longValue());
					stmt2.setInt(7, 0);
					stmt2.setInt(8, 0);
					stmt2.setInt(9, 0);
					stmt2.setInt(10, 0);
					stmt2.setInt(11, 0);
					stmt2.setInt(12, 0);
					stmt2.setInt(13, 0);
					stmt2.setInt(14, 0);
					stmt2.setInt(15, 0);
					stmt2.setInt(16, 0);
					stmt2.setInt(17, base.getLojaDestino());
					stmt2.setInt(18, 1);
					stmt2.setInt(19, 0);
					stmt2.setInt(20, 0);
					stmt2.setInt(21, 0);
					stmt2.setInt(22, 0);
					stmt2.setInt(23, 0);
					stmt2.setInt(24, 0);
					stmt2.setInt(25, 0);
					stmt2.setString(26, prd.getPrdno());
					stmt2.setString(27, prd.getGrade());
					stmt2.setInt(28, 0);
					stmt2.setInt(29, 0);
					stmt2.setInt(30, 0);
					stmt2.setInt(31, 0);
					stmt2.setInt(32, 0);
					stmt2.executeUpdate();
					qtd = (qtd.intValue() > 0) ? qtd : qtd.multiply(new BigDecimal(-1));
					valorTotal = valorTotal.add(qtd.multiply(prd.getCusto()));
				}
			}
			if (valorTotal.compareTo(BigDecimal.ZERO) > 0) {
				StringBuilder sql3 = new StringBuilder();
				sql3.append(
								"INSERT INTO `nf` (`xano`, `nfno`, `custno`, `issuedate`, `delivdate`, `sec_amt`, `fre_amt`, `netamt`, `grossamt`, `discount`, `icms_amt`, `tax_paid`, `ipi_amt`, `base_calculo_ipi`, `iss_amt`, `base_iss_amt`, `isento_amt`, `subst_amt`, `baseIcmsSubst`, `icmsSubst`, `vol_no`, `vol_qtty`, `cfo`, `invno`, `cfo2`, `auxLong1`, `auxLong2`, `auxLong3`, `auxLong4`, `auxMy1`, `auxMy2`, `auxMy3`, `auxMy4`, `eordno`, `l1`, `l2`, `l3`, `l4`, `l5`, `l6`, `l7`, `l8`, `m1`, `m2`, `m3`, `m4`, `m5`, `m6`, `m7`, `m8`, `vol_gross`, `vol_net`, `mult`, `storeno`, `pdvno`, `carrno`, `empno`, `status`, `natopno`, `xatype`, `storeno_from`, `tipo`, `padbits`, `bits`, `usernoCancel`, `custno_addno`, `empnoDiscount`, `auxShort1`, `auxShort2`, `auxShort3`, `auxShort4`, `auxShort5`, `paymno`, `s1`, `s2`, `s3`, `s4`, `s5`, `s6`, `s7`, `s8`, `nfse`, `ship_by`, `vol_make`, `vol_kind`, `remarks`, `padbyte`, `print_remarks`, `remarksCancel`, `c1`, `c2`, `wshash`) VALUES ( ?,  ?,  ?,  ?,  ?,  0,  0,  ?,  ?,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  5949,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  ?,  0,  0,  1,  0,  7,  0,  0,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  '66',  '',  '',  '',  ?,  '',  ?,  '',  '',  '',  ''); ");
				PreparedStatement stmt3 = cx.prepareStatement(sql3.toString());
				stmt3.setInt(1, transacaoNF);
				stmt3.setInt(2, numnf);
				stmt3.setInt(3, cliente);
				stmt3.setInt(4, Integer.valueOf(sdf.format(new Date())));
				stmt3.setInt(5, Integer.valueOf(sdf.format(new Date())));
				stmt3.setInt(6, valorTotal.multiply(new BigDecimal(1)).intValue());
				stmt3.setInt(7, valorTotal.multiply(new BigDecimal(100)).intValue());
				stmt3.setInt(8, base.getLojaDestino());
				stmt3.setString(9, transacaoEstoque.toString());
				stmt3.setString(10, "CNF");
				stmt3.executeUpdate();
				for (Produtos prd : lista) {
					StringBuilder sql4 = new StringBuilder();
					BigDecimal qtd = BigDecimal.ZERO;
					qtd = prd.getQtdConsiderada();
					sql4.append(
									"INSERT INTO `xaprd` (`xano`, `nfno`, `price`, `date`, `qtty`, `storeno`, `pdvno`, `prdno`, `grade`, `nfse`, `padbyte`, `wshash`) VALUES ( ?,  ?,  ?,  ?,  ?,  ?,  0,  ?,  ?,  '66' ,  0,  ''); ");
					PreparedStatement stmt4 = cx.prepareStatement(sql4.toString());
					stmt4.setInt(1, transacaoNF);
					stmt4.setInt(2, numnf);
					stmt4.setInt(3, prd.getCusto().multiply(new BigDecimal(100)).intValue());
					stmt4.setInt(4, Integer.valueOf(sdf.format(new Date())));
					stmt4.setInt(5, (qtd.intValue() > 0) ? qtd.intValue() : (qtd.intValue() * -1));
					stmt4.setInt(6, base.getLojaDestino());
					stmt4.setString(7, prd.getPrdno());
					stmt4.setString(8, prd.getGrade());
					stmt4.executeUpdate();
				}
				for (Produtos prd : lista) {
					StringBuilder sql5 = new StringBuilder();
					BigDecimal qtd = BigDecimal.ZERO;
					qtd = prd.getQtdConsiderada();
					sql5.append(
									"UPDATE stk SET stk.qtty_varejo = (stk.qtty_varejo + ?),                 stk.qtty_atacado =  ?   WHERE ( stk.storeno = ? ) AND ( stk.prdno = ? ) AND ( stk.grade = ? ) ");
					PreparedStatement stmt5 = cx.prepareStatement(sql5.toString());
					stmt5.setBigDecimal(1, qtd.multiply(new BigDecimal(1000)));
					stmt5.setBigDecimal(2, prd.getQtdNfForn().multiply(new BigDecimal(1000)));
					stmt5.setInt(3, base.getLojaDestino());
					stmt5.setString(4, prd.getPrdno());
					stmt5.setString(5, prd.getGrade());
					stmt5.executeUpdate();
				}
				if (!base.getIncluiNfFornecedor()) {
					StringBuilder sql7 = new StringBuilder();
					sql7.append(
									"UPDATE nf  INNER JOIN custp ON (custp.no = nf.custno)  INNER JOIN vend ON (custp.cpf_cgc = vend.cgc)  LEFT  JOIN store ON (store.cgc = custp.cpf_cgc)  SET nf.c1 = '1'           WHERE (nf.storeno = ? )  AND   (nf.nfse = '66')  AND   (nf.tipo = 2)  AND   (store.name IS NULL)  AND   (nf.c1 <> '1')  AND   (nf.cfo in (6949,5949)) ");
					if (!base.getFornecedorNf().equals("")) {
						sql7.append(" AND   (vend.no = ? ) ");
					}
					if (base.getDtNfIni() != 0) {
						sql7.append(" AND   (nf.issuedate between ? AND ?) ");
					}
					PreparedStatement stmt7 = cx.prepareStatement(sql7.toString());
					stmt7.setInt(1, base.getLojaDestino());
					int index = 1;
					if (!base.getFornecedorNf().equals("")) {
						index++;
						stmt7.setInt(index, Integer.valueOf(base.getFornecedorNf()));
					}
					if (base.getDtNfIni() != 0) {
						index++;
						stmt7.setInt(index, base.getDtNfIni());
						index++;
						stmt7.setInt(index, base.getDtNfFim());
					}
					stmt7.executeUpdate();
				}
			}
			return numnf;
		} catch (SQLException se) {
			cx.rollback();
			JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:" + se);
			se.printStackTrace();
			throw new RuntimeException(se);
		} catch (Exception e) {
			cx.rollback();
			JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:" + e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Integer gerarSaida(Base base) throws Exception {
		Connection cx = null;
		List<Produtos> lista = new ArrayList<>();
		Integer cliente = 0;
		Integer fornecedor = 0;
		cliente = buscaCliente(base.getLojaDestino());
		fornecedor = buscaFornecedor(base.getLojaDestino());
		Integer numeroNF = pegaNumNf(base.getLojaDestino());
		Integer transacaoEstoque = pegaTransacao();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			cx = getConnection();
			cx.setAutoCommit(false);
			lista = listar(base);
			BigDecimal valorTotal = BigDecimal.ZERO;
			for (Produtos prd : lista) {
				StringBuilder sql1 = new StringBuilder();
				StringBuilder sql2 = new StringBuilder();
				BigDecimal qtd = BigDecimal.ZERO;
				qtd = prd.getQtdConsiderada();
				if (qtd.compareTo(BigDecimal.ZERO) > 0) {
					qtd.multiply(new BigDecimal(-1));
				}
				if (qtd.compareTo(BigDecimal.ZERO) != 0) {
					sql1.append(
									"INSERT INTO stkmov (xano,qtty,date,cm_fiscal,cm_real,storeno,bits,prdno,grade,remarks)  values (?,?,?,?,?,?,?,?,?,?) ");
					PreparedStatement stmt1 = cx.prepareStatement(sql1.toString());
					stmt1.setInt(1, transacaoEstoque);
					stmt1.setInt(2, qtd.multiply(new BigDecimal("1000")).intValue());
					stmt1.setInt(3, Integer.valueOf(sdf.format(new Date())));
					stmt1.setLong(4, prd.getCusto().multiply(new BigDecimal("10000")).longValue());
					stmt1.setLong(5, prd.getCusto().multiply(new BigDecimal("10000")).longValue());
					stmt1.setInt(6, base.getLojaDestino());
					stmt1.setInt(7, 1);
					stmt1.setString(8, prd.getPrdno());
					stmt1.setString(9, prd.getGrade());
					stmt1.setString(10, "CONFERENCIA DE ESTOQUE");
					stmt1.executeUpdate();
					sql2.append(
									"INSERT INTO stkmovh (xano,qtty,date,nfno,cm_fiscal,cm_real,auxLong1,auxLong2,  auxLong3,auxLong4,auxLong5,auxMy1,auxMy2,auxMy3,auxMy4,auxMy5,storeno,userno,  tipo,bits,auxShort1,auxShort2,auxShort3,auxShort4,auxShort5,prdno,grade,nfse,  auxStr1,auxStr2,auxStr3,auxStr4)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
					PreparedStatement stmt2 = cx.prepareStatement(sql2.toString());
					stmt2.setInt(1, transacaoEstoque);
					stmt2.setInt(2, qtd.multiply(new BigDecimal("1000")).intValue());
					stmt2.setInt(3, Integer.valueOf(sdf.format(new Date())));
					stmt2.setInt(4, 0);
					stmt2.setLong(5, prd.getCusto().multiply(new BigDecimal("10000")).longValue());
					stmt2.setLong(6, prd.getCusto().multiply(new BigDecimal("10000")).longValue());
					stmt2.setInt(7, 0);
					stmt2.setInt(8, 0);
					stmt2.setInt(9, 0);
					stmt2.setInt(10, 0);
					stmt2.setInt(11, 0);
					stmt2.setInt(12, 0);
					stmt2.setInt(13, 0);
					stmt2.setInt(14, 0);
					stmt2.setInt(15, 0);
					stmt2.setInt(16, 0);
					stmt2.setInt(17, base.getLojaDestino());
					stmt2.setInt(18, 1);
					stmt2.setInt(19, 0);
					stmt2.setInt(20, 0);
					stmt2.setInt(21, 0);
					stmt2.setInt(22, 0);
					stmt2.setInt(23, 0);
					stmt2.setInt(24, 0);
					stmt2.setInt(25, 0);
					stmt2.setString(26, prd.getPrdno());
					stmt2.setString(27, prd.getGrade());
					stmt2.setInt(28, 0);
					stmt2.setInt(29, 0);
					stmt2.setInt(30, 0);
					stmt2.setInt(31, 0);
					stmt2.setInt(32, 0);
					stmt2.executeUpdate();
					qtd = (qtd.intValue() > 0) ? qtd : qtd.multiply(new BigDecimal(-1));
					valorTotal = valorTotal.add(qtd.multiply(prd.getCusto()));
				}
			}
			if (valorTotal.compareTo(BigDecimal.ZERO) > 0) {
				StringBuilder sql3 = new StringBuilder();
				sql3.append(
								"insert into inv (vendno,ordno,xfrno,issue_date,date,comp_date,ipi,  icm,freight,netamt,grossamt,subst_trib,discount,prdamt,despesas,  base_ipi,aliq,cfo,nfNfno,auxLong1,auxLong2,auxMoney1,auxMoney2,  dataSaida,amtServicos,amtIRRF,amtINSS,amtISS,auxMoney3,auxMoney4,  auxMoney5,auxLong3,auxLong4,auxLong5,auxLong6,auxLong7,auxLong8,  auxLong9,auxLong10,auxLong11,auxLong12,auxMoney6,auxMoney7,auxMoney8,  auxMoney9,auxMoney10,auxMoney11,auxMoney12,auxMoney13,l1,l2,l3,  l4,l5,l6,l7,l8,m1,m2,m3,m4,m5,m6,m7,m8,weight,carrno,  packages,storeno,indxno,book_bits,type,usernoFirst,usernoLast,  nfStoreno,bits,padbyte,auxShort1,auxShort2,auxShort3,auxShort4,  auxShort5,auxShort6,auxShort7,auxShort8,auxShort9,auxShort10,  auxShort11,auxShort12,auxShort13,auxShort14,bits2,bits3,bits4,  bits5,s1,s2,s3,s4,s5,s6,s7,s8,nfname,invse,account,  remarks,contaCredito,contaDebito,nfNfse,auxStr1,auxStr2,auxStr3,  auxStr4,auxStr5,auxStr6,c1,c2) values (?,0,0,?,?,?,0,0,0,0,?,0,0,?,0,0,0,1949,0,0,0,0,0,?,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,?,0,3,4,0,0,0,35,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,?,?,'2.01.20','','','','',?,?,'','','','','','') ");
				PreparedStatement stmt3 = cx.prepareStatement(sql3.toString());
				stmt3.setInt(1, fornecedor);
				stmt3.setInt(2, Integer.valueOf(sdf.format(new Date())));
				stmt3.setInt(3, Integer.valueOf(sdf.format(new Date())));
				stmt3.setInt(4, Integer.valueOf(sdf.format(new Date())));
				stmt3.setInt(5, valorTotal.multiply(new BigDecimal(100)).intValue());
				stmt3.setInt(6, valorTotal.multiply(new BigDecimal(100)).intValue());
				stmt3.setInt(7, Integer.valueOf(sdf.format(new Date())));
				stmt3.setInt(8, base.getLojaDestino());
				stmt3.setString(9, String.valueOf(numeroNF));
				stmt3.setString(10, "66");
				stmt3.setString(11, "CNF");
				stmt3.setString(12, transacaoEstoque.toString());
				stmt3.executeUpdate();
				StringBuilder sql4 = new StringBuilder();
				sql4.append(
								"SELECT invno FROM inv WHERE storeno = ? and nfname = ? and vendno = ? and invse = 66 ");
				PreparedStatement stmt4 = cx.prepareStatement(sql4.toString());
				stmt4.setInt(1, base.getLojaDestino());
				stmt4.setInt(2, numeroNF);
				stmt4.setInt(3, fornecedor);
				stmt4.executeQuery();
				ResultSet rs = stmt4.getResultSet();
				Integer invno = 0;
				if (rs.next()) {
					invno = rs.getInt("invno");
				}
				Integer i = 0;
				for (Produtos prd : lista) {
					StringBuilder sql5 = new StringBuilder();
					BigDecimal qtd = BigDecimal.ZERO;
					qtd = prd.getQtdConsiderada();
					sql5.append(
									"INSERT INTO `iprd` (`invno`, `qtty`, `fob`, `cost`, `date`, `ipi`, `auxLong1`, `auxLong2`, `frete`, `seguro`, `despesas`, `freteIpi`, `qttyRessar`, `baseIcmsSubst`, `icmsSubst`, `icms`, `discount`, `fob4`, `cost4`, `icmsAliq`, `cfop`, `auxLong3`, `auxLong4`, `auxLong5`, `auxMy1`, `auxMy2`, `auxMy3`, `baseIcms`, `baseIpi`, `ipiAmt`, `reducaoBaseIcms`, `lucroTributado`, `l1`, `l2`, `l3`, `l4`, `l5`, `l6`, `l7`, `l8`, `m1`, `m2`, `m3`, `m4`, `m5`, `m6`, `m7`, `m8`, `storeno`, `bits`, `auxShort1`, `auxShort2`, `taxtype`, `auxShort3`, `auxShort4`, `auxShort5`, `seqno`, `bits2`, `bits3`, `bits4`, `s1`, `s2`, `s3`, `s4`, `s5`, `s6`, `s7`, `s8`, `prdno`, `grade`, `auxChar`, `auxChar2`, `cstIcms`, `cstIpi`, `c1`) VALUES (?,  ?,  ?,  ?,  ?,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1949,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  ?,  0,  0,  0,  0,  0,  0,  0,  ?,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  ?,  ?,  '',  '',  '090',  '',  '');");
					PreparedStatement stmt5 = cx.prepareStatement(sql5.toString());
					stmt5.setInt(1, invno);
					stmt5.setInt(2, (qtd.intValue() > 0) ? qtd.multiply(new BigDecimal(1000)).intValue() : qtd
									.multiply(new BigDecimal(-1000)).intValue());
					stmt5.setInt(3, prd.getCusto().multiply(new BigDecimal(100)).intValue());
					stmt5.setInt(4, prd.getCusto().multiply(new BigDecimal(100)).intValue());
					stmt5.setInt(5, Integer.valueOf(sdf.format(new Date())));
					stmt5.setInt(6, base.getLojaDestino());
					Integer integer1 = i, integer2 = i = i.intValue() + 1;
					stmt5.setInt(7, i);
					stmt5.setString(8, prd.getPrdno());
					stmt5.setString(9, prd.getGrade());
					stmt5.executeUpdate();
				}
				for (Produtos prd : lista) {
					StringBuilder sql6 = new StringBuilder();
					BigDecimal qtd = BigDecimal.ZERO;
					qtd = prd.getQtdConsiderada();
					sql6.append(
									"UPDATE stk SET stk.qtty_varejo = (stk.qtty_varejo + ? ),                 stk.qtty_atacado = ( ? )  WHERE ( stk.storeno = ? ) AND ( stk.prdno = ? ) AND ( stk.grade = ? ) ");
					PreparedStatement stmt6 = cx.prepareStatement(sql6.toString());
					stmt6.setBigDecimal(1, qtd.multiply(new BigDecimal(1000)));
					stmt6.setBigDecimal(2, prd.getQtdNfForn().multiply(new BigDecimal(1000)));
					stmt6.setInt(3, base.getLojaDestino());
					stmt6.setString(4, prd.getPrdno());
					stmt6.setString(5, prd.getGrade());
					stmt6.executeUpdate();
				}
				if (!base.getIncluiNfFornecedor()) {
					StringBuilder sql7 = new StringBuilder();
					sql7.append(
									"UPDATE nf  INNER JOIN custp ON (custp.no = nf.custno)  INNER JOIN vend ON (custp.cpf_cgc = vend.cgc)  LEFT  JOIN store ON (store.cgc = custp.cpf_cgc)  SET nf.c1 = '1'           WHERE (nf.storeno = ? )  AND   (nf.nfse = '66' )  AND   (nf.tipo = 2)  AND   (store.name IS NULL)  AND   (nf.c1 <> '1')  AND   (nf.cfo in (6949,5949)) ");
					if (!base.getFornecedorNf().equals("")) {
						sql7.append(" AND   (vend.no = ? ) ");
					}
					if (base.getDtNfIni() != 0) {
						sql7.append(" AND   (nf.issuedate between ? AND ?) ");
					}
					PreparedStatement stmt7 = cx.prepareStatement(sql7.toString());
					stmt7.setInt(1, base.getLojaDestino());
					int index = 1;
					if (!base.getFornecedorNf().equals("")) {
						index++;
						stmt7.setInt(index, Integer.valueOf(base.getFornecedorNf()));
					}
					if (base.getDtNfIni() != 0) {
						index++;
						stmt7.setInt(index, base.getDtNfIni());
						index++;
						stmt7.setInt(index, base.getDtNfFim());
					}
					stmt7.executeUpdate();
				}
			}
			return numeroNF;
		} catch (SQLException se) {
			cx.rollback();
			JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:" + se);
			se.printStackTrace();
			throw new RuntimeException(se);
		} catch (Exception e) {
			cx.rollback();
			JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:" + e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}


