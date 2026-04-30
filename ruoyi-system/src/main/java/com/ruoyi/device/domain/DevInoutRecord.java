package com.ruoyi.device.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 设备管理 - 进出库记录实体
 */
public class DevInoutRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "操作时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date recordTime;

    @NotBlank(message = "类型不能为空")
    private String type; // in / out

    @NotNull(message = "数量不能为空")
    private Integer quantity = 1;

    @NotNull(message = "进出库原因不能为空")
    private Long inoutCategoryId;

    private Long warehouseCategoryId;

    /** 出库接收方类型: employee/outside/warehouse */
    private String receiverType;

    private Long receiverEmployeeId;

    private String receiverOutsideNote;

    /** 入库来源类型: employee/warehouse */
    private String senderType;

    private Long senderEmployeeId;

    private String note;

    private String delFlag;

    // ---- 查询条件字段（非持久化） ----
    private String startDate;
    private String endDate;

    // ---- 非持久化展示字段 ----
    private String inoutCategoryName;
    private String warehouseCategoryName;
    private String receiverEmployeeName;
    private String senderEmployeeName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getRecordTime() { return recordTime; }
    public void setRecordTime(Date recordTime) { this.recordTime = recordTime; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Long getInoutCategoryId() { return inoutCategoryId; }
    public void setInoutCategoryId(Long inoutCategoryId) { this.inoutCategoryId = inoutCategoryId; }

    public Long getWarehouseCategoryId() { return warehouseCategoryId; }
    public void setWarehouseCategoryId(Long warehouseCategoryId) { this.warehouseCategoryId = warehouseCategoryId; }

    public String getReceiverType() { return receiverType; }
    public void setReceiverType(String receiverType) { this.receiverType = receiverType; }

    public Long getReceiverEmployeeId() { return receiverEmployeeId; }
    public void setReceiverEmployeeId(Long receiverEmployeeId) { this.receiverEmployeeId = receiverEmployeeId; }

    public String getReceiverOutsideNote() { return receiverOutsideNote; }
    public void setReceiverOutsideNote(String receiverOutsideNote) { this.receiverOutsideNote = receiverOutsideNote; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public Long getSenderEmployeeId() { return senderEmployeeId; }
    public void setSenderEmployeeId(Long senderEmployeeId) { this.senderEmployeeId = senderEmployeeId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getInoutCategoryName() { return inoutCategoryName; }
    public void setInoutCategoryName(String inoutCategoryName) { this.inoutCategoryName = inoutCategoryName; }

    public String getWarehouseCategoryName() { return warehouseCategoryName; }
    public void setWarehouseCategoryName(String warehouseCategoryName) { this.warehouseCategoryName = warehouseCategoryName; }

    public String getReceiverEmployeeName() { return receiverEmployeeName; }
    public void setReceiverEmployeeName(String receiverEmployeeName) { this.receiverEmployeeName = receiverEmployeeName; }

    public String getSenderEmployeeName() { return senderEmployeeName; }
    public void setSenderEmployeeName(String senderEmployeeName) { this.senderEmployeeName = senderEmployeeName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
