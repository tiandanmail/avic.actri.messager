/**
 * @copyright actri.avic
 */
package avic.actri.messager.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * �����û���������Ϊ����
 * 
 * @author tdan 2020-12-04
 */
public class LocalUser implements ILocalUser {

	private static ILocalUser localUser = null;

	private IHostInfo hostInfo = null;

	private LocalUser() {
		try {
			InetAddress address = InetAddress.getLocalHost();
			hostInfo = new HostInfo(address.getHostName(),
					address.getHostAddress(), IConstants.PORT_LISTEN);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static ILocalUser getLocalUser() {
		if (localUser == null) {
			localUser = new LocalUser();
		}
		return localUser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see avic1.actri.tools.etalk.ILocalUser#getHostInfo()
	 */
	public IHostInfo getHostInfo() {
		return hostInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see avic1.actri.tools.etalk.ILocalUser#notifyOffline()
	 */
	public void notifyOffline() {
		broadcast(CommandCenter.notifyOffLineCommand());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see avic1.actri.tools.etalk.ILocalUser#notifyOnline()
	 */
	public void notifyOnline() {
		broadcast(CommandCenter.notifyOnLineCommand());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see avic1.actri.tools.etalk.ILocalUser#upDate()
	 */
	public void upDate() {
		broadcast(CommandCenter.UpdateCommand());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see avic1.actri.tools.etalk.ILocalUser#upDate(IHostInfo)
	 */
	public void upDate(IHostInfo toHost) {
		singleSend(toHost, CommandCenter.UpdateCommand());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see avic1.actri.tools.etalk.ILocalUser#sendMessage()
	 */
	public void sendMessage(IHostInfo toHost, String message) {
		singleSend(toHost, CommandCenter.sendMessageCommand(message));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see avic1.actri.tools.etalk.ILocalUser#sendFile()
	 */
	public void sendFile(final IHostInfo toHost, String message) {
		singleSend(toHost, CommandCenter.sendFileCommand(message));
		// ����message�����Ҫ���͵��ļ��б�
		String[] fileNames = message.split(IConstants.SEND_FILE);
		if (fileNames.length > 0) {
			final ArrayList<File> files = new ArrayList<File>();
			for (int i = 0; i < fileNames.length; i++) {
				if (fileNames[i].length() < 2) {
					continue;
				}
				String fileNameAndSize = fileNames[i].substring(0,
						fileNames[i].length() - 2);
				int index = -1;
				if ((index = fileNameAndSize
						.indexOf(IConstants.SEND_FILE_SIZE_DIV)) != -1) {
					String filename = fileNameAndSize.substring(0, index);
					File file = new File(filename);
					if (file.exists() && file.canRead()) {
						files.add(file);
					}
				}
			}
			// ������Ϣ֮����Socket�����˿�,����ֻ����ͬʱ��һ���û������ļ�
			Job sendJob = new Job("senFile") {
				protected IStatus run(IProgressMonitor monitor) {
					if (files.size() > 0) {
						ServerSocket serversocket = null;
						Socket socket = null;
						try {
							// �ȴ��Է��Դ����ļ�����Ӧ
							serversocket = new ServerSocket(
									IConstants.PORT_SEND_CONTRAL);
							socket = serversocket.accept();
							// ��ȡ�Է��Դ����ļ�����Ӧ
							int reaction = socket.getInputStream().read();
							serversocket.close();
							socket.close();

							if (reaction == IConstants.SEND_ACCEPT) {
								serversocket = new ServerSocket(
										IConstants.PORT_SEND_FILE);
								socket = serversocket.accept();
								byte[] bytes = new byte[IConstants.SEND_BYTE_SIZE];
								for (int i = 0; i < files.size(); i++) {
									FileInputStream fis = null;
									try {
										fis = new FileInputStream(files.get(i));
										int readed = 0;
										while ((readed = fis.read(bytes)) > 0) {
											socket.getOutputStream().write(
													bytes, 0, readed);
										}
									} finally {
										if (fis != null) {
											fis.close();
										}
									}
								}
							} else {
								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										MessageDialog
												.openInformation(new Shell(),
														"�����ļ�", "�Է��ܾ������ļ�");
									}
								});
							}
						} catch (UnknownHostException e) {
						} catch (IOException e) {
						} finally {
							try {
								if (serversocket != null) {
									serversocket.close();
								}
							} catch (IOException e) {
							}
							try {
								if (socket != null) {
									socket.close();

								}
							} catch (IOException e) {
							}
						}
					}
					return Status.OK_STATUS;
				}

			};
			sendJob.setUser(true);
			sendJob.schedule();
		}
	}

	/**
	 * ��Ե㷢��
	 * 
	 * @param toHost
	 * @param command
	 */
	private void singleSend(IHostInfo toHost, byte[] command) {
		try {
			DatagramSocket ds = new DatagramSocket();
			ds.send(new DatagramPacket(command, command.length, InetAddress
					.getByName(toHost.getHostIP()), IConstants.PORT_LISTEN));
			ds.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �㲥����
	 * 
	 * @param command
	 */
	private void broadcast(byte[] command) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(IConstants.BROADCAST_ADDRESS);
			DatagramPacket datagramPacket = new DatagramPacket(command,
					command.length, inetAddress, IConstants.PORT_MUTI_LISTEN);

			MulticastSocket multicastSocket = new MulticastSocket();
			multicastSocket.send(datagramPacket);
			multicastSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
