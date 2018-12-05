namespace java thrift.iface

# ����ṹ�嶨���˷�������ߵ�������Ϣ
struct Request {
	# ���ݵĲ�����Ϣ��ʹ�ø�ʽ���б�ʾ
	1:required binary paramJSON;
	# �������������ķ�������ʹ��serviceName���Խ��д���
	2:required string serviceName
}

# ����ṹ�壬�����˷����ṩ�ߵķ�����Ϣ
struct Reponse {
	# RESCODE �Ǵ���״̬���룬��һ��ö�����͡�����RESCODE._200��ʾ����ɹ�
	1:required  RESCODE responeCode;
	# ���صĴ�������ͬ��ʹ��JSON��ʽ��������
	2:required  binary responseJSON;
}

# �쳣�������壬�������ṩ�ߴ�����̳����쳣ʱ�����������߷���
exception ServiceException {
	# EXCCODE ���쳣���룬Ҳ��һ��ö�����͡�
	# ����EXCCODE.PARAMNOTFOUND��ʾ��Ҫ���������û���ҵ�
	1:required EXCCODE exceptionCode;
	# �쳣��������Ϣ��ʹ���ַ�����������
	2:required string exceptionMess;
}

# ���ö�ٽṹ���������ַ����ṩ�ߵ���Ӧ����
enum RESCODE {
	_200=200;
	_500=500;
	_400=400;
}

# ���ö�ٽṹ���������ַ����ṩ�ߵ��쳣����
enum EXCCODE {
	PARAMNOTFOUND = 2001;
	SERVICENOTFOUND = 2002;
}

# ���Ǿ����������Apache Thrift�ӿ�
service DIYFrameworkService {
	Reponse send(1:required Request request) throws (1: ServiceException e);
}