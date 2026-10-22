package com.ruoyi.system.domain.dto;

import com.ruoyi.common.annotation.Excel;

/**
 * 用户导入DTO - 用于Excel批量导入
 * 提供更友好的字段格式，支持部门名称、性别文本等
 *
 * @author ruoyi
 */
public class UserImportDto
{
    /** 部门名称（填写部门名称，系统自动匹配部门ID） */
    @Excel(name = "部门名称")
    private String deptName;

    /** 登录名称 */
    @Excel(name = "登录名称")
    private String userName;

    /** 用户名称 */
    @Excel(name = "用户名称")
    private String nickName;

    /** 用户邮箱 */
    @Excel(name = "用户邮箱")
    private String email;

    /** 手机号码 */
    @Excel(name = "手机号码")
    private String phonenumber;

    /** 用户性别（填写：男/女） */
    @Excel(name = "用户性别")
    private String sexName;

    /** 出生年月 */
    @Excel(name = "出生年月", dateFormat = "yyyy-MM-dd")
    private String birthday;

    /** 学历 */
    @Excel(name = "学历")
    private String education;

    /** 进公司时间 */
    @Excel(name = "进公司时间", dateFormat = "yyyy-MM-dd")
    private String joinDate;

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getSexName()
    {
        return sexName;
    }

    public void setSexName(String sexName)
    {
        this.sexName = sexName;
    }

    public String getBirthday()
    {
        return birthday;
    }

    public void setBirthday(String birthday)
    {
        this.birthday = birthday;
    }

    public String getEducation()
    {
        return education;
    }

    public void setEducation(String education)
    {
        this.education = education;
    }

    public String getJoinDate()
    {
        return joinDate;
    }

    public void setJoinDate(String joinDate)
    {
        this.joinDate = joinDate;
    }
}
