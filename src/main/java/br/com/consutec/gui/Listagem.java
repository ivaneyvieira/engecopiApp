package br.com.consutec.gui;

import br.com.consutec.modelo.Produtos;
import br.com.consutec.modelo.ProdutosModelo;
import br.com.consutec.modelo.RowNumberTable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.EventQueue;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Listagem extends JFrame {
	ProdutosModelo pm;
	private JScrollPane jScrollPane1;
	private JLabel lbTotalGeral;
	private JTable tbprodutos;
	public Listagem() {
		initComponents();
		setExtendedState(6);
	}

	public static void main(String[] args) {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException ex) {
			Logger.getLogger(Listagem.class.getName()).log(Level.SEVERE, null, ex);
		}
		EventQueue.invokeLater(() -> (new Listagem()).setVisible(true));
	}

	public void setDados(List<Produtos> dados, BigDecimal total) {
		DecimalFormat df = new DecimalFormat("###,##0.0000");
		this.lbTotalGeral.setText(df.format(total));
		this.pm = new ProdutosModelo();
		this.pm.setDados(dados);
		this.tbprodutos.setModel(this.pm);
		DefaultTableCellRenderer alinhaCelulaDir = new DefaultTableCellRenderer();
		alinhaCelulaDir.setHorizontalAlignment(4);
		RowNumberTable rowNumberTable = new RowNumberTable(this.tbprodutos);
		this.tbprodutos.getColumn("Centro de Lucro").setCellRenderer(alinhaCelulaDir);
		this.jScrollPane1.setRowHeaderView(rowNumberTable);
		this.jScrollPane1.setCorner("UPPER_LEFT_CORNER", rowNumberTable.getTableHeader());
	}

	private void initComponents() {
		JLabel jLabel20 = new JLabel();
		this.lbTotalGeral = new JLabel();
		JPanel jPanel1 = new JPanel();
		this.jScrollPane1 = new JScrollPane();
		this.tbprodutos = new JTable();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Listagem");
		jLabel20.setFont(new Font("Tahoma", Font.BOLD, 18));
		jLabel20.setText("Total Geral: ");
		this.lbTotalGeral.setFont(new Font("Tahoma", Font.BOLD, 18));
		this.lbTotalGeral.setHorizontalAlignment(4);
		this.tbprodutos.setAutoCreateRowSorter(true);
		this.tbprodutos.setModel(new DefaultTableModel(new Object[0][],
            new String[]{"Codigo Prod", "Grade", "Descrição", "Fornecedor", "Centro de Lucro", "Tipo", "Quantidade", "Total"}));
		this.tbprodutos.setMaximumSize(null);
		this.jScrollPane1.setViewportView(this.tbprodutos);
		GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(
						jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 0, 32767)
										.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
														.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
																		.addComponent(this.jScrollPane1, -1, 1281, 32767)
																		.addContainerGap())));
		jPanel1Layout.setVerticalGroup(
						jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 758, 32767)
										.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
														.addGroup(GroupLayout.Alignment.TRAILING,
																		jPanel1Layout.createSequentialGroup().addContainerGap()
																						.addComponent(this.jScrollPane1, -1, 736, 32767)
																						.addContainerGap())));
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						layout.createSequentialGroup().addGroup(
										layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
														layout.createSequentialGroup().addContainerGap(891, 32767)
																		.addComponent(jLabel20)
																		.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(this.lbTotalGeral, -2, 279, -2)
																		.addGap(11, 11, 11)).addComponent(jPanel1, -1, -1, 32767))
										.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(GroupLayout.Alignment.TRAILING,
										layout.createSequentialGroup().addContainerGap()
														.addComponent(jPanel1, -2, -1, -2)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
														.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
																		.addComponent(jLabel20)
																		.addComponent(this.lbTotalGeral, -2, 22, -2))
														.addContainerGap()));
		pack();
	}
}


/* Location:              /home/ivaneyvieira/Dropbox/engecopi/ajustes/ajusteEstoque.jar!/br/com/consutec/gui/Listagem.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */