package com.marcello.merge;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Observable;
import java.util.Observer;

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
		class Text extends JTextArea{
			JTextArea textArea;
			private void updateTextArea(final String text) {
				  SwingUtilities.invokeLater(new Runnable() {
				    public void run() {
				      textArea.append(text);
				    }
				  });
				}
				 
				private void redirectSystemStreams() {
				  OutputStream out = new OutputStream() {
				    @Override
				    public void write(int b) throws IOException {
				      updateTextArea(String.valueOf((char) b));
				    }
				 
				    @Override
				    public void write(byte[] b, int off, int len) throws IOException {
				      updateTextArea(new String(b, off, len));
				    }
				 
				    @Override
				    public void write(byte[] b) throws IOException {
				      write(b, 0, b.length);
				    }
				  };
				 
				  System.setOut(new PrintStream(out, true));
				  System.setErr(new PrintStream(out, true));
				}
		}
		Text text = new Text();
		text.redirectSystemStreams();
		frame = new JFrame();
		frame.setBounds(100, 100, 500, 350);
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
				class obs implements Observer{
					@Override
					public void update(Observable o, Object arg) {
						JOptionPane.showMessageDialog(null,(String)arg);
					}					
				}
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
		frame.getContentPane().add(btnStart);
		JFrame outputFrame = new JFrame("Output");
		outputFrame.add(text.textArea);
		
		outputFrame.setBounds(100, 100, 500, 350);
		outputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		outputFrame.getContentPane().setLayout(null);
		outputFrame.setVisible(true);
	}
}
