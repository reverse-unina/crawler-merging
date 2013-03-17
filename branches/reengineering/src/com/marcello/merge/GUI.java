package com.marcello.merge;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GUI {

	private JFrame frame;
	private JTextField txtInsertFilesPath;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblCrawlerMerging = new JLabel("Crawler Merging");
		lblCrawlerMerging.setHorizontalAlignment(SwingConstants.CENTER);
		lblCrawlerMerging.setBounds(6, 6, 438, 16);
		frame.getContentPane().add(lblCrawlerMerging);

		JLabel lblDevelopedByMarcello = new JLabel("Developed by Marcello Traiola");
		lblDevelopedByMarcello.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		lblDevelopedByMarcello.setBounds(284, 256, 160, 16);
		frame.getContentPane().add(lblDevelopedByMarcello);

		txtInsertFilesPath = new JTextField();
		txtInsertFilesPath.setText("Insert file's path here");
		txtInsertFilesPath.setBounds(6, 70, 279, 28);
		frame.getContentPane().add(txtInsertFilesPath);
		txtInsertFilesPath.setColumns(10);

		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				txtInsertFilesPath.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});
		btnBrowse.setBounds(327, 71, 117, 29);
		frame.getContentPane().add(btnBrowse);

		JButton btnStart = new JButton("Start!");
		btnStart.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
				String arg;
				if(!txtInsertFilesPath.getText().equals("Insert file's path here")){
					arg = txtInsertFilesPath.getText();
					CrawlerMerging merging = new CrawlerMerging(arg);
					merging.start();
				}
				else{
					JOptionPane.showMessageDialog(null,"Select file's path first");
				}
			}
		});
		btnStart.setBounds(310, 215, 117, 29);
		frame.getContentPane().add(btnStart);
	}
}
