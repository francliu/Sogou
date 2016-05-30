package com.sogou.cm.pa.multipage.maincontent;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.xml.sax.Attributes;

import com.sogou.cm.pa.maincontent.HtmlContentHandler;


public class TreeNode {
	String tag;
	String text;
	String clean_text;
	Attributes atts;
	HashMap<String, String> add_atts;
	ArrayList<TreeNode> children;
	boolean in_contract;
	boolean in_hidden;
	int text_len;
	int anchor_len;
//	TreeNode parent;
	public TreeNode() {
		tag = "";
		text = "";
		clean_text = "";
		text_len = anchor_len = 0;
		atts = null;
		add_atts = new HashMap<String, String>();
		children = new ArrayList<TreeNode>();
		in_contract = in_hidden = false;
//		parent = null;
	}
	
	public String traverse() {
		StringBuffer sb = new StringBuffer();
		if (tag == "") {
			return text;
		}
		sb.append("<" + tag + " ");
		if (atts != null) {
			for (int i = 0; i < atts.getLength(); ++i) {
				String name = atts.getQName(i);
				String value = atts.getValue(i);
				sb.append(name + "=\"" + value + "\" ");
			}
		}
		if (add_atts.size() > 0) {
			sb.append("blockfeature=\"");
			Iterator iter = this.add_atts.entrySet().iterator();
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				sb.append(key + ":" + value + ";");
			}
			sb.append("\" ");
		}
		sb.append(">\n");
		for (TreeNode tn: children) {
			sb.append(tn.traverse());
		}

