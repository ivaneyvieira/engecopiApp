package br.com.engecopi.app.model

import kotlin.Throws
import java.lang.Exception
import javax.swing.JOptionPane
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.math.BigDecimal
import java.math.RoundingMode.CEILING
import java.sql.Connection
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*

class GestorDADOS(val connection: Connection) {
  @Throws(Exception::class)
  fun pegaTransacao(): Int? {
    var transacao = 0
    val cx = connection
    cx.autoCommit = false
    val sql1 = "SELECT (MAX(xano) + 1) AS xano FROM xa "
    val sql2 = "INSERT INTO xa VALUES (?)"
    val stmt = cx.prepareStatement(sql1)
    val rs = stmt.executeQuery()
    if(rs.next()) {
      try {
        rs.findColumn("xano")
      } catch(ignored: Exception) {
      }
      val stmt2 = cx.prepareStatement(sql2)
      stmt2.setLong(1, rs.getInt("xano").toLong())
      stmt2.executeUpdate()
      transacao = rs.getInt("xano")
      cx.commit()
      return transacao
    }
    return null
  }
  
  @Throws(Exception::class)
  fun pegaNumNf(loja: Int?): Int? {
    val numNf: Int
    val cx = connection
    cx.autoCommit = false
    val sql1 = "SELECT (MAX(no) + 1) AS numero FROM lastno WHERE storeno = ? AND se = 66"
    val sql2 = "INSERT INTO lastno (no,storeno,dupse,se,padbyte) VALUES (?,?,0,'66','')"
    val stmt = cx.prepareStatement(sql1)
    stmt.setInt(1, loja!!)
    val rs = stmt.executeQuery()
    if(rs.next()) {
      try {
        rs.findColumn("numero")
      } catch(ignored: Exception) {
      }
      val stmt2 = cx.prepareStatement(sql2)
      stmt2.setInt(1, rs.getInt("numero"))
      stmt2.setInt(2, loja)
      stmt2.executeUpdate()
      numNf = rs.getInt("numero")
      cx.commit()
      return numNf
    }
    return null
  }
  
  @Throws(Exception::class)
  fun buscaFornecedor(lojaid: Int?): Int {
    val sql1 = StringBuilder()
    var codFornecedor = 0
    sql1.append(" SELECT vend.no AS no  FROM vend  inner join store on (vend.cgc = store.cgc)  where store.no = ? ")
    val stmt1 = connection.prepareStatement(sql1.toString())
    stmt1.setInt(1, lojaid!!)
    stmt1.executeQuery()
    val rs1 = stmt1.resultSet
    while(rs1.next()) {
      codFornecedor = rs1.getInt("no")
    }
    return codFornecedor
  }
  
  @Throws(Exception::class)
  fun buscaCliente(lojaid: Int?): Int {
    val sql1 = StringBuilder()
    var codCliente = 0
    sql1.append(" SELECT custp.no AS no  FROM custp  inner join store on (custp.cpf_cgc = store.cgc)  where store.no = ? ")
    val stmt1 = connection.prepareStatement(sql1.toString())
    stmt1.setInt(1, lojaid!!)
    stmt1.executeQuery()
    val rs1 = stmt1.resultSet
    while(rs1.next()) {
      codCliente = rs1.getInt("no")
    }
    return codCliente
  }
  
