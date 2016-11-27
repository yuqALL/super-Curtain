package com.yuq.curtain.supercurtain.utils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by yuq32 on 2016/10/5.
 */

public class SerializableObject  implements Serializable {

    private List<Map<String, Object>> list;

    public List<Map<String, Object>> getList() {
        return list;
    }

    public void setList(List<Map<String, Object>> list) {
        this.list = list;
    }
}
