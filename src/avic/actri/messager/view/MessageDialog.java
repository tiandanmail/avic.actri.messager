/**
 * @copyright actri.avic
 */
package avic.actri.messager.view;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import avic.actri.messager.Activator;
import avic.actri.messager.internal.IConstants;
import avic.actri.messager.internal.LocalUser;

/**
 * 消息对话框
 * 
 * @author tdan
 */
public class MessageDialog extends Dialog {

	private boolean sendFile = false;

	private Text text_message;

	private String messageToSend = null;

	private String showMessage = null;

	private String toHostName = null;

	private DialogType type = DialogType.SendMsg;

	/**
	 * Create the dialog
	 * 
	 * @param parentShell
	 */
	public MessageDialog(Shell parentShell, String message, String toHostName,
			DialogType type) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM);
		if (message != null) {
			showMessage = message;
		}
		this.toHostName = toHostName;
		this.type = type;
	}

	/**
	 * Create contents of the dialog
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout());

		text_message = new Text(container, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI
				| SWT.BORDER);
		if (showMessage != null) {
			text_message.setText(showMessage);
			text_message.setEditable(false);
		} else {
			text_message.setText(LocalUser.getLocalUser().getHostInfo()
					.getHostName()
					+ "说： ");
			text_message.setFocus();
		}

		final DropTarget text_messageDropTarget = new DropTarget(text_message,
				DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		Transfer[] transfer = new Transfer[] { FileTransfer.getInstance() };
		text_messageDropTarget.setTransfer(transfer);
		text_messageDropTarget.addDropListener(new DropTargetListener() {

			public void dragEnter(DropTargetEvent event) {
			}

			public void dragLeave(DropTargetEvent event) {
			}

			public void dragOperationChanged(DropTargetEvent event) {
			}

			public void dragOver(DropTargetEvent event) {
			}

			public void drop(DropTargetEvent event) {
				// TODO 对文件的合法性进行检查，是否存在，可读，重复,只发送文件,不发送文件夹
				text_message.setEditable(false);
				StringBuffer buffer = new StringBuffer();
				String[] datas = (String[]) event.data;
				for (int i = 0; i < datas.length; i++) {
					File file = new File(datas[i]);
					if (file.exists() && file.canRead() && !file.isDirectory()) {
						buffer.append(IConstants.SEND_FILE);
						// 组织发送消息，包括文件全路径和文件大小
						buffer.append(datas[i]);
						buffer.append(IConstants.SEND_FILE_SIZE_DIV);
						buffer.append(file.length());
						buffer.append(IConstants.SEND_FILE_DIV);

					}
				}
				messageToSend = buffer.toString();
				text_message.append(messageToSend);
				sendFile = true;
			}

			public void dropAccept(DropTargetEvent event) {
			}
		});
		return container;
	}

	/**
	 * Create contents of the button bar
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, 900, (type == DialogType.RecFile) ? "接收文件"
				: "选择发送文件", true);
		createButton(parent, IDialogConstants.OK_ID, showMessage == null ? "发送"
				: "回复", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "关闭", false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(426, 202);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			// 点击回复，使能文本框
			if (showMessage != null) {
				text_message.setEditable(true);
				getButton(IDialogConstants.OK_ID).setText("发送");
				text_message.setFocus();
				text_message.append(LocalUser.getLocalUser().getHostInfo()
						.getHostName()
						+ "说： ");
				showMessage = null;
				return;
			}
			// 点击发送
			else {
				messageToSend = text_message.getText() + "\r\n";
			}
		} else if (buttonId == 900) {
			// //
			if (type == DialogType.RecFile) {
				DirectoryDialog dd = new DirectoryDialog(getShell());
				dd.setMessage("请选择存储路径");
				messageToSend = dd.open();
			} else {
				FileDialog fd = new FileDialog(getShell());
				String tosend = fd.open();
				text_message.setEditable(false);
				StringBuffer buffer = new StringBuffer();
				buffer.append(IConstants.SEND_FILE);
				// 组织发送消息，包括文件全路径和文件大小
				buffer.append(tosend);
				buffer.append(IConstants.SEND_FILE_SIZE_DIV);
				File file = new File(tosend);
				buffer.append(file.length());
				buffer.append(IConstants.SEND_FILE_DIV);
				messageToSend = buffer.toString();
				text_message.append(messageToSend);
				sendFile = true;
			}
			buttonId = IDialogConstants.OK_ID;
		}
		super.buttonPressed(buttonId);
	}

	public String getMessageToSend() {
		return messageToSend;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("正在" + toHostName + "对话");
		newShell.setImage(ResourceManager.getPluginImage(
				Activator.getDefault(), "icons/qq48.GIF"));
	}

	public boolean isSendFile() {
		return sendFile;
	}
}

enum DialogType {
	SendMsg, RecMes, SendFile, RecFile
}
