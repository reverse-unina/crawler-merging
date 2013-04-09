package it.unina.android.ripper_vs_intent_executor;

import java.awt.Desktop;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.GridLayout;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
public class GUI extends JFrame {

	private JPanel contentPane;
	private JTextField txtInsertActivitiesxmlPath;
	private JTextField txtActivitiesFoldersPath;
	private JLabel lblDone;
	private JLabel lblReportPath;
	private JButton btnOpenReport;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setTitle("RipperXMLvsIntentExecutor");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(4, 0, 0, 0));

		JPanel panel = new JPanel();
		contentPane.add(panel);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		txtInsertActivitiesxmlPath = new JTextField();
		txtInsertActivitiesxmlPath.setText("Insert activities.xml's path here...");
		panel.add(txtInsertActivitiesxmlPath);
		txtInsertActivitiesxmlPath.setColumns(20);

		JButton btnNewButton = new JButton("Browse");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter(".xml", "xml"));
				if(chooser.showOpenDialog(null)!=JFileChooser.CANCEL_OPTION)
					txtInsertActivitiesxmlPath.setText(chooser.getSelectedFile().getAbsolutePath());

			}
		});
		panel.add(btnNewButton);

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1);

		txtActivitiesFoldersPath = new JTextField();
		txtActivitiesFoldersPath.setText("Insert activities folder's path here...");
		panel_1.add(txtActivitiesFoldersPath);
		txtActivitiesFoldersPath.setColumns(20);

		JButton btnNewButton_1 = new JButton("Browse");
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(chooser.showOpenDialog(null)!=JFileChooser.CANCEL_OPTION)
					txtActivitiesFoldersPath.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});
		panel_1.add(btnNewButton_1);

		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2);

		JButton btnGo = new JButton("GO!");

		panel_2.add(btnGo);

		lblDone = new JLabel("Done!");
		lblDone.setEnabled(true);
		lblDone.setVisible(false);
		panel_2.add(lblDone);

		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3);
		lblReportPath = new JLabel("Report");
		panel_3.add(lblReportPath);
		lblReportPath.setEnabled(true);
		
		btnOpenReport = new JButton("Open");
		
		panel_3.add(btnOpenReport);
		btnOpenReport.setVisible(false);
		lblReportPath.setVisible(false);
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RipperIntentExecutorComparator comparator = new RipperIntentExecutorComparator(txtInsertActivitiesxmlPath.getText(), txtActivitiesFoldersPath.getText());
				comparator.start();
				try {
					comparator.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lblDone.setVisible(true);
				lblReportPath.setText("Report created");
				
				lblReportPath.setVisible(true);
				btnOpenReport.setVisible(true);
			}
		});
		btnOpenReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.open(new File(txtActivitiesFoldersPath.getText() + File.separator + "report.xml"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

}
