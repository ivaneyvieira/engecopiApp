package br.com.engecopi.app.model

import br.com.engecopi.app.model.TipoMov.ENTRADA
import br.com.engecopi.app.model.TipoMov.SAIDA
import java.sql.Connection
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JOptionPane

private class GestorDADOS(val connection: Connection) {
  @Throws(Exception::class)
  fun pegaTransacao(): Int? {
    val cx = connection
    cx.autoCommit = false
    val sql1 = "SELECT (MAX(xano) + 1) AS xano FROM sqldados.xa "
    val sql2 = "INSERT INTO sqldados.xa VALUES (?)"
    val stmt = cx.prepareStatement(sql1)
    val rs = stmt.executeQuery()
    if (rs.next()) {
      try {
        rs.findColumn("xano")
      } catch (ignored: Exception) {
      }
      val stmt2 = cx.prepareStatement(sql2)
      stmt2.setLong(1, rs.getInt("xano").toLong())
      stmt2.executeUpdate()
      val transacao = rs.getInt("xano")
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
    val sql1 = "SELECT (MAX(no) + 1) AS numero FROM sqldados.lastno WHERE storeno = ? AND se = 66"
    val sql2 = "INSERT INTO sqldados.lastno (no,storeno,dupse,se,padbyte) VALUES (?,?,0,'66','')"
    val stmt = cx.prepareStatement(sql1)
    stmt.setInt(1, loja!!)
    val rs = stmt.executeQuery()
    if (rs.next()) {
      try {
        rs.findColumn("numero")
      } catch (ignored: Exception) {
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
    sql1.append(" SELECT vend.no AS no  FROM vend  inner join store on (vend.cgc = store.cgc)  where store.no = ? ")
    val stmt1 = connection.prepareStatement(sql1.toString())
    stmt1.setInt(1, lojaid!!)
    stmt1.executeQuery()
    val rs1 = stmt1.resultSet
    return if (rs1.next()) {
      rs1.getInt("no")
    } else 0
  }

  @Throws(Exception::class)
  fun buscaCliente(lojaid: Int?): Int {
    val sql1 = StringBuilder()
    sql1.append(" SELECT custp.no AS no  FROM custp  inner join store on (custp.cpf_cgc = store.cgc)  where store.no = ? ")
    val stmt1 = connection.prepareStatement(sql1.toString())
    stmt1.setInt(1, lojaid!!)
    stmt1.executeQuery()
    val rs1 = stmt1.resultSet
    return if (rs1.next()) {
      rs1.getInt("no")
    } else 0
  }

  @Throws(Exception::class)
  fun listar(base: Base): List<Produtos> {
    val listagem: MutableList<Produtos> = ArrayList()
    val sql1 = StringBuilder()
    val sql2 = StringBuilder()

    val colunaCusto = "     , ifnull(stk.cm_varejo,0) AS ultimocusto"

    val colunaQtdNotas = when (base.operacao) {
      ENTRADA -> {
        " ifnull(y.qtty*1000,0) "
      }

      SAIDA -> {
        " ifnull(y.qtty*(-1000),0) "
      }
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
    when (base.operacao) {
      ENTRADA -> {
        sql1.append(" AND (qtty_atacado > 0 )")
      }

      SAIDA -> {
        sql1.append(" AND (qtty_atacado < 0 )")
      }
    }
    sql2.append(sql1.toString())
    sql2.append(" GROUP BY 1,2 ")
    sql2.append(" ) AS x")
    val sqlNotas0 =
        " LEFT JOIN ( SELECT  xaprd.prdno        , xaprd.grade        , SUM(xaprd.qtty) qtty  FROM  nf  INNER JOIN xaprd ON (nf.storeno = xaprd.storeno AND  nf.pdvno = xaprd.pdvno AND  nf.xano = xaprd.xano)  INNER JOIN custp ON (custp.no = nf.custno)  INNER JOIN vend ON (custp.cpf_cgc = vend.cgc) LEFT  JOIN store ON (store.cgc = custp.cpf_cgc)  WHERE (nf.storeno = ? )  AND   (nf.nfse = '66')  AND   (nf.tipo = 2)  AND   (store.name IS NULL)  AND   (nf.cfo in (6949,5949))  AND   (nf.c1 <> '1')"

    val sqlNotas =
        "$sqlNotas0 GROUP BY xaprd.prdno, xaprd.grade  ) y ON (x.prdno = y.prdno " + "and " + "x.grade = y.grade) "

    sql2.append(sqlNotas)

    sql2.append(" ) z ")

    sql1.append(" AND (ROUND(prd.no) = ? ) ")

    sql1.append(" AND (prd.mfno IN ( ").append(base.fornecedores).append(" )) ")

    sql1.append(" AND (prd.typeno IN ( ").append(base.tipos).append(" )) ")

    sql1.append(" GROUP BY 1,2 ")
    sql1.append(" ) AS x")

    sql1.append(sqlNotas)

    sql1.append(" ) z")
    val sql = sql1.toString()

    val stmt = connection.prepareStatement(sql)
    stmt.setInt(1, base.lojaDestino)


    stmt.executeQuery()
    val rs = stmt.resultSet
    while (rs.next()) {
      if (rs.findColumn("prdno") > 0) {
        val prd =
            Produtos(prdno = rs.getString("prdno"),
                grade = rs.getString("grade"),
                descricao = rs.getString("descricao"),
                fornecedor = rs.getLong("fornecedor"),
                centrodelucro = rs.getString("centrodelucro"),
                tipo = rs.getLong("tipo"),
                qtdNfForn = rs.getDouble("qttynfs"),
                qtdAtacado = rs.getDouble("qttyatacado"),
                qtdConsiderada = rs.getDouble("qttyconsiderada"),
                custo = rs.getDouble("ultimocusto"),
                total = rs.getDouble("total"))
        listagem.add(prd)
      }
    }
    stmt.close()
    return listagem
  }

  @Throws(Exception::class)
  fun executar(base: Base): Int? {
    return when (base.operacao) {
      ENTRADA -> {
        gerarEntrada(base)
      }

      SAIDA -> {
        gerarSaida(base)
      }
    }
  }

  @Throws(Exception::class)
  fun validarNfEntrada(loja: Int?, nota: Int?): Boolean {
    val sql = StringBuilder()
    sql.append(" SELECT count(*) qtd from nf  where storeno = ? and nfno = ? and print_remarks like ? and nfse = '66' ")
    val stmt = connection.prepareStatement(sql.toString())
    stmt.setInt(1, loja!!)
    stmt.setInt(2, nota!!)
    stmt.setString(3, "CNF")
    stmt.executeQuery()
    val rs = stmt.resultSet
    val ehValido = rs.next() && rs.getInt("qtd") > 0
    stmt.close()
    return ehValido
  }

  @Throws(Exception::class)
  fun validarNfSaida(loja: Int?, nota: Int?): Boolean {
    val sql = StringBuilder()
    sql.append("SELECT invno FROM inv WHERE storeno = ? AND nfname = ? AND  vendno = ? AND auxStr1 = ? AND invse = '66'")
    val stmt = connection.prepareStatement(sql.toString())
    stmt.setInt(1, loja!!)
    stmt.setInt(2, nota!!)
    stmt.setInt(3, buscaFornecedor(loja))
    stmt.setString(4, "CNF")
    stmt.executeQuery()
    val rs = stmt.resultSet
    val ehValido = rs.next() && rs.getInt("invno") > 0
    stmt.close()
    return ehValido
  }

  @Throws(Exception::class)
  fun desfazerEntrada(loja: Int?, nota: Int?) {
    val cx = connection
    try {
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
      if (rs.next()) {
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
    } catch (se: SQLException) {
      cx.rollback()
      JOptionPane.showMessageDialog(null, " Erro:$se")
      se.printStackTrace()
      throw RuntimeException(se)
    } catch (e: Exception) {
      cx.rollback()
      JOptionPane.showMessageDialog(null, " Erro:$e")
      throw RuntimeException(e)
    } finally {
    }
  }

  @Throws(SQLException::class)
  fun desfazerSaida(loja: Int?, nota: Int?) {
    val cx: Connection = connection
    try {
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
      if (rs.next()) {
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
    } catch (se: SQLException) {
      cx.rollback()
      JOptionPane.showMessageDialog(null, " Erro:$se")
      se.printStackTrace()
      throw RuntimeException(se)
    } catch (e: Exception) {
      cx.rollback()
      JOptionPane.showMessageDialog(null, " Erro:$e")
      e.printStackTrace()
      throw RuntimeException(e)
    }
  }

  @Throws(Exception::class)
  fun gerarEntrada(base: Base): Int? {
    val cx = connection
    val cliente = buscaCliente(base.lojaDestino)
    val transacaoNF = pegaTransacao() ?: return null
    val transacaoEstoque = pegaTransacao() ?: return null
    val numnf = pegaNumNf(base.lojaDestino) ?: return null
    val sdf = SimpleDateFormat("yyyyMMdd")
    return try {
      cx.autoCommit = false
      val lista = listar(base)
      var valorTotal = 0.00
      lista.forEach { prd ->
        val sql1 = StringBuilder()
        val sql2 = StringBuilder()

        var qtd = prd.qtdConsiderada
        if (qtd < 0) {
          -qtd
        }
        if (qtd != 0.00) {
          sql1.append("INSERT INTO stkmov (xano,qtty,date,cm_fiscal,cm_real,storeno,bits,prdno,grade,remarks)  values (?,?,?,?,?,?,?,?,?,?) ")
          val stmt1 = cx.prepareStatement(sql1.toString())
          stmt1.setInt(1, transacaoEstoque)
          stmt1.setInt(2, qtd.toInt())
          stmt1.setInt(3, Integer.valueOf(sdf.format(Date())))
          stmt1.setLong(4, prd.custo.toLong())
          stmt1.setLong(5, prd.custo.toLong())
          stmt1.setInt(6, base.lojaDestino)
          stmt1.setInt(7, 1)
          stmt1.setString(8, prd.prdno)
          stmt1.setString(9, prd.grade)
          stmt1.setString(10, "CONFERENCIA DE ESTOQUE")
          stmt1.executeUpdate()
          sql2.append("INSERT INTO stkmovh (xano,qtty,date,nfno,cm_fiscal,cm_real,auxLong1,auxLong2,  auxLong3,auxLong4,auxLong5,auxMy1,auxMy2,auxMy3,auxMy4,auxMy5,storeno,userno,  tipo,bits,auxShort1,auxShort2,auxShort3,auxShort4,auxShort5,prdno,grade,nfse,  auxStr1,auxStr2,auxStr3,auxStr4)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
          val stmt2 = cx.prepareStatement(sql2.toString())
          stmt2.setInt(1, transacaoEstoque)
          stmt2.setInt(2, qtd.toInt())
          stmt2.setInt(3, Integer.valueOf(sdf.format(Date())))
          stmt2.setInt(4, 0)
          stmt2.setLong(5, prd.custo.toLong())
          stmt2.setLong(6, prd.custo.toLong())
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
          stmt2.setInt(17, base.lojaDestino)
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
          qtd = if (qtd.toInt() > 0) qtd else -qtd
          valorTotal = valorTotal + (qtd * prd.custo)
        }
      }
      if (valorTotal > 0.00) {
        val sql3 = StringBuilder()
        sql3.append("INSERT INTO `nf` (`xano`, `nfno`, `custno`, `issuedate`, `delivdate`, `sec_amt`, `fre_amt`, `netamt`, `grossamt`, `discount`, `icms_amt`, `tax_paid`, `ipi_amt`, `base_calculo_ipi`, `iss_amt`, `base_iss_amt`, `isento_amt`, `subst_amt`, `baseIcmsSubst`, `icmsSubst`, `vol_no`, `vol_qtty`, `cfo`, `invno`, `cfo2`, `auxLong1`, `auxLong2`, `auxLong3`, `auxLong4`, `auxMy1`, `auxMy2`, `auxMy3`, `auxMy4`, `eordno`, `l1`, `l2`, `l3`, `l4`, `l5`, `l6`, `l7`, `l8`, `m1`, `m2`, `m3`, `m4`, `m5`, `m6`, `m7`, `m8`, `vol_gross`, `vol_net`, `mult`, `storeno`, `pdvno`, `carrno`, `empno`, `status`, `natopno`, `xatype`, `storeno_from`, `tipo`, `padbits`, `bits`, `usernoCancel`, `custno_addno`, `empnoDiscount`, `auxShort1`, `auxShort2`, `auxShort3`, `auxShort4`, `auxShort5`, `paymno`, `s1`, `s2`, `s3`, `s4`, `s5`, `s6`, `s7`, `s8`, `nfse`, `ship_by`, `vol_make`, `vol_kind`, `remarks`, `padbyte`, `print_remarks`, `remarksCancel`, `c1`, `c2`, `wshash`) VALUES ( ?,  ?,  ?,  ?,  ?,  0,  0,  ?,  ?,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  5949,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  ?,  0,  0,  1,  0,  7,  0,  0,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  '66',  '',  '',  '',  ?,  '',  ?,  '',  '',  '',  ''); ")
        val stmt3 = cx.prepareStatement(sql3.toString())
        stmt3.setInt(1, transacaoNF)
        stmt3.setInt(2, numnf)
        stmt3.setInt(3, cliente)
        stmt3.setInt(4, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(5, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(6, valorTotal.toInt())
        stmt3.setInt(7, valorTotal.toInt())
        stmt3.setInt(8, base.lojaDestino)
        stmt3.setString(9, transacaoEstoque.toString())
        stmt3.setString(10, "CNF")
        stmt3.executeUpdate()
        lista.forEach { prd ->
          val sql4 = StringBuilder()
          val qtd = prd.qtdConsiderada
          sql4.append("INSERT INTO `xaprd` (`xano`, `nfno`, `price`, `date`, `qtty`, `storeno`, `pdvno`, `prdno`, `grade`, `nfse`, `padbyte`, `wshash`) VALUES ( ?,  ?,  ?,  ?,  ?,  ?,  0,  ?,  ?,  '66' ,  0,  ''); ")
          val stmt4 = cx.prepareStatement(sql4.toString())
          stmt4.setInt(1, transacaoNF)
          stmt4.setInt(2, numnf)
          stmt4.setInt(3, prd.custo.toInt())
          stmt4.setInt(4, Integer.valueOf(sdf.format(Date())))
          stmt4.setInt(5, if (qtd.toInt() > 0) qtd.toInt() else qtd.toInt() * -1)
          stmt4.setInt(6, base.lojaDestino)
          stmt4.setString(7, prd.prdno)
          stmt4.setString(8, prd.grade)
          stmt4.executeUpdate()
        }
        lista.forEach { prd ->
          val sql5 = StringBuilder()

          val qtd = prd.qtdConsiderada
          sql5.append("UPDATE stk SET stk.qtty_varejo = (stk.qtty_varejo + ?),                 stk.qtty_atacado =  ?   WHERE ( stk.storeno = ? ) AND ( stk.prdno = ? ) AND ( stk.grade = ? ) ")
          val stmt5 = cx.prepareStatement(sql5.toString())
          stmt5.setDouble(1, qtd)
          stmt5.setDouble(2, prd.qtdNfForn)
          stmt5.setInt(3, base.lojaDestino)
          stmt5.setString(4, prd.prdno)
          stmt5.setString(5, prd.grade)
          stmt5.executeUpdate()
        }
        val sql7 = StringBuilder()
        sql7.append("UPDATE nf  INNER JOIN custp ON (custp.no = nf.custno)  INNER JOIN vend ON (custp.cpf_cgc = vend.cgc)  LEFT  JOIN store ON (store.cgc = custp.cpf_cgc)  SET nf.c1 = '1'           WHERE (nf.storeno = ? )  AND   (nf.nfse = '66')  AND   (nf.tipo = 2)  AND   (store.name IS NULL)  AND   (nf.c1 <> '1')  AND   (nf.cfo in (6949,5949)) ")

        val stmt7 = cx.prepareStatement(sql7.toString())
        stmt7.setInt(1, base.lojaDestino)
        stmt7.executeUpdate()
      }
      numnf
    } catch (se: SQLException) {
      cx.rollback()
      JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:$se")
      se.printStackTrace()
      throw RuntimeException(se)
    } catch (e: Exception) {
      cx.rollback()
      JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:$e")
      e.printStackTrace()
      throw RuntimeException(e)
    }
  }

  @Throws(Exception::class)
  fun gerarSaida(base: Base): Int? {
    val cx: Connection = connection
    val fornecedor = buscaFornecedor(base.lojaDestino)
    val numeroNF = pegaNumNf(base.lojaDestino) ?: return null
    val transacaoEstoque = pegaTransacao() ?: return null
    val sdf = SimpleDateFormat("yyyyMMdd")
    return try {
      cx.autoCommit = false
      val lista = listar(base)
      var valorTotal = 0.00
      lista.forEach { prd ->
        val sql1 = StringBuilder()
        val sql2 = StringBuilder()
        var qtd = prd.qtdConsiderada
        if (qtd > 0.0) {
          -qtd
        }
        if (qtd != 0.00) {
          sql1.append("INSERT INTO stkmov (xano,qtty,date,cm_fiscal,cm_real,storeno,bits,prdno,grade,remarks)  values (?,?,?,?,?,?,?,?,?,?) ")
          val stmt1 = cx.prepareStatement(sql1.toString())
          stmt1.setInt(1, transacaoEstoque)
          stmt1.setInt(2, qtd.toInt())
          stmt1.setInt(3, Integer.valueOf(sdf.format(Date())))
          stmt1.setLong(4, prd.custo.toLong())
          stmt1.setLong(5, prd.custo.toLong())
          stmt1.setInt(6, base.lojaDestino)
          stmt1.setInt(7, 1)
          stmt1.setString(8, prd.prdno)
          stmt1.setString(9, prd.grade)
          stmt1.setString(10, "CONFERENCIA DE ESTOQUE")
          stmt1.executeUpdate()
          sql2.append("INSERT INTO stkmovh (xano,qtty,date,nfno,cm_fiscal,cm_real,auxLong1,auxLong2,  auxLong3,auxLong4,auxLong5,auxMy1,auxMy2,auxMy3,auxMy4,auxMy5,storeno,userno,  tipo,bits,auxShort1,auxShort2,auxShort3,auxShort4,auxShort5,prdno,grade,nfse,  auxStr1,auxStr2,auxStr3,auxStr4)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
          val stmt2 = cx.prepareStatement(sql2.toString())
          stmt2.setInt(1, transacaoEstoque)
          stmt2.setInt(2, qtd.toInt())
          stmt2.setInt(3, Integer.valueOf(sdf.format(Date())))
          stmt2.setInt(4, 0)
          stmt2.setLong(5, prd.custo.toLong())
          stmt2.setLong(6, prd.custo.toLong())
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
          stmt2.setInt(17, base.lojaDestino)
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
          qtd = if (qtd.toInt() > 0) qtd else -qtd
          valorTotal = valorTotal + (qtd)
        }
      }
      if (valorTotal > 0.00) {
        val sql3 = StringBuilder()
        sql3.append("insert into inv (vendno,ordno,xfrno,issue_date,date,comp_date,ipi,  icm,freight,netamt,grossamt,subst_trib,discount,prdamt,despesas,  base_ipi,aliq,cfo,nfNfno,auxLong1,auxLong2,auxMoney1,auxMoney2,  dataSaida,amtServicos,amtIRRF,amtINSS,amtISS,auxMoney3,auxMoney4,  auxMoney5,auxLong3,auxLong4,auxLong5,auxLong6,auxLong7,auxLong8,  auxLong9,auxLong10,auxLong11,auxLong12,auxMoney6,auxMoney7,auxMoney8,  auxMoney9,auxMoney10,auxMoney11,auxMoney12,auxMoney13,l1,l2,l3,  l4,l5,l6,l7,l8,m1,m2,m3,m4,m5,m6,m7,m8,weight,carrno,  packages,storeno,indxno,book_bits,type,usernoFirst,usernoLast,  nfStoreno,bits,padbyte,auxShort1,auxShort2,auxShort3,auxShort4,  auxShort5,auxShort6,auxShort7,auxShort8,auxShort9,auxShort10,  auxShort11,auxShort12,auxShort13,auxShort14,bits2,bits3,bits4,  bits5,s1,s2,s3,s4,s5,s6,s7,s8,nfname,invse,account,  remarks,contaCredito,contaDebito,nfNfse,auxStr1,auxStr2,auxStr3,  auxStr4,auxStr5,auxStr6,c1,c2) values (?,0,0,?,?,?,0,0,0,0,?,0,0,?,0,0,0,1949,0,0,0,0,0,?,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,?,0,3,4,0,0,0,35,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,?,?,'2.01.20','','','','',?,?,'','','','','','') ")
        val stmt3 = cx.prepareStatement(sql3.toString())
        stmt3.setInt(1, fornecedor)
        stmt3.setInt(2, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(3, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(4, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(5, valorTotal.toInt())
        stmt3.setInt(6, valorTotal.toInt())
        stmt3.setInt(7, Integer.valueOf(sdf.format(Date())))
        stmt3.setInt(8, base.lojaDestino)
        stmt3.setString(9, numeroNF.toString())
        stmt3.setString(10, "66")
        stmt3.setString(11, "CNF")
        stmt3.setString(12, transacaoEstoque.toString())
        stmt3.executeUpdate()
        val invno = getInvno(base, numeroNF, fornecedor)
        val i = 0
        lista.forEach { prd ->
          val sql5 = StringBuilder()
          val qtd = prd.qtdConsiderada
          sql5.append("INSERT INTO `iprd` (`invno`, `qtty`, `fob`, `cost`, `date`, `ipi`, `auxLong1`, `auxLong2`, `frete`, `seguro`, `despesas`, `freteIpi`, `qttyRessar`, `baseIcmsSubst`, `icmsSubst`, `icms`, `discount`, `fob4`, `cost4`, `icmsAliq`, `cfop`, `auxLong3`, `auxLong4`, `auxLong5`, `auxMy1`, `auxMy2`, `auxMy3`, `baseIcms`, `baseIpi`, `ipiAmt`, `reducaoBaseIcms`, `lucroTributado`, `l1`, `l2`, `l3`, `l4`, `l5`, `l6`, `l7`, `l8`, `m1`, `m2`, `m3`, `m4`, `m5`, `m6`, `m7`, `m8`, `storeno`, `bits`, `auxShort1`, `auxShort2`, `taxtype`, `auxShort3`, `auxShort4`, `auxShort5`, `seqno`, `bits2`, `bits3`, `bits4`, `s1`, `s2`, `s3`, `s4`, `s5`, `s6`, `s7`, `s8`, `prdno`, `grade`, `auxChar`, `auxChar2`, `cstIcms`, `cstIpi`, `c1`) VALUES (?,  ?,  ?,  ?,  ?,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1949,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  ?,  0,  0,  0,  0,  0,  0,  0,  ?,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  ?,  ?,  '',  '',  '090',  '',  '');")
          val stmt5 = cx.prepareStatement(sql5.toString())
          stmt5.setInt(1, invno)
          stmt5.setInt(2, if (qtd.toInt() > 0) qtd.toInt()
          else -qtd.toInt())
          stmt5.setInt(3, prd.custo.toInt())
          stmt5.setInt(4, prd.custo.toInt())
          stmt5.setInt(5, Integer.valueOf(sdf.format(Date())))
          stmt5.setInt(6, base.lojaDestino)
          stmt5.setInt(7, i)
          stmt5.setString(8, prd.prdno)
          stmt5.setString(9, prd.grade)
          stmt5.executeUpdate()
        }
        lista.forEach { prd ->
          val sql6 = StringBuilder()
          val qtd = prd.qtdConsiderada
          sql6.append("UPDATE stk SET stk.qtty_varejo = (stk.qtty_varejo + ? ),                 stk.qtty_atacado = ( ? )  WHERE ( stk.storeno = ? ) AND ( stk.prdno = ? ) AND ( stk.grade = ? ) ")
          val stmt6 = cx.prepareStatement(sql6.toString())
          stmt6.setDouble(1, qtd)
          stmt6.setDouble(2, prd.qtdNfForn)
          stmt6.setInt(3, base.lojaDestino)
          stmt6.setString(4, prd.prdno)
          stmt6.setString(5, prd.grade)
          stmt6.executeUpdate()
        }
        val sql7 = StringBuilder()
        sql7.append("UPDATE nf  INNER JOIN custp ON (custp.no = nf.custno)  INNER JOIN vend ON (custp.cpf_cgc = vend.cgc)  LEFT  JOIN store ON (store.cgc = custp.cpf_cgc)  SET nf.c1 = '1'           WHERE (nf.storeno = ? )  AND   (nf.nfse = '66' )  AND   (nf.tipo = 2)  AND   (store.name IS NULL)  AND   (nf.c1 <> '1')  AND   (nf.cfo in (6949,5949)) ")

        val stmt7 = cx.prepareStatement(sql7.toString())
        stmt7.setInt(1, base.lojaDestino)
        stmt7.executeUpdate()
      }
      numeroNF
    } catch (se: SQLException) {
      cx.rollback()
      JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:$se")
      se.printStackTrace()
      throw RuntimeException(se)
    } catch (e: Exception) {
      cx.rollback()
      JOptionPane.showMessageDialog(null, "Não foi possível gerar a movimentação! Erro:$e")
      e.printStackTrace()
      throw RuntimeException(e)
    }
  }

  private fun getInvno(base: Base, numeroNF: Int, fornecedor: Int): Int {
    val cx: Connection = connection
    val sql4 = StringBuilder()
    sql4.append("SELECT invno FROM inv WHERE storeno = ? and nfname = ? and vendno = ? and invse = 66 ")
    val stmt4 = cx.prepareStatement(sql4.toString())
    stmt4.setInt(1, base.lojaDestino)
    stmt4.setInt(2, numeroNF)
    stmt4.setInt(3, fornecedor)
    stmt4.executeQuery()
    val rs = stmt4.resultSet
    var invno = 0
    if (rs.next()) {
      invno = rs.getInt("invno")
    }
    return invno
  }
}