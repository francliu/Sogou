package com.sogou.cm.pa.maincontent;

public class UrlUtils {
	public static boolean urlIsHomepage(String url) {
		int idx_path_begin = url.indexOf('/', 9);

		if (idx_path_begin <= 0)
			return false;

		if (url.length() == idx_path_begin + 1)
			return true;

		// StringTokenizer st = new StringTokenizer(url.substring(idx_path_begin
		// + 1), "?/=");
		// if (st.countTokens() > 1)
		int url_length = url.length();
		for (int i = idx_path_begin + 1; i < url_length; i++) {
			char c = url.charAt(i);
			if (c == '?' || c == '/' || c == '=')
				return false;
		}

		String path = url.substring(idx_path_begin + 1);
		if (path.startsWith("index."))
			return true;
		if (path.startsWith("default."))
			return true;
		if (path.startsWith("home."))
			return true;
		if (path.startsWith("homepage."))
			return true;
		if (path.startsWith("main."))
			return true;
		if (path.equals("home"))
			return true;

		return false;
	}

	public static boolean urlIsErrorPage(String url) {
		int idx_path_begin = url.indexOf('/', 9);

		if (idx_path_begin <= 0)
			return false;

		if (url.length() == idx_path_begin + 1)
			return false;

		String path = url.substring(idx_path_begin + 1);

		String datas[] = path.split("/");

		String last_seg = datas[datas.length - 1];
		last_seg = last_seg.toLowerCase();

		for (int i = 0; i < datas.length; i++) {
			if (datas[i].equals("error"))
				return true;
		}

		// if (last_seg.startsWith("error.") ||
		// last_seg.startsWith("404.")||last_seg.startsWith("errorpage.")
		// ||last_seg.startsWith("errormsg."))
		if (last_seg.startsWith("error") || last_seg.startsWith("404.") || last_seg.startsWith("nopage.")) {
			return true;
		}
		else {
			int idx_lst = last_seg.indexOf("?error");
			if (idx_lst >= 0)
				return true;

			idx_lst = last_seg.indexOf("404");
			char last_ch;
			char next_ch;
			if (idx_lst >= 0) {
				boolean dflg = true;
				if (idx_lst > 0) {
					last_ch = last_seg.charAt(idx_lst - 1);
					if (last_ch >= '0' && last_ch <= '9') {
						dflg = false;
					}
				}
				if (idx_lst < last_seg.length() - 4) {
					next_ch = last_seg.charAt(idx_lst + 3);
					if (next_ch >= '0' && next_ch <= '9') {
						dflg = false;
					}
				}
				if (dflg == true) {
					return true;
				}

			}
		}

		return false;
	}
}
