package com.cqt.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * 接口返回数据格式
 *
 * @author scott
 * jeecgos@163.com
 * @date 2019年1月19日
 */
@Data
@ApiModel(value = "接口返回对象", description = "接口返回对象")
public class ResultT<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 成功标志
	 */
	@ApiModelProperty(value = "成功标志")
	private boolean success = true;

	/**
	 * 返回处理消息
	 */
	@ApiModelProperty(value = "返回处理消息")
	private String message = "操作成功！";

	/**
	 * 返回代码
	 */
	@ApiModelProperty(value = "返回代码")
	private Integer code = 0;

	/**
	 * 返回数据对象 data
	 */
	@ApiModelProperty(value = "返回数据对象")
	private T result;

	@ApiModelProperty(value = "返回数据对象data")
	private T data;


	/**
	 * 时间戳
	 */
	@ApiModelProperty(value = "时间戳")
	private long timestamp = System.currentTimeMillis();

	public ResultT() {

	}

	public ResultT<T> success(String message) {
		this.message = message;
		code = HttpStatus.OK.value();
		success = true;
		return this;
	}

	public static <T> ResultT<T> ok() {
		ResultT<T> r = new ResultT<>();
		r.setSuccess(true);
		r.setCode(HttpStatus.OK.value());
		r.setMessage("成功");
		return r;
	}

	public static <T> ResultT<T> ok(String msg) {
		ResultT<T> r = new ResultT<>();
		r.setSuccess(true);
		r.setCode(HttpStatus.OK.value());
		r.setMessage(msg);
		return r;
	}

	public static <T> ResultT<T> ok(T data) {
		ResultT<T> r = new ResultT<>();
		r.setSuccess(true);
		r.setCode(HttpStatus.OK.value());
		r.setResult(data);
		return r;
	}

	public static <T> ResultT<T> ok(String msg, T data) {
		ResultT<T> r = new ResultT<>();
		r.setSuccess(true);
		r.setCode(HttpStatus.OK.value());
		r.setMessage(msg);
		r.setResult(data);
		return r;
	}

	public static <T> ResultT<T> error(String msg, T data) {
		ResultT<T> r = new ResultT<>();
		r.setSuccess(false);
		r.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		r.setMessage(msg);
		r.setResult(data);
		return r;
	}

	public static <T> ResultT<T> error(String msg) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg);
	}

	public static <T> ResultT<T> error() {
		return error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "操作失败");
	}

	public static <T> ResultT<T> error(int code, String msg) {
		ResultT<T> r = new ResultT<>();
		r.setCode(code);
		r.setMessage(msg);
		r.setSuccess(false);
		return r;
	}

	public static <T> ResultT<T> error(int code, String msg, T data) {
		ResultT<T> r = new ResultT<>();
		r.setCode(code);
		r.setMessage(msg);
		r.setResult(data);
		r.setSuccess(false);
		return r;
	}

	@JsonIgnore
	private String onlTable;

}
