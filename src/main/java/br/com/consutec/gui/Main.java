package br.com.consutec.gui;

import br.com.consutec.dao.GestorDADOS;
import br.com.engecopi.app.model.Base;
import br.com.engecopi.app.model.Produtos;
import br.com.consutec.modelo.ProdutosModelo;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;

public class Main extends JFrame {
	private final Base base;
	private ProdutosModelo pm;
	private JDialog DialogDesfazer;
	private JButton btexecutar;
	private JCheckBox cbIncluiNFs;
	private JComboBox<String> cbdtipo;
	private JCheckBox cbfiltros;
	private JComboBox<String> cbsinalCusto;
	private JComboBox<String> cbsinalQtd;
	private JLabel lbstatus;
	private JRadioButton rbentrada;
	private JRadioButton rbestoque;
	private ButtonGroup rbgroup;
	private ButtonGroup rbgroup2;
	private JRadioButton rbprecificacao;
	private JRadioButton rbsaida;
	private JFormattedTextField tfDtNfFim;
	private JFormattedTextField tfDtNfIni;
	private JTextField tfFornecedorNF;
	private JTextField tfcentrol;
	private JTextField tfcodprd;
	private JFormattedTextField tfcusto1;
	private JFormattedTextField tfcusto2;
	private JTextField tfdescFim;
	private JTextField tfdescIni;
	private JTextField tfdloja;
	private JTextField tfdnota;
	private JTextField tffornecedores;
	private JFormattedTextField tffunc;
	private JTextField tflocalizacao;
	private JFormattedTextField tflojaDestino;
	private JFormattedTextField tflojaloc;
	private JFormattedTextField tfqtd1;
	private JFormattedTextField tfqtd2;
	private JTextField tftipos;

	public Main() {
		initComponents();
		this.base = new Base();
		this.rbgroup.add(this.rbsaida);
		this.rbgroup.add(this.rbentrada);
		this.rbgroup2.add(this.rbestoque);
		this.rbgroup2.add(this.rbprecificacao);
	}

