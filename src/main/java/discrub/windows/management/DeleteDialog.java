package discrub.windows.management;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import discrub.Main;
import discrub.domain.Channel;
import discrub.domain.Conversation;
import discrub.domain.DiscordAccount;
import discrub.domain.Message;
import discrub.services.MessageService;
import discrub.windows.Configuration;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class DeleteDialog extends JDialog {

	/**
	 * Create the dialog.
	 */
	@SuppressWarnings("deprecation")
	public DeleteDialog(JFrame main, DiscordAccount discordAccount, int[] selectedRows, List<Message> messages,
			Object o) {
		MessageService ms = new MessageService();
		try {setIconImage(ImageIO.read(Main.class.getResourceAsStream("/logo.png")));} catch (IOException e) {}
		setResizable(false);
		setBounds(100, 100, 426, 99);
		setTitle("Discrub");
		setLocation(main.getLocation());
		main.disable();
		JTextArea debugTextBox = new JTextArea();
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(debugTextBox, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(debugTextBox, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(88, Short.MAX_VALUE))
		);
		getContentPane().setLayout(groupLayout);
		List<Message> selectedMessages = new ArrayList<Message>();

		Collections.reverse(messages);
		for (int row : selectedRows) {
			selectedMessages.add(messages.get(row));
		}

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				for (Message message : selectedMessages) {
					boolean successful = false;
					while (!successful) {
						Date now = new Date();
						String dateStr = now.getHours() + ":" + now.getMinutes() + ":" + now.getSeconds();
						debugTextBox.setText(dateStr + " - Deleting message with id " + message.getId() + "\n"
								+ debugTextBox.getText());
						if (o instanceof Conversation) {
							Conversation conversation = (Conversation) o;
							successful = ms.deleteMessage(message, conversation.getId(), discordAccount);
						} else if (o instanceof Channel) {
							Channel channel = (Channel) o;
							successful = ms.deleteMessage(message, channel.getId(), discordAccount);
						}
					}
				}
				debugTextBox.setText("Messages Deleted!\n" + debugTextBox.getText());
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				thread.stop();
				main.enable();
				main.setContentPane(new Configuration(discordAccount));
				Point oldLoc = main.getLocation();
				main.setVisible(false);
				main.setBounds(100, 100, 249, 197);
				main.setLocation(oldLoc);
				main.setVisible(true);
				main.revalidate();
				main.repaint();
			}
		});
		thread.start();
	}
}
