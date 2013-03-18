package com.marcello.merge;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class GUI{

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
					//window.outputFrame.setVisible(true);
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

		frame = new JFrame("Crawler Merging");
		frame.setBounds(100, 100, 500, 170);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 0, 10, 0));
		frame.getContentPane().add(panel, BorderLayout.NORTH);

		txtInsertFilesPath = new JTextField();
		panel.add(txtInsertFilesPath);
		txtInsertFilesPath.setText("Insert file's path here");
		txtInsertFilesPath.setBounds(6, 70, 279, 28);
		txtInsertFilesPath.setColumns(20);

		JButton btnBrowse = new JButton("Browse");
		panel.add(btnBrowse);
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				if(chooser.getSelectedFile()!=null)
					txtInsertFilesPath.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});
		btnBrowse.setBounds(327, 71, 117, 29);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 0, 0, 0));
		frame.getContentPane().add(panel_1, BorderLayout.CENTER);

		JButton btnStart = new JButton("Start!");
		panel_1.add(btnStart);
		btnStart.addMouseListener(new MouseAdapter() {
			class obs implements Observer{
				@Override
				public void update(Observable o, Object arg) {
					JOptionPane.showMessageDialog(null,arg);							
				}
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
				String arg;

				if(!txtInsertFilesPath.getText().equals("Insert file's path here")){
					arg = txtInsertFilesPath.getText();
					CrawlerMerging merging = new CrawlerMerging(arg);
					merging.addObserver(new obs());
					Thread t = new Thread(merging);
					t.start();
				}
				else{
					JOptionPane.showMessageDialog(null,"Select file's path first");
				}
			}
		});
		btnStart.setBounds(310, 215, 117, 29);

		JPanel panel_2 = new JPanel();
		frame.getContentPane().add(panel_2, BorderLayout.SOUTH);

		JLabel lblDevelopedByMarcello = new JLabel("Developed by Marcello Traiola");
		panel_2.add(lblDevelopedByMarcello);
		lblDevelopedByMarcello.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		lblDevelopedByMarcello.setBounds(284, 256, 160, 16);

	}

}
