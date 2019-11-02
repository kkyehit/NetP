package actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExitListener implements ActionListener {
	// Exit 버튼 눌렸을 때.
	public void actionPerformed(ActionEvent event) {
		System.exit(0);
	}
}
