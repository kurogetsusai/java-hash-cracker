import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Alert;
import javafx.application.Platform;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.HashSet;
import java.security.MessageDigest;
import java.nio.charset.Charset;

public class MainWindow implements Initializable {
	@FXML private AnchorPane anchorPane;
	@FXML private Label hashLabel;
	@FXML private TextField hashTextField;
	@FXML private Label charsLabel;
	@FXML private TextField charsTextField;
	@FXML private ToggleGroup mode;
	@FXML private RadioButton md5Radio;
	@FXML private RadioButton sha1Radio;
	@FXML private Button button;
	@FXML private ProgressIndicator progress;

	private Thread crackerThread;

	public void initialize(URL url, ResourceBundle rb) {
	}

	@FXML
	private void crack(ActionEvent event) {
		if (progress.isVisible()) {
			crackerThread.stop();
			progress.setVisible(false);
			hashLabel.setDisable(false);
			hashTextField.setDisable(false);
			charsLabel.setDisable(false);
			charsTextField.setDisable(false);
			md5Radio.setDisable(false);
			sha1Radio.setDisable(false);
			button.setText("Crack");

			return;
		}

		if (!( md5Radio.isSelected() && hashTextField.getText().length() == 32 && hashTextField.getText().matches("-?[0-9a-fA-F]+")) &&
		    !(sha1Radio.isSelected() && hashTextField.getText().length() == 40 && hashTextField.getText().matches("-?[0-9a-fA-F]+"))) {
			Alert a = new Alert(Alert.AlertType.ERROR);
			a.setTitle("Error!");
			a.setHeaderText(null);
			a.setContentText("Invalid hash!");
			a.showAndWait();

			return;
		}

		hashLabel.setDisable(true);
		hashTextField.setDisable(true);
		charsLabel.setDisable(true);
		charsTextField.setDisable(true);
		md5Radio.setDisable(true);
		sha1Radio.setDisable(true);
		progress.setVisible(true);
		button.setText("Cancel");

		String hashAlgorithm = md5Radio.isSelected() ? "MD5" : "SHA1";
		String hashString = hashTextField.getText().toLowerCase();

		// remove duplicates from chars
		String tmp = charsTextField.getText();
		Set<Character> characters = new HashSet<Character>();
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < tmp.length(); ++i) {
			char c = tmp.charAt(i);
			if (characters.add(c)) {
				s.append(c);
			}
		}
		String chars = s.toString();

		crackerThread = new Thread(new Runnable() {
			public void run() {
				StringBuffer s = new StringBuffer();
				String str, hash;
				int i, j;

				s.append(chars.charAt(0));

				do {
					// check string
					str = s.toString();
					hash = hashString(hashAlgorithm, str);

					if (hash.equals(hashString)) {
						final String solution = str;
						Platform.runLater(new Runnable() {
							public void run() {
								progress.setVisible(false);
								hashLabel.setDisable(false);
								hashTextField.setDisable(false);
								charsLabel.setDisable(false);
								charsTextField.setDisable(false);
								md5Radio.setDisable(false);
								sha1Radio.setDisable(false);
								button.setText("Crack");

								Alert a = new Alert(Alert.AlertType.INFORMATION);
								a.setTitle("Success!");
								a.setHeaderText(null);
								a.setContentText("The answer is \"" + solution + "\"!");
								a.showAndWait();
							}
						});
						break;
					}

					// inc string
					for (i = 0; i < s.length(); ++i) {
						if (s.charAt(s.length() - 1 - i) != chars.charAt(chars.length() - 1)) {
							s.setCharAt(s.length() - 1 - i, chars.charAt(chars.indexOf(s.charAt(s.length() - 1 - i)) + 1));
							for (j = s.length() - i; j < s.length(); ++j)
								s.setCharAt(j, chars.charAt(0));
							break;
						}

						if (i == s.length() - 1) {
							for (j = 0; j < s.length(); ++j)
								s.setCharAt(j, chars.charAt(0));
							s.append(chars.charAt(0));
							break;
						}
					}
				} while (true);

				return;
			}
		});
		crackerThread.start();
	}

	public String hashString(String hashAlgorithm, String str) {
		try {
			MessageDigest m = MessageDigest.getInstance(hashAlgorithm);
			StringBuffer hash = new StringBuffer();
			byte[] bytes = m.digest(str.getBytes(Charset.forName("UTF-8")));

			for (int i = 0; i < bytes.length; ++i)
				hash.append(Integer.toHexString((bytes[i] & 0xFF) | 0x100).substring(1, 3));

			return hash.toString();
		} catch (Exception e) {}

		return null;
	}
}