		sb.append("</" + tag + ">\n");
		return sb.toString();
	}
	
	public String traverse_debug() {
		StringBuffer sb = new StringBuffer();
		if (this.tag.equals("")) {
			return text;
		}
		sb.append("<" + tag + " ");
		String style = "";
		if (atts != null) {
			for (int i = 0; i < atts.getLength(); ++i) {
				String name = atts.getQName(i);
				String value = atts.getValue(i);
				if (!name.equalsIgnoreCase("style")) 
					sb.append(name + "=\"" + value + "\" ");
				else 
					style = value;
			}
		}
		String added_border = "border:3px solid blue;";
		boolean is_add_border = false;
		if (add_atts.size() > 0) {
			sb.append("blockfeature=\"");
			Iterator iter = this.add_atts.entrySet().iterator();
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				sb.append(key + ":" + value + ";");

			//	if (key.equalsIgnoreCase("ismaincontent")) {

				if (key.equalsIgnoreCase("ismaincontent")) {
					if (!is_add_border) {
						is_add_border = true; 
						added_border = "border:3px solid red;";
					}
					
				}
				if (key.equalsIgnoreCase("blocktype") && !value.equals("1")) {
			//		System.out.println("eeeeeeeeeeee");
					is_add_border = true; 
					added_border = "border:3px solid blue;";
				}
			}
			sb.append("\" ");
		}
		if (is_add_border) {
		//	System.out.println("hrer.");
			sb.append("style=\"" + added_border + style + "\" ");
		} else if (style.length() > 0){
			sb.append("style=\"" + style + "\" ");
		}
		sb.append(">\n");
		for (TreeNode tn: children) {
			sb.append(tn.traverse_debug());
		}

		sb.append("</" + tag + ">\n");
		return sb.toString();
	}
	
	public String toMaincontentString() {
		StringBuffer sb = new StringBuffer();
		if (this.tag.equals("")) {
			return "";
		}
		String s = add_atts.get("ismaincontent");
		String s2 = add_atts.get("blocktype");
		if (s!= null) {
			if (s2 == null || (s2.equals("2") || s2.equals("6") || s2.equals("1") || s2.equals("0") || s2.equals("16"))) {
				return this.getText();
		    } else if (s2.equals("10")) {
				return this.getContract();
			} else if (s2.equals("14")) {
				return this.getHidden();
			} else {
				return "";
			}
		}
		
		if (s2 != null) {
			if (s2.equals("2") || s2.equals("6")|| s2.equals("16")) {
				return this.getText();
			} else if (s2.equals("10")) {
				return this.getContract();
			}else if (s2.equals("14")) {
				;
			} else {
				return "";
			}
		}
		for (TreeNode tn: children) {
			//sb.append(tn.toMaincontentString());
			String temp = tn.toMaincontentString();
			if (temp.length() > 0) {
				if (sb.length() > 0 && sb.charAt(sb.length()-1) != ' ') {
					sb.append(' ');
				}
				sb.append(temp);
			}
		}
		return sb.toString();
	}
	
	private String getHidden() {
		//System.out.println("hhhh");
		for (TreeNode child: children) {
			child.in_hidden = true;
		}
		String hidden = this.getText();
		if (hidden != null && hidden.length() > 0) {
			try {
			//	System.out.println("here");
				byte[] begin_flag = new byte[]{(byte) 0x0c, (byte) 0xe5};
				String begin_flag_s = new String(begin_flag, "UTF-16LE");
				byte[] end_flag = new byte[]{(byte) 0x0d, (byte) 0xe5};
				String end_flag_s = new String(end_flag, "UTF-16LE");
				StringBuffer sb = new StringBuffer();
				sb.append(begin_flag_s);
				sb.append(hidden);
				if (hidden.charAt(hidden.length()-1) != ' ') {
					sb.append(' ');
				}
				sb.append(end_flag_s);
				
				return sb.toString();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				return hidden;
			}
		} else {
			return hidden;
		}
	}
	
	class ContractInfo {
		int anchor_len;
		StringBuffer full_text;
		StringBuffer contract_text;
		ContractInfo() {
			anchor_len = 0;
			full_text = new StringBuffer();
			contract_text = new StringBuffer();
		}
	}
	
	private String getContract() {
		this.in_contract = true;
		ContractInfo ci = getContractInternal();
		//String contract = getContractText(original_text);
		String contract = "";
		if (ci.full_text.length() > 200 || ci.anchor_len > 20 
				|| (ci.full_text.length() > ci.contract_text.length()*2 && ci.full_text.length() > 100 )
				|| (ci.full_text.length()-ci.contract_text.length()>50)) {
			contract = (ci.contract_text.toString());
		} else {
			contract = (ci.full_text.toString());
		}
		
		
		if (contract.length() > 0) {
			try {
				byte[] begin_flag = new byte[]{(byte) 0x0a, (byte) 0xe5};
				String begin_flag_s = new String(begin_flag, "UTF-16LE");
				byte[] end_flag = new byte[]{(byte) 0x0b, (byte) 0xe5};
				String end_flag_s = new String(end_flag, "UTF-16LE");
				StringBuffer sb = new StringBuffer();
				sb.append(begin_flag_s);
				sb.append(contract);
				if (contract.charAt(contract.length()-1) != ' ') {
					sb.append(' ');
				}
				sb.append(end_flag_s);
				return sb.toString();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				return contract;
			}
		} else {
			return contract;
		}
	}
	
	private ContractInfo getContractInternal() {
		ContractInfo ci = new ContractInfo();
		for (TreeNode tn: children) {
			if (tn.tag.equals("")) {
				if (tn.clean_text.length() > 0) {
					if (ci.full_text.length() > 0 && ci.full_text.charAt(ci.full_text.length()-1) != ' ') {
						ci.full_text.append(' ');
					}
					ci.full_text.append(tn.clean_text);
					if (tn.tag.equals("a")) {
						ci.anchor_len += tn.clean_text.length();
					}
					if (HtmlContentHandler.containContract(tn.clean_text)) {
						if (tn.clean_text.length() >= 50) {
							String temp = tn.clean_text;
							if (temp.indexOf("@")>=0) {
								temp = temp.replace('@', ' ');
								if (!HtmlContentHandler.containContract(temp)) {
									String contract_text = getContractText(tn.clean_text);
									if (contract_text.length()>0) {
										if (ci.contract_text.length()>0 && ci.contract_text.charAt(ci.contract_text.length()-1)!=' ') {
											ci.contract_text.append(' ');
										}
										ci.contract_text.append(contract_text);
										
									}
									continue;
								}
							}
						}
						if (ci.contract_text.length()>0 && ci.contract_text.charAt(ci.contract_text.length()-1)!=' ') {
							ci.contract_text.append(' ');
						}
						ci.contract_text.append(tn.clean_text);
					}
				}
				//to do
			} else {
				ContractInfo ci2 = tn.getContractInternal();
				if (tn.tag.equals("a")) {
					ci.anchor_len += ci2.full_text.length();
				} else {
					ci.anchor_len += ci2.anchor_len;
				}
				if (ci2.full_text.length()>0) {
					if (ci.full_text.length() > 0 && ci.full_text.charAt(ci.full_text.length()-1) != ' ') {
						ci.full_text.append(' ');
					}
					ci.full_text.append(ci2.full_text);
				}
				if (ci2.contract_text.length()>0) {
					if (ci.contract_text.length()>0 && ci.contract_text.charAt(ci.contract_text.length()-1)!=' ') {
						ci.contract_text.append(' ');
					}
					ci.contract_text.append(ci2.contract_text);
				}
			}
		}
		return ci;
	}
	
	private String getContractText(String s) {
		//System.out.println("hhh  " + s);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c == '。' || c == '，' || c == ',' ||c == '!'||c == '！' || c == '\u00a0' || c == '\u3000') {
				sb.append(' ');
			} else {
				sb.append(c);
			}
			if (c == ':' || c == '：' || c == ' ' || c == '\u00a0' || c == '\u3000') {
				while (i+1<s.length()) {
					c = s.charAt(i+1);
					if (c == ' ' || c == '\u00a0' || c == '\u3000') {
						++i;
					} else {
						break;
					}
				}
				if (i+1<s.length() && s.charAt(i+1)=='(' && sb.charAt(sb.length()-1)==' ') {
					sb.deleteCharAt(sb.length()-1);
				}
			}
		}
		String[] segs = sb.toString().split(" ");
		StringBuffer contact = new StringBuffer();
		for (String seg: segs) {
			if(HtmlContentHandler.containContract(seg)) {
				if (contact.length() > 0) {
					contact.append(' ');
				}
				contact.append(seg);
			}
		}
		return contact.toString();
	}
	
	private String getText() {
		StringBuffer sb = new StringBuffer();
		boolean last_is_text = false;
		boolean last_is_merge = false;
	//	for (TreeNode tn: children) {
		for (int i = 0; i < children.size(); ++i) {
			TreeNode tn = children.get(i);
		
			if (this.in_hidden) {
				tn.in_hidden = true;
			}
			boolean flag = false;
			if (tn.tag.equals("")) {
				//sb.append(tn.clean_text);
			//	System.out.println("hhh " + tn.clean_text);
				String temp = tn.clean_text;
				if (temp.length() > 0) {
					if (sb.length() > 0 && sb.charAt(sb.length()-1) != ' ' && !last_is_merge) {
						sb.append(' ');
					}
					sb.append(temp);
				}
				if (temp.length() > 0) {
					last_is_text = true;
					last_is_merge = false;
				}

			} else {
				String s = tn.add_atts.get("blocktype");
				if (s != null && !(s.equals("2") || s.equals("6") || s.equals("10") || s.equals("1") || s.equals("0") || s.equals("14") || s.equals("16"))) {
					continue;
				}
				String temp = null;
				if (s == null) {
					temp = tn.getText();
					
				} else if (s.equals("10")) {
					temp = tn.getContract();
				} else if (s.equals("14") && !tn.in_hidden) {
				//	System.out.println("asdf");
					flag = true;
					temp = tn.getHidden();
				} else {
					temp = tn.getText();
				}

				if (temp != null && temp.length() > 0) {
					if (sb.length() > 0 && sb.charAt(sb.length()-1) != ' ' && !(last_is_text && (tn.tag.equals("a")||tn.tag.equals("strong")))) {
						sb.append(' ');
					}
				//	System.out.println(temp + "\t" + last_is_text + "\t" + tn.tag + "\t" + tn.tag.equals(""));
					sb.append(temp);
				}
				if (temp != null && temp.length() > 0) {
					last_is_text = false;
					last_is_merge = false;
					if (tn.tag.equals("a") || tn.tag.equals("strong")) {
						last_is_merge = true;
					}
				}
				
			}
		//	System.out.println(last_is_text + "\t" + tn.tag + "\t" + tn.tag.equals(""));
		//	if ()
			
		}

		return sb.toString();
	}
}