	private void initComponents() {
		this.rbgroup = new ButtonGroup();
		this.DialogDesfazer = new JDialog();
		JLabel lbdloja = new JLabel();
		JLabel lbdpedido = new JLabel();
		JLabel lbdtipo = new JLabel();
		this.tfdloja = new JTextField();
		this.tfdnota = new JTextField();
		this.cbdtipo = new JComboBox<>();
		JButton btnDesfazer = new JButton();
		this.rbgroup2 = new ButtonGroup();
		JPanel painelDuplicacao = new JPanel();
		JLabel jLabel3 = new JLabel();
		this.tflojaDestino = new JFormattedTextField();
		JLabel jLabel14 = new JLabel();
		JLabel jLabel13 = new JLabel();
		this.rbentrada = new JRadioButton();
		this.rbsaida = new JRadioButton();
		JLabel jLabel19 = new JLabel();
		this.tffunc = new JFormattedTextField();
		JLabel jLabel1 = new JLabel();
		this.rbestoque = new JRadioButton();
		this.rbprecificacao = new JRadioButton();
		JPanel painelFiltros = new JPanel();
		JLabel jLabel4 = new JLabel();
		this.cbfiltros = new JCheckBox();
		JLabel jLabel5 = new JLabel();
		JLabel jLabel9 = new JLabel();
		JLabel jLabel10 = new JLabel();
		this.tfdescIni = new JTextField();
		this.tfdescFim = new JTextField();
		JLabel jLabel6 = new JLabel();
		JLabel jLabel7 = new JLabel();
		this.tffornecedores = new JTextField();
		this.tftipos = new JTextField();
		JLabel jLabel8 = new JLabel();
		JLabel jLabel11 = new JLabel();
		JLabel jLabel12 = new JLabel();
		this.tflojaloc = new JFormattedTextField();
		this.tflocalizacao = new JTextField();
		JLabel jLabel15 = new JLabel();
		this.tfcentrol = new JTextField();
		JLabel jLabel16 = new JLabel();
		this.tfcodprd = new JTextField();
		JLabel jLabel17 = new JLabel();
		JLabel jLabel18 = new JLabel();
		this.tfqtd1 = new JFormattedTextField();
		this.tfcusto1 = new JFormattedTextField();
		this.tfcusto2 = new JFormattedTextField();
		this.cbsinalCusto = new JComboBox<>();
		this.cbsinalQtd = new JComboBox<>();
		this.tfqtd2 = new JFormattedTextField();
		JButton btbuscar = new JButton();
		this.btexecutar = new JButton();
		JButton btnAbreDesfazer = new JButton();
		JLabel jLabel2 = new JLabel();
		this.lbstatus = new JLabel();
		JLabel jLabel20 = new JLabel();
		this.cbIncluiNFs = new JCheckBox();
		JLabel jLabel21 = new JLabel();
		this.tfFornecedorNF = new JTextField();
		JLabel jLabel22 = new JLabel();
		JLabel jLabel23 = new JLabel();
		JLabel jLabel24 = new JLabel();
		this.tfDtNfIni = new JFormattedTextField();
		this.tfDtNfFim = new JFormattedTextField();
		JLabel jLabel25 = new JLabel();
		this.DialogDesfazer.setTitle("Desfazer Pedido");
		lbdloja.setFont(new Font("Tahoma", 0, 12));
		lbdloja.setText("Loja:");
		lbdpedido.setFont(new Font("Tahoma", 0, 12));
		lbdpedido.setText("Numero NF:");
		lbdtipo.setFont(new Font("Tahoma", 0, 12));
		lbdtipo.setText("Tipo de Movimentação:");
		this.tfdloja.setFont(new Font("Tahoma", 0, 12));
		this.tfdnota.setFont(new Font("Tahoma", 0, 12));
		this.cbdtipo.setFont(new Font("Tahoma", 0, 12));
		this.cbdtipo.setModel(new DefaultComboBoxModel<>(new String[]{"Entrada", "Saída"}));
		btnDesfazer.setFont(new Font("Tahoma", 0, 18));
		btnDesfazer.setText("Desfazer");
		btnDesfazer.addActionListener(evt -> Main.this.btnDesfazerActionPerformed(evt));
		GroupLayout DialogDesfazerLayout = new GroupLayout(this.DialogDesfazer.getContentPane());
		this.DialogDesfazer.getContentPane().setLayout(DialogDesfazerLayout);
		DialogDesfazerLayout.setHorizontalGroup(
						DialogDesfazerLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
										DialogDesfazerLayout.createSequentialGroup().addContainerGap().addGroup(
														DialogDesfazerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
																		.addGroup(DialogDesfazerLayout.createSequentialGroup().addGroup(
																						DialogDesfazerLayout.createParallelGroup(
																										GroupLayout.Alignment.LEADING)
																										.addComponent(lbdloja).addComponent(lbdpedido)
																										.addComponent(lbdtipo)).addGap(18, 18, 18)
																						.addGroup(DialogDesfazerLayout.createParallelGroup(
																										GroupLayout.Alignment.LEADING)
																										.addComponent(this.tfdnota, -2, 119, -2)
																										.addComponent(this.cbdtipo, -2, -1, -2)
																										.addComponent(this.tfdloja, -2, 44, -2)))
																		.addComponent(btnDesfazer)).addContainerGap(25, 32767)));
		DialogDesfazerLayout.setVerticalGroup(
						DialogDesfazerLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
										DialogDesfazerLayout.createSequentialGroup().addGap(14, 14, 14).addGroup(
														DialogDesfazerLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(lbdloja).addComponent(this.tfdloja, -2, -1, -2))
														.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
														DialogDesfazerLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
																		.addComponent(this.tfdnota, -2, -1, -2).addComponent(lbdpedido))
														.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
														DialogDesfazerLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(this.cbdtipo, -2, -1, -2).addComponent(lbdtipo))
														.addGap(32, 32, 32).addComponent(btnDesfazer)
														.addContainerGap(76, 32767)));
		setDefaultCloseOperation(3);
		setTitle("Ajuste de Estoque");
		painelDuplicacao.setBackground(new Color(102, 102, 102));
		jLabel3.setFont(new Font("Tahoma", 0, 12));
		jLabel3.setForeground(new Color(255, 255, 255));
		jLabel3.setText("Loja de Destino:");
		this.tflojaDestino.setFormatterFactory(
						new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
		this.tflojaDestino.setFont(new Font("Tahoma", 0, 12));
		jLabel14.setFont(new Font("Tahoma", 1, 24));
		jLabel14.setForeground(new Color(255, 255, 255));
		jLabel14.setText("Ajuste Estoque");
		jLabel13.setFont(new Font("Tahoma", 0, 12));
		jLabel13.setForeground(new Color(255, 255, 255));
		jLabel13.setText("Operação:");
		this.rbentrada.setFont(new Font("Tahoma", 0, 12));
		this.rbentrada.setForeground(new Color(255, 255, 255));
		this.rbentrada.setSelected(true);
		this.rbentrada.setText("Entrada");
		this.rbentrada.setName("");
		this.rbsaida.setFont(new Font("Tahoma", 0, 12));
		this.rbsaida.setForeground(new Color(255, 255, 255));
		this.rbsaida.setText("Saída");
		jLabel19.setFont(new Font("Tahoma", 0, 12));
		jLabel19.setForeground(new Color(255, 255, 255));
		jLabel19.setText("Codigo do Funcionario:");
		this.tffunc.setFormatterFactory(
						new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
		this.tffunc.setFont(new Font("Tahoma", 0, 12));
		jLabel1.setFont(new Font("Tahoma", 0, 12));
		jLabel1.setForeground(new Color(255, 255, 255));
		jLabel1.setText("Custo:");
		this.rbestoque.setFont(new Font("Tahoma", 0, 12));
		this.rbestoque.setForeground(new Color(255, 255, 255));
		this.rbestoque.setSelected(true);
		this.rbestoque.setText("Custo Médio Varejo");
		this.rbprecificacao.setFont(new Font("Tahoma", 0, 12));
		this.rbprecificacao.setForeground(new Color(255, 255, 255));
		this.rbprecificacao.setText("Custo Médio");
		GroupLayout painelDuplicacaoLayout = new GroupLayout(painelDuplicacao);
		painelDuplicacao.setLayout(painelDuplicacaoLayout);
		painelDuplicacaoLayout.setHorizontalGroup(
						painelDuplicacaoLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
										painelDuplicacaoLayout.createSequentialGroup().addContainerGap().addGroup(
														painelDuplicacaoLayout
																		.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
																		painelDuplicacaoLayout.createSequentialGroup().addGroup(
																						painelDuplicacaoLayout.createParallelGroup(
																										GroupLayout.Alignment.LEADING, false)
																										.addComponent(jLabel19, -1, -1, 32767)
																										.addComponent(jLabel3, -2, 140, -2))
																						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																						.addGroup(painelDuplicacaoLayout.createParallelGroup(
																										GroupLayout.Alignment.LEADING).addGroup(
																										painelDuplicacaoLayout.createSequentialGroup()
																														.addComponent(this.tflojaDestino, -2,
																																		40, -2).addGap(18, 18, 18)
																														.addComponent(jLabel13, -2, 73, -2))
																										.addComponent(this.tffunc, -2, 40, -2))
																						.addPreferredGap(
																										LayoutStyle.ComponentPlacement.UNRELATED)
																						.addGroup(painelDuplicacaoLayout.createParallelGroup(
																										GroupLayout.Alignment.LEADING).addGroup(
																										painelDuplicacaoLayout.createSequentialGroup()
																														.addComponent(this.rbentrada, -2, 106,
																																		-2).addGap(60, 60, 60)
																														.addComponent(jLabel1))
																										.addComponent(this.rbsaida, -2, 114, -2))
																						.addGap(18, 18, 18).addGroup(painelDuplicacaoLayout
																						.createParallelGroup(GroupLayout.Alignment.LEADING)
																						.addComponent(this.rbprecificacao)
																						.addComponent(this.rbestoque))).addComponent(jLabel14))
														.addContainerGap(-1, 32767)));
		painelDuplicacaoLayout.setVerticalGroup(
						painelDuplicacaoLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
										painelDuplicacaoLayout.createSequentialGroup().addGap(4, 4, 4)
														.addComponent(jLabel14)
														.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
														painelDuplicacaoLayout
																		.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel3)
																		.addComponent(this.tflojaDestino, -2, -1, -2)
																		.addComponent(jLabel13).addComponent(this.rbentrada)
																		.addComponent(jLabel1).addComponent(this.rbestoque))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
														painelDuplicacaoLayout
																		.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
																		painelDuplicacaoLayout
																						.createParallelGroup(GroupLayout.Alignment.BASELINE)
																						.addComponent(jLabel19)
																						.addComponent(this.tffunc, -2, -1, -2)).addGroup(
																		painelDuplicacaoLayout
																						.createParallelGroup(GroupLayout.Alignment.BASELINE)
																						.addComponent(this.rbsaida)
																						.addComponent(this.rbprecificacao)))
														.addContainerGap(12, 32767)));
		painelFiltros.setBackground(new Color(102, 102, 255));
		jLabel4.setFont(new Font("Tahoma", 0, 12));
		jLabel4.setForeground(new Color(255, 255, 255));
		jLabel4.setText("Filtro Produtos:");
		this.cbfiltros.setFont(new Font("Tahoma", 0, 12));
		this.cbfiltros.setForeground(new Color(255, 255, 255));
		this.cbfiltros.setSelected(true);
		this.cbfiltros.setText("Utiliza filtros");
		this.cbfiltros.addActionListener(evt -> Main.this.cbfiltrosActionPerformed(evt));
		jLabel5.setFont(new Font("Tahoma", 0, 12));
		jLabel5.setForeground(new Color(255, 255, 255));
		jLabel5.setText("Descrição: ");
		jLabel9.setFont(new Font("Tahoma", 0, 12));
		jLabel9.setForeground(new Color(255, 255, 255));
		jLabel9.setText("Inicio:");
		jLabel10.setFont(new Font("Tahoma", 0, 12));
		jLabel10.setForeground(new Color(255, 255, 255));
		jLabel10.setText("Fim:");
		this.tfdescIni.setFont(new Font("Tahoma", 0, 12));
		this.tfdescIni.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent evt) {
				Main.this.tfdescIniFocusLost(evt);
			}
		});
		this.tfdescIni.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent evt) {
				Main.this.tfdescIniKeyReleased(evt);
			}
		});
		this.tfdescFim.setFont(new Font("Tahoma", 0, 12));
		this.tfdescFim.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent evt) {
				Main.this.tfdescFimFocusLost(evt);
			}
		});
		this.tfdescFim.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent evt) {
				Main.this.tfdescFimKeyReleased(evt);
			}
		});
		jLabel6.setFont(new Font("Tahoma", 0, 12));
		jLabel6.setForeground(new Color(255, 255, 255));
		jLabel6.setText("Fornecedores:");
		jLabel7.setFont(new Font("Tahoma", 0, 12));
		jLabel7.setForeground(new Color(255, 255, 255));
		jLabel7.setText("Tipos:");
		this.tffornecedores.setFont(new Font("Tahoma", 0, 12));
		this.tftipos.setFont(new Font("Tahoma", 0, 12));
		jLabel8.setFont(new Font("Tahoma", 0, 12));
		jLabel8.setForeground(new Color(255, 255, 255));
		jLabel8.setText("Areas:");
		jLabel11.setFont(new Font("Tahoma", 0, 12));
		jLabel11.setForeground(new Color(255, 255, 255));
		jLabel11.setText("Lj Localização");
		jLabel12.setFont(new Font("Tahoma", 0, 12));
		jLabel12.setForeground(new Color(255, 255, 255));
		jLabel12.setText("Descrição da Localização");
		this.tflojaloc.setFormatterFactory(
						new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
		this.tflojaloc.setFont(new Font("Tahoma", 0, 12));
		this.tflocalizacao.setFont(new Font("Tahoma", 0, 12));
		this.tflocalizacao.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent evt) {
				Main.this.tflocalizacaoFocusLost(evt);
			}
		});
		jLabel15.setFont(new Font("Tahoma", 0, 12));
		jLabel15.setForeground(new Color(255, 255, 255));
		jLabel15.setText("Cent. de Lucro:");
		this.tfcentrol.setFont(new Font("Tahoma", 0, 12));
		this.tfcentrol.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent evt) {
				Main.this.tfcentrolFocusLost(evt);
			}
		});
		this.tfcentrol.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent evt) {
				Main.this.tfcentrolKeyReleased(evt);
			}
		});
		jLabel16.setFont(new Font("Tahoma", 0, 12));
		jLabel16.setForeground(new Color(255, 255, 255));
		jLabel16.setText("Codigo Produto:");
		this.tfcodprd.setFont(new Font("Tahoma", 0, 12));
		this.tfcodprd.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent evt) {
				Main.this.tfcodprdFocusLost(evt);
			}
		});
		this.tfcodprd.addActionListener(evt -> Main.this.tfcodprdActionPerformed(evt));
		this.tfcodprd.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent evt) {
				Main.this.tfcodprdKeyReleased(evt);
			}
		});
		jLabel17.setFont(new Font("Tahoma", 0, 12));
		jLabel17.setForeground(new Color(255, 255, 255));
		jLabel17.setText("Quantidade:");
		jLabel18.setFont(new Font("Tahoma", 0, 12));
		jLabel18.setForeground(new Color(255, 255, 255));
		jLabel18.setText("Valor do Custo: ");
		this.tfqtd1.setFormatterFactory(
						new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
		this.tfqtd1.setFont(new Font("Tahoma", 0, 12));
		this.tfcusto1.setFormatterFactory(
						new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
		this.tfcusto1.setFont(new Font("Tahoma", 0, 12));
		this.tfcusto2.setFormatterFactory(
						new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
		this.tfcusto2.setFont(new Font("Tahoma", 0, 12));
		this.cbsinalCusto.setFont(new Font("Tahoma", 0, 12));
		this.cbsinalCusto.setModel(new DefaultComboBoxModel<>(
						new String[]{"Todos", "Maior que   \">\"", "Menor que  \"<\"", "Igual a        \"=\"", "Entre"}));
		this.cbsinalQtd.setFont(new Font("Tahoma", 0, 12));
		this.cbsinalQtd.setModel(new DefaultComboBoxModel<>(
						new String[]{"Todos", "Maior que   \">\"", "Menor que  \"<\"", "Igual a        \"=\"", "Entre", " "}));
		this.tfqtd2.setFormatterFactory(
						new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
		this.tfqtd2.setFont(new Font("Tahoma", 0, 12));
		btbuscar.setFont(new Font("Tahoma", 0, 18));
		btbuscar.setText("Buscar");
		btbuscar.addActionListener(evt -> Main.this.btbuscarActionPerformed(evt));
		this.btexecutar.setFont(new Font("Tahoma", 0, 18));
		this.btexecutar.setText("Executar");
		this.btexecutar.setEnabled(false);
		this.btexecutar.addActionListener(evt -> Main.this.btexecutarActionPerformed(evt));
		btnAbreDesfazer.setFont(new Font("Tahoma", 0, 18));
		btnAbreDesfazer.setText("Desfazer");
		btnAbreDesfazer.addActionListener(evt -> Main.this.btnAbreDesfazerActionPerformed(evt));
		jLabel2.setFont(new Font("Tahoma", 1, 18));
		jLabel2.setForeground(new Color(255, 255, 255));
		jLabel2.setText("Status: ");
		this.lbstatus.setFont(new Font("Tahoma", 0, 18));
		this.lbstatus.setForeground(new Color(255, 0, 0));
		jLabel20.setFont(new Font("Tahoma", 0, 12));
		jLabel20.setForeground(new Color(255, 255, 255));
		jLabel20.setText("Inclui notas de fornecedor:");
		this.cbIncluiNFs.setFont(new Font("Tahoma", 0, 12));
		this.cbIncluiNFs.setForeground(new Color(255, 255, 255));
		this.cbIncluiNFs.setText("Incluir Notas");
		this.cbIncluiNFs.addActionListener(evt -> Main.this.cbIncluiNFsActionPerformed(evt));
		jLabel21.setFont(new Font("Tahoma", 0, 12));
		jLabel21.setForeground(new Color(255, 255, 255));
		jLabel21.setText("Fornecedor NF:");
		this.tfFornecedorNF.setFont(new Font("Tahoma", 0, 12));
		this.tfFornecedorNF.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent evt) {
				Main.this.tfFornecedorNFFocusLost(evt);
			}
		});
		this.tfFornecedorNF.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent evt) {
				Main.this.tfFornecedorNFKeyReleased(evt);
			}
		});
		jLabel22.setFont(new Font("Tahoma", 0, 12));
		jLabel22.setForeground(new Color(255, 255, 255));
		jLabel22.setText("Periodo:");
		jLabel23.setFont(new Font("Tahoma", 0, 12));
		jLabel23.setForeground(new Color(255, 255, 255));
		jLabel23.setText("Inicio:");
		jLabel24.setFont(new Font("Tahoma", 0, 12));
		jLabel24.setForeground(new Color(255, 255, 255));
		jLabel24.setText("Fim:");
		try {
			this.tfDtNfIni
							.setFormatterFactory(new DefaultFormatterFactory(new MaskFormatter("##/##/####")));
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		this.tfDtNfIni.setFont(new Font("Tahoma", 0, 12));
		try {
			this.tfDtNfFim
							.setFormatterFactory(new DefaultFormatterFactory(new MaskFormatter("##/##/####")));
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		this.tfDtNfFim.setFont(new Font("Tahoma", 0, 12));
		jLabel25.setFont(new Font("Tahoma", 0, 12));
		jLabel25.setForeground(new Color(255, 255, 255));
		jLabel25.setText("DD/MM/YYYY");
		GroupLayout painelFiltrosLayout = new GroupLayout(painelFiltros);
		painelFiltros.setLayout(painelFiltrosLayout);
		painelFiltrosLayout.setHorizontalGroup(
						painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
										painelFiltrosLayout.createSequentialGroup().addContainerGap().addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
																		.addGroup(painelFiltrosLayout.createSequentialGroup().addGroup(
																						painelFiltrosLayout.createParallelGroup(
																										GroupLayout.Alignment.LEADING)
																										.addGroup(GroupLayout.Alignment.TRAILING,
																														painelFiltrosLayout
																																		.createSequentialGroup()
																																		.addGap(0, 0, 32767)
																																		.addComponent(jLabel23)
																																		.addGap(104, 104, 104)
																																		.addComponent(jLabel24)
																																		.addGap(218, 218, 218))
																										.addGroup(painelFiltrosLayout
																														.createSequentialGroup().addGroup(
																																		painelFiltrosLayout
																																						.createParallelGroup(
																																										GroupLayout.Alignment.TRAILING)
																																						.addComponent(jLabel20,
																																										-2, 153, -2)
																																						.addComponent(jLabel21,
																																										-2, 153, -2))
																														.addPreferredGap(
																																		LayoutStyle.ComponentPlacement.RELATED)
																														.addGroup(painelFiltrosLayout
																																		.createParallelGroup(
																																						GroupLayout.Alignment.LEADING)
																																		.addComponent(this.cbIncluiNFs)
																																		.addComponent(
																																						this.tfFornecedorNF, -2,
																																						140, -2))).addGroup(
																										painelFiltrosLayout.createSequentialGroup()
																														.addComponent(jLabel22, -2, 153, -2)
																														.addPreferredGap(
																																		LayoutStyle.ComponentPlacement.RELATED)
																														.addComponent(this.tfDtNfIni, -2, 100,
																																		-2).addGap(36, 36, 36)
																														.addComponent(this.tfDtNfFim, -2, 103,
																																		-2).addGap(18, 18, 18)
																														.addComponent(jLabel25, -2, 153, -2)))
																						.addGap(686, 686, 686)).addGroup(
																		painelFiltrosLayout.createSequentialGroup().addGroup(
																						painelFiltrosLayout.createParallelGroup(
																										GroupLayout.Alignment.LEADING).addGroup(
																										painelFiltrosLayout.createSequentialGroup()
																														.addGroup(painelFiltrosLayout
																																		.createParallelGroup(
																																						GroupLayout.Alignment.LEADING)
																																		.addGroup(painelFiltrosLayout
																																						.createParallelGroup(
																																										GroupLayout.Alignment.TRAILING,
																																										false)
																																						.addComponent(jLabel4,
																																										-1, -1, 32767)
																																						.addComponent(jLabel16,
																																										-1, -1, 32767))
																																		.addComponent(jLabel6)
																																		.addComponent(jLabel7, -2, 60,
																																						-2)).addGap(18, 18, 18)
																														.addGroup(painelFiltrosLayout
																																		.createParallelGroup(
																																						GroupLayout.Alignment.LEADING)
																																		.addComponent(this.tfcodprd, -2,
																																						125, -2)
																																		.addComponent(this.cbfiltros)
																																		.addGroup(painelFiltrosLayout
																																						.createParallelGroup(
																																										GroupLayout.Alignment.TRAILING,
																																										false)
																																						.addComponent(
																																										this.tftipos,
																																										GroupLayout.Alignment.LEADING,
																																										-1, 1014, 32767)
																																						.addComponent(
																																										this.tffornecedores,
																																										GroupLayout.Alignment.LEADING))))
																										.addGroup(painelFiltrosLayout
																														.createSequentialGroup().addGroup(
																																		painelFiltrosLayout
																																						.createParallelGroup(
																																										GroupLayout.Alignment.TRAILING)
																																						.addComponent(jLabel17,
																																										-2, 88, -2)
																																						.addComponent(jLabel18))
																														.addGap(18, 18, 18).addGroup(
																																		painelFiltrosLayout
																																						.createParallelGroup(
																																										GroupLayout.Alignment.LEADING,
																																										false)
																																						.addComponent(
																																										this.cbsinalCusto,
																																										0, 253, 32767)
																																						.addComponent(
																																										this.cbsinalQtd,
																																										0, -1, 32767))
																														.addGap(18, 18, 18).addGroup(
																																		painelFiltrosLayout
																																						.createParallelGroup(
																																										GroupLayout.Alignment.LEADING)
																																						.addGroup(
																																										GroupLayout.Alignment.TRAILING,
																																										painelFiltrosLayout
																																														.createSequentialGroup()
																																														.addComponent(
																																																		this.tfcusto1,
																																																		-2,
																																																		90,
																																																		-2)
																																														.addPreferredGap(
																																																		LayoutStyle.ComponentPlacement.UNRELATED)
																																														.addComponent(
																																																		this.tfcusto2,
																																																		-2,
																																																		91,
																																																		-2))
																																						.addGroup(
																																										GroupLayout.Alignment.TRAILING,
																																										painelFiltrosLayout
																																														.createSequentialGroup()
																																														.addComponent(
																																																		this.tfqtd1,
																																																		-2,
																																																		90,
																																																		-2)
																																														.addPreferredGap(
																																																		LayoutStyle.ComponentPlacement.UNRELATED)
																																														.addComponent(
																																																		this.tfqtd2,
																																																		-2,
																																																		91,
																																																		-2))))
																										.addGroup(painelFiltrosLayout
																														.createSequentialGroup().addGroup(
																																		painelFiltrosLayout
																																						.createParallelGroup(
																																										GroupLayout.Alignment.LEADING,
																																										false)
																																						.addComponent(jLabel15,
																																										-1, -1, 32767)
																																						.addComponent(jLabel5,
																																										-2, 96, -2))
																														.addGap(11, 11, 11).addGroup(
																																		painelFiltrosLayout
																																						.createParallelGroup(
																																										GroupLayout.Alignment.LEADING)
																																						.addComponent(
																																										this.tfcentrol,
																																										-2, 162, -2)
																																						.addGroup(
																																										painelFiltrosLayout
																																														.createSequentialGroup()
																																														.addGroup(
																																																		painelFiltrosLayout
																																																						.createParallelGroup(
																																																										GroupLayout.Alignment.LEADING)
																																																						.addComponent(
																																																										this.tfdescIni,
																																																										-2,
																																																										68,
																																																										-2)
																																																						.addComponent(
																																																										jLabel9))
																																														.addGap(18,
																																																		18,
																																																		18)
																																														.addGroup(
																																																		painelFiltrosLayout
																																																						.createParallelGroup(
																																																										GroupLayout.Alignment.LEADING)
																																																						.addComponent(
																																																										jLabel10)
																																																						.addComponent(
																																																										this.tfdescFim,
																																																										-2,
																																																										71,
																																																										-2)))))
																										.addGroup(painelFiltrosLayout
																														.createSequentialGroup()
																														.addComponent(jLabel8, -2, 96, -2)
																														.addPreferredGap(
																																		LayoutStyle.ComponentPlacement.UNRELATED)
																														.addGroup(painelFiltrosLayout
																																		.createParallelGroup(
																																						GroupLayout.Alignment.LEADING,
																																						false)
																																		.addComponent(jLabel11, -1, -1,
																																						32767)
																																		.addComponent(this.tflojaloc,
																																						-2, 72, -2))
																														.addGap(21, 21, 21).addGroup(
																																		painelFiltrosLayout
																																						.createParallelGroup(
																																										GroupLayout.Alignment.LEADING)
																																						.addComponent(jLabel12)
																																						.addComponent(
																																										this.tflocalizacao,
																																										-2, 351, -2)))
																										.addGroup(painelFiltrosLayout
																														.createSequentialGroup()
																														.addComponent(jLabel2).addPreferredGap(
																																		LayoutStyle.ComponentPlacement.UNRELATED)
																														.addComponent(this.lbstatus, -2, 533,
																																		-2)).addGroup(
																										painelFiltrosLayout.createSequentialGroup()
																														.addComponent(btbuscar, -2, 109, -2)
																														.addGap(18, 18, 18)
																														.addComponent(this.btexecutar, -2, 112,
																																		-2).addGap(18, 18, 18)
																														.addComponent(btnAbreDesfazer, -2, 118,
																																		-2))).addGap(82, 82, 82)))));
		painelFiltrosLayout.setVerticalGroup(
						painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
										painelFiltrosLayout.createSequentialGroup().addGap(17, 17, 17).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel4).addComponent(this.cbfiltros))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel16, -2, 31, -2)
																		.addComponent(this.tfcodprd, -2, -1, -2))
														.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel6)
																		.addComponent(this.tffornecedores, -2, -1, -2))
														.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(this.tftipos, -2, -1, -2).addComponent(jLabel7))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel9).addComponent(jLabel10))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel5).addComponent(this.tfdescIni, -2, -1, -2)
																		.addComponent(this.tfdescFim, -2, -1, -2))
														.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel15, -2, 31, -2)
																		.addComponent(this.tfcentrol, -2, -1, -2)).addGap(3, 3, 3)
														.addGroup(painelFiltrosLayout
																		.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel11).addComponent(jLabel12))
														.addGap(12, 12, 12).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel8).addComponent(this.tflojaloc, -2, -1, -2)
																		.addComponent(this.tflocalizacao, -2, -1, -2))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel17, -2, 31, -2)
																		.addComponent(this.cbsinalQtd, -2, -1, -2)
																		.addComponent(this.tfqtd1, -2, -1, -2)
																		.addComponent(this.tfqtd2, -2, -1, -2))
														.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(this.cbsinalCusto, -2, -1, -2)
																		.addComponent(this.tfcusto1, -2, -1, -2)
																		.addComponent(this.tfcusto2, -2, -1, -2)
																		.addComponent(jLabel18, -2, 31, -2))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel20, -2, 31, -2)
																		.addComponent(this.cbIncluiNFs))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel21, -2, 31, -2)
																		.addComponent(this.tfFornecedorNF, -2, -1, -2))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel24).addComponent(jLabel23))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
														.addGroup(painelFiltrosLayout
																		.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(jLabel22, -2, 20, -2)
																		.addComponent(this.tfDtNfIni, -2, -1, -2)
																		.addComponent(this.tfDtNfFim, -2, -1, -2)
																		.addComponent(jLabel25, -2, 20, -2)).addGap(18, 18, 18)
														.addGroup(painelFiltrosLayout
																		.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(btbuscar).addComponent(this.btexecutar)
																		.addComponent(btnAbreDesfazer))
														.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
														painelFiltrosLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
																		.addComponent(this.lbstatus, -2, 22, -2).addComponent(jLabel2))
														.addGap(58, 58, 58)));
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						layout.createSequentialGroup().addContainerGap().addGroup(
										layout.createParallelGroup(GroupLayout.Alignment.LEADING)
														.addComponent(painelDuplicacao, -1, -1, 32767).addGroup(
														layout.createSequentialGroup().addComponent(painelFiltros, -2, -1, -2)
																		.addGap(0, 0, 32767))).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						layout.createSequentialGroup().addContainerGap()
										.addComponent(painelDuplicacao, -2, -1, -2)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(painelFiltros, -2, 611, -2).addContainerGap(-1, 32767)));
		pack();
	}

	private void tfdescIniKeyReleased(KeyEvent evt) {
		if (this.tfdescIni.getText().length() >= 3) {
			this.tfdescFim.requestFocus();
		}
	}

	private void tfdescFimKeyReleased(KeyEvent evt) {
		if (this.tfdescFim.getText().length() >= 3) {
			this.tffornecedores.requestFocus();
		}
	}

	private void btbuscarActionPerformed(ActionEvent evt) {
		this.lbstatus.setText("");
		try {
			if (!this.tflojaDestino.getText().equals("") && !this.tffunc.getText().equals("")) {
				if (!this.tflocalizacao.getText().equals("") && this.tflojaloc.getText().equals("")) {
					JOptionPane.showMessageDialog(this,
									"Ao utilizar o filtro por Area, o campo loja da area se torna obrigatório");
				} else if (this.tflocalizacao.getText().equals("") &&
				           !this.tflojaloc.getText().equals("")) {
					JOptionPane.showMessageDialog(this,
									"Ao utilizar o filtro por Area, o campo descrição da area se torna obrigatório");
				} else {
					if (this.rbentrada.isSelected()) {
						this.base.setOperacao("entrada");
					} else {
						this.base.setOperacao("saida");
					}
					if (this.rbestoque.isSelected()) {
						this.base.setTipoCusto("est");
					} else {
						this.base.setTipoCusto("prec");
					}
					this.base.setLojaDestino(Integer.valueOf(Integer.parseInt(this.tflojaDestino.getText())));
					this.base.setCodFuncionario(Integer.valueOf(Integer.parseInt(this.tffunc.getText())));
					this.base.setCodprd(this.tfcodprd.getText());
					this.base.setDescIni(this.tfdescIni.getText());
					this.base.setDescFim(this.tfdescFim.getText());
					this.base.setFornecedores(this.tffornecedores.getText());
					this.base.setTipos(this.tftipos.getText());
					if (this.tflojaloc.getText().equals("")) {
						this.base.setLojaArea(Integer.valueOf(0));
					} else {
						this.base.setLojaArea(Integer.valueOf(Integer.parseInt(this.tflojaloc.getText())));
					}
					this.base.setAreas(this.tflocalizacao.getText());
					this.base.setCentrodeLucro(this.tfcentrol.getText());
					if (this.cbsinalQtd.getSelectedIndex() == 0) {
						this.base.setSinalQtd("todos");
					} else if (this.cbsinalQtd.getSelectedIndex() == 1) {
						this.base.setSinalQtd(">");
					} else if (this.cbsinalQtd.getSelectedIndex() == 2) {
						this.base.setSinalQtd("<");
					} else if (this.cbsinalQtd.getSelectedIndex() == 3) {
						this.base.setSinalQtd("=");
					} else {
						this.base.setSinalQtd("entre");
					}
					if (this.cbsinalCusto.getSelectedIndex() == 0) {
						this.base.setSinalCusto("todos");
					} else if (this.cbsinalCusto.getSelectedIndex() == 1) {
						this.base.setSinalCusto(">");
					} else if (this.cbsinalCusto.getSelectedIndex() == 2) {
						this.base.setSinalCusto("<");
					} else if (this.cbsinalCusto.getSelectedIndex() == 3) {
						this.base.setSinalCusto("=");
					} else {
						this.base.setSinalCusto("entre");
					}
					if (this.tfqtd1.getText().equals("")) {
						this.base.setQtd1(BigDecimal.ZERO);
					} else {
						this.base.setQtd1(new BigDecimal(this.tfqtd1.getText().replace(",", ".")));
					}
					if (this.tfqtd2.getText().equals("")) {
						this.base.setQtd2(BigDecimal.ZERO);
					} else {
						this.base.setQtd2(new BigDecimal(this.tfqtd2.getText().replace(",", ".")));
					}
					if (this.tfcusto1.getText().equals("")) {
						this.base.setCusto1(BigDecimal.ZERO);
					} else {
						this.base.setCusto1(new BigDecimal(this.tfcusto1.getText().replace(",", ".")));
					}
					if (this.tfcusto2.getText().equals("")) {
						this.base.setCusto2(BigDecimal.ZERO);
					} else {
						this.base.setCusto2(new BigDecimal(this.tfcusto2.getText().replace(",", ".")));
					}
					boolean liberado = false;
					if (this.cbIncluiNFs.isSelected()) {
						this.base.setIncluiNfFornecedor(Boolean.valueOf(true));
						this.base.setDtNfIni(Integer.valueOf(0));
						this.base.setDtNfFim(Integer.valueOf(0));
						this.base.setFornecedorNf("");
						liberado = true;
					} else {
						if (this.tfFornecedorNF.getText().equals("")) {
							this.base.setFornecedorNf("");
							liberado = true;
						}
						Boolean temErro = Boolean.valueOf(false);
						String dtFormatada = "";
						if (this.tfDtNfIni.getText().replace("/", "").replace(" ", "").equals("")) {
							liberado = true;
							this.base.setDtNfIni(Integer.valueOf(0));
						} else {
							try {
								dtFormatada = this.tfDtNfIni.getText().split("/")[2] +
								              this.tfDtNfIni.getText().split("/")[1] +
								              this.tfDtNfIni.getText().split("/")[0];
								this.base.setDtNfIni(Integer.valueOf(dtFormatada));
							} catch (Exception e) {
								temErro = Boolean.valueOf(true);
								liberado = false;
								JOptionPane.showMessageDialog(this,
												"Valor informado no campo data inicial é invalido, infone no formato: DD/MM/YYYY");
							}
						}
						if (this.tfDtNfFim.getText().replace("/", "").replace(" ", "").equals("")) {
							liberado = true;
							this.base.setDtNfFim(Integer.valueOf(0));
						} else {
							try {
								dtFormatada = this.tfDtNfFim.getText().split("/")[2] +
								              this.tfDtNfFim.getText().split("/")[1] +
								              this.tfDtNfFim.getText().split("/")[0];
								this.base.setDtNfFim(Integer.valueOf(dtFormatada));
							} catch (Exception e) {
								temErro = Boolean.valueOf(true);
								liberado = false;
								JOptionPane.showMessageDialog(this,
												"Valor informado no campo data final é invalido, infone no formato: DD/MM/YYYY");
							}
						}
						if (this.base.getDtNfIni().intValue() == 0 && this.base.getDtNfFim().intValue() != 0) {
							JOptionPane.showMessageDialog(this,
											"Se uma data for preenchida a outra se torna obrigatória");
							temErro = Boolean.valueOf(true);
							liberado = false;
						}
						if (this.base.getDtNfIni().intValue() != 0 && this.base.getDtNfFim().intValue() == 0) {
							JOptionPane.showMessageDialog(this,
											"Se uma data for preenchida a outra se torna obrigatória");
							temErro = Boolean.valueOf(true);
							liberado = false;
						}
						if (!temErro.booleanValue()) {
							this.base.setFornecedorNf(this.tfFornecedorNF.getText());
							this.base.setIncluiNfFornecedor(Boolean.valueOf(false));
							liberado = true;
						}
					}
					if (liberado) {
						this.base.setUsaFiltrosProduto(Boolean.valueOf(this.cbfiltros.isSelected()));
						GestorDADOS gestorDADOS = new GestorDADOS(null);
						List<Produtos> produtos = gestorDADOS.listar(this.base);
						BigDecimal valorTotal = BigDecimal.ZERO;
						for (Produtos prd : produtos) {
							valorTotal = valorTotal.add(prd.getTotal());
						}
						Listagem listagem = new Listagem();
						listagem.setDados(produtos, valorTotal);
						listagem.setVisible(true);
						this.btexecutar.setEnabled(true);
					}
				}
			} else {
				JOptionPane.showMessageDialog(this,
								"O Campo Loja de Destino e o código do funcionário são obrigatórios");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Não foi possível Listar os produtos! Erro:" + e);
			e.printStackTrace();
		}
	}

	private void tfdescIniFocusLost(FocusEvent evt) {
		this.tfdescIni.setText(this.tfdescIni.getText().toUpperCase());
	}

	private void tfdescFimFocusLost(FocusEvent evt) {
		this.tfdescFim.setText(this.tfdescFim.getText().toUpperCase());
	}

	private void btexecutarActionPerformed(ActionEvent evt) {
		int resp = JOptionPane.showConfirmDialog(this, "Tem Certeza?", "Alerta", 0);
		if (resp == 0) {
			try {
				Integer nota = Integer.valueOf(0);
				GestorDADOS gestorDADOS = new GestorDADOS(null);
				nota = gestorDADOS.executar(this.base);
				this.lbstatus.setText("Nota de movimentação gerada: " + nota);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Não foi possível gerar a movimentação! Erro:" + e);
				e.printStackTrace();
			}
			this.btexecutar.setEnabled(false);
		}
	}

	private void tflocalizacaoFocusLost(FocusEvent evt) {
		this.tflocalizacao.setText(this.tflocalizacao.getText().toUpperCase());
	}

	private void cbfiltrosActionPerformed(ActionEvent evt) {
		if (this.cbfiltros.isSelected()) {
			this.tfcodprd.setEnabled(true);
			this.tfdescIni.setEnabled(true);
			this.tfdescFim.setEnabled(true);
			this.tffornecedores.setEnabled(true);
			this.tftipos.setEnabled(true);
			this.tflojaloc.setEnabled(true);
			this.tflocalizacao.setEnabled(true);
			this.tfcentrol.setEnabled(true);
			this.tfqtd1.setEnabled(true);
			this.tfqtd2.setEnabled(true);
			this.tfcusto1.setEnabled(true);
			this.tfcusto2.setEnabled(true);
		} else {
			this.tfcodprd.setEnabled(false);
			this.tfcodprd.setText("");
			this.tfdescIni.setEnabled(false);
			this.tfdescIni.setText("");
			this.tfdescFim.setEnabled(false);
			this.tfdescFim.setText("");
			this.tffornecedores.setEnabled(false);
			this.tffornecedores.setText("");
			this.tftipos.setEnabled(false);
			this.tftipos.setText("");
			this.tflojaloc.setEnabled(false);
			this.tflojaloc.setText("");
			this.tflocalizacao.setEnabled(false);
			this.tflocalizacao.setText("");
			this.tfcentrol.setEnabled(false);
			this.tfcentrol.setText("");
			this.tfqtd1.setEnabled(false);
			this.tfqtd1.setText("");
			this.tfqtd2.setEnabled(false);
			this.tfqtd2.setText("");
			this.tfcusto1.setEnabled(false);
			this.tfcusto1.setText("");
			this.tfcusto2.setEnabled(false);
			this.tfcusto2.setText("");
		}
	}

	private void tfcentrolFocusLost(FocusEvent evt) {
	}

	private void tfcentrolKeyReleased(KeyEvent evt) {
	}

	private void tfcodprdFocusLost(FocusEvent evt) {
	}

	private void tfcodprdKeyReleased(KeyEvent evt) {
	}

	private void btnAbreDesfazerActionPerformed(ActionEvent evt) {
		this.tfdloja.setText("");
		this.tfdnota.setText("");
		this.DialogDesfazer.setSize(280, 240);
		this.DialogDesfazer.setModal(true);
		this.DialogDesfazer.setLocationRelativeTo(this);
		this.DialogDesfazer.setVisible(true);
	}

	private void btnDesfazerActionPerformed(ActionEvent evt) {
		try {
			GestorDADOS dao = new GestorDADOS(null);
			Boolean valido = Boolean.valueOf(false);
			if (this.cbdtipo.getSelectedIndex() == 0) {
				valido = dao.validarNfEntrada(Integer.valueOf(this.tfdloja.getText()),
								Integer.valueOf(this.tfdnota.getText()));
				if (valido.booleanValue()) {
					dao.desfazerEntrada(Integer.valueOf(this.tfdloja.getText()),
									Integer.valueOf(this.tfdnota.getText()));
					JOptionPane.showMessageDialog(this, "Movimentacao referente a nota: " +
					                                    this.tfdnota.getText() +
					                                    " da loja: " +
					                                    this.tfdloja.getText() +
					                                    " foi desfeita com sucesso!");
					this.DialogDesfazer.dispose();
				} else {
					JOptionPane.showMessageDialog(this, "Informe uma nota válida!");
				}
			} else {
				valido = dao.validarNfSaida(Integer.valueOf(this.tfdloja.getText()),
								Integer.valueOf(this.tfdnota.getText()));
				if (valido.booleanValue()) {
					dao.desfazerSaida(Integer.valueOf(this.tfdloja.getText()),
									Integer.valueOf(this.tfdnota.getText()));
					JOptionPane.showMessageDialog(this, "Movimentacao referente a nota: " +
					                                    this.tfdnota.getText() +
					                                    " da loja: " +
					                                    this.tfdloja.getText() +
					                                    " foi desfeita com sucesso!");
					this.DialogDesfazer.dispose();
				} else {
					JOptionPane.showMessageDialog(this, "Informe uma nota válida!");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Não foi possível executar o processo! Erro:" + e);
		}
	}

	private void tfFornecedorNFFocusLost(FocusEvent evt) {
	}

	private void tfFornecedorNFKeyReleased(KeyEvent evt) {
	}

	private void tfcodprdActionPerformed(ActionEvent evt) {
	}

	private void cbIncluiNFsActionPerformed(ActionEvent evt) {
		if (this.cbIncluiNFs.isSelected()) {
			this.tfFornecedorNF.setEnabled(false);
			this.tfDtNfIni.setEnabled(false);
			this.tfDtNfFim.setEnabled(false);
		} else {
			this.tfFornecedorNF.setEnabled(true);
			this.tfDtNfIni.setEnabled(true);
			this.tfDtNfFim.setEnabled(true);
		}
	}
}


/* Location:              /home/ivaneyvieira/Dropbox/engecopi/ajustes/ajusteEstoque.jar!/br/com/consutec/gui/Main.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */