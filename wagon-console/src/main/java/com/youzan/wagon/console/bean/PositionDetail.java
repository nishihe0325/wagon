package com.youzan.wagon.console.bean;

/**
 * @author wangguofeng since 2016年6月27日 上午10:23:29
 */
public class PositionDetail {

    private String id;
    private String title;
    private String description;
    private String is_delete;
    private String created_time;
    private String total_count;
    private String updated_time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIs_delete() {
        return is_delete;
    }

    public void setIs_delete(String is_delete) {
        this.is_delete = is_delete;
    }

    public String getCreated_time() {
        return created_time;
    }

    public void setCreated_time(String created_time) {
        this.created_time = created_time;
    }

    public String getTotal_count() {
        return total_count;
    }

    public void setTotal_count(String total_count) {
        this.total_count = total_count;
    }

    public String getUpdated_time() {
        return updated_time;
    }

    public void setUpdated_time(String updated_time) {
        this.updated_time = updated_time;
    }

    @Override
    public String toString() {
        return "ClassPojo [id = " + id + ", title = " + title + ", description = " + description + ", is_delete = " + is_delete + ", created_time = " + created_time + ", total_count = " + total_count + ", updated_time = " + updated_time + "]";
    }
}
