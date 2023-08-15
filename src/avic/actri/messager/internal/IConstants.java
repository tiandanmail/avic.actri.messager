/**
 * @copyright actri.avic
 */
package avic.actri.messager.internal;

/**
 * ��������
 * 
 * @author tdan
 */
public interface IConstants {
	public static final int PORT_BINDING = 9527;// ��������ʱ����Ҫbinding�Ķ˿�
	public static final int PORT_LISTEN = 9528;// ���������������ݵĶ˿�,�������ݵĶ˿�
	public static final int PORT_MUTI_BINDING = 9529;// ��������ʱ����Ҫbinding�Ķ˿�
	public static final int PORT_MUTI_LISTEN = 9530;// ���������������ݵĶ˿�,�������ݵĶ˿�
	public static final int PORT_SEND_FILE = 9531;// �����ļ��˿�
	public static final int PORT_SEND_CONTRAL = 9532;// �����ļ����ƶ˿�

	public static final int MAX_MSG_BYTE = 1024;// �����Ϣ����

	public static final String BROADCAST_ADDRESS = "230.0.0.1";// �㲥��ַ

	public static final String DATA_HOST = "host";// ΪtableItem�������ݵĹؼ���,�洢��Ӧ�Ŀͻ���Ϣ

	public static final int SEND_ACCEPT = 1;// ͬ�����

	public static final int SEND_REFUZE = 0;// �ܾ�����

	public static final int SEND_BYTE_SIZE = 1024;// �����ļ�ʱ��һ�η��͵��ֽ���

	public static final String SEND_FILE = "�����ļ�";

	public static final String SEND_FILE_DIV = ",";// ���ļ����ֵ�

	public static final String SEND_FILE_SIZE_DIV = "#";// �ļ������С�Ļ��ֵ�

	public static final int HOSTNAME_LEN = 32;// ����������

	public static final int MSG_BEGIN = 36;// ��Ϣ����Ϣͷ���ȣ���Ϣ���+�û���

	public enum Command {
		ONLINE, // ����֪ͨ���������Host��״̬
		OFFLINE, // ����֪ͨ
		MESSAGE, // ������Ϣ
		FILE, // �����ļ�
		UPDATE, // ����״̬,֪ͨ�Լ���״̬
	}
}
