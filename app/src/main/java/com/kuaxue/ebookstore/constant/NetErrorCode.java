package com.kuaxue.ebookstore.constant;

import android.text.TextUtils;
import android.util.SparseArray;

/**
 * 网络请求错误码对应表
 * @author Administrator
 *
 */
public class NetErrorCode {	
	private static SparseArray<String> codeMessage;
		
	/**
	 * 根据返回码获得相应的提示信息
	 * @param code
	 * @return
	 */
	public static String getCodeMsg(int code){
		String msg = codeMessage.get(code);
		if (TextUtils.isEmpty(msg)) {
			msg = "未知错误 " + code;
		}
		return msg;
	}
	
	public static final int NET_REQUEST_SUCCESS = 200;		
	public static final int NET_REQUEST_TIMEOUT = 90000001;
	public static final int NET_REQUEST_EXCEPTION = 90000002;	
	public static final int NET_REQUEST_TOKENWRONG = 1;
	
	public static final int USER_NO_EXITST = 30101001;
	public static final int USER_NO_EXITST2 = 30102002;
	
	static{
		codeMessage = new SparseArray<String>();
		codeMessage.put(1, "token验证错误，请检查signature");
		codeMessage.put(2, "系统错误，一般为服务器未知错误");
		codeMessage.put(3, "没有登陆");
		codeMessage.put(4, "参数错误");
		
		codeMessage.put(NET_REQUEST_TIMEOUT,  "网络访问超时！");	
		codeMessage.put(NET_REQUEST_EXCEPTION,"网络访问失败！");
		
		//帐号:		
		codeMessage.put(10101001,"验证码不正确！请重新输入");
		codeMessage.put(10101002," 密码位数不对");
		codeMessage.put(10101003,"请输入11位的手机号码");
		codeMessage.put(10101004,"该号码已经被注册过");
		codeMessage.put(10102001,"手机号码未注册");
		codeMessage.put(10102002,"验证码输入错误");
		codeMessage.put(10102003,"密码错误");
		codeMessage.put(10103001,"旧密码不正确");
		codeMessage.put(10103002,"帐号不存在，请确认手机号码是否输入正确");		
		codeMessage.put(10103003,"密码位数不对");
		codeMessage.put(10105001,"帐号不存在，请确认手机号码是否输入正确");
		codeMessage.put(10105002,"密码位数不对");
			                
		codeMessage.put(10201001,"请输入11位的手机号码");
		codeMessage.put(10201002,"该号码已经被注册过");
		codeMessage.put(10202001,"手机号码未注册");		
		
		
		//SMSController:		
		codeMessage.put(10301001,"该号码已经被注册过");
		codeMessage.put(10301002,"手机号码未注册");
		codeMessage.put(10302001,"验证码不正确！请重新输入");
		codeMessage.put(10302002,"该号码已经被注册过");
		codeMessage.put(10301003,"上次发送时间间隔时间只有1分钟");
		codeMessage.put(10301004,"短信发送失败");
		codeMessage.put(10302003,"验证码超时！请重新获取");				
					
		//文件系统：
		codeMessage.put(20101001,"文件不存在");
		
		//用户、家庭：
		//UserController:		
		codeMessage.put(30101001,"用户不存在");
		codeMessage.put(30102001,"家庭不存在");		
		codeMessage.put(30102002,"用户不存在");
		codeMessage.put(30103001,"用户已在家庭里");
		codeMessage.put(30104001,"成员不存在");		
		codeMessage.put(30201001,"此号码已经是该手表的APP用户");						
		codeMessage.put(30201002,"此号码已经是该手表的联系人");  
		codeMessage.put(30201003,"绑定码错误");
		codeMessage.put(30201004,"申请已通过审核");  
		codeMessage.put(30201005,"用户已加入家庭");
		codeMessage.put(30201006,"此用户为管理员，不能编辑");
		codeMessage.put(30201007,"此用户为管理员，不能删除");
		codeMessage.put(30201008,"该用户不存在待审核信息");		
		codeMessage.put(30201009,"该用户不存在");		
		
		//#升级：
		codeMessage.put(40101001,"该版本已存在");
		codeMessage.put(40103002,"没有更新的版本了");
		
		codeMessage.put(30201010,"SOS紧急联系人不能超过3个");
	}	
}
