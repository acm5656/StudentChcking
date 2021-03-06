package com.example.checkingsystem.entity;

import java.sql.Timestamp;

import java.io.Serializable;


/**
 * <p>
 * 图片基本信息
 * </p>
 *
 * @author Ren Gui Jie 812022339@qq.com
 * @since 2017-03-19
 */
//Name("t_picture")
public class Picture {

    private static final long serialVersionUID = 1L;
	public static final String STATUS_INVALID = "invalid";//班级无效,被删除等

    /**
     * 主键-uuid-32
     */
	//Id("picture_id")
	private String pictureId;
    /**
     * 图片URL
     */
	//Field("picture_url")
	private String pictureUrl;
    /**
     * 图片CDN加速地址
     */
	//Field("picture_cdn")
	private String pictureCdn;
    /**
     * 照片名称
     */
	//Field("picture_name")
	private String pictureName;
    /**
     * 图片备注
     */
	//Field("picture_note")
	private String pictureNote;
    /**
     * 记录创建时间
     */
	//Field("picture_gmt_created")
	private Timestamp pictureGmtCreated;
    /**
     * 记录修改时间
     */
	//Field("picture_gmt_modified")
	private Timestamp pictureGmtModified;
    /**
     * 记录的状态
     */
	//Field("picture_status")
	private String pictureStatus;


	public String getPictureId() {
		return pictureId;
	}

	public void setPictureId(String pictureId) {
		this.pictureId = pictureId;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public String getPictureCdn() {
		return pictureCdn;
	}

	public void setPictureCdn(String pictureCdn) {
		this.pictureCdn = pictureCdn;
	}

	public String getPictureName() {
		return pictureName;
	}

	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
	}

	public String getPictureNote() {
		return pictureNote;
	}

	public void setPictureNote(String pictureNote) {
		this.pictureNote = pictureNote;
	}

	public Timestamp getPictureGmtCreated() {
		return pictureGmtCreated;
	}

	public void setPictureGmtCreated(Timestamp pictureGmtCreated) {
		this.pictureGmtCreated = pictureGmtCreated;
	}

	public Timestamp getPictureGmtModified() {
		return pictureGmtModified;
	}

	public void setPictureGmtModified(Timestamp pictureGmtModified) {
		this.pictureGmtModified = pictureGmtModified;
	}

	public String getPictureStatus() {
		return pictureStatus;
	}

	public void setPictureStatus(String pictureStatus) {
		this.pictureStatus = pictureStatus;
	}


}