  @Throws(Exception::class)
  fun listar(base: Base): List<Produtos> {
    val listagem: MutableList<Produtos> = ArrayList()
    val sql1 = StringBuilder()
    val sql2 = StringBuilder()
    var colunaCusto = ""
    var colunaQtdNotas = ""
    colunaCusto = if(base.tipoCusto == "est") {
      "     , ifnull(stk.cm_varejo,0) AS ultimocusto"
    }
    else {
      "     , ifnull(stk.cm_real,0) AS ultimocusto"
    }
    colunaQtdNotas = if(base.incluiNfFornecedor!!) {
      " 0 "
    }
    else if(base.operacao == "entrada") {
      " ifnull(y.qtty*1000,0) "
    }
    else {
      " ifnull(y.qtty*(-1000),0) "
    }
    sql1.append("SELECT z.prdno, z.grade, z.descricao, z.fornecedor, z.centrodelucro, z.tipo, z.qttynfs, z.qttyatacado, (z.qttyatacado - z.qttynfs) qttyconsiderada, z.ultimocusto, ")
    sql1.append(" (((z.qttyatacado - z.qttynfs)/1000) * z.ultimocusto) AS total ")
    sql1.append(" FROM ( ")
    sql1.append(" SELECT x.prdno, x.grade, x.descricao, x.fornecedor, x.centrodelucro, x.tipo, ")
    sql1.append(colunaQtdNotas)
    sql1.append(" qttynfs, x.qttyatacado, x.ultimocusto ")
    sql1.append(" FROM ( ")
    sql1.append(" SELECT stk.prdno AS prdno      , stk.grade AS grade      , prd.name AS descricao      , prd.mfno AS fornecedor      , LPAD(prd.clno,6,'0') AS centrodelucro      , prd.typeno AS tipo     , stk.qtty_atacado AS qttyatacado ")
    sql1.append(colunaCusto)
    sql1.append(" FROM stk  INNER JOIN prd on (stk.prdno = prd.no)  LEFT JOIN prp on (stk.prdno = prp.prdno AND                    stk.storeno = prp.storeno) LEFT JOIN prdloc on (prdloc.prdno = stk.prdno and                                   prdloc.grade = stk.grade) WHERE stk.storeno = ?  AND dereg&POW(2,2) <> POW(2,2) ")
    if(base.operacao == "entrada") {
      sql1.append(" AND (qtty_atacado > 0 )")
    }
    else {
      sql1.append(" AND (qtty_atacado < 0 )")
    }
    sql2.append(sql1.toString())
    sql2.append(" GROUP BY 1,2 ")
    sql2.append(" ) AS x")
    var sqlNotas =
      " LEFT JOIN ( SELECT  xaprd.prdno        , xaprd.grade        , SUM(xaprd.qtty) qtty  FROM  nf  INNER JOIN xaprd ON (nf.storeno = xaprd.storeno AND  nf.pdvno = xaprd.pdvno AND  nf.xano = xaprd.xano)  INNER JOIN custp ON (custp.no = nf.custno)  INNER JOIN vend ON (custp.cpf_cgc = vend.cgc) LEFT  JOIN store ON (store.cgc = custp.cpf_cgc)  WHERE (nf.storeno = ? )  AND   (nf.nfse = '66')  AND   (nf.tipo = 2)  AND   (store.name IS NULL)  AND   (nf.cfo in (6949,5949))  AND   (nf.c1 <> '1')"
    if(base.fornecedorNf != "") {
      sqlNotas = "$sqlNotas AND   (vend.no = ? ) "
    }
    if(base.dtNfIni != 0) {
      sqlNotas = "$sqlNotas AND   (nf.issuedate between ? AND ?) "
    }
    sqlNotas = sqlNotas + " GROUP BY xaprd.prdno, xaprd.grade  ) y ON (x.prdno = y.prdno and x.grade = y.grade) "
    if(!base.incluiNfFornecedor!!) {
      sql2.append(sqlNotas)
    }
    sql2.append(" ) z ")
    var filtro1 = false
    var filtro2 = false
    var filtro3 = false
    var filtro4 = false
    var filtro5 = false
    var filtro6 = false
    var filtro7 = false
    var filtro8 = false
    if(base.codprd == "") {
      filtro1 = false
    }
    else {
      filtro1 = true
      sql1.append(" AND (ROUND(prd.no) = ? ) ")
    }
    if(base.descIni == "") {
      filtro2 = false
    }
    else {
      filtro2 = true
      sql1.append(" AND (MID(prd.name,1,3) BETWEEN ? AND ? ) ")
    }
    if(base.fornecedores == "") {
      filtro3 = false
    }
    else {
      filtro3 = true
      sql1.append(" AND (prd.mfno IN ( ").append(base.fornecedores).append(" )) ")
    }
    if(base.tipos == "") {
      filtro4 = false
    }
    else {
      filtro4 = true
      sql1.append(" AND (prd.typeno IN ( ").append(base.tipos).append(" )) ")
    }
    if(base.areas == "") {
      filtro5 = false
    }
    else {
      filtro5 = true
      sql1.append(" AND (prdloc.storeno = ? ) ")
      sql1.append(" AND (prdloc.localizacao LIKE ? ) ")
    }
    if(base.centrodeLucro == "") {
      filtro6 = false
    }
    else {
      filtro6 = true
      if(base.centrodeLucro!!.endsWith("0000")) {
        sql1.append(" AND (prd.groupno = ? ) ")
      }
      else if(base.centrodeLucro!!.endsWith("00")) {
        sql1.append(" AND (prd.deptno = ? ) ")
      }
      else {
        sql1.append(" AND (prd.clno = ? ) ")
      }
    }
    if(base.sinalQtd == "todos") {
      filtro7 = false
    }
    else {
      filtro7 = true
      when(base.sinalQtd) {
        ">"  -> sql1.append(" AND (stk.qtty_atacado > ? ) ")
        "<"  -> sql1.append(" AND (stk.qtty_atacado < ? ) ")
        "="  -> sql1.append(" AND (stk.qtty_atacado = ? ) ")
        else -> sql1.append(" AND (stk.qtty_atacado BETWEEN ? AND ? ) ")
      }
    }
    if(base.sinalCusto == "todos") {
      filtro8 = false
    }
    else {
      filtro8 = true
      when(base.sinalCusto) {
        ">"  -> sql1.append(" AND (stk.last_cost > ? ) ")
        "<"  -> sql1.append(" AND (stk.last_cost < ? ) ")
        "="  -> sql1.append(" AND (stk.last_cost = ? ) ")
        else -> sql1.append(" AND (stk.last_cost BETWEEN ? AND ? ) ")
      }
    }
    sql1.append(" GROUP BY 1,2 ")
    sql1.append(" ) AS x")
    if(!base.incluiNfFornecedor!!) {
      sql1.append(sqlNotas)
    }
    sql1.append(" ) z")
    var sql = ""
    sql = if(base.usaFiltrosProduto!!) {
      sql1.toString()
    }
    else {
      sql2.toString()
    }
    val stmt = connection.prepareStatement(sql)
    stmt.setInt(1, base.lojaDestino!!)
    var index = 1
    if(filtro1) {
      index++
      stmt.setString(index, base.codprd)
    }
    if(filtro2) {
      index++
      stmt.setString(index, base.descIni)
      index++
      stmt.setString(index, base.descFim)
    }
    if(filtro3);
    if(filtro4);
    if(filtro5) {
      index++
      stmt.setInt(index, base.lojaArea!!)
      index++
      stmt.setString(index, "%" + base.areas + "%")
    }
    if(filtro6) {
      index++
      stmt.setInt(index, base.centrodeLucro!!.toInt())
    }
    if(filtro7) {
      when(base.sinalQtd) {
        ">"  -> {
          index++
          val qtd1 = base.qtd1!!.multiply(BigDecimal("1000"))
          stmt.setLong(index, qtd1.toLong())
        }
        "<"  -> {
          index++
          val qtd1 = base.qtd1!!.multiply(BigDecimal("1000"))
          stmt.setLong(index, qtd1.toLong())
        }
        "="  -> {
          index++
          val qtd1 = base.qtd1!!.multiply(BigDecimal("1000"))
          stmt.setLong(index, qtd1.toLong())
        }
        else -> {
          index++
          val qtd1 = base.qtd1!!.multiply(BigDecimal("1000"))
          stmt.setLong(index, qtd1.toLong())
          index++
          val qtd2 = base.qtd2!!.multiply(BigDecimal("1000"))
          stmt.setLong(index, qtd2.toLong())
        }
      }
    }
    if(filtro8) {
      when(base.sinalCusto) {
        ">"  -> {
          index++
          val custo1 = base.custo1!!.multiply(BigDecimal("10000"))
          stmt.setLong(index, custo1.toLong())
        }
        "<"  -> {
          index++
          val custo1 = base.custo1!!.multiply(BigDecimal("10000"))
          stmt.setLong(index, custo1.toLong())
        }
        "="  -> {
          index++
          val custo1 = base.custo1!!.multiply(BigDecimal("10000"))
          stmt.setLong(index, custo1.toLong())
        }
        else -> {
          index++
          val custo1 = base.custo1!!.multiply(BigDecimal("10000"))
          stmt.setLong(index, custo1.toLong())
          index++
          val custo2 = base.custo2!!.multiply(BigDecimal("10000"))
          stmt.setLong(index, custo2.toLong())
        }
      }
    }
    if(!base.incluiNfFornecedor!!) {
      index++
      stmt.setInt(index, base.lojaDestino!!)
      if(base.fornecedorNf != "") {
        index++
        stmt.setInt(index, Integer.valueOf(base.fornecedorNf))
      }
      if(base.dtNfIni != 0) {
        index++
        stmt.setInt(index, base.dtNfIni!!)
        index++
        stmt.setInt(index, base.dtNfFim!!)
      }
    }
    stmt.executeQuery()
    val rs = stmt.resultSet
    while(rs.next()) {
      if(rs.findColumn("prdno") > 0) {
        val prd = Produtos()
        prd.prdno = rs.getString("prdno")
        prd.grade = rs.getString("grade")
        prd.descricao = rs.getString("descricao")
        prd.fornecedor = rs.getLong("fornecedor")
        prd.centrodelucro = rs.getString("centrodelucro")
        prd.tipo = rs.getLong("tipo")
        prd.qtdNfForn = rs.getBigDecimal("qttynfs").divide(BigDecimal("1000")).setScale(4, CEILING)
        prd.qtdAtacado = rs.getBigDecimal("qttyatacado").divide(BigDecimal("1000")).setScale(4, CEILING)
        prd.qtdConsiderada = rs.getBigDecimal("qttyconsiderada").divide(BigDecimal("1000")).setScale(4, CEILING)
        prd.custo = rs.getBigDecimal("ultimocusto").divide(BigDecimal("10000")).setScale(4, CEILING)
        prd.total = rs.getBigDecimal("total").divide(BigDecimal("10000")).setScale(4, CEILING)
        listagem.add(prd)
      }
    }
    stmt.close()
    return listagem
  }
  
