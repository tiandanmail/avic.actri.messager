/**
 * @copyright actri.avic
 */
package avic.actri.messager.internal;

/**
 * �����û�����
 * 
 * @author tdan
 */
public interface ILocalUser {

	/**
	 * ֪ͨ�������
	 *
	 */
	public void notifyOnline();

	/**
	 * ֪ͨ�������
	 *
	 */
	public void notifyOffline();

	/**
	 * ��toHost������Ϣmessage
	 */
	public void sendMessage(IHostInfo toHost, String message);

	/**
	 * ��toHost������Ϣmessage
	 */
	public void sendFile(IHostInfo toHost, String message);

	/**
	 * ����û���Ϣ
	 */
	public IHostInfo getHostInfo();

	/**
	 * �㲥�����Լ����ߵ���Ϣ
	 */
	public void upDate();

	/**
	 * ��toHost�����Լ����ߵ���Ϣ
	 */
	public void upDate(IHostInfo toHost);

}
