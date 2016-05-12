package com.socket;

import static java.lang.System.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

public class EncodeDecode {
	public static String decode(String s) {
		return StringUtils.newStringUtf8(Base64.decodeBase64(s));
	}

	public static String encode(String s) {
		return Base64.encodeBase64String(StringUtils.getBytesUtf8(s));
	}
	public static void main(String args[]){
		
		out.println(encode("select * from demo"));
		out.println(encode("select name from demo"));
		out.println(decode("c2VsZWN0ICogZnJvbSBzYW1wbGVkZGIuZGVtbw=="));
		out.println(decode(encode("select name from demo")));
	}
}