  @Throws(Exception::class)
  fun executar(base: Base): Int? {
    var pedido: Int? = 0
    pedido = if(base.operacao == "entrada") {
      gerarEntrada(base)
    }
    else {
      gerarSaida(base)
    }
    return pedido
  }
  
  @Throws(Exception::class)
  fun validarNfEntrada(loja: Int?, nota: Int?): Boolean {
    var ehValido = java.lang.Boolean.FALSE
    val sql = StringBuilder()
    sql.append(" SELECT count(*) qtd from nf  where storeno = ? and nfno = ? and print_remarks like ? and nfse = '66' ")
    val stmt = connection.prepareStatement(sql.toString())
    stmt.setInt(1, loja!!)
    stmt.setInt(2, nota!!)
    stmt.setString(3, "CNF")
    stmt.executeQuery()
    val rs = stmt.resultSet
    if(rs.next() && rs.getInt("qtd") > 0) {
      ehValido = java.lang.Boolean.TRUE
    }
    stmt.close()
    return ehValido
  }
  
  @Throws(Exception::class)
  fun validarNfSaida(loja: Int?, nota: Int?): Boolean {
    var ehValido = java.lang.Boolean.FALSE
    val sql = StringBuilder()
    sql.append("SELECT invno FROM inv WHERE storeno = ? AND nfname = ? AND  vendno = ? AND auxStr1 = ? AND invse = '66'")
    val stmt = connection.prepareStatement(sql.toString())
    stmt.setInt(1, loja!!)
    stmt.setInt(2, nota!!)
    stmt.setInt(3, buscaFornecedor(loja))
    stmt.setString(4, "CNF")
    stmt.executeQuery()
    val rs = stmt.resultSet
    if(rs.next() && rs.getInt("invno") > 0) {
      ehValido = java.lang.Boolean.TRUE
    }
    stmt.close()
    return ehValido
  }
  
