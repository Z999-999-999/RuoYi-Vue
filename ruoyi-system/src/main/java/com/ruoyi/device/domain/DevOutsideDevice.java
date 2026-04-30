package com.ruoyi.device.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 设备管理 - 在外设备实体
 */
public class DevOutsideDevice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "原因描述不能为空")
    private String reason;

    @NotNull(message = "数量不能为空")
    private Integer quantity = 1;

    /** 关联的进出库记录ID（出库→在外时记录，入库←在外时查找） */
    private Long inoutRecordId;

    /** 在外设备原本所属的库房分类（归还入库时用于恢复库房库存） */
    private Long warehouseCategoryId;

    private String note;

    @NotNull(message = "出去日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date outDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expectedReturnDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualReturnDate;

    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Date getOutDate() { return outDate; }
    public void setOutDate(Date outDate) { this.outDate = outDate; }

    public Date getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(Date expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }

    public Date getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(Date actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public Long getInoutRecordId() { return inoutRecordId; }
    public void setInoutRecordId(Long inoutRecordId) { this.inoutRecordId = inoutRecordId; }

    public Long getWarehouseCategoryId() { return warehouseCategoryId; }
    public void setWarehouseCategoryId(Long warehouseCategoryId) { this.warehouseCategoryId = warehouseCategoryId; }
}
