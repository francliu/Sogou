package com.sogou.cm.pa.pagecluster;

public class KMP {	
/**
	 * ��target�ַ���Ѱ��source��ƥ��
	 * @param target
	 * @param source
	 */
	public void kmp(String target, String source) {
		  int sourceLength = source.length();
		  int targetLength = target.length();
		  int[] result = preProcess(source);
		  int j = 0;
		  int k = 0;
		  for(int i=0;i<targetLength;i++){
		      //�ҵ�ƥ����ַ�ʱ��ִ��
			  while(j>0 && source.charAt(j) != target.charAt(i)){
		      //����Ϊsource�к��ʵ�λ��
				  j = result[j-1];
			  }
		      //�ҵ�һ��ƥ����ַ�
			  if(source.charAt(j) == target.charAt(i)){
				  j++;
			  }
		      //ƥ�䵽һ����������
			  if(j == sourceLength){
				//  j = result[j-1];
				  j = 0;
				  k++;
			//	  System.out.println("find  " + i);
			  }
		  	}
		 }
	/**
	 * Ԥ����
	 * @param s
	 * @return
	 */
	public int[] preProcess(final String s) {
		  int size = s.length();
		  int[] result = new int[size];
		  result[0] = 0;
		  int j = 0;
		  //ѭ������
		  for(int i=1;i<size;i++){
			  while(j>0 && s.charAt(j) != s.charAt(i)){
				  j = result[j];
			  }
			  if(s.charAt(j) == s.charAt(i)){
				  j++;
			  }
		      //�ҵ�һ�����
			  result[i] = j;
		  }
	//	  System.out.println(java.util.Arrays.toString(result));
		  return result;
		 }
	public static void main(String[]args) throws Exception {
		final String s = "abcdhababcdhjabcd";
		KMP k = new KMP();
		k.kmp(s, "abcd");
	}
}