  @Throws(Exception::class)
  fun desfazerEntrada(loja: Int?, nota: Int?) {
    var cx: Connection? = null
    try {
      cx = connection
      cx.autoCommit = false
      val sql1 = StringBuilder()
      val sql2 = StringBuilder()
      val sql3 = StringBuilder()
      val sql4 = StringBuilder()
      val sql5 = StringBuilder()
      val sql6 = StringBuilder()
      sql1.append("UPDATE stk  INNER JOIN xaprd ON (stk.storeno = xaprd.storeno AND  stk.prdno = xaprd.prdno AND  stk.grade = xaprd.grade)  SET stk.qtty_varejo = (stk.qtty_varejo - (xaprd.qtty * 1000)), stk.qtty_atacado = (stk.qtty_atacado + (xaprd.qtty * 1000))  WHERE stk.storeno = ? AND xaprd.nfno = ? AND xaprd.nfse = 66 ")
      val stmt1 = cx.prepareStatement(sql1.toString())
      stmt1.setInt(1, loja!!)
      stmt1.setInt(2, nota!!)
      stmt1.executeUpdate()
      sql2.append("SELECT xano, ROUND(remarks) as xastk FROM nf WHERE storeno = ? AND nfno = ? AND auxLong1 = ? AND nfse = 66")
      val stmt2 = cx.prepareStatement(sql2.toString())
      stmt2.setInt(1, loja)
      stmt2.setInt(2, nota)
      stmt2.setString(3, "CNF")
      stmt2.executeQuery()
      val rs = stmt2.resultSet
      var transacaoNF = 0
      var transacaoES: Int? = 0
      if(rs.next()) {
        transacaoNF = rs.getInt("xano")
        transacaoES = Integer.valueOf(rs.getString("xastk"))
      }
      sql3.append("DELETE FROM nf WHERE storeno = ? AND nfno = ? AND nfse = '66' AND xano = ? ")
      val stmt3 = cx.prepareStatement(sql3.toString())
      stmt3.setInt(1, loja)
      stmt3.setInt(2, nota)
      stmt3.setInt(3, transacaoNF)
      stmt3.executeUpdate()
      sql4.append("DELETE FROM xaprd WHERE storeno = ? AND nfno = ? AND nfse = '66' AND xano = ? ")
      val stmt4 = cx.prepareStatement(sql4.toString())
      stmt4.setInt(1, loja)
      stmt4.setInt(2, nota)
      stmt4.setInt(3, transacaoNF)
      stmt4.executeUpdate()
      sql5.append("DELETE FROM stkmov WHERE storeno = ? AND xano = ? ")
      val stmt5 = cx.prepareStatement(sql5.toString())
      stmt5.setInt(1, loja)
      stmt5.setInt(2, transacaoES!!)
      stmt5.executeUpdate()
      sql6.append("DELETE FROM stkmovh WHERE storeno = ? AND xano = ? ")
      val stmt6 = cx.prepareStatement(sql6.toString())
      stmt6.setInt(1, loja)
      stmt6.setInt(2, transacaoES)
      stmt6.executeUpdate()
      cx.commit()
    } catch(se: SQLException) {
      cx!!.rollback()
      JOptionPane.showMessageDialog(null, " Erro:$se")
      se.printStackTrace()
      throw RuntimeException(se)
    } catch(e: Exception) {
      cx!!.rollback()
      JOptionPane.showMessageDialog(null, " Erro:$e")
      throw RuntimeException(e)
    } finally {
    }
  }
  
  @Throws(SQLException::class)
  fun desfazerSaida(loja: Int?, nota: Int?) {
    var cx: Connection? = null
    try {
      cx = connection
      cx.autoCommit = false
      val sql1 = StringBuilder()
      val sql2 = StringBuilder()
      val sql3 = StringBuilder()
      val sql4 = StringBuilder()
      val sql5 = StringBuilder()
      val sql6 = StringBuilder()
      sql1.append("SELECT invno, auxStr2 FROM inv WHERE storeno = ? AND nfname = ? AND auxStr1 = ? AND invse = 66")
      val stmt1 = cx.prepareStatement(sql1.toString())
      stmt1.setInt(1, loja!!)
      stmt1.setInt(2, nota!!)
      stmt1.setString(3, "CNF")
      stmt1.executeQuery()
      val rs = stmt1.resultSet
      var invno = 0
      var transacao = 0
      if(rs.next()) {
        invno = rs.getInt("invno")
        transacao = rs.getInt("auxStr2")
      }
      sql2.append("UPDATE stk  INNER JOIN iprd ON (stk.storeno = iprd.storeno AND  stk.prdno = iprd.prdno AND  stk.grade = iprd.grade)  SET   stk.qtty_varejo = (stk.qtty_varejo + iprd.qtty), stk.qtty_atacado = (stk.qtty_atacado + (iprd.qtty * (-1)))  WHERE iprd.storeno = ? AND iprd.invno = ? ")
      val stmt2 = cx.prepareStatement(sql2.toString())
      stmt2.setInt(1, loja)
      stmt2.setInt(2, invno)
      stmt2.executeUpdate()
      sql3.append("DELETE FROM inv WHERE storeno = ? AND invno = ? AND invse = 66 ")
      val stmt3 = cx.prepareStatement(sql3.toString())
      stmt3.setInt(1, loja)
      stmt3.setInt(2, invno)
      stmt3.executeUpdate()
      sql4.append("DELETE FROM iprd WHERE storeno = ? AND invno = ? ")
      val stmt4 = cx.prepareStatement(sql4.toString())
      stmt4.setInt(1, loja)
      stmt4.setInt(2, invno)
      stmt4.executeUpdate()
      sql5.append("DELETE FROM stkmov WHERE storeno = ? AND xano = ? ")
      val stmt5 = cx.prepareStatement(sql5.toString())
      stmt5.setInt(1, loja)
      stmt5.setInt(2, transacao)
      stmt5.executeUpdate()
      sql6.append("DELETE FROM stkmovh WHERE storeno = ? AND xano = ? ")
      val stmt6 = cx.prepareStatement(sql6.toString())
      stmt6.setInt(1, loja)
      stmt6.setInt(2, transacao)
      stmt6.executeUpdate()
      cx.commit()
    } catch(se: SQLException) {
      cx!!.rollback()
      JOptionPane.showMessageDialog(null, " Erro:$se")
      se.printStackTrace()
      throw RuntimeException(se)
    } catch(e: Exception) {
      cx!!.rollback()
      JOptionPane.showMessageDialog(null, " Erro:$e")
      e.printStackTrace()
      throw RuntimeException(e)
    }
  }
  
