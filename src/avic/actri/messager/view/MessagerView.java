/**
 * @copyright actri.avic
 */
package avic.actri.messager.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import avic.actri.messager.Activator;
import avic.actri.messager.internal.HostInfo;
import avic.actri.messager.internal.IConstants;
import avic.actri.messager.internal.IConstants.Command;
import avic.actri.messager.internal.IHostInfo;
import avic.actri.messager.internal.LocalUser;

/**
 * Messager��ͼ
 * 
 * @author tdan 2020-12-04
 */
public class MessagerView extends ViewPart {
	public static final String ID = MessagerView.class.getName();
	private Table table;
	private Action actionShowIP;
	private Action doubleClickAction;
	private TableColumn newColumnTableColumn_name;
	private TableColumn newColumnTableColumn_ip;

	/**
	 * The constructor.
	 */
	public MessagerView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		table = new Table(parent, SWT.BORDER);
		table.setLayoutDeferred(true);
		table.setHeaderVisible(true);
		table.setDragDetect(false);

		newColumnTableColumn_name = new TableColumn(table, SWT.NONE);
		newColumnTableColumn_name.setWidth(100);
		newColumnTableColumn_name.setText("����");

		newColumnTableColumn_ip = new TableColumn(table, SWT.NONE);
		newColumnTableColumn_ip.setWidth(100);
		newColumnTableColumn_ip.setText("IP");

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		singlethread.schedule();
		mutiThread.schedule();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getPage().addPartListener(partListener);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionShowIP);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionShowIP);

		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionShowIP);
	}

	private void makeActions() {
		actionShowIP = new Action() {
			boolean visiable = false;

			public void run() {
				newColumnTableColumn_ip.setWidth(visiable ? 100 : 0);
				setText(visiable ? "����IP" : "��ʾIP");
				visiable = !visiable;
				table.update();
			}
		};
		actionShowIP.setText("����IP");
		actionShowIP.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		doubleClickAction = new Action() {
			public void run() {
				TableItem item = table.getSelection()[0];
				Object objHost = item.getData(IConstants.DATA_HOST);
				if (objHost != null && objHost instanceof HostInfo) {
					MessageDialog omd = new MessageDialog(new Shell(), null,
							((IHostInfo) objHost).getHostName(),
							DialogType.SendMsg);
					if (omd.open() == Window.OK) {
						String toSend = omd.getMessageToSend();
						if (omd.isSendFile()) {
							LocalUser.getLocalUser().sendFile(
									(IHostInfo) objHost, toSend);
						} else {
							LocalUser.getLocalUser().sendMessage(
									(IHostInfo) objHost, toSend);
						}
					}
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		table.setFocus();
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(partListener);
		super.dispose();
	}

	private Job mutiThread = new Job("theMutiReceiver") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			MulticastSocket ds = null;
			try {
				byte[] buffer = new byte[IConstants.MAX_MSG_BYTE];
				ds = new MulticastSocket(IConstants.PORT_MUTI_LISTEN);
				InetAddress inetAddress = InetAddress
						.getByName(IConstants.BROADCAST_ADDRESS);
				ds.joinGroup(inetAddress);
				while (true) {
					DatagramPacket p = new DatagramPacket(buffer, buffer.length);
					ds.receive(p);

					dealMessage(ByteBuffer.wrap(p.getData()), p.getAddress(),
							p.getPort(), p.getLength());
				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (ds != null) {
					ds.close();
				}
			}
			return Status.OK_STATUS;
		}
	};

	private Job singlethread = new Job("singlethread") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			DatagramSocket ds = null;
			try {
				byte[] buffer = new byte[IConstants.MAX_MSG_BYTE];
				final DatagramPacket p = new DatagramPacket(buffer,
						buffer.length);
				ds = new DatagramSocket(IConstants.PORT_LISTEN);
				while (true) {
					ds.receive(p);
					dealMessage(ByteBuffer.wrap(p.getData()), p.getAddress(),
							p.getPort(), p.getLength());
				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (ds != null) {
					ds.close();
				}
			}
			return Status.OK_STATUS;
		}
	};

	private final IPartListener2 partListener = new IPartListener2() {
		public void partActivated(IWorkbenchPartReference partRef) {
		}

		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		public void partClosed(IWorkbenchPartReference partRef) {
			// �ر���Ҫ�ٿ���
			mutiThread.cancel();
			singlethread.cancel();
			LocalUser.getLocalUser().notifyOffline();
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		public void partHidden(IWorkbenchPartReference partRef) {
		}

		public void partInputChanged(IWorkbenchPartReference partRef) {

		}

		public void partOpened(IWorkbenchPartReference partRef) {
			LocalUser.getLocalUser().notifyOnline();
		}

		public void partVisible(IWorkbenchPartReference partRef) {
		}
	};

	private void dealMessage(final ByteBuffer bbf,
			final InetAddress hostAddress, final int hostPort, final int bytelen) {
		final int cmd = bbf.getInt();
		byte namebyt[] = new byte[IConstants.HOSTNAME_LEN];
		for (int i = 0; i < namebyt.length; i++) {
			namebyt[i] = bbf.get(4 + i);
		}
		final String name = new String(namebyt).trim();
		final HostInfo toHost = new HostInfo(name,
				hostAddress.getHostAddress(), hostPort);

		// TODO �����ļ����ܺܲ��ȶ�,��������Socket�ܾ�����
		// ���ļ�������ȡ������ԭ������Display�����ķ���UI�ķ����д���Job����
		if (cmd == Command.FILE.ordinal()) {
			int msglen = bytelen - IConstants.MSG_BEGIN;
			byte msgbyt[] = new byte[msglen];
			for (int i = 0; i < msgbyt.length; i++) {
				msgbyt[i] = bbf.get(IConstants.MSG_BEGIN + i);
			}
			final String msg = new String(msgbyt);

			final String[] savePath = new String[1];
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog omd = new MessageDialog(new Shell(), msg,
							toHost.getHostName(), DialogType.RecFile);
					if (omd.open() == Window.OK) {
						savePath[0] = omd.getMessageToSend();
					}
				}
			});

			// �ڷ����ļ��Ŀ��ƶ˿ڷ��Ϳ�������
			Socket socket = null;
			OutputStream os = null;
			try {
				socket = new Socket(
						InetAddress.getByName(toHost.getHostName()),
						IConstants.PORT_SEND_CONTRAL);
				os = socket.getOutputStream();
				os.write(savePath == null ? IConstants.SEND_REFUZE
						: IConstants.SEND_ACCEPT);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (socket != null) {
						socket.close();
					}
					if (os != null) {
						os.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// �ܾ������ļ�,���;ܾ�ָ��
			if (savePath[0] == null) {
				return;
			} else {
				// ��ȡ��Ϣ������·������Ϣ�е��ļ����б��������߳�
				// ������Ϣ֮����Socket�����˿�,����ֻ���Ǹ�һ���û������ļ�
				Job sendJob = new Job("receiveFile") {
					protected IStatus run(IProgressMonitor monitor) {
						String[] filenames = msg
								.split(IConstants.SEND_FILE_DIV);

						byte[] bytes = new byte[IConstants.SEND_BYTE_SIZE];
						Socket socket = null;
						InputStream is = null;
						FileOutputStream fos = null;
						try {
							socket = new Socket(InetAddress.getByName(toHost
									.getHostName()), IConstants.PORT_SEND_FILE);
							is = socket.getInputStream();
							for (int i = 0; i < filenames.length; i++) {
								// ����UDP��Ϣ�õ��ļ������ļ���С
								String fileInfo = filenames[i];
								int lastIndexOf = fileInfo.lastIndexOf('\\');
								if (lastIndexOf == -1) {
									continue;
								}
								String fileNameAndLen = fileInfo
										.substring(++lastIndexOf);
								String[] divides = fileNameAndLen
										.split(IConstants.SEND_FILE_SIZE_DIV);

								// Ҫ�������ļ�ȫ·��
								String filePath = savePath[0] + '\\'
										+ divides[0];
								savePath[0] = null;

								// �ļ��ֽڴ�С
								long filesize = Long.parseLong(divides[1]);
								File receiveFile = new File(filePath);
								fos = new FileOutputStream(receiveFile);

								int readed = 0;
								int readedSize = 0;
								while ((readed = is.read(bytes)) > 0
										&& readedSize < filesize) {
									fos.write(bytes, 0, readed);
									readedSize += readed;
								}
								fos.close();
							}
						} catch (IOException e1) {
						} finally {
							try {
								if (is != null) {
									is.close();
								}
							} catch (IOException e) {
							}
							try {
								if (socket != null) {
									socket.close();
								}
							} catch (IOException e) {
							}
							try {
								if (fos != null) {
									fos.close();
								}
							} catch (IOException e) {
							}
						}
						return Status.OK_STATUS;
					}

				};
				sendJob.schedule();
			}
		} else {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (table == null || table.isDisposed()) {
						return;
					}
					if (cmd == Command.ONLINE.ordinal()) {
						boolean existInTable = false;
						TableItem[] items = table.getItems();
						for (int i = 0; i < items.length; i++) {
							Object obj = items[i].getData(IConstants.DATA_HOST);
							if (obj != null && toHost.equals(obj)) {
								existInTable = true;
								break;
							}
						}

						if (!existInTable) {
							TableItem item = new TableItem(table, SWT.NONE);
							item.setImage(ResourceManager.getPluginImage(
									Activator.getDefault(), "icons/qq16.GIF"));
							item.setText(toHost.toStringArray());
							item.setData(IConstants.DATA_HOST, toHost);
						}
						LocalUser.getLocalUser().upDate(toHost);
					} else if (cmd == Command.OFFLINE.ordinal()) {
						TableItem[] items = table.getItems();
						for (int i = 0; i < items.length; i++) {
							Object obj = items[i].getData(IConstants.DATA_HOST);
							if (obj != null && toHost.equals(obj)) {
								table.remove(i);
								table.update();
								break;
							}
						}
					} else if (cmd == Command.MESSAGE.ordinal()) {
						int msglen = bytelen - IConstants.MSG_BEGIN;
						byte msgbyt[] = new byte[msglen];
						for (int i = 0; i < msgbyt.length; i++) {
							msgbyt[i] = bbf.get(IConstants.MSG_BEGIN + i);
						}
						String msg = new String(msgbyt);

						TableItem item = null;
						TableItem[] items = table.getItems();
						for (int i = 0; i < items.length; i++) {
							Object obj = items[i].getData(IConstants.DATA_HOST);
							if (obj != null && toHost.equals(obj)) {
								item = items[i];
								break;
							}
						}

						if (item == null) {
							item = new TableItem(table, SWT.NONE);
							item.setImage(ResourceManager.getPluginImage(
									Activator.getDefault(), "icons/qq16.GIF"));
							item.setText(toHost.toStringArray());
							item.setData(IConstants.DATA_HOST, toHost);
						}

						MessageDialog omd = new MessageDialog(new Shell(), msg,
								toHost.getHostName(), DialogType.RecMes);
						if (omd.open() == Window.OK) {
							String toSend = omd.getMessageToSend();
							LocalUser.getLocalUser()
									.sendMessage(toHost, toSend);
						}
					} else if (cmd == Command.UPDATE.ordinal()) {
						boolean existInTable = false;
						TableItem[] items = table.getItems();
						for (int i = 0; i < items.length; i++) {
							Object obj = items[i].getData(IConstants.DATA_HOST);
							if (obj != null && toHost.equals(obj)) {
								existInTable = true;
								break;
							}
						}

						if (!existInTable) {
							TableItem item = new TableItem(table, SWT.NONE);
							item.setImage(ResourceManager.getPluginImage(
									Activator.getDefault(), "icons/qq16.GIF"));
							item.setText(toHost.toStringArray());
							item.setData(IConstants.DATA_HOST, toHost);
						}
					}
				}
			});
		}
	}
}