  @Throws(Exception::class)
  fun gerarEntrada(base: Base): Int? {
    var cx: Connection? = null
    var cliente = 0
    var fornecedor = 0
    cliente = buscaCliente(base.lojaDestino)
    fornecedor = buscaFornecedor(base.lojaDestino)
    val transacaoNF = pegaTransacao()
    val transacaoEstoque = pegaTransacao()
    val numnf = pegaNumNf(base.lojaDestino)
    val sdf = SimpleDateFormat("yyyyMMdd")
    return try {
      cx = connection
      cx.autoCommit = false
      var lista: List<Produtos> = ArrayList()
      lista = listar(base)
      var valorTotal = BigDecimal.ZERO
      for(prd in lista) {
        val sql1 = StringBuilder()
        val sql2 = StringBuilder()
        var qtd = BigDecimal.ZERO
        qtd = prd.qtdConsiderada
        if(qtd.compareTo(BigDecimal.ZERO) < 0) {
          qtd.multiply(BigDecimal(-1))
        }
        if(qtd.compareTo(BigDecimal.ZERO) != 0) {
          sql1.append("INSERT INTO stkmov (xano,qtty,date,cm_fiscal,cm_real,storeno,bits,prdno,grade,remarks)  values (?,?,?,?,?,?,?,?,?,?) ")
          val stmt1 = cx.prepareStatement(sql1.toString())
          stmt1.setInt(1, transacaoEstoque!!)
          stmt1.setInt(2, qtd.multiply(BigDecimal("1000")).toInt())
          stmt1.setInt(3, Integer.valueOf(sdf.format(Date())))
          stmt1.setLong(4, prd.custo!!.multiply(BigDecimal("10000")).toLong())
          stmt1.setLong(5, prd.custo!!.multiply(BigDecimal("10000")).toLong())
          stmt1.setInt(6, base.lojaDestino!!)
          stmt1.setInt(7, 1)
          stmt1.setString(8, prd.prdno)
          stmt1.setString(9, prd.grade)
          stmt1.setString(10, "CONFERENCIA DE ESTOQUE")
          stmt1.executeUpdate()
          sql2.append("INSERT INTO stkmovh (xano,qtty,date,nfno,cm_fiscal,cm_real,auxLong1,auxLong2,  auxLong3,auxLong4,auxLong5,auxMy1,auxMy2,auxMy3,auxMy4,auxMy5,storeno,userno,  tipo,bits,auxShort1,auxShort2,auxShort3,auxShort4,auxShort5,prdno,grade,nfse,  auxStr1,auxStr2,auxStr3,auxStr4)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
          val stmt2 = cx.prepareStatement(sql2.toString())
          stmt2.setInt(1, transacaoEstoque)
          stmt2.setInt(2, qtd.multiply(BigDecimal("1000")).toInt())
          stmt2.setInt(3, Integer.valueOf(sdf.format(Date())))
          stmt2.setInt(4, 0)
          stmt2.setLong(5, prd.custo!!.multiply(BigDecimal("10000")).toLong())
          stmt2.setLong(6, prd.custo!!.multiply(BigDecimal("10000")).toLong())
          stmt2.setInt(7, 0)
          stmt2.setInt(8, 0)
          stmt2.setInt(9, 0)
          stmt2.setInt(10, 0)
          stmt2.setInt(11, 0)
          stmt2.setInt(12, 0)
          stmt2.setInt(13, 0)
          stmt2.setInt(14, 0)
          stmt2.setInt(15, 0)
          stmt2.setInt(16, 0)
          stmt2.setInt(17, base.lojaDestino!!)
          stmt2.setInt(18, 1)
          stmt2.setInt(19, 0)
          stmt2.setInt(20, 0)
          stmt2.setInt(21, 0)
          stmt2.setInt(22, 0)
          stmt2.setInt(23, 0)
          stmt2.setInt(24, 0)
          stmt2.setInt(25, 0)
          stmt2.setString(26, prd.prdno)
          stmt2.setString(27, prd.grade)
          stmt2.setInt(28, 0)
          stmt2.setInt(29, 0)
          stmt2.setInt(30, 0)
          stmt2.setInt(31, 0)
          stmt2.setInt(32, 0)
          stmt2.executeUpdate()
          qtd = if(qtd.toInt() > 0) qtd else qtd.multiply(BigDecimal(-1))
          valorTotal = valorTotal.add(qtd.multiply(prd.custo))
        }
      }
      if(valorTotal.compareTo(BigDecimal.ZERO) > 0) {
        val sql3 = StringBuilder()
        sql3.append("INSERT INTO `nf` (`xano`, `nfno`, `custno`, `issuedate`, `delivdate`, `sec_amt`, `fre_amt`, `netamt`, `grossamt`, `discount`, `icms_amt`, `tax_paid`, `ipi_amt`, `base_calculo_ipi`, `iss_amt`, `base_iss_amt`, `isento_amt`, `subst_amt`, `baseIcmsSubst`, `icmsSubst`, `vol_no`, `vol_qtty`, `cfo`, `invno`, `cfo2`, `auxLong1`, `auxLong2`, `auxLong3`, `auxLong4`, `auxMy1`, `auxMy2`, `auxMy3`, `auxMy4`, `eordno`, `l1`, `l2`, `l3`, `l4`, `l5`, `l6`, `l7`, `l8`, `m1`, `m2`, `m3`, `m4`, `m5`, `m6`, `m7`, `m8`, `vol_gross`, `vol_net`, `mult`, `storeno`, `pdvno`, `carrno`, `empno`, `status`, `natopno`, `xatype`, `storeno_from`, `tipo`, `padbits`, `bits`, `usernoCancel`, `custno_addno`, `empnoDiscount`, `auxShort1`, `auxShort2`, `auxShort3`, `auxShort4`, `auxShort5`, `paymno`, `s1`, `s2`, `s3`, `s4`, `s5`, `s6`, `s7`, `s8`, `nfse`, `ship_by`, `vol_make`, `vol_kind`, `remarks`, `padbyte`, `print_remarks`, `remarksCancel`, `c1`, `c2`, `wshash`) VALUES ( ?,  ?,  ?,  ?,  ?,  0,  0,  ?,  ?,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  5949,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  ?,  0,  0,  1,  0,  7,  0,  0,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  '66',  '',  '',  '',  ?,  '',  ?,  '',  '',  '',  ''); ")
        val stmt3 = cx.prepareStatement(sql3.toString())
        stmt3.setInt(1, transacaoNF!!)
        stmt3.setInt(2, numnf!!)
        stmt3.setInt(3, cliente)
        stmt3.setInt(4, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(5, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(6, valorTotal.multiply(BigDecimal(1)).toInt())
        stmt3.setInt(7, valorTotal.multiply(BigDecimal(100)).toInt())
        stmt3.setInt(8, base.lojaDestino!!)
        stmt3.setString(9, transacaoEstoque.toString())
        stmt3.setString(10, "CNF")
        stmt3.executeUpdate()
        for(prd in lista) {
          val sql4 = StringBuilder()
          var qtd = BigDecimal.ZERO
          qtd = prd.qtdConsiderada
          sql4.append("INSERT INTO `xaprd` (`xano`, `nfno`, `price`, `date`, `qtty`, `storeno`, `pdvno`, `prdno`, `grade`, `nfse`, `padbyte`, `wshash`) VALUES ( ?,  ?,  ?,  ?,  ?,  ?,  0,  ?,  ?,  '66' ,  0,  ''); ")
          val stmt4 = cx.prepareStatement(sql4.toString())
          stmt4.setInt(1, transacaoNF)
          stmt4.setInt(2, numnf)
          stmt4.setInt(3, prd.custo!!.multiply(BigDecimal(100)).toInt())
          stmt4.setInt(4, Integer.valueOf(sdf.format(Date())))
          stmt4.setInt(5, if(qtd.toInt() > 0) qtd.toInt() else qtd.toInt() * -1)
          stmt4.setInt(6, base.lojaDestino!!)
          stmt4.setString(7, prd.prdno)
          stmt4.setString(8, prd.grade)
          stmt4.executeUpdate()
        }
        for(prd in lista) {
          val sql5 = StringBuilder()
          var qtd = BigDecimal.ZERO
          qtd = prd.qtdConsiderada
          sql5.append("UPDATE stk SET stk.qtty_varejo = (stk.qtty_varejo + ?),                 stk.qtty_atacado =  ?   WHERE ( stk.storeno = ? ) AND ( stk.prdno = ? ) AND ( stk.grade = ? ) ")
          val stmt5 = cx.prepareStatement(sql5.toString())
          stmt5.setBigDecimal(1, qtd.multiply(BigDecimal(1000)))
          stmt5.setBigDecimal(2, prd.qtdNfForn!!.multiply(BigDecimal(1000)))
          stmt5.setInt(3, base.lojaDestino!!)
          stmt5.setString(4, prd.prdno)
          stmt5.setString(5, prd.grade)
          stmt5.executeUpdate()
        }
        if(!base.incluiNfFornecedor!!) {
          val sql7 = StringBuilder()
          sql7.append("UPDATE nf  INNER JOIN custp ON (custp.no = nf.custno)  INNER JOIN vend ON (custp.cpf_cgc = vend.cgc)  LEFT  JOIN store ON (store.cgc = custp.cpf_cgc)  SET nf.c1 = '1'           WHERE (nf.storeno = ? )  AND   (nf.nfse = '66')  AND   (nf.tipo = 2)  AND   (store.name IS NULL)  AND   (nf.c1 <> '1')  AND   (nf.cfo in (6949,5949)) ")
          if(base.fornecedorNf != "") {
            sql7.append(" AND   (vend.no = ? ) ")
          }
          if(base.dtNfIni != 0) {
            sql7.append(" AND   (nf.issuedate between ? AND ?) ")
          }
          val stmt7 = cx.prepareStatement(sql7.toString())
          stmt7.setInt(1, base.lojaDestino!!)
          var index = 1
          if(base.fornecedorNf != "") {
            index++
            stmt7.setInt(index, Integer.valueOf(base.fornecedorNf))
          }
          if(base.dtNfIni != 0) {
            index++
            stmt7.setInt(index, base.dtNfIni!!)
            index++
            stmt7.setInt(index, base.dtNfFim!!)
          }
          stmt7.executeUpdate()
        }
      }
      numnf
    } catch(se: SQLException) {
      cx!!.rollback()
      JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:$se")
      se.printStackTrace()
      throw RuntimeException(se)
    } catch(e: Exception) {
      cx!!.rollback()
      JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:$e")
      e.printStackTrace()
      throw RuntimeException(e)
    }
  }
  
  @Throws(Exception::class)
  fun gerarSaida(base: Base): Int? {
    var cx: Connection? = null
    var lista: List<Produtos> = ArrayList()
    var cliente = 0
    var fornecedor = 0
    cliente = buscaCliente(base.lojaDestino)
    fornecedor = buscaFornecedor(base.lojaDestino)
    val numeroNF = pegaNumNf(base.lojaDestino)
    val transacaoEstoque = pegaTransacao()
    val sdf = SimpleDateFormat("yyyyMMdd")
    return try {
      cx = connection
      cx.autoCommit = false
      lista = listar(base)
      var valorTotal = BigDecimal.ZERO
      for(prd in lista) {
        val sql1 = StringBuilder()
        val sql2 = StringBuilder()
        var qtd = BigDecimal.ZERO
        qtd = prd.qtdConsiderada
        if(qtd.compareTo(BigDecimal.ZERO) > 0) {
          qtd.multiply(BigDecimal(-1))
        }
        if(qtd.compareTo(BigDecimal.ZERO) != 0) {
          sql1.append("INSERT INTO stkmov (xano,qtty,date,cm_fiscal,cm_real,storeno,bits,prdno,grade,remarks)  values (?,?,?,?,?,?,?,?,?,?) ")
          val stmt1 = cx.prepareStatement(sql1.toString())
          stmt1.setInt(1, transacaoEstoque!!)
          stmt1.setInt(2, qtd.multiply(BigDecimal("1000")).toInt())
          stmt1.setInt(3, Integer.valueOf(sdf.format(Date())))
          stmt1.setLong(4, prd.custo!!.multiply(BigDecimal("10000")).toLong())
          stmt1.setLong(5, prd.custo!!.multiply(BigDecimal("10000")).toLong())
          stmt1.setInt(6, base.lojaDestino!!)
          stmt1.setInt(7, 1)
          stmt1.setString(8, prd.prdno)
          stmt1.setString(9, prd.grade)
          stmt1.setString(10, "CONFERENCIA DE ESTOQUE")
          stmt1.executeUpdate()
          sql2.append("INSERT INTO stkmovh (xano,qtty,date,nfno,cm_fiscal,cm_real,auxLong1,auxLong2,  auxLong3,auxLong4,auxLong5,auxMy1,auxMy2,auxMy3,auxMy4,auxMy5,storeno,userno,  tipo,bits,auxShort1,auxShort2,auxShort3,auxShort4,auxShort5,prdno,grade,nfse,  auxStr1,auxStr2,auxStr3,auxStr4)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
          val stmt2 = cx.prepareStatement(sql2.toString())
          stmt2.setInt(1, transacaoEstoque)
          stmt2.setInt(2, qtd.multiply(BigDecimal("1000")).toInt())
          stmt2.setInt(3, Integer.valueOf(sdf.format(Date())))
          stmt2.setInt(4, 0)
          stmt2.setLong(5, prd.custo!!.multiply(BigDecimal("10000")).toLong())
          stmt2.setLong(6, prd.custo!!.multiply(BigDecimal("10000")).toLong())
          stmt2.setInt(7, 0)
          stmt2.setInt(8, 0)
          stmt2.setInt(9, 0)
          stmt2.setInt(10, 0)
          stmt2.setInt(11, 0)
          stmt2.setInt(12, 0)
          stmt2.setInt(13, 0)
          stmt2.setInt(14, 0)
          stmt2.setInt(15, 0)
          stmt2.setInt(16, 0)
          stmt2.setInt(17, base.lojaDestino!!)
          stmt2.setInt(18, 1)
          stmt2.setInt(19, 0)
          stmt2.setInt(20, 0)
          stmt2.setInt(21, 0)
          stmt2.setInt(22, 0)
          stmt2.setInt(23, 0)
          stmt2.setInt(24, 0)
          stmt2.setInt(25, 0)
          stmt2.setString(26, prd.prdno)
          stmt2.setString(27, prd.grade)
          stmt2.setInt(28, 0)
          stmt2.setInt(29, 0)
          stmt2.setInt(30, 0)
          stmt2.setInt(31, 0)
          stmt2.setInt(32, 0)
          stmt2.executeUpdate()
          qtd = if(qtd.toInt() > 0) qtd else qtd.multiply(BigDecimal(-1))
          valorTotal = valorTotal.add(qtd.multiply(prd.custo))
        }
      }
      if(valorTotal.compareTo(BigDecimal.ZERO) > 0) {
        val sql3 = StringBuilder()
        sql3.append("insert into inv (vendno,ordno,xfrno,issue_date,date,comp_date,ipi,  icm,freight,netamt,grossamt,subst_trib,discount,prdamt,despesas,  base_ipi,aliq,cfo,nfNfno,auxLong1,auxLong2,auxMoney1,auxMoney2,  dataSaida,amtServicos,amtIRRF,amtINSS,amtISS,auxMoney3,auxMoney4,  auxMoney5,auxLong3,auxLong4,auxLong5,auxLong6,auxLong7,auxLong8,  auxLong9,auxLong10,auxLong11,auxLong12,auxMoney6,auxMoney7,auxMoney8,  auxMoney9,auxMoney10,auxMoney11,auxMoney12,auxMoney13,l1,l2,l3,  l4,l5,l6,l7,l8,m1,m2,m3,m4,m5,m6,m7,m8,weight,carrno,  packages,storeno,indxno,book_bits,type,usernoFirst,usernoLast,  nfStoreno,bits,padbyte,auxShort1,auxShort2,auxShort3,auxShort4,  auxShort5,auxShort6,auxShort7,auxShort8,auxShort9,auxShort10,  auxShort11,auxShort12,auxShort13,auxShort14,bits2,bits3,bits4,  bits5,s1,s2,s3,s4,s5,s6,s7,s8,nfname,invse,account,  remarks,contaCredito,contaDebito,nfNfse,auxStr1,auxStr2,auxStr3,  auxStr4,auxStr5,auxStr6,c1,c2) values (?,0,0,?,?,?,0,0,0,0,?,0,0,?,0,0,0,1949,0,0,0,0,0,?,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,?,0,3,4,0,0,0,35,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,?,?,'2.01.20','','','','',?,?,'','','','','','') ")
        val stmt3 = cx.prepareStatement(sql3.toString())
        stmt3.setInt(1, fornecedor)
        stmt3.setInt(2, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(3, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(4, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(5, valorTotal.multiply(BigDecimal(100)).toInt())
        stmt3.setInt(6, valorTotal.multiply(BigDecimal(100)).toInt())
        stmt3.setInt(7, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(8, base.lojaDestino!!)
        stmt3.setString(9, numeroNF.toString())
        stmt3.setString(10, "66")
        stmt3.setString(11, "CNF")
        stmt3.setString(12, transacaoEstoque.toString())
        stmt3.executeUpdate()
        val sql4 = StringBuilder()
        sql4.append("SELECT invno FROM inv WHERE storeno = ? and nfname = ? and vendno = ? and invse = 66 ")
        val stmt4 = cx.prepareStatement(sql4.toString())
        stmt4.setInt(1, base.lojaDestino!!)
        stmt4.setInt(2, numeroNF!!)
        stmt4.setInt(3, fornecedor)
        stmt4.executeQuery()
        val rs = stmt4.resultSet
        var invno = 0
        if(rs.next()) {
          invno = rs.getInt("invno")
        }
        var i = 0
        for(prd in lista) {
          val sql5 = StringBuilder()
          var qtd = BigDecimal.ZERO
          qtd = prd.qtdConsiderada
          sql5.append("INSERT INTO `iprd` (`invno`, `qtty`, `fob`, `cost`, `date`, `ipi`, `auxLong1`, `auxLong2`, `frete`, `seguro`, `despesas`, `freteIpi`, `qttyRessar`, `baseIcmsSubst`, `icmsSubst`, `icms`, `discount`, `fob4`, `cost4`, `icmsAliq`, `cfop`, `auxLong3`, `auxLong4`, `auxLong5`, `auxMy1`, `auxMy2`, `auxMy3`, `baseIcms`, `baseIpi`, `ipiAmt`, `reducaoBaseIcms`, `lucroTributado`, `l1`, `l2`, `l3`, `l4`, `l5`, `l6`, `l7`, `l8`, `m1`, `m2`, `m3`, `m4`, `m5`, `m6`, `m7`, `m8`, `storeno`, `bits`, `auxShort1`, `auxShort2`, `taxtype`, `auxShort3`, `auxShort4`, `auxShort5`, `seqno`, `bits2`, `bits3`, `bits4`, `s1`, `s2`, `s3`, `s4`, `s5`, `s6`, `s7`, `s8`, `prdno`, `grade`, `auxChar`, `auxChar2`, `cstIcms`, `cstIpi`, `c1`) VALUES (?,  ?,  ?,  ?,  ?,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1949,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  ?,  0,  0,  0,  0,  0,  0,  0,  ?,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  ?,  ?,  '',  '',  '090',  '',  '');")
          val stmt5 = cx.prepareStatement(sql5.toString())
          stmt5.setInt(1, invno)
          stmt5.setInt(2, if(qtd.toInt() > 0) qtd.multiply(BigDecimal(1000)).toInt()
          else qtd.multiply(BigDecimal(-1000)).toInt())
          stmt5.setInt(3, prd.custo!!.multiply(BigDecimal(100)).toInt())
          stmt5.setInt(4, prd.custo!!.multiply(BigDecimal(100)).toInt())
          stmt5.setInt(5, Integer.valueOf(sdf.format(Date())))
          stmt5.setInt(6, base.lojaDestino!!)
          val integer1 = i
          val integer2 = i + 1.also {i = it}
          stmt5.setInt(7, i)
          stmt5.setString(8, prd.prdno)
          stmt5.setString(9, prd.grade)
          stmt5.executeUpdate()
        }
        for(prd in lista) {
          val sql6 = StringBuilder()
          var qtd = BigDecimal.ZERO
          qtd = prd.qtdConsiderada
          sql6.append("UPDATE stk SET stk.qtty_varejo = (stk.qtty_varejo + ? ),                 stk.qtty_atacado = ( ? )  WHERE ( stk.storeno = ? ) AND ( stk.prdno = ? ) AND ( stk.grade = ? ) ")
          val stmt6 = cx.prepareStatement(sql6.toString())
          stmt6.setBigDecimal(1, qtd.multiply(BigDecimal(1000)))
          stmt6.setBigDecimal(2, prd.qtdNfForn!!.multiply(BigDecimal(1000)))
          stmt6.setInt(3, base.lojaDestino!!)
          stmt6.setString(4, prd.prdno)
          stmt6.setString(5, prd.grade)
          stmt6.executeUpdate()
        }
        if(!base.incluiNfFornecedor!!) {
          val sql7 = StringBuilder()
          sql7.append("UPDATE nf  INNER JOIN custp ON (custp.no = nf.custno)  INNER JOIN vend ON (custp.cpf_cgc = vend.cgc)  LEFT  JOIN store ON (store.cgc = custp.cpf_cgc)  SET nf.c1 = '1'           WHERE (nf.storeno = ? )  AND   (nf.nfse = '66' )  AND   (nf.tipo = 2)  AND   (store.name IS NULL)  AND   (nf.c1 <> '1')  AND   (nf.cfo in (6949,5949)) ")
          if(base.fornecedorNf != "") {
            sql7.append(" AND   (vend.no = ? ) ")
          }
          if(base.dtNfIni != 0) {
            sql7.append(" AND   (nf.issuedate between ? AND ?) ")
          }
          val stmt7 = cx.prepareStatement(sql7.toString())
          stmt7.setInt(1, base.lojaDestino!!)
          var index = 1
          if(base.fornecedorNf != "") {
            index++
            stmt7.setInt(index, Integer.valueOf(base.fornecedorNf))
          }
          if(base.dtNfIni != 0) {
            index++
            stmt7.setInt(index, base.dtNfIni!!)
            index++
            stmt7.setInt(index, base.dtNfFim!!)
          }
          stmt7.executeUpdate()
        }
      }
      numeroNF
    } catch(se: SQLException) {
      cx!!.rollback()
      JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:$se")
      se.printStackTrace()
      throw RuntimeException(se)
    } catch(e: Exception) {
      cx!!.rollback()
      JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:$e")
      e.printStackTrace()
      throw RuntimeException(e)
    }
  }
}